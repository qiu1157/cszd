package com.changsha.zhongdian.station.repository;

public interface StationDetailProjection {

    Long getId();

    String getStationCode();

    String getStationName();

    Long getPlatformItemId();

    String getPlatformName();

    String getPlatformStationCode();

    String getAddress();

    String getContactName();

    String getContactPhone();

    String getMeterAccountNumber();

    Long getStationStatusItemId();

    String getStationStatusName();
}
