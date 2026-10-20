package com.changsha.zhongdian.shareholding.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.operationlog.entity.OperationTargetType;
import com.changsha.zhongdian.operationlog.entity.OperationType;
import com.changsha.zhongdian.operationlog.service.OperationLogService;
import com.changsha.zhongdian.shareholding.dto.ShareholdingImportError;
import com.changsha.zhongdian.shareholding.dto.ShareholdingImportResult;
import com.changsha.zhongdian.shareholding.entity.HoldingStatus;
import com.changsha.zhongdian.shareholding.entity.Shareholding;
import com.changsha.zhongdian.shareholding.repository.ShareholdingRepository;
import com.changsha.zhongdian.shareholder.entity.Shareholder;
import com.changsha.zhongdian.shareholder.entity.ShareholderStatus;
import com.changsha.zhongdian.shareholder.repository.ShareholderRepository;
import com.changsha.zhongdian.station.entity.Station;
import com.changsha.zhongdian.station.repository.StationRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 充电站-股东关系 Excel 批量导入：模板生成（预填站点名称）+ 解析校验 + 仅新增落库 + 逐条写站点操作日志。
 * 为什么独立成服务：关系导入涉及按名称解析站点/股东、联合主键去重、原子落库与逐条日志，
 * 与单条关联、退股、股权调整的职责不同，独立后 ShareholdingCommandService 保持单条写操作内聚。
 */
@Service
public class ShareholdingImportService {

    private static final String TEMPLATE_SHEET_NAME = "股东关系导入模板";
    private static final List<String> TEMPLATE_HEADERS = List.of("充电站名称", "股东名称", "股份占比");
    private static final BigDecimal MAX_OWNERSHIP_PERCENTAGE = new BigDecimal("100");
    private static final int MAX_ROW_COUNT = 1000;

    private final ShareholdingRepository shareholdingRepository;
    private final ShareholderRepository shareholderRepository;
    private final StationRepository stationRepository;
    private final OperationLogService operationLogService;
    private final DataFormatter dataFormatter = new DataFormatter();

    public ShareholdingImportService(ShareholdingRepository shareholdingRepository,
                                     ShareholderRepository shareholderRepository,
                                     StationRepository stationRepository,
                                     OperationLogService operationLogService) {
        this.shareholdingRepository = shareholdingRepository;
        this.shareholderRepository = shareholderRepository;
        this.stationRepository = stationRepository;
        this.operationLogService = operationLogService;
    }

    /**
     * 生成导入模板（.xlsx）：表头三列，第一列预填全部充电站名称（按名称升序）。
     */
    public byte[] generateTemplate() {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet(TEMPLATE_SHEET_NAME);
            XSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < TEMPLATE_HEADERS.size(); i++) {
                headerRow.createCell(i).setCellValue(TEMPLATE_HEADERS.get(i));
            }

            List<Station> stations = stationRepository.findAllByOrderByStationNameAsc();
            int rowIndex = 1;
            for (Station station : stations) {
                XSSFRow row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(station.getStationName());
            }
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new BusinessException("TEMPLATE_GENERATE_FAILED", "模板生成失败，请稍后重试", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 导入股东关系：只允许新增持股中关系，以（充电站名称，股东名称）为联合业务主键去重。
     * 策略：全量校验，任一数据非法则整体不落库并返回逐行错误；全部合法才在单事务中批量落库，
     * 并为每条关系写一条关联到对应充电站的操作日志。
     */
    @Transactional
    public ShareholdingImportResult importShareholdings(MultipartFile file, String operatorAccount, String operatorRole) {
        List<ParsedRow> rows = parseRows(file);

        if (rows.isEmpty()) {
            throw new BusinessException("VALIDATION_ERROR", "Excel 文件中没有可导入的数据行", HttpStatus.BAD_REQUEST);
        }

        Map<String, Station> stationByName = loadStationsByName(rows);
        Map<String, Shareholder> shareholderByName = loadShareholdersByName(rows);
        Set<String> existingActivePairs = loadExistingActivePairs(stationByName.values());

        List<ShareholdingImportError> errors = new ArrayList<>();
        List<ResolvedRow> validRows = new ArrayList<>();
        Set<String> seenPairs = new HashSet<>();

        for (ParsedRow row : rows) {
            validateRow(row, stationByName, shareholderByName, existingActivePairs, seenPairs, errors, validRows);
        }

        if (!errors.isEmpty()) {
            return new ShareholdingImportResult(rows.size(), 0, errors);
        }

        List<Shareholding> savedRelations = shareholdingRepository.saveAll(
                validRows.stream().map(this::toShareholding).toList()
        );

        // 每条导入关系写一条操作日志，关联到对应充电站，进入站点时间线
        for (Shareholding relation : savedRelations) {
            operationLogService.log(
                    operatorAccount,
                    operatorRole,
                    OperationTargetType.SHAREHOLDING,
                    String.valueOf(relation.getId()),
                    "股东ID:" + relation.getShareholderId(),
                    OperationType.CREATE,
                    null,
                    relation,
                    "批量导入",
                    relation.getStationId()
            );
        }

        return new ShareholdingImportResult(rows.size(), savedRelations.size(), List.of());
    }

    private List<ParsedRow> parseRows(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("VALIDATION_ERROR", "请选择要导入的 Excel 文件", HttpStatus.BAD_REQUEST);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".xlsx")) {
            throw new BusinessException("VALIDATION_ERROR", "仅支持 .xlsx 格式的 Excel 文件", HttpStatus.BAD_REQUEST);
        }

        try (InputStream inputStream = file.getInputStream(); XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new BusinessException("VALIDATION_ERROR", "Excel 文件内容为空", HttpStatus.BAD_REQUEST);
            }

            validateHeader(sheet.getRow(0));

            int lastRowNum = sheet.getLastRowNum();
            if (lastRowNum > MAX_ROW_COUNT) {
                throw new BusinessException("VALIDATION_ERROR", "单次导入最多 " + MAX_ROW_COUNT + " 行数据", HttpStatus.BAD_REQUEST);
            }

            List<ParsedRow> rows = new ArrayList<>();
            for (int rowIndex = 1; rowIndex <= lastRowNum; rowIndex++) {
                XSSFRow row = sheet.getRow(rowIndex);
                if (row == null || isBlankRow(row)) {
                    continue;
                }
                rows.add(new ParsedRow(
                        rowIndex + 1,
                        readCellString(row.getCell(0)),
                        readCellString(row.getCell(1)),
                        readCellString(row.getCell(2))
                ));
            }
            return rows;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException("INVALID_FILE", "Excel 文件解析失败，请使用系统提供的模板", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateHeader(XSSFRow headerRow) {
        if (headerRow == null) {
            throw new BusinessException("VALIDATION_ERROR", "Excel 文件缺少表头，请使用系统提供的模板", HttpStatus.BAD_REQUEST);
        }
        for (int i = 0; i < TEMPLATE_HEADERS.size(); i++) {
            String actual = readCellString(headerRow.getCell(i));
            if (!TEMPLATE_HEADERS.get(i).equals(actual)) {
                throw new BusinessException(
                        "VALIDATION_ERROR",
                        "表头不正确，请使用系统提供的模板（第 " + (i + 1) + " 列应为「" + TEMPLATE_HEADERS.get(i) + "」）",
                        HttpStatus.BAD_REQUEST
                );
            }
        }
    }

    private void validateRow(ParsedRow row,
                             Map<String, Station> stationByName,
                             Map<String, Shareholder> shareholderByName,
                             Set<String> existingActivePairs,
                             Set<String> seenPairs,
                             List<ShareholdingImportError> errors,
                             List<ResolvedRow> validRows) {
        String stationName = row.stationName().trim();
        if (stationName.isEmpty()) {
            errors.add(new ShareholdingImportError(row.rowNumber(), "充电站名称不能为空"));
            return;
        }
        Station station = stationByName.get(stationName);
        if (station == null) {
            errors.add(new ShareholdingImportError(row.rowNumber(), "充电站不存在：「" + stationName + "」"));
            return;
        }

        String shareholderName = row.shareholderName().trim();
        if (shareholderName.isEmpty()) {
            errors.add(new ShareholdingImportError(row.rowNumber(), "股东名称不能为空"));
            return;
        }
        Shareholder shareholder = shareholderByName.get(shareholderName);
        if (shareholder == null) {
            errors.add(new ShareholdingImportError(row.rowNumber(), "股东不存在：「" + shareholderName + "」"));
            return;
        }
        if (shareholder.getShareholderStatus() != ShareholderStatus.ENABLED) {
            errors.add(new ShareholdingImportError(row.rowNumber(), "股东「" + shareholderName + "」已停用，不能新建持股关系"));
            return;
        }

        BigDecimal percentage = parsePercentage(row.percentageRaw());
        if (percentage == null) {
            errors.add(new ShareholdingImportError(row.rowNumber(), "股份占比必须为 0~100 且最多 2 位小数"));
            return;
        }

        if (!seenPairs.add(stationName + "\u0000" + shareholderName)) {
            errors.add(new ShareholdingImportError(row.rowNumber(), "（充电站名称，股东名称）组合在文件中重复"));
            return;
        }

        if (existingActivePairs.contains(station.getId() + "\u0000" + shareholder.getId())) {
            errors.add(new ShareholdingImportError(row.rowNumber(), "该股东在「" + stationName + "」已是持股中，无需重复导入"));
            return;
        }

        validRows.add(new ResolvedRow(station.getId(), shareholder.getId(), shareholderName, percentage));
    }

    private BigDecimal parsePercentage(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) {
            return null;
        }
        BigDecimal percentage;
        try {
            percentage = new BigDecimal(value);
        } catch (NumberFormatException exception) {
            return null;
        }
        if (percentage.compareTo(BigDecimal.ZERO) < 0 || percentage.compareTo(MAX_OWNERSHIP_PERCENTAGE) > 0) {
            return null;
        }
        if (percentage.scale() > 2) {
            return null;
        }
        return percentage;
    }

    private Map<String, Station> loadStationsByName(List<ParsedRow> rows) {
        Set<String> names = rows.stream()
                .map(ParsedRow::stationName)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toSet());
        if (names.isEmpty()) {
            return Map.of();
        }
        return stationRepository.findByStationNameIn(names).stream()
                .collect(Collectors.toMap(Station::getStationName, Function.identity()));
    }

    private Map<String, Shareholder> loadShareholdersByName(List<ParsedRow> rows) {
        Set<String> names = rows.stream()
                .map(ParsedRow::shareholderName)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toSet());
        if (names.isEmpty()) {
            return Map.of();
        }
        return shareholderRepository.findByShareholderNameIn(names).stream()
                .collect(Collectors.toMap(Shareholder::getShareholderName, Function.identity()));
    }

    private Set<String> loadExistingActivePairs(java.util.Collection<Station> stations) {
        Set<Long> stationIds = stations.stream().map(Station::getId).collect(Collectors.toSet());
        if (stationIds.isEmpty()) {
            return Set.of();
        }
        return shareholdingRepository.findByStationIdInAndHoldingStatus(stationIds, HoldingStatus.ACTIVE).stream()
                .map(relation -> relation.getStationId() + "\u0000" + relation.getShareholderId())
                .collect(Collectors.toSet());
    }

    private Shareholding toShareholding(ResolvedRow row) {
        Shareholding shareholding = new Shareholding();
        shareholding.setStationId(row.stationId());
        shareholding.setShareholderId(row.shareholderId());
        shareholding.setShareholderName(row.shareholderName());
        shareholding.setOwnershipPercentage(row.ownershipPercentage());
        shareholding.setHoldingStatus(HoldingStatus.ACTIVE);
        shareholding.setEffectiveAt(LocalDateTime.now());
        return shareholding;
    }

    private String readCellString(Cell cell) {
        if (cell == null) {
            return "";
        }
        CellType type = cell.getCellType();
        if (type == CellType.STRING) {
            return cell.getStringCellValue().trim();
        }
        if (type == CellType.NUMERIC) {
            return dataFormatter.formatCellValue(cell).trim();
        }
        if (type == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        }
        return "";
    }

    private boolean isBlankRow(XSSFRow row) {
        return readCellString(row.getCell(1)).isEmpty() && readCellString(row.getCell(2)).isEmpty();
    }

    private record ParsedRow(int rowNumber, String stationName, String shareholderName, String percentageRaw) {
    }

    private record ResolvedRow(Long stationId, Long shareholderId, String shareholderName, BigDecimal ownershipPercentage) {
    }
}
