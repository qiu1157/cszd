package com.changsha.zhongdian.station.dto;

public record StationDetailResponse(
        Long id,
        String stationCode,
        String stationName,
        Long platformItemId,
        String platformName,
        String platformStationCode,
        String address,
        String contactName,
        String contactPhone,
        String meterAccountNumber,
        Long stationStatusItemId,
        String stationStatusName
) {
}
