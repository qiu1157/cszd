package com.changsha.zhongdian.shareholder.dto;

import java.util.List;

/**
 * 股东批量导入结果。
 * 为什么不用异常表达「部分/全部校验失败」：导入校验失败是业务结果而非系统错误，
 * 需要把成功数、总数与逐行错误一起返回，HTTP 层保持 200，由前端按 errors 是否为空决定提示。
 */
public record ShareholderImportResult(
        int totalCount,
        int successCount,
        List<ShareholderImportError> errors
) {
}
