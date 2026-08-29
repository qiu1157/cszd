package com.changsha.zhongdian.dictionary.dto;

import jakarta.validation.constraints.NotBlank;

public record SystemDictItemUpsertRequest(
        @NotBlank(message = "请输入编码")
        String itemCode,
        @NotBlank(message = "请输入名称")
        String itemName,
        @NotBlank(message = "请选择状态")
        String status,
        Integer sortOrder
) {
}
