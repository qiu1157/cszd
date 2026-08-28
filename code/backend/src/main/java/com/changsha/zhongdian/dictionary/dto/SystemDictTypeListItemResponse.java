package com.changsha.zhongdian.dictionary.dto;

public record SystemDictTypeListItemResponse(
        String typeCode,
        String typeName,
        String status,
        Integer sortOrder
) {
}
