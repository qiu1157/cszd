package com.changsha.zhongdian;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        // 为什么：第一期骨架阶段，单元测试不应强依赖真实数据库可用性（避免本地没连上 DB 就导致 CI/构建失败）。
        // 后续里程碑引入 Testcontainers 或嵌入式 DB 后，可移除此排除项，恢复真实启动链路测试。
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
class ChargingStationSystemApplicationTests {

    @Test
    void contextLoads() {
        // 最小占位：保证工程可启动、Spring 上下文可加载
    }
}
