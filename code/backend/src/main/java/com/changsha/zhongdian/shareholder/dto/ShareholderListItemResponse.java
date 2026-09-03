package com.changsha.zhongdian.shareholder.dto;

import java.time.LocalDateTime;

public record ShareholderListItemResponse(
        Long id,
        String shareholderName,
        String contactName,
        String contactPhone,
        String shareholderStatus,
        LocalDateTime updatedAt
) {
}