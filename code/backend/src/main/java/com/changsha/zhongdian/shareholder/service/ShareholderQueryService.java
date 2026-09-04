package com.changsha.zhongdian.shareholder.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.shareholding.entity.HoldingStatus;
import com.changsha.zhongdian.shareholding.repository.ShareholderStationCountProjection;
import com.changsha.zhongdian.shareholding.repository.ShareholdingRepository;
import com.changsha.zhongdian.shareholder.dto.ShareholderDetailResponse;
import com.changsha.zhongdian.shareholder.dto.ShareholderListItemResponse;
import com.changsha.zhongdian.shareholder.entity.Shareholder;
import com.changsha.zhongdian.shareholder.entity.ShareholderStatus;
import com.changsha.zhongdian.shareholder.repository.ShareholderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ShareholderQueryService {

    private final ShareholderRepository shareholderRepository;
    private final ShareholdingRepository shareholdingRepository;

    public ShareholderQueryService(ShareholderRepository shareholderRepository,
                                    ShareholdingRepository shareholdingRepository) {
        this.shareholderRepository = shareholderRepository;
        this.shareholdingRepository = shareholdingRepository;
    }

    public List<ShareholderListItemResponse> listShareholders(String shareholderNameKeyword, String shareholderStatusValue) {
        String normalizedKeyword = normalizeKeyword(shareholderNameKeyword);
        ShareholderStatus normalizedStatus = normalizeStatus(shareholderStatusValue);

        // 一次 GROUP BY 拉全量持股统计再内存合并：股东列表行数有限，比逐行 COUNT 省数据库往返
        Map<Long, Long> activeStationCountByShareholderId = loadActiveStationCountByShareholderId();

        return resolveShareholders(normalizedKeyword, normalizedStatus).stream()
                .map(shareholder -> toListItemResponse(shareholder, activeStationCountByShareholderId))
                .toList();
    }

    // 详情页只读主档字段：关联站点与时间线在里程碑 F/G 由独立接口提供，避免详情接口承载聚合数据
    public ShareholderDetailResponse getShareholderDetail(Long shareholderId) {
        if (shareholderId == null || shareholderId <= 0L) {
            throw new BusinessException("VALIDATION_ERROR", "股东不存在", HttpStatus.BAD_REQUEST);
        }

        Shareholder shareholder = shareholderRepository.findById(shareholderId)
                .orElseThrow(() -> new BusinessException("SHAREHOLDER_NOT_FOUND", "股东不存在", HttpStatus.NOT_FOUND));

        return new ShareholderDetailResponse(
                shareholder.getId(),
                shareholder.getShareholderName(),
                shareholder.getContactName(),
                shareholder.getContactPhone(),
                shareholder.getShareholderStatus().name(),
                shareholder.getCreatedAt(),
                shareholder.getUpdatedAt()
        );
    }

    private List<Shareholder> resolveShareholders(String shareholderNameKeyword, ShareholderStatus shareholderStatus) {
        if (shareholderNameKeyword != null && shareholderStatus != null) {
            return shareholderRepository.findAllByShareholderNameContainingAndShareholderStatusOrderByUpdatedAtDesc(
                    shareholderNameKeyword,
                    shareholderStatus
            );
        }
        if (shareholderNameKeyword != null) {
            return shareholderRepository.findAllByShareholderNameContainingOrderByUpdatedAtDesc(shareholderNameKeyword);
        }
        if (shareholderStatus != null) {
            return shareholderRepository.findAllByShareholderStatusOrderByUpdatedAtDesc(shareholderStatus);
        }
        return shareholderRepository.findAllByOrderByUpdatedAtDesc();
    }

    private Map<Long, Long> loadActiveStationCountByShareholderId() {
        return shareholdingRepository.countStationsGroupByShareholder(HoldingStatus.ACTIVE).stream()
                .collect(Collectors.toMap(
                        ShareholderStationCountProjection::getShareholderId,
                        ShareholderStationCountProjection::getStationCount
                ));
    }

    // 关联站点统计只数持股中（ACTIVE）的在管站点：已退出属历史痕迹，不计入统计口径
    private ShareholderListItemResponse toListItemResponse(Shareholder shareholder, Map<Long, Long> activeStationCountByShareholderId) {
        long stationCount = activeStationCountByShareholderId.getOrDefault(shareholder.getId(), 0L);
        return new ShareholderListItemResponse(
                shareholder.getId(),
                shareholder.getShareholderName(),
                shareholder.getContactName(),
                shareholder.getContactPhone(),
                shareholder.getShareholderStatus().name(),
                shareholder.getUpdatedAt(),
                stationCount
        );
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String normalizedKeyword = keyword.trim();
        return normalizedKeyword.isEmpty() ? null : normalizedKeyword;
    }

    private ShareholderStatus normalizeStatus(String shareholderStatusValue) {
        if (shareholderStatusValue == null) {
            return null;
        }
        String normalizedValue = shareholderStatusValue.trim();
        if (normalizedValue.isEmpty()) {
            return null;
        }
        try {
            return ShareholderStatus.valueOf(normalizedValue);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}