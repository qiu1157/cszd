package com.changsha.zhongdian.shareholding.repository;

import java.util.List;

import com.changsha.zhongdian.shareholding.entity.HoldingStatus;
import com.changsha.zhongdian.shareholding.entity.Shareholding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShareholdingRepository extends JpaRepository<Shareholding, Long> {

    // 为什么原生 SQL：持股行要联站点与字典项翻译状态名，与 StationRepository 列表查询同一口径；
    // holding_status 以字符串入参传递（枚举 name），避免原生查询直接绑定枚举的序列化歧义
    @Query(value = """
            SELECT
                sh.id AS id,
                s.id AS stationId,
                s.station_code AS stationCode,
                s.station_name AS stationName,
                status.item_name AS stationStatusName,
                sh.ownership_percentage AS ownershipPercentage,
                sh.effective_at AS effectiveAt
            FROM biz_shareholding sh
            INNER JOIN biz_station s ON sh.station_id = s.id
            LEFT JOIN sys_dict_item status ON s.station_status_item_id = status.id
            WHERE sh.shareholder_id = :shareholderId
              AND sh.holding_status = :holdingStatus
            ORDER BY s.station_code ASC, sh.id ASC
            """, nativeQuery = true)
    List<ShareholderStationListItemProjection> findStationListItemsByShareholderIdAndHoldingStatus(
            @Param("shareholderId") Long shareholderId,
            @Param("holdingStatus") String holdingStatus
    );

    // 为什么 JPQL 而非原生 SQL：纯单实体聚合、无跨表 JOIN，JPQL 可直接绑定 HoldingStatus 枚举入参；
    // GROUP BY 一次取出全部股东的统计，由调用方内存合并，避免列表行数级的逐股东 COUNT（N+1）
    @Query("""
            SELECT sh.shareholderId AS shareholderId, COUNT(sh.id) AS stationCount
            FROM Shareholding sh
            WHERE sh.holdingStatus = :holdingStatus
            GROUP BY sh.shareholderId
            """)
    List<ShareholderStationCountProjection> countStationsGroupByShareholder(
            @Param("holdingStatus") HoldingStatus holdingStatus
    );
}