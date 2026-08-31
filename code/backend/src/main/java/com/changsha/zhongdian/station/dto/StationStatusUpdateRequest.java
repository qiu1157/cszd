package com.changsha.zhongdian.station.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StationStatusUpdateRequest(
        @NotNull(message = "请选择新状态")
        Long stationStatusItemId,
        @Size(max = 500, message = "备注最多输入500字符")
        String remark
) {
}
