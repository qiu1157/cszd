package com.changsha.zhongdian.operationlog.service;

import com.changsha.zhongdian.operationlog.dto.OperationLogPageDTO;
import com.changsha.zhongdian.operationlog.dto.OperationLogQueryParam;
import com.changsha.zhongdian.operationlog.dto.TimelineEventDTO;
import com.changsha.zhongdian.operationlog.entity.OperationLog;
import com.changsha.zhongdian.operationlog.entity.OperationTargetType;
import com.changsha.zhongdian.operationlog.entity.OperationType;
import com.changsha.zhongdian.operationlog.repository.OperationLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationLogQueryServiceTest {

    @Mock
    private OperationLogRepository operationLogRepository;

    private OperationLogQueryService operationLogQueryService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        operationLogQueryService = new OperationLogQueryService(operationLogRepository, objectMapper);
    }

    @Test
    void queryLogs_应返回分页结果() {
        OperationLog log1 = createOperationLog(1L, "站点A", OperationTargetType.STATION, "1", OperationType.CREATE);
        OperationLog log2 = createOperationLog(1L, "站点B", OperationTargetType.STATION, "2", OperationType.UPDATE);
        
        Page<OperationLog> page = new PageImpl<>(Arrays.asList(log1, log2), 
            org.springframework.data.domain.PageRequest.of(0, 20), 2);
        
        when(operationLogRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(page);

        OperationLogQueryParam param = new OperationLogQueryParam();
        param.setPage(1);
        param.setSize(20);
        
        OperationLogPageDTO result = operationLogQueryService.queryLogs(param);

        assertThat(result.getRecords()).hasSize(2);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(20);
    }

    @Test
    void queryLogs_应支持时间范围筛选() {
        OperationLog log = createOperationLog(1L, "站点A", OperationTargetType.STATION, "1", OperationType.CREATE);
        Page<OperationLog> page = new PageImpl<>(Arrays.asList(log));
        
        when(operationLogRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(page);

        OperationLogQueryParam param = new OperationLogQueryParam();
        param.setStartTime(LocalDateTime.now().minusDays(7));
        param.setEndTime(LocalDateTime.now());
        param.setPage(1);
        param.setSize(20);
        
        OperationLogPageDTO result = operationLogQueryService.queryLogs(param);

        assertThat(result.getRecords()).hasSize(1);
    }

    @Test
    void queryLogs_应支持操作人筛选() {
        OperationLog log = createOperationLog(1L, "站点A", OperationTargetType.STATION, "1", OperationType.CREATE);
        Page<OperationLog> page = new PageImpl<>(Arrays.asList(log));
        
        when(operationLogRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(page);

        OperationLogQueryParam param = new OperationLogQueryParam();
        param.setOperatorAccount("admin");
        param.setPage(1);
        param.setSize(20);
        
        OperationLogPageDTO result = operationLogQueryService.queryLogs(param);

        assertThat(result.getRecords()).hasSize(1);
    }

    @Test
    void queryLogs_应支持对象类型筛选() {
        OperationLog log = createOperationLog(1L, "站点A", OperationTargetType.STATION, "1", OperationType.CREATE);
        Page<OperationLog> page = new PageImpl<>(Arrays.asList(log));
        
        when(operationLogRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(page);

        OperationLogQueryParam param = new OperationLogQueryParam();
        param.setTargetType("STATION");
        param.setPage(1);
        param.setSize(20);
        
        OperationLogPageDTO result = operationLogQueryService.queryLogs(param);

        assertThat(result.getRecords()).hasSize(1);
    }

    @Test
    void queryLogs_应支持关键词搜索() {
        OperationLog log = createOperationLog(1L, "站点A", OperationTargetType.STATION, "1", OperationType.CREATE);
        Page<OperationLog> page = new PageImpl<>(Arrays.asList(log));
        
        when(operationLogRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(page);

        OperationLogQueryParam param = new OperationLogQueryParam();
        param.setKeyword("站点");
        param.setPage(1);
        param.setSize(20);
        
        OperationLogPageDTO result = operationLogQueryService.queryLogs(param);

        assertThat(result.getRecords()).hasSize(1);
    }

    @Test
    void getLogDetail_应返回日志详情() {
        OperationLog log = createOperationLog(1L, "站点A", OperationTargetType.STATION, "1", OperationType.CREATE);
        log.setBeforeContent("{\"name\":\"旧名称\"}");
        log.setAfterContent("{\"name\":\"新名称\"}");
        
        when(operationLogRepository.findById(1L)).thenReturn(Optional.of(log));

        TimelineEventDTO result = operationLogQueryService.getLogDetail(1L);

        assertThat(result).isNotNull();
        assertThat(result.getTargetName()).isEqualTo("站点A");
        assertThat(result.getBeforeState()).isEqualTo("{\"name\":\"旧名称\"}");
        assertThat(result.getAfterState()).isEqualTo("{\"name\":\"新名称\"}");
    }

    @Test
    void getLogDetail_日志不存在时应返回null() {
        when(operationLogRepository.findById(999L)).thenReturn(Optional.empty());

        TimelineEventDTO result = operationLogQueryService.getLogDetail(999L);

        assertThat(result).isNull();
    }

    @Test
    void getStationTimeline_应返回充电站时间线() {
        OperationLog log1 = createOperationLog(1L, "站点A", OperationTargetType.STATION, "1", OperationType.CREATE);
        OperationLog log2 = createOperationLog(1L, "站点A", OperationTargetType.STATION, "1", OperationType.UPDATE);
        
        when(operationLogRepository.findByStationIdOrderByCreatedAtDesc(1L))
            .thenReturn(Arrays.asList(log1, log2));

        List<TimelineEventDTO> results = operationLogQueryService.getStationTimeline(1L);

        assertThat(results).hasSize(2);
    }

    @Test
    void getShareholderTimeline_应返回股东时间线() {
        OperationLog log1 = createOperationLog(1L, "股东张三", OperationTargetType.SHAREHOLDER, "1", OperationType.CREATE);
        OperationLog log2 = createOperationLog(1L, "持股关系", OperationTargetType.SHAREHOLDING, "10", OperationType.CREATE);
        
        when(operationLogRepository.findShareholderTimelineEvents(1L))
            .thenReturn(Arrays.asList(log1, log2));

        List<TimelineEventDTO> results = operationLogQueryService.getShareholderTimeline(1L);

        assertThat(results).hasSize(2);
    }

    @Test
    void getShareholderTimelineByStation_应返回股东在指定站点的时间线() {
        OperationLog log1 = createOperationLog(1L, "股东张三", OperationTargetType.SHAREHOLDER, "1", OperationType.CREATE);
        
        when(operationLogRepository.findShareholderTimelineEventsByStation(1L, 1L))
            .thenReturn(Arrays.asList(log1));

        List<TimelineEventDTO> results = operationLogQueryService.getShareholderTimelineByStation(1L, 1L);

        assertThat(results).hasSize(1);
    }

    private OperationLog createOperationLog(Long stationId, String targetName, 
                                           OperationTargetType targetType, String targetId, 
                                           OperationType operationType) {
        OperationLog log = new OperationLog();
        log.setStationId(stationId);
        log.setOperatorAccount("admin");
        log.setOperatorRole("系统管理员");
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setTargetName(targetName);
        log.setOperationType(operationType);
        log.setRemark("测试日志");
        return log;
    }
}
