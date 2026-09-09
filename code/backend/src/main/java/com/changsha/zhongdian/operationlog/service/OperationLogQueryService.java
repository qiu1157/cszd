package com.changsha.zhongdian.operationlog.service;

import com.changsha.zhongdian.operationlog.dto.OperationLogPageDTO;
import com.changsha.zhongdian.operationlog.dto.OperationLogQueryParam;
import com.changsha.zhongdian.operationlog.dto.TimelineEventDTO;
import com.changsha.zhongdian.operationlog.entity.OperationLog;
import com.changsha.zhongdian.operationlog.entity.OperationTargetType;
import com.changsha.zhongdian.operationlog.repository.OperationLogRepository;
import com.changsha.zhongdian.shareholder.entity.Shareholder;
import com.changsha.zhongdian.shareholder.repository.ShareholderRepository;
import com.changsha.zhongdian.station.entity.Station;
import com.changsha.zhongdian.station.repository.StationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OperationLogQueryService {

    private final OperationLogRepository operationLogRepository;
    private final ObjectMapper objectMapper;
    private final ShareholderRepository shareholderRepository;
    private final StationRepository stationRepository;

    public OperationLogQueryService(
            OperationLogRepository operationLogRepository,
            ObjectMapper objectMapper,
            ShareholderRepository shareholderRepository,
            StationRepository stationRepository
    ) {
        this.operationLogRepository = operationLogRepository;
        this.objectMapper = objectMapper;
        this.shareholderRepository = shareholderRepository;
        this.stationRepository = stationRepository;
    }
    
    public OperationLogPageDTO queryLogs(OperationLogQueryParam param) {
        Specification<OperationLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (param.getStartTime() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), param.getStartTime()));
            }
            
            if (param.getEndTime() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), param.getEndTime()));
            }
            
            if (param.getOperatorAccount() != null && !param.getOperatorAccount().isEmpty()) {
                predicates.add(cb.equal(root.get("operatorAccount"), param.getOperatorAccount()));
            }
            
            if (param.getTargetType() != null && !param.getTargetType().isEmpty()) {
                predicates.add(cb.equal(root.get("targetType"), OperationTargetType.valueOf(param.getTargetType())));
            }
            
            if (param.getStationId() != null) {
                predicates.add(cb.equal(root.get("stationId"), param.getStationId()));
            }
            
            if (param.getKeyword() != null && !param.getKeyword().isEmpty()) {
                String keyword = "%" + param.getKeyword() + "%";
                Predicate namePredicate = cb.like(root.get("targetName"), keyword);
                Predicate remarkPredicate = cb.like(root.get("remark"), keyword);
                predicates.add(cb.or(namePredicate, remarkPredicate));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Pageable pageable = PageRequest.of(
            param.getPage() - 1,
            param.getSize(),
            Sort.by(Sort.Direction.DESC, "createdAt")
        );
        
        Page<OperationLog> page = operationLogRepository.findAll(spec, pageable);
        
        List<TimelineEventDTO> records = page.getContent()
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        return new OperationLogPageDTO(records, page.getTotalElements(), param.getPage(), param.getSize());
    }
    
    public TimelineEventDTO getLogDetail(Long id) {
        Optional<OperationLog> logOpt = operationLogRepository.findById(id);
        return logOpt.map(this::convertToDTO).orElse(null);
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
        dto.setBeforeState(enrichState(log.getBeforeContent()));
        dto.setAfterState(enrichState(log.getAfterContent()));
        return dto;
    }

    private String enrichState(String stateJson) {
        if (stateJson == null || stateJson.isEmpty()) {
            return null;
        }
        try {
            Map<String, Object> state = objectMapper.readValue(stateJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
            });
            
            // Enrich shareholderName
            if (state.containsKey("shareholderId") && !state.containsKey("shareholderName")) {
                Long shareholderId = parseLong(state.get("shareholderId"));
                if (shareholderId != null) {
                    shareholderRepository.findById(shareholderId)
                            .map(Shareholder::getShareholderName)
                            .ifPresent(name -> state.put("shareholderName", name));
                }
            }
            
            // Enrich stationName
            if (state.containsKey("stationId") && !state.containsKey("stationName")) {
                Long stationId = parseLong(state.get("stationId"));
                if (stationId != null) {
                    stationRepository.findById(stationId)
                            .map(Station::getStationName)
                            .ifPresent(name -> state.put("stationName", name));
                }
            }
            
            return objectMapper.writeValueAsString(state);
        } catch (Exception exception) {
            return stateJson;
        }
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String serializeState(String state) {
        if (state == null || state.isEmpty()) {
            return null;
        }
        return state;
    }
}
