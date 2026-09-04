package com.changsha.zhongdian.shareholding.service;

import java.util.List;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.shareholding.dto.ShareholderStationListItemResponse;
import com.changsha.zhongdian.shareholding.entity.HoldingStatus;
import com.changsha.zhongdian.shareholding.repository.ShareholdingRepository;
import com.changsha.zhongdian.shareholder.repository.ShareholderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

// 股东详情的关联站点只读查询：E-7 先交付只读链路，关联/调整/退出等维护能力由里程碑 F 在同一张表上补齐
@Service
public class ShareholdingQueryService {

    private final ShareholdingRepository shareholdingRepository;
    private final ShareholderRepository shareholderRepository;

    public ShareholdingQueryService(ShareholdingRepository shareholdingRepository,
                                     ShareholderRepository shareholderRepository) {
        this.shareholdingRepository = shareholdingRepository;
        this.shareholderRepository = shareholderRepository;
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
}