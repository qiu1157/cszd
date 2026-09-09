package com.changsha.zhongdian.diagnostic;

import com.changsha.zhongdian.operationlog.dto.TimelineEventDTO;
import com.changsha.zhongdian.operationlog.entity.OperationLog;
import com.changsha.zhongdian.operationlog.repository.OperationLogRepository;
import com.changsha.zhongdian.operationlog.service.OperationLogQueryService;
import com.changsha.zhongdian.station.entity.Station;
import com.changsha.zhongdian.station.repository.StationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diagnostic")
public class DiagnosticController {

    private final StationRepository stationRepository;
    private final OperationLogRepository operationLogRepository;
    private final OperationLogQueryService operationLogQueryService;

    public DiagnosticController(
            StationRepository stationRepository,
            OperationLogRepository operationLogRepository,
            OperationLogQueryService operationLogQueryService
    ) {
        this.stationRepository = stationRepository;
        this.operationLogRepository = operationLogRepository;
        this.operationLogQueryService = operationLogQueryService;
    }

    @GetMapping("/station/ST-202608-0001")
    public Map<String, Object> diagnoseStation() {
        Map<String, Object> result = new HashMap<>();
        
        List<Station> allStations = stationRepository.findAll();
        Station station = allStations.stream()
            .filter(s -> "ST-202608-0001".equals(s.getStationCode()))
            .findFirst()
            .orElse(null);
            
        if (station == null) {
            result.put("error", "充电站 ST-202608-0001 不存在");
            result.put("totalStationsInDb", allStations.size());
            return result;
        }
        
        result.put("station", Map.of(
            "id", station.getId(),
            "stationCode", station.getStationCode(),
            "stationName", station.getStationName(),
            "address", station.getAddress() != null ? station.getAddress() : "null",
            "updatedAt", station.getUpdatedAt()
        ));
        
        List<OperationLog> logsByStationId = operationLogRepository
            .findByStationIdOrderByCreatedAtDesc(station.getId());
        result.put("logsByStationId", logsByStationId);
        result.put("logsByStationIdCount", logsByStationId.size());
        
        List<OperationLog> allLogs = operationLogRepository.findAll();
        result.put("totalLogsInDatabase", allLogs.size());
        
        if (!allLogs.isEmpty()) {
            OperationLog latestLog = allLogs.stream()
                .max((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .orElse(null);
            if (latestLog != null) {
                result.put("latestLog", Map.of(
                    "id", latestLog.getId(),
                    "operatorAccount", latestLog.getOperatorAccount(),
                    "targetType", latestLog.getTargetType(),
                    "targetId", latestLog.getTargetId(),
                    "operationType", latestLog.getOperationType(),
                    "createdAt", latestLog.getCreatedAt()
                ));
            }
        }
        
        return result;
    }
    
    @GetMapping("/station/ST-202608-0001/timeline")
    public Map<String, Object> diagnoseStationTimeline() {
        Map<String, Object> result = new HashMap<>();
        
        List<Station> allStations = stationRepository.findAll();
        Station station = allStations.stream()
            .filter(s -> "ST-202608-0001".equals(s.getStationCode()))
            .findFirst()
            .orElse(null);
            
        if (station == null) {
            result.put("error", "充电站 ST-202608-0001 不存在");
            return result;
        }
        
        result.put("stationId", station.getId());
        result.put("stationCode", station.getStationCode());
        
        List<TimelineEventDTO> timeline = operationLogQueryService.getStationTimeline(station.getId());
        result.put("timeline", timeline);
        result.put("timelineCount", timeline.size());
        
        return result;
    }
}
