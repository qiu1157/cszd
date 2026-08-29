package com.changsha.zhongdian.station.repository;

import java.util.Optional;

import com.changsha.zhongdian.station.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationRepository extends JpaRepository<Station, Long> {

    Optional<Station> findTopByStationCodeStartingWithOrderByStationCodeDesc(String stationCodePrefix);
}
