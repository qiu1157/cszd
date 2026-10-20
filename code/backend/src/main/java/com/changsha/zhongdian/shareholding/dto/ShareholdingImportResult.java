package com.changsha.zhongdian.shareholding.dto;

import java.util.List;

/**
 * 股东关系批量导入结果。
 * 为什么不用异常表达「部分/全部校验失败」：导入校验失败是业务结果而非系统错误，
 * HTTP 层保持 200，由前端按 errors 是否为空决定提示。
 */
public record ShareholdingImportResult(
        int totalCount,
        int successCount,
        List<ShareholdingImportError> errors
) {
}
