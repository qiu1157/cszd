package com.changsha.zhongdian.shareholder.dto;

import java.time.LocalDateTime;

public record ShareholderDetailResponse(
        Long id,
        String shareholderName,
        String contactName,
        String contactPhone,
        String shareholderStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}