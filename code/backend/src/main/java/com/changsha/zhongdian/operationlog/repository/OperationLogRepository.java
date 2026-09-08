package com.changsha.zhongdian.operationlog.repository;

import com.changsha.zhongdian.operationlog.entity.OperationLog;
import com.changsha.zhongdian.operationlog.entity.OperationTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
    
    List<OperationLog> findByStationIdOrderByCreatedAtDesc(Long stationId);
    
    @Query("SELECT l FROM OperationLog l WHERE " +
           "(l.targetType = 'SHAREHOLDER' AND l.targetId = :shareholderId) OR " +
           "(l.targetType = 'SHAREHOLDING' AND l.targetId IN " +
           "(SELECT CAST(sh.id AS string) FROM Shareholding sh WHERE sh.shareholder.id = :shareholderId)) " +
           "ORDER BY l.createdAt DESC")
    List<OperationLog> findShareholderTimelineEvents(@Param("shareholderId") Long shareholderId);
    
    @Query("SELECT l FROM OperationLog l WHERE l.stationId = :stationId AND " +
           "((l.targetType = 'SHAREHOLDER' AND l.targetId = :shareholderId) OR " +
           "(l.targetType = 'SHAREHOLDING' AND l.targetId IN " +
           "(SELECT CAST(sh.id AS string) FROM Shareholding sh WHERE sh.shareholder.id = :shareholderId AND sh.station.id = :stationId))) " +
           "ORDER BY l.createdAt DESC")
    List<OperationLog> findShareholderTimelineEventsByStation(
        @Param("shareholderId") Long shareholderId, 
        @Param("stationId") Long stationId
    );
}

