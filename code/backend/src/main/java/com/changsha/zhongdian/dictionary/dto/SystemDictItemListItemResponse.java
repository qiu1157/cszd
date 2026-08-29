package com.changsha.zhongdian.dictionary.dto;

import java.time.LocalDateTime;

public record SystemDictItemListItemResponse(
        Long id,
        String itemCode,
        String itemName,
        String status,
        Integer sortOrder,
        LocalDateTime updatedAt
) {
}
