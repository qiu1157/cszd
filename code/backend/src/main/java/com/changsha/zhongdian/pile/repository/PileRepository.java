package com.changsha.zhongdian.pile.repository;

import java.util.List;
import java.util.Optional;

import com.changsha.zhongdian.pile.entity.Pile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PileRepository extends JpaRepository<Pile, Long> {

    Optional<Pile> findTopByPileCodeStartingWithOrderByPileCodeDesc(String pileCodePrefix);

    long countByChargeType(com.changsha.zhongdian.pile.entity.ChargeType chargeType);

    long countByPileStatusItemId(Long pileStatusItemId);

    @Query(value = """
            SELECT
                p.id AS id,
                p.pile_code AS pileCode,
                p.charge_type AS chargeType,
                p.gun_count AS gunCount,
                status.item_name AS pileStatusName,
                p.updated_at AS updatedAt
            FROM biz_pile p
            LEFT JOIN sys_dict_item status ON p.pile_status_item_id = status.id
            WHERE p.station_id = :stationId
            ORDER BY p.updated_at DESC, p.pile_code DESC
            """, nativeQuery = true)
    List<PileListItemProjection> findAllListItemsByStationIdOrderByUpdatedAtDesc(Long stationId);
}
