package com.changsha.zhongdian.pile.dto;

import java.util.List;

public record PileStatisticsResponse(
        long totalCount,
        long fastCount,
        long slowCount,
        List<PileStatusCount> statusCounts
) {
    public record PileStatusCount(
            Long statusItemId,
            String statusItemCode,
            String statusItemName,
            long count
    ) {
    }
}
