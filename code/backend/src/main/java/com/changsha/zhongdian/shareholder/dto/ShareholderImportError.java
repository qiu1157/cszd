package com.changsha.zhongdian.shareholder.dto;

/**
 * 股东批量导入的单行校验错误。
 * 为什么单独建模：导入失败需要把「行号 + 原因」逐行回传给前端展示，
 * 用记录类型保持响应结构稳定、字段语义清晰。
 */
public record ShareholderImportError(
        int rowNumber,
        String message
) {
}
