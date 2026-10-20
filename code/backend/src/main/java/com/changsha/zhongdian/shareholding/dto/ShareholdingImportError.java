package com.changsha.zhongdian.shareholding.dto;

/**
 * 股东关系批量导入的单行校验错误。
 * 为什么单独建模：导入失败需要把「行号 + 原因」逐行回传给前端展示。
 */
public record ShareholdingImportError(
        int rowNumber,
        String message
) {
}
