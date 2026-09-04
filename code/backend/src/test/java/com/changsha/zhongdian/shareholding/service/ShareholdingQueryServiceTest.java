package com.changsha.zhongdian.shareholding.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.shareholding.dto.ShareholderStationListItemResponse;
import com.changsha.zhongdian.shareholding.repository.ShareholderStationListItemProjection;
import com.changsha.zhongdian.shareholding.repository.ShareholdingRepository;
import com.changsha.zhongdian.shareholder.repository.ShareholderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ShareholdingQueryServiceTest {

    @Test
    void getShareholderStationsShouldReturnActiveStations() {
        ShareholdingRepository shareholdingRepository = org.mockito.Mockito.mock(ShareholdingRepository.class);
        ShareholderRepository shareholderRepository = org.mockito.Mockito.mock(ShareholderRepository.class);
        // 先完成投影 stub 再注册 repository stub：Mockito 不允许在一次 stubbing 完成前嵌套另一个 stubbing
        ShareholderStationListItemProjection first = mockStationItem(
                1001L,
                "ST-20260829-0001",
                "长沙高铁南站充电站",
                "运营中",
                "60.00",
                LocalDateTime.of(2026, 9, 1, 9, 0)
        );
        ShareholderStationListItemProjection second = mockStationItem(
                1002L,
                "ST-20260829-0002",
                "长沙黄花机场充电站",
                "建设中",
                "40.00",
                LocalDateTime.of(2026, 9, 1, 9, 0)
        );
        when(shareholderRepository.existsById(3001L)).thenReturn(true);
        when(shareholdingRepository.findStationListItemsByShareholderIdAndHoldingStatus(3001L, "ACTIVE"))
                .thenReturn(List.of(first, second));

        ShareholdingQueryService shareholdingQueryService =
                new ShareholdingQueryService(shareholdingRepository, shareholderRepository);

        List<ShareholderStationListItemResponse> responses = shareholdingQueryService.getShareholderStations(3001L);

        assertEquals(2, responses.size());
        assertEquals(1001L, responses.get(0).stationId());
        assertEquals("ST-20260829-0001", responses.get(0).stationCode());
        assertEquals("长沙高铁南站充电站", responses.get(0).stationName());
        assertEquals("运营中", responses.get(0).stationStatusName());
        assertEquals(new BigDecimal("60.00"), responses.get(0).ownershipPercentage());
        assertEquals(LocalDateTime.of(2026, 9, 1, 9, 0), responses.get(0).effectiveAt());
        assertEquals(1002L, responses.get(1).stationId());
        assertEquals(new BigDecimal("40.00"), responses.get(1).ownershipPercentage());
    }

    @Test
    void getShareholderStationsShouldReturnEmptyListWhenNoActiveHoldings() {
        ShareholdingRepository shareholdingRepository = org.mockito.Mockito.mock(ShareholdingRepository.class);
        ShareholderRepository shareholderRepository = org.mockito.Mockito.mock(ShareholderRepository.class);
        when(shareholderRepository.existsById(3001L)).thenReturn(true);
        when(shareholdingRepository.findStationListItemsByShareholderIdAndHoldingStatus(3001L, "ACTIVE"))
                .thenReturn(List.of());

        ShareholdingQueryService shareholdingQueryService =
                new ShareholdingQueryService(shareholdingRepository, shareholderRepository);

        List<ShareholderStationListItemResponse> responses = shareholdingQueryService.getShareholderStations(3001L);

        assertTrue(responses.isEmpty());
    }

    @Test
    void getShareholderStationsShouldRejectMissingShareholder() {
        ShareholdingRepository shareholdingRepository = org.mockito.Mockito.mock(ShareholdingRepository.class);
        ShareholderRepository shareholderRepository = org.mockito.Mockito.mock(ShareholderRepository.class);
        when(shareholderRepository.existsById(3999L)).thenReturn(false);

        ShareholdingQueryService shareholdingQueryService =
                new ShareholdingQueryService(shareholdingRepository, shareholderRepository);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> shareholdingQueryService.getShareholderStations(3999L)
        );

        assertEquals("SHAREHOLDER_NOT_FOUND", exception.getCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void getShareholderStationsShouldRejectInvalidShareholderId() {
        ShareholdingRepository shareholdingRepository = org.mockito.Mockito.mock(ShareholdingRepository.class);
        ShareholderRepository shareholderRepository = org.mockito.Mockito.mock(ShareholderRepository.class);
        ShareholdingQueryService shareholdingQueryService =
                new ShareholdingQueryService(shareholdingRepository, shareholderRepository);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> shareholdingQueryService.getShareholderStations(0L)
        );

        assertEquals("VALIDATION_ERROR", exception.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    // 投影接口无实现类，读路径通过 mock stub 提供展示字段
    private ShareholderStationListItemProjection mockStationItem(
            Long stationId,
            String stationCode,
            String stationName,
            String stationStatusName,
            String ownershipPercentage,
            LocalDateTime effectiveAt
    ) {
        ShareholderStationListItemProjection item =
                org.mockito.Mockito.mock(ShareholderStationListItemProjection.class);
        when(item.getStationId()).thenReturn(stationId);
        when(item.getStationCode()).thenReturn(stationCode);
        when(item.getStationName()).thenReturn(stationName);
        when(item.getStationStatusName()).thenReturn(stationStatusName);
        when(item.getOwnershipPercentage()).thenReturn(new BigDecimal(ownershipPercentage));
        when(item.getEffectiveAt()).thenReturn(effectiveAt);
        return item;
    }
}