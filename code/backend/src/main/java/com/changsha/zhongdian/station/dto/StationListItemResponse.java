package com.changsha.zhongdian.station.dto;

import java.time.LocalDateTime;

public record StationListItemResponse(
        String stationCode,
        String stationName,
        String platformName,
        String platformStationCode,
        String contactName,
        String contactPhone,
        String stationStatusName,
        LocalDateTime updatedAt
) {
}
