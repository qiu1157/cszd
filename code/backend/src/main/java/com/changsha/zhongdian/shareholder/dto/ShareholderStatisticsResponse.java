package com.changsha.zhongdian.shareholder.dto;

public record ShareholderStatisticsResponse(
        long totalCount,
        long enabledCount,
        long disabledCount
) {
}
