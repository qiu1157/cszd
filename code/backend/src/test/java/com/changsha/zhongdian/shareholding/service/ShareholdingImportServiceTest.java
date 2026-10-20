package com.changsha.zhongdian.shareholding.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.operationlog.entity.OperationTargetType;
import com.changsha.zhongdian.operationlog.entity.OperationType;
import com.changsha.zhongdian.operationlog.service.OperationLogService;
import com.changsha.zhongdian.shareholding.dto.ShareholdingImportResult;
import com.changsha.zhongdian.shareholding.entity.HoldingStatus;
import com.changsha.zhongdian.shareholding.entity.Shareholding;
import com.changsha.zhongdian.shareholding.repository.ShareholdingRepository;
import com.changsha.zhongdian.shareholder.entity.Shareholder;
import com.changsha.zhongdian.shareholder.entity.ShareholderStatus;
import com.changsha.zhongdian.shareholder.repository.ShareholderRepository;
import com.changsha.zhongdian.station.entity.Station;
import com.changsha.zhongdian.station.repository.StationRepository;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class ShareholdingImportServiceTest {

    private final ShareholdingRepository shareholdingRepository = mock(ShareholdingRepository.class);
    private final ShareholderRepository shareholderRepository = mock(ShareholderRepository.class);
    private final StationRepository stationRepository = mock(StationRepository.class);
    private final OperationLogService operationLogService = mock(OperationLogService.class);
    private final ShareholdingImportService importService = new ShareholdingImportService(
            shareholdingRepository, shareholderRepository, stationRepository, operationLogService);

    @Test
    void generateTemplateShouldPreFillStationNames() throws Exception {
        when(stationRepository.findAllByOrderByStationNameAsc()).thenReturn(List.of(
                station(2001L, "站点B"),
                station(2002L, "站点A")
        ));

        byte[] content = importService.generateTemplate();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            assertEquals("充电站名称", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("站点B", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("站点A", sheet.getRow(2).getCell(0).getStringCellValue());
        }
    }

    @Test
    void importShareholdingsShouldInsertAllRowsWhenValid() throws Exception {
        when(stationRepository.findByStationNameIn(anyCollection()))
                .thenReturn(List.of(station(2001L, "站点A")));
        when(shareholderRepository.findByShareholderNameIn(anyCollection()))
                .thenReturn(List.of(
                        shareholder(1L, "股东A", ShareholderStatus.ENABLED),
                        shareholder(2L, "股东B", ShareholderStatus.ENABLED)
                ));
        when(shareholdingRepository.findByStationIdInAndHoldingStatus(anyCollection(), eq(HoldingStatus.ACTIVE)))
                .thenReturn(List.of());
        when(shareholdingRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = buildXlsxFile(
                new String[]{"充电站名称", "股东名称", "股份占比"},
                new String[]{"站点A", "股东A", "60"},
                new String[]{"站点A", "股东B", "40"}
        );

        ShareholdingImportResult result = importService.importShareholdings(file, "admin", "SYSTEM_ADMIN");

        assertEquals(2, result.totalCount());
        assertEquals(2, result.successCount());
        assertTrue(result.errors().isEmpty());

        verify(shareholdingRepository).saveAll(any());
        verify(operationLogService, times(2)).log(
                any(), any(),
                eq(OperationTargetType.SHAREHOLDING),
                any(), any(),
                eq(OperationType.CREATE),
                isNull(), any(), any(), any()
        );
    }

    @Test
    void importShareholdingsShouldRejectUnknownStation() throws Exception {
        when(stationRepository.findByStationNameIn(anyCollection())).thenReturn(List.of());
        when(shareholderRepository.findByShareholderNameIn(anyCollection()))
                .thenReturn(List.of(shareholder(1L, "股东A", ShareholderStatus.ENABLED)));

        MockMultipartFile file = buildXlsxFile(
                new String[]{"充电站名称", "股东名称", "股份占比"},
                new String[]{"不存在的站点", "股东A", "60"}
        );

        ShareholdingImportResult result = importService.importShareholdings(file, "admin", "SYSTEM_ADMIN");

        assertEquals(0, result.successCount());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).message().contains("充电站不存在"));

        verify(shareholdingRepository, never()).saveAll(any());
    }

    @Test
    void importShareholdingsShouldRejectUnknownShareholder() throws Exception {
        when(stationRepository.findByStationNameIn(anyCollection()))
                .thenReturn(List.of(station(2001L, "站点A")));
        when(shareholderRepository.findByShareholderNameIn(anyCollection())).thenReturn(List.of());

        MockMultipartFile file = buildXlsxFile(
                new String[]{"充电站名称", "股东名称", "股份占比"},
                new String[]{"站点A", "不存在的股东", "60"}
        );

        ShareholdingImportResult result = importService.importShareholdings(file, "admin", "SYSTEM_ADMIN");

        assertEquals(0, result.successCount());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).message().contains("股东不存在"));

        verify(shareholdingRepository, never()).saveAll(any());
    }

    @Test
    void importShareholdingsShouldRejectDisabledShareholder() throws Exception {
        when(stationRepository.findByStationNameIn(anyCollection()))
                .thenReturn(List.of(station(2001L, "站点A")));
        when(shareholderRepository.findByShareholderNameIn(anyCollection()))
                .thenReturn(List.of(shareholder(1L, "股东A", ShareholderStatus.DISABLED)));

        MockMultipartFile file = buildXlsxFile(
                new String[]{"充电站名称", "股东名称", "股份占比"},
                new String[]{"站点A", "股东A", "60"}
        );

        ShareholdingImportResult result = importService.importShareholdings(file, "admin", "SYSTEM_ADMIN");

        assertEquals(0, result.successCount());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).message().contains("已停用"));

        verify(shareholdingRepository, never()).saveAll(any());
    }

    @Test
    void importShareholdingsShouldRejectInvalidPercentage() throws Exception {
        when(stationRepository.findByStationNameIn(anyCollection()))
                .thenReturn(List.of(station(2001L, "站点A")));
        when(shareholderRepository.findByShareholderNameIn(anyCollection()))
                .thenReturn(List.of(shareholder(1L, "股东A", ShareholderStatus.ENABLED)));
        when(shareholdingRepository.findByStationIdInAndHoldingStatus(anyCollection(), eq(HoldingStatus.ACTIVE)))
                .thenReturn(List.of());

        MockMultipartFile file = buildXlsxFile(
                new String[]{"充电站名称", "股东名称", "股份占比"},
                new String[]{"站点A", "股东A", "60.123"}
        );

        ShareholdingImportResult result = importService.importShareholdings(file, "admin", "SYSTEM_ADMIN");

        assertEquals(0, result.successCount());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).message().contains("股份占比"));

        verify(shareholdingRepository, never()).saveAll(any());
    }

    @Test
    void importShareholdingsShouldRejectDuplicatePairInFile() throws Exception {
        when(stationRepository.findByStationNameIn(anyCollection()))
                .thenReturn(List.of(station(2001L, "站点A")));
        when(shareholderRepository.findByShareholderNameIn(anyCollection()))
                .thenReturn(List.of(shareholder(1L, "股东A", ShareholderStatus.ENABLED)));
        when(shareholdingRepository.findByStationIdInAndHoldingStatus(anyCollection(), eq(HoldingStatus.ACTIVE)))
                .thenReturn(List.of());

        MockMultipartFile file = buildXlsxFile(
                new String[]{"充电站名称", "股东名称", "股份占比"},
                new String[]{"站点A", "股东A", "60"},
                new String[]{"站点A", "股东A", "40"}
        );

        ShareholdingImportResult result = importService.importShareholdings(file, "admin", "SYSTEM_ADMIN");

        assertEquals(0, result.successCount());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).message().contains("在文件中重复"));

        verify(shareholdingRepository, never()).saveAll(any());
    }

    @Test
    void importShareholdingsShouldRejectExistingActivePair() throws Exception {
        when(stationRepository.findByStationNameIn(anyCollection()))
                .thenReturn(List.of(station(2001L, "站点A")));
        when(shareholderRepository.findByShareholderNameIn(anyCollection()))
                .thenReturn(List.of(shareholder(1L, "股东A", ShareholderStatus.ENABLED)));

        Shareholding existing = new Shareholding();
        existing.setStationId(2001L);
        existing.setShareholderId(1L);
        when(shareholdingRepository.findByStationIdInAndHoldingStatus(anyCollection(), eq(HoldingStatus.ACTIVE)))
                .thenReturn(List.of(existing));

        MockMultipartFile file = buildXlsxFile(
                new String[]{"充电站名称", "股东名称", "股份占比"},
                new String[]{"站点A", "股东A", "60"}
        );

        ShareholdingImportResult result = importService.importShareholdings(file, "admin", "SYSTEM_ADMIN");

        assertEquals(0, result.successCount());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).message().contains("已是持股中"));

        verify(shareholdingRepository, never()).saveAll(any());
    }

    @Test
    void importShareholdingsShouldRejectInvalidHeader() throws Exception {
        MockMultipartFile file = buildXlsxFile(
                new String[]{"站点", "股东", "比例"},
                new String[]{"站点A", "股东A", "60"}
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> importService.importShareholdings(file, "admin", "SYSTEM_ADMIN")
        );

        assertEquals("VALIDATION_ERROR", exception.getCode());
    }

    @Test
    void importShareholdingsShouldRejectFileWithoutDataRows() throws Exception {
        MockMultipartFile file = buildXlsxFile(
                new String[]{"充电站名称", "股东名称", "股份占比"}
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> importService.importShareholdings(file, "admin", "SYSTEM_ADMIN")
        );

        assertTrue(exception.getMessage().contains("没有可导入的数据行"));
    }

    private Station station(Long id, String name) throws Exception {
        Station station = new Station();
        station.setStationName(name);
        setField(station, "id", id);
        return station;
    }

    private Shareholder shareholder(Long id, String name, ShareholderStatus status) throws Exception {
        Shareholder shareholder = new Shareholder();
        shareholder.setShareholderName(name);
        shareholder.setShareholderStatus(status);
        setField(shareholder, "id", id);
        return shareholder;
    }

    private void setField(Object target, String field, Object value) throws Exception {
        java.lang.reflect.Field declaredField = target.getClass().getDeclaredField(field);
        declaredField.setAccessible(true);
        declaredField.set(target, value);
    }

    private MockMultipartFile buildXlsxFile(String[]... rows) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("股东关系导入模板");
            for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
                XSSFRow row = sheet.createRow(rowIndex);
                String[] values = rows[rowIndex];
                for (int columnIndex = 0; columnIndex < values.length; columnIndex++) {
                    row.createCell(columnIndex).setCellValue(values[columnIndex]);
                }
            }
            workbook.write(outputStream);
            return new MockMultipartFile(
                    "file",
                    "shareholdings.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    outputStream.toByteArray()
            );
        }
    }
}
