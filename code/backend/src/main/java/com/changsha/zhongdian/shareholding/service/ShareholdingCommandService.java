package com.changsha.zhongdian.shareholding.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.operationlog.annotation.LogOperation;
import com.changsha.zhongdian.operationlog.entity.OperationTargetType;
import com.changsha.zhongdian.operationlog.entity.OperationType;
import com.changsha.zhongdian.shareholding.dto.ShareholdingCreateRequest;
import com.changsha.zhongdian.shareholding.dto.ShareholdingEquityAdjustmentRequest;
import com.changsha.zhongdian.shareholding.dto.ShareholdingEquityAdjustmentRequest.EquityAdjustmentItem;
import com.changsha.zhongdian.shareholding.entity.EquityAdjustmentEvent;
import com.changsha.zhongdian.shareholding.entity.HoldingStatus;
import com.changsha.zhongdian.shareholding.entity.Shareholding;
import com.changsha.zhongdian.shareholding.repository.EquityAdjustmentEventRepository;
import com.changsha.zhongdian.shareholding.repository.ShareholdingRepository;
import com.changsha.zhongdian.shareholder.entity.Shareholder;
import com.changsha.zhongdian.shareholder.entity.ShareholderStatus;
import com.changsha.zhongdian.shareholder.repository.ShareholderRepository;
import com.changsha.zhongdian.station.repository.StationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShareholdingCommandService {

    private static final BigDecimal MAX_OWNERSHIP_PERCENTAGE = new BigDecimal("100");

    private final ShareholdingRepository shareholdingRepository;
    private final ShareholderRepository shareholderRepository;
    private final StationRepository stationRepository;
    private final EquityAdjustmentEventRepository equityAdjustmentEventRepository;
    private final ObjectMapper objectMapper;

    public ShareholdingCommandService(
            ShareholdingRepository shareholdingRepository,
            ShareholderRepository shareholderRepository,
            StationRepository stationRepository,
            EquityAdjustmentEventRepository equityAdjustmentEventRepository,
            ObjectMapper objectMapper
    ) {
        this.shareholdingRepository = shareholdingRepository;
        this.shareholderRepository = shareholderRepository;
        this.stationRepository = stationRepository;
        this.equityAdjustmentEventRepository = equityAdjustmentEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @LogOperation(
            targetType = OperationTargetType.SHAREHOLDING,
            operationType = OperationType.CREATE,
            targetIdSpEL = "#result.id",
            targetNameSpEL = "'股东ID:' + #result.shareholderId",
            stationIdSpEL = "#stationId"
    )
    public Shareholding createShareholding(Long stationId, ShareholdingCreateRequest request) {
        Long validStationId = requirePositiveId(stationId, "充电站不存在");
        if (!stationRepository.existsById(validStationId)) {
            throw new BusinessException("STATION_NOT_FOUND", "充电站不存在", HttpStatus.NOT_FOUND);
        }

        Long shareholderId = requirePositiveId(request.shareholderId(), "请选择股东");
        Shareholder shareholder = shareholderRepository.findById(shareholderId)
                .orElseThrow(() -> new BusinessException("SHAREHOLDER_NOT_FOUND", "股东不存在", HttpStatus.NOT_FOUND));

        if (shareholder.getShareholderStatus() != ShareholderStatus.ENABLED) {
            throw new BusinessException("SHAREHOLDER_DISABLED", "股东已停用，不能新建持股关系", HttpStatus.BAD_REQUEST);
        }

        HoldingStatus holdingStatus = resolveHoldingStatus(request.holdingStatus());
        BigDecimal ownershipPercentage = requireValidOwnershipPercentage(request.ownershipPercentage());

        if (holdingStatus == HoldingStatus.ACTIVE
                && shareholdingRepository.existsByStationIdAndShareholderIdAndHoldingStatus(
                        validStationId, shareholderId, HoldingStatus.ACTIVE)) {
            throw new BusinessException(
                    "SHAREHOLDING_DUPLICATE",
                    "该股东已是持股中状态，无需重复关联",
                    HttpStatus.BAD_REQUEST
            );
        }

        Shareholding shareholding = new Shareholding();
        shareholding.setStationId(validStationId);
        shareholding.setShareholderId(shareholderId);
        shareholding.setOwnershipPercentage(ownershipPercentage);
        shareholding.setHoldingStatus(holdingStatus);
        shareholding.setEffectiveAt(LocalDateTime.now());

        return shareholdingRepository.save(shareholding);
    }

    @Transactional
    @LogOperation(
            targetType = OperationTargetType.SHAREHOLDING,
            operationType = OperationType.EXIT,
            targetIdSpEL = "#result.id",
            targetNameSpEL = "'股东ID:' + #result.shareholderId",
            stationIdSpEL = "#stationId"
    )
    public Shareholding exitShareholding(Long stationId, Long shareholdingId) {
        Long validStationId = requirePositiveId(stationId, "充电站不存在");
        if (!stationRepository.existsById(validStationId)) {
            throw new BusinessException("STATION_NOT_FOUND", "充电站不存在", HttpStatus.NOT_FOUND);
        }

        Shareholding shareholding = shareholdingRepository.findById(requirePositiveId(shareholdingId, "持股关系不存在"))
                .filter(item -> item.getStationId().equals(validStationId))
                .orElseThrow(() -> new BusinessException(
                        "SHAREHOLDING_NOT_FOUND", "持股关系不存在", HttpStatus.NOT_FOUND));

        if (shareholding.getHoldingStatus() == HoldingStatus.EXITED) {
            throw new BusinessException(
                    "SHAREHOLDING_ALREADY_EXITED", "该股东已退出，请勿重复操作", HttpStatus.BAD_REQUEST);
        }

        shareholding.setHoldingStatus(HoldingStatus.EXITED);
        shareholding.setEffectiveAt(LocalDateTime.now());

        redistributeRemainingOwnership(validStationId, shareholding.getId());

        return shareholdingRepository.save(shareholding);
    }

    @Transactional
    @LogOperation(
            targetType = OperationTargetType.SHAREHOLDING,
            operationType = OperationType.EQUITY_ADJUST,
            targetIdSpEL = "#stationId",
            targetNameSpEL = "'充电站ID:' + #stationId",
            stationIdSpEL = "#stationId"
    )
    public Long adjustEquity(Long stationId, ShareholdingEquityAdjustmentRequest request, String operatorAccount) {
        Long validStationId = requirePositiveId(stationId, "充电站不存在");
        if (!stationRepository.existsById(validStationId)) {
            throw new BusinessException("STATION_NOT_FOUND", "充电站不存在", HttpStatus.NOT_FOUND);
        }

        List<Shareholding> activeRelations = shareholdingRepository
                .findByStationIdAndHoldingStatus(validStationId, HoldingStatus.ACTIVE);
        Map<Long, Shareholding> relationMap = activeRelations.stream()
                .collect(Collectors.toMap(Shareholding::getId, Function.identity()));

        Map<Long, String> shareholderNameMap = resolveShareholderNames(activeRelations);
        String beforeSnapshot = buildSnapshotJson(activeRelations, shareholderNameMap);

        for (EquityAdjustmentItem item : request.items()) {
            Shareholding relation = relationMap.get(item.shareholdingId());
            if (relation == null) {
                throw new BusinessException(
                        "SHAREHOLDING_NOT_FOUND", "持股关系不存在或已退出", HttpStatus.NOT_FOUND);
            }
            relation.setOwnershipPercentage(requireValidOwnershipPercentage(item.ownershipPercentage()));
            relation.setEffectiveAt(LocalDateTime.now());
            shareholdingRepository.save(relation);
        }

        String afterSnapshot = buildSnapshotJson(activeRelations, shareholderNameMap);
        BigDecimal totalPercentage = activeRelations.stream()
                .map(Shareholding::getOwnershipPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        EquityAdjustmentEvent event = new EquityAdjustmentEvent();
        event.setStationId(validStationId);
        event.setBeforeSnapshot(beforeSnapshot);
        event.setAfterSnapshot(afterSnapshot);
        event.setTotalPercentage(totalPercentage);
        event.setOperatorAccount(trimToNull(operatorAccount));
        equityAdjustmentEventRepository.save(event);

        return validStationId;
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

    private Map<Long, String> resolveShareholderNames(List<Shareholding> relations) {
        List<Long> shareholderIds = relations.stream()
                .map(Shareholding::getShareholderId)
                .distinct()
                .toList();
        return shareholderRepository.findAllById(shareholderIds).stream()
                .collect(Collectors.toMap(Shareholder::getId, Shareholder::getShareholderName));
    }

    private String buildSnapshotJson(List<Shareholding> relations, Map<Long, String> shareholderNameMap) {
        List<EquitySnapshotItem> items = relations.stream()
                .map(relation -> new EquitySnapshotItem(
                        relation.getId(),
                        relation.getShareholderId(),
                        shareholderNameMap.get(relation.getShareholderId()),
                        relation.getOwnershipPercentage()))
                .toList();
        try {
            return objectMapper.writeValueAsString(items);
        } catch (Exception exception) {
            throw new BusinessException("SERIALIZE_ERROR", "持股快照序列化失败", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private record EquitySnapshotItem(
            Long shareholdingId,
            Long shareholderId,
            String shareholderName,
            BigDecimal ownershipPercentage
    ) {
    }
}
