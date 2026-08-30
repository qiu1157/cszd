package com.changsha.zhongdian.station.repository;

import java.time.LocalDateTime;

public interface StationListItemProjection {

    Long getId();

    String getStationCode();

    String getStationName();

    String getPlatformName();

    String getPlatformStationCode();

    String getContactName();

    String getContactPhone();

    String getStationStatusName();

    LocalDateTime getUpdatedAt();
}
