package com.changsha.zhongdian.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "请输入旧密码")
        String currentPassword,
        @NotBlank(message = "请输入新密码")
        String newPassword,
        @NotBlank(message = "请输入确认新密码")
        String confirmPassword
) {
}
