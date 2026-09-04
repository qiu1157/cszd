package com.changsha.zhongdian.shareholder.dto;

import java.time.LocalDateTime;

public record ShareholderListItemResponse(
        Long id,
        String shareholderName,
        String contactName,
        String contactPhone,
        String shareholderStatus,
        LocalDateTime updatedAt,
        // 关联站点统计：只数持股中（ACTIVE）的在管站点，与详情页关联站点列表同一口径
        long stationCount
) {
}