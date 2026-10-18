package com.changsha.zhongdian.shareholder.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.util.List;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.operationlog.entity.OperationTargetType;
import com.changsha.zhongdian.operationlog.entity.OperationType;
import com.changsha.zhongdian.operationlog.service.OperationLogService;
import com.changsha.zhongdian.shareholder.dto.ShareholderImportResult;
import com.changsha.zhongdian.shareholder.entity.Shareholder;
import com.changsha.zhongdian.shareholder.entity.ShareholderStatus;
import com.changsha.zhongdian.shareholder.repository.ShareholderRepository;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class ShareholderImportServiceTest {

    private final ShareholderRepository shareholderRepository = mock(ShareholderRepository.class);
    private final OperationLogService operationLogService = mock(OperationLogService.class);
    private final ShareholderImportService importService =
            new ShareholderImportService(shareholderRepository, operationLogService);

    @Test
    void generateTemplateShouldReturnXlsxBytes() {
        byte[] content = importService.generateTemplate();

        assertTrue(content.length > 0);
    }

    @Test
    void importShareholdersShouldInsertAllRowsWhenValid() throws Exception {
        when(shareholderRepository.findByShareholderNameIn(anyCollection())).thenReturn(List.of());
        when(shareholderRepository.saveAll(any())).thenReturn(List.of());

        MockMultipartFile file = buildXlsxFile(
                new String[]{"股东名称", "联系人", "联系电话"},
                new String[]{"长沙城新投资有限公司", "刘可", "13977882211"},
                new String[]{"湘江产业基金", "", ""}
        );

        ShareholderImportResult result = importService.importShareholders(file, "admin", "SYSTEM_ADMIN");

        assertEquals(2, result.totalCount());
        assertEquals(2, result.successCount());
        assertTrue(result.errors().isEmpty());

        verify(shareholderRepository).saveAll(any());
        verify(operationLogService).log(
                eq("admin"),
                eq("SYSTEM_ADMIN"),
                eq(OperationTargetType.SHAREHOLDER),
                isNull(),
                anyString(),
                eq(OperationType.IMPORT),
                isNull(),
                any(),
                any(),
                isNull()
        );
    }

    @Test
    void importShareholdersShouldDefaultStatusToEnabled() throws Exception {
        when(shareholderRepository.findByShareholderNameIn(anyCollection())).thenReturn(List.of());
        when(shareholderRepository.saveAll(any())).thenAnswer(invocation -> {
            List<Shareholder> shareholders = invocation.getArgument(0);
            assertEquals(ShareholderStatus.ENABLED, shareholders.get(0).getShareholderStatus());
            return shareholders;
        });

        MockMultipartFile file = buildXlsxFile(
                new String[]{"股东名称", "联系人", "联系电话"},
                new String[]{"长沙城新投资有限公司", "刘可", "13977882211"}
        );

        importService.importShareholders(file, "admin", "SYSTEM_ADMIN");
    }

    @Test
    void importShareholdersShouldRejectDuplicateNameWithinFile() throws Exception {
        when(shareholderRepository.findByShareholderNameIn(anyCollection())).thenReturn(List.of());

        MockMultipartFile file = buildXlsxFile(
                new String[]{"股东名称", "联系人", "联系电话"},
                new String[]{"长沙城新投资有限公司", "", ""},
                new String[]{"长沙城新投资有限公司", "刘可", ""}
        );

        ShareholderImportResult result = importService.importShareholders(file, "admin", "SYSTEM_ADMIN");

        assertEquals(2, result.totalCount());
        assertEquals(0, result.successCount());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).message().contains("在文件中重复"));

        verify(shareholderRepository, never()).saveAll(any());
    }

    @Test
    void importShareholdersShouldRejectExistingName() throws Exception {
        Shareholder existing = new Shareholder();
        existing.setShareholderName("长沙城新投资有限公司");
        when(shareholderRepository.findByShareholderNameIn(anyCollection())).thenReturn(List.of(existing));

        MockMultipartFile file = buildXlsxFile(
                new String[]{"股东名称", "联系人", "联系电话"},
                new String[]{"长沙城新投资有限公司", "", ""}
        );

        ShareholderImportResult result = importService.importShareholders(file, "admin", "SYSTEM_ADMIN");

        assertEquals(0, result.successCount());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).message().contains("已存在"));

        verify(shareholderRepository, never()).saveAll(any());
    }

    @Test
    void importShareholdersShouldRejectBlankName() throws Exception {
        when(shareholderRepository.findByShareholderNameIn(anyCollection())).thenReturn(List.of());

        MockMultipartFile file = buildXlsxFile(
                new String[]{"股东名称", "联系人", "联系电话"},
                new String[]{"", "刘可", ""}
        );

        ShareholderImportResult result = importService.importShareholders(file, "admin", "SYSTEM_ADMIN");

        assertEquals(0, result.successCount());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).message().contains("股东名称不能为空"));

        verify(shareholderRepository, never()).saveAll(any());
    }

    @Test
    void importShareholdersShouldRejectOverlongName() throws Exception {
        when(shareholderRepository.findByShareholderNameIn(anyCollection())).thenReturn(List.of());

        String overlongName = "长".repeat(129);
        MockMultipartFile file = buildXlsxFile(
                new String[]{"股东名称", "联系人", "联系电话"},
                new String[]{overlongName, "", ""}
        );

        ShareholderImportResult result = importService.importShareholders(file, "admin", "SYSTEM_ADMIN");

        assertEquals(0, result.successCount());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).message().contains("128"));

        verify(shareholderRepository, never()).saveAll(any());
    }

    @Test
    void importShareholdersShouldRejectInvalidHeader() throws Exception {
        MockMultipartFile file = buildXlsxFile(
                new String[]{"姓名", "电话", "地址"},
                new String[]{"长沙城新投资有限公司", "", ""}
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> importService.importShareholders(file, "admin", "SYSTEM_ADMIN")
        );

        assertEquals("VALIDATION_ERROR", exception.getCode());
        verify(shareholderRepository, never()).saveAll(any());
    }

    @Test
    void importShareholdersShouldRejectNonXlsxFile() {
        MockMultipartFile file = new MockMultipartFile("file", "shareholders.csv", "text/csv", new byte[]{1});

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> importService.importShareholders(file, "admin", "SYSTEM_ADMIN")
        );

        assertEquals("VALIDATION_ERROR", exception.getCode());
    }

    @Test
    void importShareholdersShouldRejectFileWithoutDataRows() throws Exception {
        when(shareholderRepository.findByShareholderNameIn(anyCollection())).thenReturn(List.of());

        MockMultipartFile file = buildXlsxFile(
                new String[]{"股东名称", "联系人", "联系电话"}
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> importService.importShareholders(file, "admin", "SYSTEM_ADMIN")
        );

        assertTrue(exception.getMessage().contains("没有可导入的数据行"));
    }

    private MockMultipartFile buildXlsxFile(String[]... rows) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("股东导入模板");
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
                    "shareholders.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    outputStream.toByteArray()
            );
        }
    }
}
