package com.changsha.zhongdian.shareholding.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShareholderStationListItemResponse(
        Long stationId,
        String stationCode,
        String stationName,
        String stationStatusName,
        BigDecimal ownershipPercentage,
        LocalDateTime effectiveAt
) {
}