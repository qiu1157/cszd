package com.changsha.zhongdian.shareholding.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 站点详情股东分区的列表行：持股行联股东主档展示名称与联系方式，
// 状态名由应用层映射为中文口径（HoldingStatus 是应用层枚举而非字典项）
public record StationShareholdingListItemResponse(
        Long id,
        Long shareholderId,
        String shareholderName,
        String contactName,
        String contactPhone,
        BigDecimal ownershipPercentage,
        String holdingStatusName,
        LocalDateTime effectiveAt
) {
}