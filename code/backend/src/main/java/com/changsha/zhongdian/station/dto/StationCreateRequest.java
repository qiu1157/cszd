package com.changsha.zhongdian.station.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StationCreateRequest(
        @NotBlank(message = "请输入充电站名称")
        String stationName,
        @NotNull(message = "请选择所在平台")
        Long platformItemId,
        String platformStationCode,
        String address,
        String contactName,
        String contactPhone
) {
}
