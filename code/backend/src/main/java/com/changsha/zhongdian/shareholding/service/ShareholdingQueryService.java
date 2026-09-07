package com.changsha.zhongdian.shareholding.service;

import java.util.List;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.shareholding.dto.ShareholderStationListItemResponse;
import com.changsha.zhongdian.shareholding.dto.StationShareholdingListItemResponse;
import com.changsha.zhongdian.shareholding.entity.HoldingStatus;
import com.changsha.zhongdian.shareholding.repository.ShareholdingRepository;
import com.changsha.zhongdian.shareholding.repository.StationShareholderListItemProjection;
import com.changsha.zhongdian.shareholder.repository.ShareholderRepository;
import com.changsha.zhongdian.station.repository.StationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

// 持股关系只读查询：股东详情侧查"股东-站点"、站点详情侧查"站点-股东"，同一张表的两侧视角；
// 关联/调整/退出等维护能力由里程碑 F 在同一张表上补齐
@Service
public class ShareholdingQueryService {

    private final ShareholdingRepository shareholdingRepository;
    private final ShareholderRepository shareholderRepository;
    private final StationRepository stationRepository;

    public ShareholdingQueryService(ShareholdingRepository shareholdingRepository,
                                     ShareholderRepository shareholderRepository,
                                     StationRepository stationRepository) {
        this.shareholdingRepository = shareholdingRepository;
        this.shareholderRepository = shareholderRepository;
        this.stationRepository = stationRepository;
    }

    public List<ShareholderStationListItemResponse> getShareholderStations(Long shareholderId) {
        if (shareholderId == null || shareholderId <= 0L) {
            throw new BusinessException("VALIDATION_ERROR", "股东不存在", HttpStatus.BAD_REQUEST);
        }
        // 为什么先校验股东存在：该接口是股东详情的子资源，股东不存在时与详情接口同口径返回 404，
        // 避免对任意 ID 都返回空列表造成"股东存在但无关联"的误导
        if (!shareholderRepository.existsById(shareholderId)) {
            throw new BusinessException("SHAREHOLDER_NOT_FOUND", "股东不存在", HttpStatus.NOT_FOUND);
        }
        // 详情页只展示持股中的站点：已退出记录属于历史痕迹，由里程碑 F 的退出逻辑写入
        return shareholdingRepository
                .findStationListItemsByShareholderIdAndHoldingStatus(shareholderId, HoldingStatus.ACTIVE.name())
                .stream()
                .map(item -> new ShareholderStationListItemResponse(
                        item.getStationId(),
                        item.getStationCode(),
                        item.getStationName(),
                        item.getStationStatusName(),
                        item.getOwnershipPercentage(),
                        item.getEffectiveAt()
                ))
                .toList();
    }

    // 站点详情股东分区列表：站点是路径资源，站点不存在时与站点详情接口同口径返回 404
    public List<StationShareholdingListItemResponse> getStationShareholdings(Long stationId) {
        return getStationShareholdings(stationId, null);
    }

    // 站点详情股东按持股状态过滤：holdingStatus 为 null 时返回全部（供时间线复用），否则仅返回指定状态
    public List<StationShareholdingListItemResponse> getStationShareholdings(Long stationId, HoldingStatus holdingStatus) {
        if (stationId == null || stationId <= 0L) {
            throw new BusinessException("VALIDATION_ERROR", "充电站不存在", HttpStatus.BAD_REQUEST);
        }
        if (!stationRepository.existsById(stationId)) {
            throw new BusinessException("STATION_NOT_FOUND", "充电站不存在", HttpStatus.NOT_FOUND);
        }
        // 已退出关系是历史痕迹：列表保留展示（带状态标记），由前端摘要口径区分当前股权结构；
        // 状态码原值与中文名一并透出，前端按状态码做条件渲染，中文名仅用于展示
        List<StationShareholderListItemProjection> items =
                (holdingStatus == null)
                        ? shareholdingRepository.findStationShareholderListItems(stationId)
                        : shareholdingRepository.findByStationIdAndHoldingStatus(stationId, holdingStatus.name());
        return items.stream()
                .map(item -> new StationShareholdingListItemResponse(
                        item.getId(),
                        item.getShareholderId(),
                        item.getShareholderName(),
                        item.getContactName(),
                        item.getContactPhone(),
                        item.getOwnershipPercentage(),
                        item.getHoldingStatus(),
                        resolveHoldingStatusName(item.getHoldingStatus()),
                        item.getEffectiveAt()
                ))
                .toList();
    }

    // 状态名由应用层枚举映射：持股状态是应用层枚举口径（非字典项），与充电桩字典状态的来源不同
    private String resolveHoldingStatusName(String holdingStatus) {
        return switch (holdingStatus == null ? "" : holdingStatus) {
            case "ACTIVE" -> "持股中";
            case "EXITED" -> "已退出";
            default -> "--";
        };
    }
}