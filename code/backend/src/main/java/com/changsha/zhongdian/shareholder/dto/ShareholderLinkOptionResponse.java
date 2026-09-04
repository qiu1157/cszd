package com.changsha.zhongdian.shareholder.dto;

// 关联股东场景的轻量选项：选择器只需要身份与联系方式字段，
// 不携带状态与站点统计，避免把列表行 DTO 复用进选择场景
public record ShareholderLinkOptionResponse(
        Long id,
        String shareholderName,
        String contactName,
        String contactPhone
) {
}