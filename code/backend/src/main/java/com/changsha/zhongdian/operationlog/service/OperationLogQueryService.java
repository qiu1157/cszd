package com.changsha.zhongdian.operationlog.service;

import com.changsha.zhongdian.operationlog.dto.TimelineEventDTO;
import com.changsha.zhongdian.operationlog.entity.OperationLog;
import com.changsha.zhongdian.operationlog.repository.OperationLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OperationLogQueryService {
    
    private final OperationLogRepository operationLogRepository;
    private final ObjectMapper objectMapper;
    
    public OperationLogQueryService(OperationLogRepository operationLogRepository, ObjectMapper objectMapper) {
        this.operationLogRepository = operationLogRepository;
        this.objectMapper = objectMapper;
    }
    
    public List<TimelineEventDTO> getStationTimeline(Long stationId) {
        List<OperationLog> logs = operationLogRepository.findByStationIdOrderByCreatedAtDesc(stationId);
        return logs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<TimelineEventDTO> getShareholderTimeline(Long shareholderId) {
        return operationLogRepository.findShareholderTimelineEvents(shareholderId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<TimelineEventDTO> getShareholderTimelineByStation(Long shareholderId, Long stationId) {
        return operationLogRepository.findShareholderTimelineEventsByStation(shareholderId, stationId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private TimelineEventDTO convertToDTO(OperationLog log) {
        TimelineEventDTO dto = new TimelineEventDTO();
        dto.setId(log.getId());
        dto.setOperatedAt(log.getCreatedAt());
        dto.setOperatorAccount(log.getOperatorAccount());
        dto.setOperatorRole(log.getOperatorRole());
        dto.setTargetType(log.getTargetType().name());
        dto.setTargetId(log.getTargetId());
        dto.setTargetName(log.getTargetName());
        dto.setOperationType(log.getOperationType().name());
        dto.setRemark(log.getRemark());
        dto.setBeforeState(serializeState(log.getBeforeContent()));
        dto.setAfterState(serializeState(log.getAfterContent()));
        return dto;
    }
    
    private String serializeState(String state) {
        if (state == null || state.isEmpty()) {
            return null;
        }
        return state;
    }
}
