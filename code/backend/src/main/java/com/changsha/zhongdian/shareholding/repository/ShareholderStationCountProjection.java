package com.changsha.zhongdian.shareholding.repository;

// 为什么用接口投影：列表页只需"股东ID→持股中站点数"两个值，
// 与详情页站点列表投影同口径，避免为聚合查询引入实体加载
public interface ShareholderStationCountProjection {
    Long getShareholderId();
    Long getStationCount();
}