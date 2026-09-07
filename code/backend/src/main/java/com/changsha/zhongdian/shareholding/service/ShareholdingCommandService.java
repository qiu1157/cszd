package com.changsha.zhongdian.shareholding.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

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

        // spec 7.5：股权调整立即生效、不指定未来生效时间，新增关系同口径以当前时间生效
        Shareholding shareholding = new Shareholding();
        shareholding.setStationId(validStationId);
        shareholding.setShareholderId(shareholderId);
        shareholding.setOwnershipPercentage(ownershipPercentage);
        shareholding.setHoldingStatus(holdingStatus);
        shareholding.setEffectiveAt(LocalDateTime.now());

        return shareholdingRepository.save(shareholding).getId();
    }

    @Transactional
    public Long exitShareholding(Long stationId, Long shareholdingId) {
        Long validStationId = requirePositiveId(stationId, "充电站不存在");
        if (!stationRepository.existsById(validStationId)) {
            throw new BusinessException("STATION_NOT_FOUND", "充电站不存在", HttpStatus.NOT_FOUND);
        }

        // 跨站点访问按不存在处理：不区分"关系不存在"与"关系属于其他站点"，避免暴露存在性供探测
        Shareholding shareholding = shareholdingRepository.findById(requirePositiveId(shareholdingId, "持股关系不存在"))
                .filter(item -> item.getStationId().equals(validStationId))
                .orElseThrow(() -> new BusinessException(
                        "SHAREHOLDING_NOT_FOUND", "持股关系不存在", HttpStatus.NOT_FOUND));

        // 重复退出是双击/重试的常见结果：幂等拦截，避免已落定的历史痕迹被二次刷新
        if (shareholding.getHoldingStatus() == HoldingStatus.EXITED) {
            throw new BusinessException(
                    "SHAREHOLDING_ALREADY_EXITED", "该股东已退出，请勿重复操作", HttpStatus.BAD_REQUEST);
        }

        // spec 8.2：退股以"已退出"状态表达而非物理删除，历史关系留痕；
        // spec 7.5：与股权调整同口径立即生效，生效时间刷新为退出时刻
        shareholding.setHoldingStatus(HoldingStatus.EXITED);
        shareholding.setEffectiveAt(LocalDateTime.now());

        // spec 7.5：退股时必须同步调整其他股东占比。
        // 采用等比例放大策略：剩余股东按各自当前占比占剩余合计的比例瓜分 100%，
        // 最后一条做 100 减去前几条之和的兜底，避免 DECIMAL(5,2) 四舍五入导致合计偏离 100
        redistributeRemainingOwnership(validStationId, shareholding.getId());

        return shareholdingRepository.save(shareholding).getId();
    }

    private void redistributeRemainingOwnership(Long stationId, Long exitedShareholdingId) {
        List<Shareholding> activeRelations = shareholdingRepository
                .findByStationIdAndHoldingStatus(stationId, HoldingStatus.ACTIVE)
                .stream()
                .filter(relation -> !relation.getId().equals(exitedShareholdingId))
                .toList();

        if (activeRelations.isEmpty()) {
            return;
        }

        BigDecimal remainingSum = activeRelations.stream()
                .map(Shareholding::getOwnershipPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (remainingSum.compareTo(BigDecimal.ZERO) <= 0) {
            // 剩余持股中合计为 0 时无法做比例分配，保持原值即可（极端历史数据场景）
            return;
        }

        BigDecimal distributedSum = BigDecimal.ZERO;
        for (int index = 0; index < activeRelations.size(); index++) {
            Shareholding relation = activeRelations.get(index);
            if (index < activeRelations.size() - 1) {
                BigDecimal newPercentage = relation.getOwnershipPercentage()
                        .multiply(new BigDecimal("100"))
                        .divide(remainingSum, 2, RoundingMode.HALF_UP);
                relation.setOwnershipPercentage(newPercentage);
                distributedSum = distributedSum.add(newPercentage);
            } else {
                BigDecimal lastPercentage = new BigDecimal("100").subtract(distributedSum);
                relation.setOwnershipPercentage(lastPercentage);
            }
            relation.setEffectiveAt(LocalDateTime.now());
            shareholdingRepository.save(relation);
        }
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