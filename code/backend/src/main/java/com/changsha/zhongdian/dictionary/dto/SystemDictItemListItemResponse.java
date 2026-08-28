package com.changsha.zhongdian.dictionary.dto;

import java.time.LocalDateTime;

public record SystemDictItemListItemResponse(
        String itemCode,
        String itemName,
        String status,
        Integer sortOrder,
        LocalDateTime updatedAt
) {
}
