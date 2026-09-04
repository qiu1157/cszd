package com.changsha.zhongdian.shareholding.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 为什么用接口投影：站点状态名称需要联字典项翻译，与站点列表查询同一口径，
// 用一条原生 SQL 取出全部展示字段，避免逐行回表与 N+1 查询
public interface ShareholderStationListItemProjection {
    Long getId();

    Long getStationId();

    String getStationCode();

    String getStationName();

    String getStationStatusName();

    BigDecimal getOwnershipPercentage();

    LocalDateTime getEffectiveAt();
}