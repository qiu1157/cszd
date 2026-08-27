package com.changsha.zhongdian.user.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetBusinessAdminPasswordRequest(
        @NotBlank(message = "请输入新密码")
        String newPassword
) {
}
