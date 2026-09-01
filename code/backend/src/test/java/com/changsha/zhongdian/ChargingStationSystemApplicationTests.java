package com.changsha.zhongdian;

import org.junit.jupiter.api.Test;

// 为什么：继承无数据库的 Web 测试基类，保证工程在排除 JPA 的上下文下仍可启动
class ChargingStationSystemApplicationTests extends AuthenticatedWebTestSupport {

    @Test
    void contextLoads() {
        // 最小占位：保证工程可启动、Spring 上下文可加载
    }
}
