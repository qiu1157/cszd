package com.changsha.zhongdian.operationlog.dto;

import java.time.LocalDateTime;

public class TimelineEventDTO {
    
    private Long id;
    private LocalDateTime operatedAt;
    private String operatorAccount;
    private String operatorRole;
    private String targetType;
    private String targetId;
    private String targetName;
    private String operationType;
    private String remark;
    private String beforeState;
    private String afterState;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDateTime getOperatedAt() {
        return operatedAt;
    }
    
    public void setOperatedAt(LocalDateTime operatedAt) {
        this.operatedAt = operatedAt;
    }
    
    public String getOperatorAccount() {
        return operatorAccount;
    }
    
    public void setOperatorAccount(String operatorAccount) {
        this.operatorAccount = operatorAccount;
    }
    
    public String getOperatorRole() {
        return operatorRole;
    }
    
    public void setOperatorRole(String operatorRole) {
        this.operatorRole = operatorRole;
    }
    
    public String getTargetType() {
        return targetType;
    }
    
    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }
    
    public String getTargetId() {
        return targetId;
    }
    
    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
    
    public String getTargetName() {
        return targetName;
    }
    
    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }
    
    public String getOperationType() {
        return operationType;
    }
    
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark;
    }
    
    public String getBeforeState() {
        return beforeState;
    }
    
    public void setBeforeState(String beforeState) {
        this.beforeState = beforeState;
    }
    
    public String getAfterState() {
        return afterState;
    }
    
    public void setAfterState(String afterState) {
        this.afterState = afterState;
    }
}
