package com.changsha.zhongdian.station.dto;

import java.time.LocalDateTime;

public record RecentStationResponse(
        Long id,
        String stationCode,
        String stationName,
        String platformName,
        String statusName,
        String contactName,
        String contactPhone,
        LocalDateTime createdAt
) {
}
