package com.changsha.zhongdian.user.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateBusinessAdminRequest(
        @NotBlank(message = "请输入手机号")
        String phone,
        @NotBlank(message = "请输入姓名")
        String name,
        @NotBlank(message = "请输入初始密码")
        String initialPassword
) {
}
