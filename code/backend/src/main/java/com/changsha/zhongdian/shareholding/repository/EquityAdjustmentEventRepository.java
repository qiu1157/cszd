package com.changsha.zhongdian.shareholding.repository;

import java.util.List;

import com.changsha.zhongdian.shareholding.entity.EquityAdjustmentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquityAdjustmentEventRepository extends JpaRepository<EquityAdjustmentEvent, Long> {

    // 按时间倒序查询某充电站的股权调整事件，供后续时间线展示使用
    List<EquityAdjustmentEvent> findByStationIdOrderByCreatedAtDesc(Long stationId);
}
