package com.changsha.zhongdian.shareholding.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// native SQL 投影：持股行联股东主档一次取回，避免行级查股东造成 N+1
public interface StationShareholderListItemProjection {
    Long getId();
    Long getShareholderId();
    String getShareholderName();
    String getContactName();
    String getContactPhone();
    BigDecimal getOwnershipPercentage();
    String getHoldingStatus();
    LocalDateTime getEffectiveAt();
}