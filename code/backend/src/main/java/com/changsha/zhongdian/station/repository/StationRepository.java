package com.changsha.zhongdian.station.repository;

import java.util.List;
import java.util.Optional;

import com.changsha.zhongdian.station.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StationRepository extends JpaRepository<Station, Long> {

    Optional<Station> findTopByStationCodeStartingWithOrderByStationCodeDesc(String stationCodePrefix);

    boolean existsByPlatformItemIdAndPlatformStationCode(Long platformItemId, String platformStationCode);

    @Query(value = """
            SELECT
                s.station_code AS stationCode,
                s.station_name AS stationName,
                platform.item_name AS platformName,
                s.platform_station_code AS platformStationCode,
                s.contact_name AS contactName,
                s.contact_phone AS contactPhone,
                status.item_name AS stationStatusName,
                s.updated_at AS updatedAt
            FROM biz_station s
            LEFT JOIN sys_dict_item platform ON s.platform_item_id = platform.id
            LEFT JOIN sys_dict_item status ON s.station_status_item_id = status.id
            ORDER BY s.updated_at DESC, s.station_code DESC
            """, nativeQuery = true)
    List<StationListItemProjection> findAllListItemsOrderByUpdatedAtDesc();
}
