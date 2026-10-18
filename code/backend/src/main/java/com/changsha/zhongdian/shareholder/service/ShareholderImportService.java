package com.changsha.zhongdian.shareholder.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.operationlog.entity.OperationTargetType;
import com.changsha.zhongdian.operationlog.entity.OperationType;
import com.changsha.zhongdian.operationlog.service.OperationLogService;
import com.changsha.zhongdian.shareholder.dto.ShareholderImportError;
import com.changsha.zhongdian.shareholder.dto.ShareholderImportResult;
import com.changsha.zhongdian.shareholder.entity.Shareholder;
import com.changsha.zhongdian.shareholder.entity.ShareholderStatus;
import com.changsha.zhongdian.shareholder.repository.ShareholderRepository;
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
 * 股东 Excel 批量导入：模板生成 + 解析校验 + 仅新增落库。
 * 为什么独立成服务：导入涉及 POI 解析、逐行校验、原子落库与日志，职责与单条增改差异大，
 * 独立后 ShareholderCommandService 保持单条写操作的内聚。
 */
@Service
public class ShareholderImportService {

    private static final String TEMPLATE_SHEET_NAME = "股东导入模板";
    private static final List<String> TEMPLATE_HEADERS = List.of("股东名称", "联系人", "联系电话");
    private static final int MAX_NAME_LENGTH = 128;
    private static final int MAX_CONTACT_NAME_LENGTH = 64;
    private static final int MAX_CONTACT_PHONE_LENGTH = 20;
    private static final int MAX_ROW_COUNT = 1000;

    private final ShareholderRepository shareholderRepository;
    private final OperationLogService operationLogService;
    private final DataFormatter dataFormatter = new DataFormatter();

    public ShareholderImportService(ShareholderRepository shareholderRepository,
                                    OperationLogService operationLogService) {
        this.shareholderRepository = shareholderRepository;
        this.operationLogService = operationLogService;
    }

    /**
     * 生成导入模板（.xlsx）。模板动态生成，保证表头与校验规则始终一致。
     */
    public byte[] generateTemplate() {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet(TEMPLATE_SHEET_NAME);
            XSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < TEMPLATE_HEADERS.size(); i++) {
                headerRow.createCell(i).setCellValue(TEMPLATE_HEADERS.get(i));
            }
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new BusinessException("TEMPLATE_GENERATE_FAILED", "模板生成失败，请稍后重试", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 导入股东：只允许新增，股东名称为业务主键。
     * 策略：先全量解析与校验，任一数据非法则整体不落库并返回逐行错误；
     * 全部合法才在单事务中 saveAll 落库，并写一条导入操作日志。
     */
    @Transactional
    public ShareholderImportResult importShareholders(MultipartFile file, String operatorAccount, String operatorRole) {
        List<ParsedRow> rows = parseRows(file);

        if (rows.isEmpty()) {
            throw new BusinessException("VALIDATION_ERROR", "Excel 文件中没有可导入的数据行", HttpStatus.BAD_REQUEST);
        }

        Set<String> existingNames = loadExistingNames(rows);
        List<ShareholderImportError> errors = new ArrayList<>();
        List<ParsedRow> validRows = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();

        for (ParsedRow row : rows) {
            validateRow(row, seenNames, existingNames, errors, validRows);
        }

        if (!errors.isEmpty()) {
            return new ShareholderImportResult(rows.size(), 0, errors);
        }

        shareholderRepository.saveAll(validRows.stream().map(this::toShareholder).toList());

        operationLogService.log(
                operatorAccount,
                operatorRole,
                OperationTargetType.SHAREHOLDER,
                null,
                "股东批量导入",
                OperationType.IMPORT,
                null,
                Map.of("totalCount", rows.size(), "successCount", validRows.size()),
                "股东批量导入新增 " + validRows.size() + " 条",
                null
        );

        return new ShareholderImportResult(rows.size(), validRows.size(), List.of());
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
                             Set<String> seenNames,
                             Set<String> existingNames,
                             List<ShareholderImportError> errors,
                             List<ParsedRow> validRows) {
        String name = row.shareholderName().trim();
        if (name.isEmpty()) {
            errors.add(new ShareholderImportError(row.rowNumber(), "股东名称不能为空"));
            return;
        }
        if (name.length() > MAX_NAME_LENGTH) {
            errors.add(new ShareholderImportError(row.rowNumber(), "股东名称不能超过" + MAX_NAME_LENGTH + "个字符"));
            return;
        }
        if (!seenNames.add(name)) {
            errors.add(new ShareholderImportError(row.rowNumber(), "股东名称「" + name + "」在文件中重复"));
            return;
        }
        if (existingNames.contains(name)) {
            errors.add(new ShareholderImportError(row.rowNumber(), "股东名称「" + name + "」已存在，导入仅支持新增"));
            return;
        }

        String contactName = row.contactName().trim();
        if (contactName.length() > MAX_CONTACT_NAME_LENGTH) {
            errors.add(new ShareholderImportError(row.rowNumber(), "联系人不能超过" + MAX_CONTACT_NAME_LENGTH + "个字符"));
            return;
        }
        String contactPhone = row.contactPhone().trim();
        if (contactPhone.length() > MAX_CONTACT_PHONE_LENGTH) {
            errors.add(new ShareholderImportError(row.rowNumber(), "联系电话不能超过" + MAX_CONTACT_PHONE_LENGTH + "个字符"));
            return;
        }

        validRows.add(new ParsedRow(
                row.rowNumber(),
                name,
                emptyToNull(contactName),
                emptyToNull(contactPhone)
        ));
    }

    private Set<String> loadExistingNames(List<ParsedRow> rows) {
        Set<String> names = rows.stream()
                .map(ParsedRow::shareholderName)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toSet());
        if (names.isEmpty()) {
            return Set.of();
        }
        return shareholderRepository.findByShareholderNameIn(names).stream()
                .map(Shareholder::getShareholderName)
                .collect(Collectors.toSet());
    }

    private Shareholder toShareholder(ParsedRow row) {
        Shareholder shareholder = new Shareholder();
        shareholder.setShareholderName(row.shareholderName());
        shareholder.setContactName(row.contactName());
        shareholder.setContactPhone(row.contactPhone());
        shareholder.setShareholderStatus(ShareholderStatus.ENABLED);
        return shareholder;
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
        for (int i = 0; i < TEMPLATE_HEADERS.size(); i++) {
            if (!readCellString(row.getCell(i)).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String emptyToNull(String value) {
        return value.isEmpty() ? null : value;
    }

    private record ParsedRow(int rowNumber, String shareholderName, String contactName, String contactPhone) {
    }
}
