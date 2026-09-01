package com.changsha.zhongdian.pile.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PileUpdateRequest(
        String chargeType,
        @NotNull(message = "请填写充电枪数量")
        @Min(value = 1, message = "充电枪数量至少为1")
        @Max(value = 64, message = "充电枪数量不能超过64")
        Integer gunCount
) {
}
