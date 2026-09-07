package com.changsha.zhongdian.shareholding.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.shareholding.dto.ShareholdingCreateRequest;
import com.changsha.zhongdian.shareholding.dto.ShareholdingEquityAdjustmentRequest;
import com.changsha.zhongdian.shareholding.entity.EquityAdjustmentEvent;
import com.changsha.zhongdian.shareholding.entity.HoldingStatus;
import com.changsha.zhongdian.shareholding.entity.Shareholding;
import com.changsha.zhongdian.shareholding.repository.EquityAdjustmentEventRepository;
import com.changsha.zhongdian.shareholding.repository.ShareholdingRepository;
import com.changsha.zhongdian.shareholder.entity.Shareholder;
import com.changsha.zhongdian.shareholder.entity.ShareholderStatus;
import com.changsha.zhongdian.shareholder.repository.ShareholderRepository;
import com.changsha.zhongdian.station.repository.StationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Test
    void exitShareholdingShouldMarkExitedAndRefreshEffectiveAt() {
        ServiceFixture fixture = new ServiceFixture();
        when(fixture.stationRepository.existsById(2L)).thenReturn(true);
        when(fixture.shareholdingRepository.findById(9001L))
                .thenReturn(Optional.of(activeRelation(2L, 1L, new BigDecimal("30"))));

        Long exitedId = fixture.commandService.exitShareholding(2L, 9001L);

        assertEquals(9001L, exitedId);
        verify(fixture.shareholdingRepository).save(argThat(shareholding ->
                HoldingStatus.EXITED.equals(shareholding.getHoldingStatus())
                        // spec 7.5/8.2：退出立即生效，生效时间由服务端刷新为当前时间，不保留原生效时刻
                        && shareholding.getEffectiveAt() != null
                        && !ORIGINAL_EFFECTIVE_AT.equals(shareholding.getEffectiveAt())
        ));
    }

    @Test
    void exitShareholdingShouldRejectMissingStation() {
        ServiceFixture fixture = new ServiceFixture();
        when(fixture.stationRepository.existsById(9999L)).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fixture.commandService.exitShareholding(9999L, 9001L)
        );

        assertEquals("STATION_NOT_FOUND", exception.getCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(fixture.shareholdingRepository, never()).save(any(Shareholding.class));
    }

    @Test
    void exitShareholdingShouldRejectMissingRelation() {
        ServiceFixture fixture = new ServiceFixture();
        when(fixture.stationRepository.existsById(2L)).thenReturn(true);
        when(fixture.shareholdingRepository.findById(9999L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fixture.commandService.exitShareholding(2L, 9999L)
        );

        assertEquals("SHAREHOLDING_NOT_FOUND", exception.getCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(fixture.shareholdingRepository, never()).save(any(Shareholding.class));
    }

    @Test
    void exitShareholdingShouldRejectRelationOfOtherStation() {
        ServiceFixture fixture = new ServiceFixture();
        when(fixture.stationRepository.existsById(2L)).thenReturn(true);
        // 关系挂在站点 3 下：路径站点 2 的操作按不存在拦截，避免跨站篡改与存在性探测
        when(fixture.shareholdingRepository.findById(9001L))
                .thenReturn(Optional.of(activeRelation(3L, 1L, new BigDecimal("30"))));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fixture.commandService.exitShareholding(2L, 9001L)
        );

        assertEquals("SHAREHOLDING_NOT_FOUND", exception.getCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(fixture.shareholdingRepository, never()).save(any(Shareholding.class));
    }

    @Test
    void exitShareholdingShouldRejectAlreadyExitedRelation() {
        ServiceFixture fixture = new ServiceFixture();
        when(fixture.stationRepository.existsById(2L)).thenReturn(true);
        Shareholding exited = activeRelation(2L, 1L, new BigDecimal("30"));
        exited.setHoldingStatus(HoldingStatus.EXITED);
        when(fixture.shareholdingRepository.findById(9001L)).thenReturn(Optional.of(exited));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fixture.commandService.exitShareholding(2L, 9001L)
        );

        // 重复退出是并发双击/重复提交的常见结果，幂等拦截避免历史痕迹被二次改写
        assertEquals("SHAREHOLDING_ALREADY_EXITED", exception.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(fixture.shareholdingRepository, never()).save(any(Shareholding.class));
    }

    @Test
    void exitShareholdingShouldRedistributeRemainingOwnershipProportionally() {
        ServiceFixture fixture = new ServiceFixture();
        when(fixture.stationRepository.existsById(2L)).thenReturn(true);
        // A(9001) 60%, B(9002) 30%, C(9003) 10%：A 退出后 B、C 按 3:1 瓜分 100%
        Shareholding relationA = activeRelation(2L, 1L, new BigDecimal("60"));
        Shareholding relationB = activeRelation(2L, 2L, new BigDecimal("30"));
        Shareholding relationC = activeRelation(2L, 3L, new BigDecimal("10"));
        setRelationId(relationB, 9002L);
        setRelationId(relationC, 9003L);
        when(fixture.shareholdingRepository.findById(9001L)).thenReturn(Optional.of(relationA));
        when(fixture.shareholdingRepository.findByStationIdAndHoldingStatus(2L, HoldingStatus.ACTIVE))
                .thenReturn(List.of(relationB, relationC));

        fixture.commandService.exitShareholding(2L, 9001L);

        verify(fixture.shareholdingRepository).save(argThat(saved ->
                saved.getId().equals(9001L) && HoldingStatus.EXITED.equals(saved.getHoldingStatus())));
        assertEquals(new BigDecimal("75.00"), relationB.getOwnershipPercentage());
        assertEquals(new BigDecimal("25.00"), relationC.getOwnershipPercentage());
    }

    @Test
    void exitShareholdingShouldSetSingleRemainingRelationToFullOwnership() {
        ServiceFixture fixture = new ServiceFixture();
        when(fixture.stationRepository.existsById(2L)).thenReturn(true);
        Shareholding relationA = activeRelation(2L, 1L, new BigDecimal("60"));
        Shareholding relationB = activeRelation(2L, 2L, new BigDecimal("40"));
        setRelationId(relationB, 9002L);
        when(fixture.shareholdingRepository.findById(9001L)).thenReturn(Optional.of(relationA));
        when(fixture.shareholdingRepository.findByStationIdAndHoldingStatus(2L, HoldingStatus.ACTIVE))
                .thenReturn(List.of(relationB));

        fixture.commandService.exitShareholding(2L, 9001L);

        assertEquals(0, new BigDecimal("100.00").compareTo(relationB.getOwnershipPercentage()));
    }

    @Test
    void exitShareholdingShouldSkipRedistributionWhenNoActiveRelationsRemain() {
        ServiceFixture fixture = new ServiceFixture();
        when(fixture.stationRepository.existsById(2L)).thenReturn(true);
        Shareholding relationA = activeRelation(2L, 1L, new BigDecimal("100"));
        when(fixture.shareholdingRepository.findById(9001L)).thenReturn(Optional.of(relationA));
        when(fixture.shareholdingRepository.findByStationIdAndHoldingStatus(2L, HoldingStatus.ACTIVE))
                .thenReturn(List.of());

        fixture.commandService.exitShareholding(2L, 9001L);

        // 仅剩的唯一关系退出后，站点下已无持股中股东：无需调整，也不应触发除零
        verify(fixture.shareholdingRepository, times(1)).save(any(Shareholding.class));
    }

    @Test
    void exitShareholdingShouldSkipRedistributionWhenRemainingActiveSumIsZero() {
        ServiceFixture fixture = new ServiceFixture();
        when(fixture.stationRepository.existsById(2L)).thenReturn(true);
        Shareholding relationA = activeRelation(2L, 1L, new BigDecimal("60"));
        Shareholding relationB = activeRelation(2L, 2L, BigDecimal.ZERO);
        setRelationId(relationB, 9002L);
        when(fixture.shareholdingRepository.findById(9001L)).thenReturn(Optional.of(relationA));
        when(fixture.shareholdingRepository.findByStationIdAndHoldingStatus(2L, HoldingStatus.ACTIVE))
                .thenReturn(List.of(relationB));

        fixture.commandService.exitShareholding(2L, 9001L);

        assertEquals(0, BigDecimal.ZERO.compareTo(relationB.getOwnershipPercentage()));
    }

    @Test
    void adjustEquityShouldUpdateActiveRelationsAndRefreshEffectiveAt() {
        ServiceFixture fixture = new ServiceFixture();
        when(fixture.stationRepository.existsById(2L)).thenReturn(true);
        Shareholding relationA = activeRelation(2L, 1L, new BigDecimal("60"));
        Shareholding relationB = activeRelation(2L, 2L, new BigDecimal("40"));
        setRelationId(relationA, 9001L);
        setRelationId(relationB, 9002L);
        when(fixture.shareholdingRepository.findByStationIdAndHoldingStatus(2L, HoldingStatus.ACTIVE))
                .thenReturn(List.of(relationA, relationB));
        when(fixture.shareholderRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(shareholderOf(1L, "股东A"), shareholderOf(2L, "股东B")));

        fixture.commandService.adjustEquity(2L, new ShareholdingEquityAdjustmentRequest(List.of(
                new ShareholdingEquityAdjustmentRequest.EquityAdjustmentItem(9001L, new BigDecimal("55")),
                new ShareholdingEquityAdjustmentRequest.EquityAdjustmentItem(9002L, new BigDecimal("45"))
        )), "admin");

        assertEquals(0, new BigDecimal("55.00").compareTo(relationA.getOwnershipPercentage()));
        assertEquals(0, new BigDecimal("45.00").compareTo(relationB.getOwnershipPercentage()));
        // spec 7.5：股权调整立即生效，生效时间由服务端刷新为当前时间
        assertEquals(0, ORIGINAL_EFFECTIVE_AT.compareTo(relationA.getEffectiveAt()) < 0 ? 0 : -1);
    }

    @Test
    void adjustEquityShouldRecordEventWithSnapshots() {
        ServiceFixture fixture = new ServiceFixture();
        when(fixture.stationRepository.existsById(2L)).thenReturn(true);
        Shareholding relationA = activeRelation(2L, 1L, new BigDecimal("60"));
        Shareholding relationB = activeRelation(2L, 2L, new BigDecimal("40"));
        setRelationId(relationA, 9001L);
        setRelationId(relationB, 9002L);
        when(fixture.shareholdingRepository.findByStationIdAndHoldingStatus(2L, HoldingStatus.ACTIVE))
                .thenReturn(List.of(relationA, relationB));
        when(fixture.shareholderRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(shareholderOf(1L, "股东A"), shareholderOf(2L, "股东B")));

        fixture.commandService.adjustEquity(2L, new ShareholdingEquityAdjustmentRequest(List.of(
                new ShareholdingEquityAdjustmentRequest.EquityAdjustmentItem(9001L, new BigDecimal("55")),
                new ShareholdingEquityAdjustmentRequest.EquityAdjustmentItem(9002L, new BigDecimal("45"))
        )), "admin");

        // F-12：保存成功后必须写入股权调整事件，并留存调整前后快照
        verify(fixture.equityAdjustmentEventRepository, times(1)).save(fixture.eventCaptor.capture());
        EquityAdjustmentEvent event = fixture.eventCaptor.getValue();
        assertEquals(2L, event.getStationId());
        assertEquals("admin", event.getOperatorAccount());
        assertEquals(0, new BigDecimal("100.00").compareTo(event.getTotalPercentage()));
        assertTrue(event.getBeforeSnapshot().contains("股东A") && event.getBeforeSnapshot().contains("60"));
        assertTrue(event.getAfterSnapshot().contains("股东A") && event.getAfterSnapshot().contains("55"));
    }

    @Test
    void adjustEquityShouldRejectMissingStation() {
        ServiceFixture fixture = new ServiceFixture();
        when(fixture.stationRepository.existsById(9999L)).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fixture.commandService.adjustEquity(9999L, new ShareholdingEquityAdjustmentRequest(List.of(
                        new ShareholdingEquityAdjustmentRequest.EquityAdjustmentItem(9001L, new BigDecimal("50"))
                )), "admin")
        );

        assertEquals("STATION_NOT_FOUND", exception.getCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(fixture.shareholdingRepository, never()).save(any(Shareholding.class));
        verify(fixture.equityAdjustmentEventRepository, never()).save(any(EquityAdjustmentEvent.class));
    }

    @Test
    void adjustEquityShouldRejectNotActiveRelation() {
        ServiceFixture fixture = new ServiceFixture();
        when(fixture.stationRepository.existsById(2L)).thenReturn(true);
        Shareholding relationA = activeRelation(2L, 1L, new BigDecimal("60"));
        setRelationId(relationA, 9001L);
        when(fixture.shareholdingRepository.findByStationIdAndHoldingStatus(2L, HoldingStatus.ACTIVE))
                .thenReturn(List.of(relationA));
        when(fixture.shareholderRepository.findAllById(List.of(1L)))
                .thenReturn(List.of(shareholderOf(1L, "股东A")));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fixture.commandService.adjustEquity(2L, new ShareholdingEquityAdjustmentRequest(List.of(
                        new ShareholdingEquityAdjustmentRequest.EquityAdjustmentItem(9999L, new BigDecimal("50"))
                )), "admin")
        );

        assertEquals("SHAREHOLDING_NOT_FOUND", exception.getCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(fixture.shareholdingRepository, never()).save(any(Shareholding.class));
        verify(fixture.equityAdjustmentEventRepository, never()).save(any(EquityAdjustmentEvent.class));
    }

    private static final LocalDateTime ORIGINAL_EFFECTIVE_AT = LocalDateTime.of(2026, 9, 1, 10, 0);

    private static void setRelationId(Shareholding shareholding, Long id) {
        try {
            java.lang.reflect.Field idField = Shareholding.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(shareholding, id);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw new RuntimeException("测试数据构造失败：无法设置持股关系 ID", exception);
        }
    }

    private static Shareholding activeRelation(Long stationId, Long shareholderId, BigDecimal ownershipPercentage) {
        Shareholding shareholding = new Shareholding();
        shareholding.setStationId(stationId);
        shareholding.setShareholderId(shareholderId);
        shareholding.setOwnershipPercentage(ownershipPercentage);
        shareholding.setHoldingStatus(HoldingStatus.ACTIVE);
        shareholding.setEffectiveAt(ORIGINAL_EFFECTIVE_AT);
        return shareholding;
    }

    private static Shareholder shareholderOf(Long id, String name) {
        Shareholder shareholder = new Shareholder();
        shareholder.setShareholderName(name);
        try {
            java.lang.reflect.Field idField = Shareholder.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(shareholder, id);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw new RuntimeException("测试数据构造失败：无法设置股东 ID", exception);
        }
        return shareholder;
    }

    private static class ServiceFixture {
        private final ShareholdingRepository shareholdingRepository = mock(ShareholdingRepository.class);
        private final ShareholderRepository shareholderRepository = mock(ShareholderRepository.class);
        private final StationRepository stationRepository = mock(StationRepository.class);
        private final EquityAdjustmentEventRepository equityAdjustmentEventRepository = mock(EquityAdjustmentEventRepository.class);
        private final org.mockito.ArgumentCaptor<EquityAdjustmentEvent> eventCaptor = forClass(EquityAdjustmentEvent.class);
        private final ObjectMapper objectMapper = new ObjectMapper();
        private final ShareholdingCommandService commandService = new ShareholdingCommandService(
                shareholdingRepository, shareholderRepository, stationRepository, equityAdjustmentEventRepository, objectMapper
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