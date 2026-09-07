package com.changsha.zhongdian.shareholding.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 站点详情股东分区的列表行：持股行联股东主档展示名称与联系方式；
// holdingStatus 为原始状态码（ACTIVE/EXITED），holdingStatusName 为应用层映射的中文展示名——
// 前端条件渲染与摘要口径依赖状态码，中文名仅用于展示，避免按中文文案匹配产生脆弱耦合
public record StationShareholdingListItemResponse(
        Long id,
        Long shareholderId,
        String shareholderName,
        String contactName,
        String contactPhone,
        BigDecimal ownershipPercentage,
        String holdingStatus,
        String holdingStatusName,
        LocalDateTime effectiveAt
) {
}