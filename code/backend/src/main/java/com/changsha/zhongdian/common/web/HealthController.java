package com.changsha.zhongdian.common.web;

import java.time.OffsetDateTime;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    /**
     * 为什么：提供最小可用的服务健康检查，便于部署探活与联调确认后端可用。
     */
    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "ok",
                "time", OffsetDateTime.now().toString()
        );
    }
}

