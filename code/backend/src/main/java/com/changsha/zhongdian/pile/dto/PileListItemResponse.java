package com.changsha.zhongdian.pile.dto;

import java.time.LocalDateTime;

public record PileListItemResponse(
        Long id,
        String pileCode,
        String chargeTypeName,
        Integer gunCount,
        String pileStatusName,
        LocalDateTime updatedAt
) {
}
