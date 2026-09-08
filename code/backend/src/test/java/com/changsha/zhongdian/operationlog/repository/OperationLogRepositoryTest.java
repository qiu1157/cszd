package com.changsha.zhongdian.operationlog.repository;

import com.changsha.zhongdian.operationlog.entity.OperationLog;
import com.changsha.zhongdian.operationlog.entity.OperationTargetType;
import com.changsha.zhongdian.operationlog.entity.OperationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class OperationLogRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OperationLogRepository operationLogRepository;

    @Test
    void findByStationIdOrderByCreatedAtDesc_应返回指定充电站的日志() {
        OperationLog log1 = createOperationLog(1L, 101L, "站点A", OperationTargetType.STATION, "101", OperationType.CREATE);
        OperationLog log2 = createOperationLog(1L, 102L, "站点B", OperationTargetType.STATION, "102", OperationType.UPDATE);
        OperationLog log3 = createOperationLog(2L, 103L, "站点C", OperationTargetType.STATION, "103", OperationType.CREATE);

        entityManager.persist(log1);
        entityManager.persist(log2);
        entityManager.persist(log3);
        entityManager.flush();

        List<OperationLog> results = operationLogRepository.findByStationIdOrderByCreatedAtDesc(1L);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getTargetName()).isEqualTo("站点B");
        assertThat(results.get(1).getTargetName()).isEqualTo("站点A");
    }

    @Test
    void findShareholderTimelineEvents_应返回股东相关的所有日志() {
        OperationLog shareholderLog = createOperationLog(1L, 201L, "股东张三", OperationTargetType.SHAREHOLDER, "1", OperationType.CREATE);
        OperationLog shareholdingLog = createOperationLog(1L, 301L, "持股关系", OperationTargetType.SHAREHOLDING, "10", OperationType.CREATE);
        OperationLog otherLog = createOperationLog(2L, 202L, "股东李四", OperationTargetType.SHAREHOLDER, "2", OperationType.CREATE);

        entityManager.persist(shareholderLog);
        entityManager.persist(shareholdingLog);
        entityManager.persist(otherLog);
        entityManager.flush();

        List<OperationLog> results = operationLogRepository.findShareholderTimelineEvents(1L);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(OperationLog::getTargetType)
                .containsExactlyInAnyOrder(OperationTargetType.SHAREHOLDER, OperationTargetType.SHAREHOLDING);
    }

    @Test
    void findShareholderTimelineEventsByStation_应返回股东在指定站点的日志() {
        OperationLog log1 = createOperationLog(1L, 201L, "股东张三", OperationTargetType.SHAREHOLDER, "1", OperationType.CREATE);
        OperationLog log2 = createOperationLog(1L, 301L, "持股关系", OperationTargetType.SHAREHOLDING, "10", OperationType.CREATE);
        OperationLog log3 = createOperationLog(2L, 302L, "持股关系", OperationTargetType.SHAREHOLDING, "11", OperationType.CREATE);

        entityManager.persist(log1);
        entityManager.persist(log2);
        entityManager.persist(log3);
        entityManager.flush();

        List<OperationLog> results = operationLogRepository.findShareholderTimelineEventsByStation(1L, 1L);

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(log -> log.getStationId().equals(1L));
    }

    private OperationLog createOperationLog(Long stationId, Long id, String targetName, 
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
