package com.changsha.zhongdian.auth.model;

import java.time.LocalDateTime;

public record LoginUser(
        String account,
        String name,
        String phone,
        String role,
        String status,
        LocalDateTime lastLoginAt
) {
}
