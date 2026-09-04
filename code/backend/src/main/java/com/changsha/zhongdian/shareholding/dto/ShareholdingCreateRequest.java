package com.changsha.zhongdian.shareholding.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// 新建持股关系入参：站点由路径提供，这里只带股东、占比与关系状态；
// 不含备注——备注属于时间线事件描述（原型口径），时间线能力落地前无存储位置，避免虚假录入
public record ShareholdingCreateRequest(
        @NotNull(message = "请选择股东")
        Long shareholderId,
        @NotNull(message = "请填写占股比例")
        @DecimalMin(value = "0", message = "占股比例不能为负数")
        @DecimalMax(value = "100", message = "占股比例不能超过100")
        BigDecimal ownershipPercentage,
        @NotBlank(message = "请选择关系状态")
        String holdingStatus
) {
}