package com.changsha.zhongdian.operationlog.repository;

import com.changsha.zhongdian.operationlog.entity.OperationLog;
import com.changsha.zhongdian.operationlog.entity.OperationTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long>, JpaSpecificationExecutor<OperationLog> {
    
    List<OperationLog> findByStationIdOrderByCreatedAtDesc(Long stationId);
    
    @Query("SELECT l FROM OperationLog l WHERE " +
           "(l.targetType = com.changsha.zhongdian.operationlog.entity.OperationTargetType.SHAREHOLDER AND CAST(l.targetId AS long) = :shareholderId) OR " +
           "(l.targetType = com.changsha.zhongdian.operationlog.entity.OperationTargetType.SHAREHOLDING AND CAST(l.targetId AS long) IN " +
           "(SELECT sh.id FROM Shareholding sh WHERE sh.shareholderId = :shareholderId)) " +
           "ORDER BY l.createdAt DESC")
    List<OperationLog> findShareholderTimelineEvents(@Param("shareholderId") Long shareholderId);
    
    @Query("SELECT l FROM OperationLog l WHERE l.stationId = :stationId AND " +
           "((l.targetType = com.changsha.zhongdian.operationlog.entity.OperationTargetType.SHAREHOLDER AND CAST(l.targetId AS long) = :shareholderId) OR " +
           "(l.targetType = com.changsha.zhongdian.operationlog.entity.OperationTargetType.SHAREHOLDING AND CAST(l.targetId AS long) IN " +
           "(SELECT sh.id FROM Shareholding sh WHERE sh.shareholderId = :shareholderId AND sh.stationId = :stationId))) " +
           "ORDER BY l.createdAt DESC")
    List<OperationLog> findShareholderTimelineEventsByStation(
        @Param("shareholderId") Long shareholderId, 
        @Param("stationId") Long stationId
    );
}

