package com.changsha.zhongdian.shareholder.dto;

import jakarta.validation.constraints.NotBlank;

public record ShareholderCreateRequest(
        @NotBlank(message = "请输入股东名称")
        String shareholderName,
        String contactName,
        String contactPhone,
        @NotBlank(message = "请选择股东状态")
        String shareholderStatus
) {
}