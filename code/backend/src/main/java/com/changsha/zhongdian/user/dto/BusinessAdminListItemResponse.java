package com.changsha.zhongdian.user.dto;

import java.time.LocalDateTime;

public record BusinessAdminListItemResponse(
        String account,
        String name,
        String role,
        String status,
        LocalDateTime lastLoginAt
) {
}
