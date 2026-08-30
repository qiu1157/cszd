package com.changsha.zhongdian.station.repository;

import java.util.List;
import java.util.Optional;

import com.changsha.zhongdian.station.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StationRepository extends JpaRepository<Station, Long> {

    Optional<Station> findTopByStationCodeStartingWithOrderByStationCodeDesc(String stationCodePrefix);

    boolean existsByPlatformItemIdAndPlatformStationCode(Long platformItemId, String platformStationCode);

    boolean existsByPlatformItemIdAndPlatformStationCodeAndIdNot(
            Long platformItemId,
            String platformStationCode,
            Long id
    );

    @Query(value = """
            SELECT
                s.id AS id,
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

    @Query(value = """
            SELECT
                s.id AS id,
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
            WHERE s.station_name LIKE CONCAT('%', :stationName, '%')
            ORDER BY s.updated_at DESC, s.station_code DESC
            """, nativeQuery = true)
    List<StationListItemProjection> findAllListItemsByStationNameContainingOrderByUpdatedAtDesc(String stationName);

    @Query(value = """
            SELECT
                s.id AS id,
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
            WHERE s.platform_item_id = :platformItemId
            ORDER BY s.updated_at DESC, s.station_code DESC
            """, nativeQuery = true)
    List<StationListItemProjection> findAllListItemsByPlatformItemIdOrderByUpdatedAtDesc(Long platformItemId);

    @Query(value = """
            SELECT
                s.id AS id,
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
            WHERE s.station_status_item_id = :stationStatusItemId
            ORDER BY s.updated_at DESC, s.station_code DESC
            """, nativeQuery = true)
    List<StationListItemProjection> findAllListItemsByStationStatusItemIdOrderByUpdatedAtDesc(Long stationStatusItemId);

    @Query(value = """
            SELECT
                s.id AS id,
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
            WHERE s.station_name LIKE CONCAT('%', :stationName, '%')
              AND s.platform_item_id = :platformItemId
            ORDER BY s.updated_at DESC, s.station_code DESC
            """, nativeQuery = true)
    List<StationListItemProjection> findAllListItemsByStationNameContainingAndPlatformItemIdOrderByUpdatedAtDesc(
            String stationName,
            Long platformItemId
    );

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
            WHERE (:stationName IS NULL OR s.station_name LIKE CONCAT('%', :stationName, '%'))
              AND (:platformItemId IS NULL OR s.platform_item_id = :platformItemId)
              AND (:stationStatusItemId IS NULL OR s.station_status_item_id = :stationStatusItemId)
            ORDER BY s.updated_at DESC, s.station_code DESC
            """, nativeQuery = true)
    List<StationListItemProjection> findAllListItemsByFiltersOrderByUpdatedAtDesc(
            String stationName,
            Long platformItemId,
            Long stationStatusItemId
    );

    @Query(value = """
            SELECT
                s.id AS id,
                s.station_code AS stationCode,
                s.station_name AS stationName,
                s.platform_item_id AS platformItemId,
                platform.item_name AS platformName,
                s.platform_station_code AS platformStationCode,
                s.address AS address,
                s.contact_name AS contactName,
                s.contact_phone AS contactPhone,
                s.station_status_item_id AS stationStatusItemId,
                status.item_name AS stationStatusName
            FROM biz_station s
            LEFT JOIN sys_dict_item platform ON s.platform_item_id = platform.id
            LEFT JOIN sys_dict_item status ON s.station_status_item_id = status.id
            WHERE s.id = :stationId
            """, nativeQuery = true)
    Optional<StationDetailProjection> findDetailById(Long stationId);
}
