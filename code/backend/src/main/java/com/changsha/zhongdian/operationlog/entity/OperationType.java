package com.changsha.zhongdian.operationlog.entity;

/**
 * 操作日志操作类型。
 * 为什么单独抽象：同一类对象（如充电站）可能涉及新增、修改、状态变更等多种动作，
 * 时间线与日志页需要按动作语义展示；状态变更类动作（STATUS_CHANGE / ENABLE / DISABLE / EXIT）
 * 在站、桩、股东主档、持股关系上复用，避免为每个对象单独建枚举值。
 */
public enum OperationType {
    CREATE,
    UPDATE,
    STATUS_CHANGE,
    ENABLE,
    DISABLE,
    RESET_PASSWORD,
    EQUITY_ADJUST,
    EXIT
}
