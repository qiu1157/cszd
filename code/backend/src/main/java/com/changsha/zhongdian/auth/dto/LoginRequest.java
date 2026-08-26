package com.changsha.zhongdian.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "请输入账号或手机号")
        String account,
        @NotBlank(message = "请输入密码")
        String password
) {
}
