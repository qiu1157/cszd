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

    // 站点详情股东分区列表：持股行联股东主档一次取回名称与联系方式，避免行级查股东造成 N+1；
    // 排序先按状态（ACTIVE 字母序在前）再按 id 倒序，持股中关系优先展示、新关联在前
    @Query(value = """
            SELECT
                sh.id AS id,
                sh.shareholder_id AS shareholderId,
                h.shareholder_name AS shareholderName,
                h.contact_name AS contactName,
                h.contact_phone AS contactPhone,
                sh.ownership_percentage AS ownershipPercentage,
                sh.holding_status AS holdingStatus,
                sh.effective_at AS effectiveAt
            FROM biz_shareholding sh
            INNER JOIN biz_shareholder h ON sh.shareholder_id = h.id
            WHERE sh.station_id = :stationId
            ORDER BY sh.holding_status ASC, sh.id DESC
            """, nativeQuery = true)
    List<StationShareholderListItemProjection> findStationShareholderListItems(@Param("stationId") Long stationId);

    boolean existsByStationIdAndShareholderIdAndHoldingStatus(
            Long stationId, Long shareholderId, HoldingStatus holdingStatus);

    // 退股同步调整占比：需要取出同站点下除当前关系外的其他持股中关系
    List<Shareholding> findByStationIdAndHoldingStatus(Long stationId, HoldingStatus holdingStatus);
}