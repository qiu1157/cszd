package com.changsha.zhongdian.pile.repository;

import java.time.LocalDateTime;

public interface PileListItemProjection {

    Long getId();

    String getPileCode();

    String getChargeType();

    Integer getGunCount();

    String getPileStatusName();

    LocalDateTime getUpdatedAt();
}
