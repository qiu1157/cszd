package com.changsha.zhongdian.operationlog.service;

import com.changsha.zhongdian.operationlog.entity.OperationLog;
import com.changsha.zhongdian.operationlog.entity.OperationTargetType;
import com.changsha.zhongdian.operationlog.entity.OperationType;
import com.changsha.zhongdian.operationlog.repository.OperationLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperationLogService {

    private final OperationLogRepository operationLogRepository;
    private final ObjectMapper objectMapper;

    public OperationLogService(OperationLogRepository operationLogRepository, ObjectMapper objectMapper) {
        this.operationLogRepository = operationLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void log(
            String operatorAccount,
            String operatorRole,
            OperationTargetType targetType,
            String targetId,
            String targetName,
            OperationType operationType,
            Object beforeState,
            Object afterState,
            String remark,
            Long stationId
    ) {
        OperationLog log = new OperationLog();
        log.setOperatorAccount(operatorAccount);
        log.setOperatorRole(operatorRole);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setTargetName(targetName);
        log.setOperationType(operationType);
        log.setBeforeContent(serializeToJson(beforeState));
        log.setAfterContent(serializeToJson(afterState));
        log.setRemark(remark);
        log.setStationId(stationId);

        operationLogRepository.save(log);
    }

    private String serializeToJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.toString();
        }
    }
}
