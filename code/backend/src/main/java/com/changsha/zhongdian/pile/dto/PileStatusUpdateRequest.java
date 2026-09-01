package com.changsha.zhongdian.pile.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PileStatusUpdateRequest(
        @NotNull(message = "请选择新状态")
        Long pileStatusItemId,
        @Size(max = 500, message = "状态变更备注最多500字符")
        String remark
) {
}
