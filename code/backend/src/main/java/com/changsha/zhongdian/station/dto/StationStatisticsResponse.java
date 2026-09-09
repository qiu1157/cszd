package com.changsha.zhongdian.station.dto;

import java.util.List;

public record StationStatisticsResponse(
        long totalCount,
        List<StationStatusCount> statusCounts
) {
    public record StationStatusCount(
            Long statusItemId,
            String statusItemCode,
            String statusItemName,
            long count
    ) {
    }
}
