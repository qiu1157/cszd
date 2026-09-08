package com.changsha.zhongdian.shareholder.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.shareholder.dto.ShareholderCreateRequest;
import com.changsha.zhongdian.shareholder.dto.ShareholderUpdateRequest;
import com.changsha.zhongdian.shareholder.entity.Shareholder;
import com.changsha.zhongdian.shareholder.entity.ShareholderStatus;
import com.changsha.zhongdian.shareholder.repository.ShareholderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ShareholderCommandServiceTest {

    @Test
    void createShareholderShouldTrimNameAndNormalizeContactFields() throws Exception {
        ShareholderRepository shareholderRepository = org.mockito.Mockito.mock(ShareholderRepository.class);
        ShareholderCommandService commandService = new ShareholderCommandService(shareholderRepository);

        when(shareholderRepository.save(any(Shareholder.class))).thenAnswer(invocation -> {
            Shareholder shareholder = invocation.getArgument(0);
            java.lang.reflect.Field idField = Shareholder.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(shareholder, 3001L);
            return shareholder;
        });

        Shareholder result = commandService.createShareholder(new ShareholderCreateRequest(
                "  长沙城新能源合伙企业  ",
                "  ",
                "刘可",
                "13977882211"
        ));

        assertEquals(3001L, result.getId());
        verify(shareholderRepository).save(argThat(shareholder ->
                "长沙城新能源合伙企业".equals(shareholder.getShareholderName())
                        && shareholder.getContactName() == null
                        && "13977882211".equals(shareholder.getContactPhone())
                        && ShareholderStatus.ENABLED.equals(shareholder.getShareholderStatus())
        ));
    }

    @Test
    void createShareholderShouldRejectInvalidStatusValue() {
        ShareholderRepository shareholderRepository = org.mockito.Mockito.mock(ShareholderRepository.class);
        ShareholderCommandService commandService = new ShareholderCommandService(shareholderRepository);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> commandService.createShareholder(new ShareholderCreateRequest(
                        "长沙城新能源合伙企业",
                        null,
                        null,
                        "ACTIVE"
                ))
        );

        assertEquals("SHAREHOLDER_STATUS_INVALID", exception.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void updateShareholderShouldApplyFieldsAndKeepStatusUntouched() throws Exception {
        ShareholderRepository shareholderRepository = org.mockito.Mockito.mock(ShareholderRepository.class);
        ShareholderCommandService commandService = new ShareholderCommandService(shareholderRepository);

        Shareholder existing = new Shareholder();
        existing.setShareholderName("长沙城新能源合伙企业");
        existing.setShareholderStatus(ShareholderStatus.DISABLED);
        when(shareholderRepository.findById(3001L)).thenReturn(Optional.of(existing));
        // 与 create 测试一致：mock save 时通过反射回填主键，模拟 JPA 持久化行为
        when(shareholderRepository.save(any(Shareholder.class))).thenAnswer(invocation -> {
            Shareholder shareholder = invocation.getArgument(0);
            java.lang.reflect.Field idField = Shareholder.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(shareholder, 3001L);
            return shareholder;
        });

        Shareholder result = commandService.updateShareholder(3001L, new ShareholderUpdateRequest(
                "  长沙城新能源合伙企业（更新）  ",
                "刘可",
                "13977882211"
        ));

        assertEquals(3001L, result.getId());
        verify(shareholderRepository).save(argThat(shareholder ->
                "长沙城新能源合伙企业（更新）".equals(shareholder.getShareholderName())
                        && "刘可".equals(shareholder.getContactName())
                        && "13977882211".equals(shareholder.getContactPhone())
                        // 编辑仅维护名称与联系方式，状态只能经启用/停用能力调整
                        && ShareholderStatus.DISABLED.equals(shareholder.getShareholderStatus())
        ));
    }

    @Test
    void updateShareholderShouldRejectMissingShareholder() {
        ShareholderRepository shareholderRepository = org.mockito.Mockito.mock(ShareholderRepository.class);
        ShareholderCommandService commandService = new ShareholderCommandService(shareholderRepository);

        when(shareholderRepository.findById(9999L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> commandService.updateShareholder(9999L, new ShareholderUpdateRequest(
                        "长沙城新能源合伙企业",
                        null,
                        null
                ))
        );

        assertEquals("SHAREHOLDER_NOT_FOUND", exception.getCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void updateShareholderStatusShouldSwitchEnabledToDisabled() {
        ShareholderRepository shareholderRepository = org.mockito.Mockito.mock(ShareholderRepository.class);
        ShareholderCommandService commandService = new ShareholderCommandService(shareholderRepository);

        Shareholder existing = new Shareholder();
        existing.setShareholderName("长沙城新投资有限公司");
        existing.setShareholderStatus(ShareholderStatus.ENABLED);
        when(shareholderRepository.findById(2001L)).thenReturn(Optional.of(existing));

        commandService.updateShareholderStatus(2001L, "DISABLED");

        verify(shareholderRepository).save(argThat(shareholder ->
                ShareholderStatus.DISABLED.equals(shareholder.getShareholderStatus())
                        && "长沙城新投资有限公司".equals(shareholder.getShareholderName())
        ));
    }

    @Test
    void updateShareholderStatusShouldSwitchDisabledToEnabled() {
        ShareholderRepository shareholderRepository = org.mockito.Mockito.mock(ShareholderRepository.class);
        ShareholderCommandService commandService = new ShareholderCommandService(shareholderRepository);

        Shareholder existing = new Shareholder();
        existing.setShareholderName("湘江产业基金");
        existing.setShareholderStatus(ShareholderStatus.DISABLED);
        when(shareholderRepository.findById(2002L)).thenReturn(Optional.of(existing));

        commandService.updateShareholderStatus(2002L, "ENABLED");

        verify(shareholderRepository).save(argThat(shareholder ->
                ShareholderStatus.ENABLED.equals(shareholder.getShareholderStatus())
                        && "湘江产业基金".equals(shareholder.getShareholderName())
        ));
    }

    @Test
    void updateShareholderStatusShouldRejectMissingShareholder() {
        ShareholderRepository shareholderRepository = org.mockito.Mockito.mock(ShareholderRepository.class);
        ShareholderCommandService commandService = new ShareholderCommandService(shareholderRepository);

        when(shareholderRepository.findById(9999L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> commandService.updateShareholderStatus(9999L, "ENABLED")
        );

        assertEquals("SHAREHOLDER_NOT_FOUND", exception.getCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}