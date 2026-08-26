package com.changsha.zhongdian.auth.model;

public record LoginUser(
        String account,
        String name,
        String role
) {
}
