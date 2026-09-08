package com.changsha.zhongdian.operationlog.entity;

/**
 * 操作日志对象类型。
 * 为什么集中管理：spec 9.3 要求日志覆盖站点、桩、股东、持股关系、平台、账号等多类对象，
 * 统一枚举避免各模块重复定义，并保证全局日志页按对象类型筛选时口径一致。
 */
public enum OperationTargetType {
    STATION,
    PILE,
    SHAREHOLDER,
    SHAREHOLDING,
    SYSTEM_DICT,
    USER
}
