package com.changsha.zhongdian.shareholding.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.shareholding.dto.ShareholdingCreateRequest;
import com.changsha.zhongdian.shareholding.entity.HoldingStatus;
import com.changsha.zhongdian.shareholding.entity.Shareholding;
import com.changsha.zhongdian.shareholding.repository.ShareholdingRepository;
import com.changsha.zhongdian.shareholder.entity.Shareholder;
import com.changsha.zhongdian.shareholder.entity.ShareholderStatus;
import com.changsha.zhongdian.shareholder.repository.ShareholderRepository;
import com.changsha.zhongdian.station.repository.StationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 持股关系写侧：站点关联已有股东并落一条持股关系（F 里程碑首个写能力）
@Service
public class ShareholdingCommandService {

    private static final BigDecimal MAX_OWNERSHIP_PERCENTAGE = new BigDecimal("100");

    private final ShareholdingRepository shareholdingRepository;
    private final ShareholderRepository shareholderRepository;
    private final StationRepository stationRepository;

    public ShareholdingCommandService(
            ShareholdingRepository shareholdingRepository,
            ShareholderRepository shareholderRepository,
            StationRepository stationRepository
    ) {
        this.shareholdingRepository = shareholdingRepository;
        this.shareholderRepository = shareholderRepository;
        this.stationRepository = stationRepository;
    }

    @Transactional
    public Long createShareholding(Long stationId, ShareholdingCreateRequest request) {
        Long validStationId = requirePositiveId(stationId, "充电站不存在");
        if (!stationRepository.existsById(validStationId)) {
            throw new BusinessException("STATION_NOT_FOUND", "充电站不存在", HttpStatus.NOT_FOUND);
        }

        Long shareholderId = requirePositiveId(request.shareholderId(), "请选择股东");
        Shareholder shareholder = shareholderRepository.findById(shareholderId)
                .orElseThrow(() -> new BusinessException("SHAREHOLDER_NOT_FOUND", "股东不存在", HttpStatus.NOT_FOUND));

        // E-8 停用兜底：关联弹窗候选只给启用股东，但直连接口仍可传停用股东 ID，写侧必须二次拦截
        if (shareholder.getShareholderStatus() != ShareholderStatus.ENABLED) {
            throw new BusinessException("SHAREHOLDER_DISABLED", "股东已停用，不能新建持股关系", HttpStatus.BAD_REQUEST);
        }

        HoldingStatus holdingStatus = resolveHoldingStatus(request.holdingStatus());
        BigDecimal ownershipPercentage = requireValidOwnershipPercentage(request.ownershipPercentage());

        // 为什么只校验 ACTIVE 重复：同一股东的多条已退出记录是历史痕迹可保留，持股中才是唯一有效关系
        if (holdingStatus == HoldingStatus.ACTIVE
                && shareholdingRepository.existsByStationIdAndShareholderIdAndHoldingStatus(
                        validStationId, shareholderId, HoldingStatus.ACTIVE)) {
            throw new BusinessException(
                    "SHAREHOLDING_DUPLICATE",
                    "该股东已是持股中状态，无需重复关联",
                    HttpStatus.BAD_REQUEST
            );
        }

        // 原型口径：新增只拦截合计将超过 100%，允许暂时低于 100% 由股权调整统一补齐；
        // 已退出关系不计入合计，与占比总和统计口径一致
        if (holdingStatus == HoldingStatus.ACTIVE) {
            BigDecimal currentSum = shareholdingRepository
                    .sumOwnershipPercentageByStationIdAndHoldingStatus(validStationId, HoldingStatus.ACTIVE);
            if (currentSum.add(ownershipPercentage).compareTo(MAX_OWNERSHIP_PERCENTAGE) > 0) {
                throw new BusinessException(
                        "SHAREHOLDING_PERCENTAGE_EXCEEDED",
                        "占股比例合计将超过 100%，请先通过股权调整重新分配后再关联",
                        HttpStatus.BAD_REQUEST
                );
            }
        }

        // spec 7.5：股权调整立即生效、不指定未来生效时间，新增关系同口径以当前时间生效
        Shareholding shareholding = new Shareholding();
        shareholding.setStationId(validStationId);
        shareholding.setShareholderId(shareholderId);
        shareholding.setOwnershipPercentage(ownershipPercentage);
        shareholding.setHoldingStatus(holdingStatus);
        shareholding.setEffectiveAt(LocalDateTime.now());

        return shareholdingRepository.save(shareholding).getId();
    }

    private Long requirePositiveId(Long value, String message) {
        if (value == null || value <= 0L) {
            throw new BusinessException("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
        }
        return value;
    }

    private HoldingStatus resolveHoldingStatus(String holdingStatus) {
        if (holdingStatus == null || holdingStatus.trim().isEmpty()) {
            throw new BusinessException("VALIDATION_ERROR", "请选择关系状态", HttpStatus.BAD_REQUEST);
        }
        try {
            return HoldingStatus.valueOf(holdingStatus.trim());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("HOLDING_STATUS_INVALID", "关系状态不合法", HttpStatus.BAD_REQUEST);
        }
    }

    private BigDecimal requireValidOwnershipPercentage(BigDecimal ownershipPercentage) {
        if (ownershipPercentage == null) {
            throw new BusinessException("VALIDATION_ERROR", "请填写占股比例", HttpStatus.BAD_REQUEST);
        }
        if (ownershipPercentage.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("VALIDATION_ERROR", "占股比例不能为负数", HttpStatus.BAD_REQUEST);
        }
        if (ownershipPercentage.compareTo(MAX_OWNERSHIP_PERCENTAGE) > 0) {
            throw new BusinessException("VALIDATION_ERROR", "占股比例不能超过100", HttpStatus.BAD_REQUEST);
        }
        return ownershipPercentage;
    }
}