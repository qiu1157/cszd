package com.changsha.zhongdian.shareholder.dto;

import jakarta.validation.constraints.NotBlank;

public record ShareholderUpdateRequest(
        @NotBlank(message = "请输入股东名称")
        String shareholderName,
        String contactName,
        String contactPhone
) {
}