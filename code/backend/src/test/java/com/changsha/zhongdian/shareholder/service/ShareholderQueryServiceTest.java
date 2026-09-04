package com.changsha.zhongdian.shareholder.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.shareholding.entity.HoldingStatus;
import com.changsha.zhongdian.shareholding.repository.ShareholderStationCountProjection;
import com.changsha.zhongdian.shareholding.repository.ShareholdingRepository;
import com.changsha.zhongdian.shareholder.dto.ShareholderDetailResponse;
import com.changsha.zhongdian.shareholder.dto.ShareholderLinkOptionResponse;
import com.changsha.zhongdian.shareholder.dto.ShareholderListItemResponse;
import com.changsha.zhongdian.shareholder.entity.Shareholder;
import com.changsha.zhongdian.shareholder.entity.ShareholderStatus;
import com.changsha.zhongdian.shareholder.repository.ShareholderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ShareholderQueryServiceTest {

    @Test
    void getShareholderDetailShouldMapEntityToResponse() {
        ShareholderRepository shareholderRepository = org.mockito.Mockito.mock(ShareholderRepository.class);
        // 先完成实体 stub 再注册 repository stub：Mockito 不允许在一次 stubbing 完成前嵌套另一个 stubbing
        Shareholder shareholder = mockShareholder();
        when(shareholderRepository.findById(3001L)).thenReturn(Optional.of(shareholder));

        ShareholderQueryService shareholderQueryService = new ShareholderQueryService(
                shareholderRepository,
                org.mockito.Mockito.mock(ShareholdingRepository.class)
        );

        ShareholderDetailResponse response = shareholderQueryService.getShareholderDetail(3001L);

        assertNotNull(response);
        assertEquals(3001L, response.id());
        assertEquals("长沙城新投资有限公司", response.shareholderName());
        assertEquals("刘可", response.contactName());
        assertEquals("13977882211", response.contactPhone());
        assertEquals("ENABLED", response.shareholderStatus());
        assertEquals(LocalDateTime.of(2026, 9, 1, 9, 0), response.createdAt());
        assertEquals(LocalDateTime.of(2026, 9, 4, 9, 40), response.updatedAt());
    }

    @Test
    void getShareholderDetailShouldRejectMissingShareholder() {
        ShareholderRepository shareholderRepository = org.mockito.Mockito.mock(ShareholderRepository.class);
        when(shareholderRepository.findById(3999L)).thenReturn(Optional.empty());

        ShareholderQueryService shareholderQueryService = new ShareholderQueryService(
                shareholderRepository,
                org.mockito.Mockito.mock(ShareholdingRepository.class)
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> shareholderQueryService.getShareholderDetail(3999L)
        );

        assertEquals("SHAREHOLDER_NOT_FOUND", exception.getCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void getShareholderDetailShouldRejectInvalidShareholderId() {
        ShareholderRepository shareholderRepository = org.mockito.Mockito.mock(ShareholderRepository.class);
        ShareholderQueryService shareholderQueryService = new ShareholderQueryService(
                shareholderRepository,
                org.mockito.Mockito.mock(ShareholdingRepository.class)
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> shareholderQueryService.getShareholderDetail(0L)
        );

        assertEquals("VALIDATION_ERROR", exception.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void listShareholdersShouldAttachActiveStationCount() {
        ShareholderRepository shareholderRepository = org.mockito.Mockito.mock(ShareholderRepository.class);
        ShareholdingRepository shareholdingRepository = org.mockito.Mockito.mock(ShareholdingRepository.class);
        // 先完成实体与投影 stub 再注册 repository stub：Mockito 不允许在一次 stubbing 完成前嵌套另一个 stubbing
        Shareholder shareholder = mockShareholder();
        ShareholderStationCountProjection projection = mockStationCount(3001L, 3L);
        when(shareholderRepository.findAllByOrderByUpdatedAtDesc()).thenReturn(List.of(shareholder));
        when(shareholdingRepository.countStationsGroupByShareholder(HoldingStatus.ACTIVE))
                .thenReturn(List.of(projection));

        ShareholderQueryService shareholderQueryService = new ShareholderQueryService(
                shareholderRepository,
                shareholdingRepository
        );

        List<ShareholderListItemResponse> responses = shareholderQueryService.listShareholders(null, null);

        assertEquals(1, responses.size());
        assertEquals(3001L, responses.get(0).id());
        assertEquals(3L, responses.get(0).stationCount());
    }

    @Test
    void listShareholdersShouldFallbackToZeroWhenNoActiveHoldings() {
        ShareholderRepository shareholderRepository = org.mockito.Mockito.mock(ShareholderRepository.class);
        ShareholdingRepository shareholdingRepository = org.mockito.Mockito.mock(ShareholdingRepository.class);
        Shareholder shareholder = mockShareholder();
        when(shareholderRepository.findAllByOrderByUpdatedAtDesc()).thenReturn(List.of(shareholder));
        when(shareholdingRepository.countStationsGroupByShareholder(HoldingStatus.ACTIVE))
                .thenReturn(List.of());

        ShareholderQueryService shareholderQueryService = new ShareholderQueryService(
                shareholderRepository,
                shareholdingRepository
        );

        List<ShareholderListItemResponse> responses = shareholderQueryService.listShareholders(null, null);

        assertEquals(1, responses.size());
        assertEquals(0L, responses.get(0).stationCount());
    }

    @Test
    void listLinkableShareholderOptionsShouldOnlyQueryEnabledShareholders() {
        ShareholderRepository shareholderRepository = org.mockito.Mockito.mock(ShareholderRepository.class);
        Shareholder shareholder = mockShareholder();
        when(shareholderRepository.findAllByShareholderStatusOrderByShareholderNameAsc(ShareholderStatus.ENABLED))
                .thenReturn(List.of(shareholder));

        ShareholderQueryService shareholderQueryService = new ShareholderQueryService(
                shareholderRepository,
                org.mockito.Mockito.mock(ShareholdingRepository.class)
        );

        List<ShareholderLinkOptionResponse> responses = shareholderQueryService.listLinkableShareholderOptions();

        assertEquals(1, responses.size());
        assertEquals(3001L, responses.get(0).id());
        assertEquals("长沙城新投资有限公司", responses.get(0).shareholderName());
        assertEquals("刘可", responses.get(0).contactName());
        assertEquals("13977882211", responses.get(0).contactPhone());
        // 固定以 ENABLED 查询是 E-8 停用限制的核心：停用股东不会出现在关联候选列表
        verify(shareholderRepository).findAllByShareholderStatusOrderByShareholderNameAsc(ShareholderStatus.ENABLED);
    }

    @Test
    void listLinkableShareholderOptionsShouldReturnEmptyWhenNoEnabledShareholders() {
        ShareholderRepository shareholderRepository = org.mockito.Mockito.mock(ShareholderRepository.class);
        when(shareholderRepository.findAllByShareholderStatusOrderByShareholderNameAsc(ShareholderStatus.ENABLED))
                .thenReturn(List.of());

        ShareholderQueryService shareholderQueryService = new ShareholderQueryService(
                shareholderRepository,
                org.mockito.Mockito.mock(ShareholdingRepository.class)
        );

        List<ShareholderLinkOptionResponse> responses = shareholderQueryService.listLinkableShareholderOptions();

        assertTrue(responses.isEmpty());
    }

    // 主键与时间戳由 JPA 自动维护且无 setter，详情读路径通过 mock stub 提供
    private Shareholder mockShareholder() {
        Shareholder shareholder = org.mockito.Mockito.mock(Shareholder.class);
        when(shareholder.getId()).thenReturn(3001L);
        when(shareholder.getShareholderName()).thenReturn("长沙城新投资有限公司");
        when(shareholder.getContactName()).thenReturn("刘可");
        when(shareholder.getContactPhone()).thenReturn("13977882211");
        when(shareholder.getShareholderStatus()).thenReturn(ShareholderStatus.ENABLED);
        when(shareholder.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 9, 1, 9, 0));
        when(shareholder.getUpdatedAt()).thenReturn(LocalDateTime.of(2026, 9, 4, 9, 40));
        return shareholder;
    }

    // 聚合投影接口无实现类，通过 mock stub 提供股东ID与站点统计数
    private ShareholderStationCountProjection mockStationCount(Long shareholderId, Long stationCount) {
        ShareholderStationCountProjection projection = org.mockito.Mockito.mock(ShareholderStationCountProjection.class);
        when(projection.getShareholderId()).thenReturn(shareholderId);
        when(projection.getStationCount()).thenReturn(stationCount);
        return projection;
    }
}