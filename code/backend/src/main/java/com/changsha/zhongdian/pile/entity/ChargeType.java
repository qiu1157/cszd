package com.changsha.zhongdian.pile.entity;

/**
 * 充电类型。为什么用枚举而不是字典：spec 第一期仅定义平台/充电站状态/充电桩状态三类字典，
 * 快充/慢充是稳定的业务二值口径，由应用层校验即可，避免过度设计。
 */
public enum ChargeType {
    FAST,
    SLOW
}
