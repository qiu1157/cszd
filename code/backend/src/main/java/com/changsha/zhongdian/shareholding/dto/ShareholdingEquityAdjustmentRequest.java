package com.changsha.zhongdian.shareholding.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

// 股权调整批量入参：以站点为维度一次性调整多条持股中关系的占比
public record ShareholdingEquityAdjustmentRequest(
        @NotEmpty(message = "请至少选择一条持股关系")
        @Valid
        List<EquityAdjustmentItem> items
) {

    public record EquityAdjustmentItem(
            @NotNull(message = "持股关系ID不能为空")
            Long shareholdingId,
            @NotNull(message = "请填写占股比例")
            @DecimalMin(value = "0", message = "占股比例不能为负数")
            @DecimalMax(value = "100", message = "占股比例不能超过100")
            BigDecimal ownershipPercentage
    ) {
    }
}
