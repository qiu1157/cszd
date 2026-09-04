package com.changsha.zhongdian.shareholding.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.shareholding.dto.ShareholdingCreateRequest;
import com.changsha.zhongdian.shareholding.entity.HoldingStatus;
import com.changsha.zhongdian.shareholding.entity.Shareholding;
import com.changsha.zhongdian.shareholding.repository.ShareholdingRepository;
import com.changsha.zhongdian.shareholder.entity.Shareholder;
import com.changsha.zhongdian.shareholder.entity.ShareholderStatus;
import com.changsha.zhongdian.shareholder.repository.ShareholderRepository;
import com.changsha.zhongdian.station.repository.StationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ShareholdingCommandServiceTest {

    @Test
    void createShareholdingShouldSaveActiveRelationAndTakeEffectImmediately() {
        ServiceFixture fixture = new ServiceFixture();
        when(fixture.stationRepository.existsById(2L)).thenReturn(true);
        when(fixture.shareholderRepository.findById(1L)).thenReturn(Optional.of(fixture.enabledShareholder()));
        when(fixture.shareholdingRepository
                .existsByStationIdAndShareholderIdAndHoldingStatus(2L, 1L, HoldingStatus.ACTIVE)).thenReturn(false);

        Long createdId = fixture.commandService.createShareholding(2L, new ShareholdingCreateRequest(
                1L,
                new BigDecimal("30"),
                "ACTIVE"
        ));

        assertEquals(9001L, createdId);
        verify(fixture.shareholdingRepository).save(argThat(shareholding ->
                shareholding.getStationId().equals(2L)
                        && shareholding.getShareholderId().equals(1L)
                        && new BigDecimal("30").compareTo(shareholding.getOwnershipPercentage()) == 0
                        && HoldingStatus.ACTIVE.equals(shareholding.getHoldingStatus())
                        // spec 7.5：新增关系与股权调整同口径立即生效，生效时间在保存时由服务端落定
                        && shareholding.getEffectiveAt() != null
        ));
    }

    @Test
    void createShareholdingShouldRejectMissingStation() {
        ServiceFixture fixture = new ServiceFixture();
        when(fixture.stationRepository.existsById(9999L)).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fixture.commandService.createShareholding(9999L, new ShareholdingCreateRequest(
                        1L,
                        new BigDecimal("30"),
                        "ACTIVE"
                ))
        );

        assertEquals("STATION_NOT_FOUND", exception.getCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void createShareholdingShouldRejectMissingShareholder() {
        ServiceFixture fixture = new ServiceFixture();
        when(fixture.stationRepository.existsById(2L)).thenReturn(true);
        when(fixture.shareholderRepository.findById(9999L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fixture.commandService.createShareholding(2L, new ShareholdingCreateRequest(
                        9999L,
                        new BigDecimal("30"),
                        "ACTIVE"
                ))
        );

        assertEquals("SHAREHOLDER_NOT_FOUND", exception.getCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void createShareholdingShouldRejectDisabledShareholder() {
        ServiceFixture fixture = new ServiceFixture();
        when(fixture.stationRepository.existsById(2L)).thenReturn(true);
        Shareholder disabled = fixture.enabledShareholder();
        disabled.setShareholderStatus(ShareholderStatus.DISABLED);
        when(fixture.shareholderRepository.findById(1L)).thenReturn(Optional.of(disabled));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fixture.commandService.createShareholding(2L, new ShareholdingCreateRequest(
                        1L,
                        new BigDecimal("30"),
                        "ACTIVE"
                ))
        );

        // E-8 停用兜底在写侧生效：候选接口过滤之外，直连接口传停用股东同样被拦截
        assertEquals("SHAREHOLDER_DISABLED", exception.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(fixture.shareholdingRepository, never()).save(any(Shareholding.class));
    }

    @Test
    void createShareholdingShouldRejectDuplicateActiveRelation() {
        ServiceFixture fixture = new ServiceFixture();
        when(fixture.stationRepository.existsById(2L)).thenReturn(true);
        when(fixture.shareholderRepository.findById(1L)).thenReturn(Optional.of(fixture.enabledShareholder()));
        when(fixture.shareholdingRepository
                .existsByStationIdAndShareholderIdAndHoldingStatus(2L, 1L, HoldingStatus.ACTIVE)).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fixture.commandService.createShareholding(2L, new ShareholdingCreateRequest(
                        1L,
                        new BigDecimal("30"),
                        "ACTIVE"
                ))
        );

        assertEquals("SHAREHOLDING_DUPLICATE", exception.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(fixture.shareholdingRepository, never()).save(any(Shareholding.class));
    }

    @Test
    void createShareholdingShouldAllowSumExceedingHundred() {
        ServiceFixture fixture = new ServiceFixture();
        when(fixture.stationRepository.existsById(2L)).thenReturn(true);
        when(fixture.shareholderRepository.findById(1L)).thenReturn(Optional.of(fixture.enabledShareholder()));
        when(fixture.shareholdingRepository
                .existsByStationIdAndShareholderIdAndHoldingStatus(2L, 1L, HoldingStatus.ACTIVE)).thenReturn(false);

        Long createdId = fixture.commandService.createShareholding(2L, new ShareholdingCreateRequest(
                1L,
                new BigDecimal("20"),
                "ACTIVE"
        ));

        // 变更口径：合计不等于 100% 不再强制拦截（如已有 90% 再加 20% 也放行），
        // 保存确认提示由前端弹窗承担，服务端仅保留单条 0% ~ 100% 强制校验
        assertEquals(9001L, createdId);
        verify(fixture.shareholdingRepository).save(argThat(shareholding ->
                HoldingStatus.ACTIVE.equals(shareholding.getHoldingStatus())
                        && new BigDecimal("20").compareTo(shareholding.getOwnershipPercentage()) == 0
        ));
    }

    @Test
    void createShareholdingShouldRejectNegativePercentage() {
        ServiceFixture fixture = new ServiceFixture();
        when(fixture.stationRepository.existsById(2L)).thenReturn(true);
        when(fixture.shareholderRepository.findById(1L)).thenReturn(Optional.of(fixture.enabledShareholder()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fixture.commandService.createShareholding(2L, new ShareholdingCreateRequest(
                        1L,
                        new BigDecimal("-5"),
                        "ACTIVE"
                ))
        );

        assertEquals("VALIDATION_ERROR", exception.getCode());
        assertEquals("占股比例不能为负数", exception.getMessage());
    }

    @Test
    void createShareholdingShouldSkipDuplicateAndSumCheckForExited() {
        ServiceFixture fixture = new ServiceFixture();
        when(fixture.stationRepository.existsById(2L)).thenReturn(true);
        when(fixture.shareholderRepository.findById(1L)).thenReturn(Optional.of(fixture.enabledShareholder()));
        // 同股东已有持股中关系：以已退出状态补录历史关系仍应放行，不受持股中重复与合计限制
        when(fixture.shareholdingRepository
                .existsByStationIdAndShareholderIdAndHoldingStatus(2L, 1L, HoldingStatus.ACTIVE)).thenReturn(true);

        Long createdId = fixture.commandService.createShareholding(2L, new ShareholdingCreateRequest(
                1L,
                new BigDecimal("10"),
                "EXITED"
        ));

        assertEquals(9001L, createdId);
        verify(fixture.shareholdingRepository).save(argThat(shareholding ->
                HoldingStatus.EXITED.equals(shareholding.getHoldingStatus())
        ));
    }

    private static class ServiceFixture {
        private final ShareholdingRepository shareholdingRepository = mock(ShareholdingRepository.class);
        private final ShareholderRepository shareholderRepository = mock(ShareholderRepository.class);
        private final StationRepository stationRepository = mock(StationRepository.class);
        private final ShareholdingCommandService commandService = new ShareholdingCommandService(
                shareholdingRepository, shareholderRepository, stationRepository
        );

        private Shareholder enabledShareholder() {
            Shareholder shareholder = new Shareholder();
            shareholder.setShareholderName("长沙城新投资有限公司");
            shareholder.setContactName("刘可");
            shareholder.setContactPhone("13977882211");
            shareholder.setShareholderStatus(ShareholderStatus.ENABLED);
            return shareholder;
        }

        // Mockito 的 Answer.answer 已声明 throws Throwable，lambda 内的反射受检异常可合法抛出，
        // 构造器无需再声明 throws，调用方保持干净签名
        ServiceFixture() {
            when(shareholdingRepository.save(any(Shareholding.class))).thenAnswer(invocation -> {
                Shareholding shareholding = invocation.getArgument(0);
                // 与股东命令测试一致：mock save 时通过反射回填主键，模拟 JPA 持久化行为
                java.lang.reflect.Field idField = Shareholding.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(shareholding, 9001L);
                return shareholding;
            });
        }
    }
}