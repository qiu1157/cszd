package com.changsha.zhongdian.pile.service;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.dictionary.entity.DictionaryStatus;
import com.changsha.zhongdian.dictionary.entity.DictionaryTypeCode;
import com.changsha.zhongdian.dictionary.entity.SystemDictItem;
import com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository;
import com.changsha.zhongdian.operationlog.annotation.LogOperation;
import com.changsha.zhongdian.operationlog.entity.OperationTargetType;
import com.changsha.zhongdian.operationlog.entity.OperationType;
import com.changsha.zhongdian.operationlog.util.BeforeStateHolder;
import com.changsha.zhongdian.pile.dto.PileCreateRequest;
import com.changsha.zhongdian.pile.dto.PileStatusUpdateRequest;
import com.changsha.zhongdian.pile.dto.PileUpdateRequest;
import com.changsha.zhongdian.pile.entity.ChargeType;
import com.changsha.zhongdian.pile.entity.Pile;
import com.changsha.zhongdian.pile.entity.PileStatusLog;
import com.changsha.zhongdian.pile.repository.PileRepository;
import com.changsha.zhongdian.pile.repository.PileStatusLogRepository;
import com.changsha.zhongdian.station.repository.StationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PileCommandService {

    private static final String DEFAULT_PILE_STATUS_NAME = "运营中";

    private final PileRepository pileRepository;
    private final PileCodeGenerator pileCodeGenerator;
    private final StationRepository stationRepository;
    private final SystemDictItemRepository dictItemRepository;
    private final PileStatusLogRepository pileStatusLogRepository;

    public PileCommandService(
            PileRepository pileRepository,
            PileCodeGenerator pileCodeGenerator,
            StationRepository stationRepository,
            SystemDictItemRepository dictItemRepository,
            PileStatusLogRepository pileStatusLogRepository
    ) {
        this.pileRepository = pileRepository;
        this.pileCodeGenerator = pileCodeGenerator;
        this.stationRepository = stationRepository;
        this.dictItemRepository = dictItemRepository;
        this.pileStatusLogRepository = pileStatusLogRepository;
    }

    @LogOperation(
            targetType = OperationTargetType.PILE,
            operationType = OperationType.CREATE,
            targetIdSpEL = "#result.pileCode",
            targetNameSpEL = "#result.pileCode",
            stationIdSpEL = "#stationId"
    )
    public Pile createPile(Long stationId, PileCreateRequest request) {
        if (stationId == null || stationId <= 0L) {
            throw new BusinessException("VALIDATION_ERROR", "充电站不存在", HttpStatus.BAD_REQUEST);
        }

        if (!stationRepository.existsById(stationId)) {
            throw new BusinessException("STATION_NOT_FOUND", "充电站不存在", HttpStatus.NOT_FOUND);
        }

        ChargeType chargeType = resolveChargeType(request.chargeType());
        Integer gunCount = requireValidGunCount(request.gunCount());
        Long defaultStatusItemId = resolveDefaultStatusItemId();

        Pile pile = new Pile();
        pile.setPileCode(pileCodeGenerator.generateNextCode());
        pile.setStationId(stationId);
        pile.setChargeType(chargeType);
        pile.setGunCount(gunCount);
        pile.setPileStatusItemId(defaultStatusItemId);

        return pileRepository.save(pile);
    }

    @LogOperation(
            targetType = OperationTargetType.PILE,
            operationType = OperationType.UPDATE,
            targetIdSpEL = "#result.pileCode",
            targetNameSpEL = "#result.pileCode",
            stationIdSpEL = "#stationId",
            captureBeforeState = true
    )
    public Pile updatePile(Long stationId, Long pileId, PileUpdateRequest request) {
        if (stationId == null || stationId <= 0L) {
            throw new BusinessException("VALIDATION_ERROR", "充电站不存在", HttpStatus.BAD_REQUEST);
        }

        ChargeType chargeType = resolveChargeType(request.chargeType());
        Integer gunCount = requireValidGunCount(request.gunCount());

        Pile pile = pileRepository.findById(pileId == null ? -1L : pileId)
                .filter(existingPile -> existingPile.getStationId().equals(stationId))
                .orElseThrow(() -> new BusinessException("PILE_NOT_FOUND", "充电桩不存在", HttpStatus.NOT_FOUND));

        BeforeStateHolder.set(clonePile(pile));

        pile.setChargeType(chargeType);
        pile.setGunCount(gunCount);

        return pileRepository.save(pile);
    }

    @Transactional
    @LogOperation(
            targetType = OperationTargetType.PILE,
            operationType = OperationType.STATUS_CHANGE,
            targetIdSpEL = "#result.pileCode",
            targetNameSpEL = "#result.pileCode",
            stationIdSpEL = "#stationId",
            remarkSpEL = "#request.remark"
    )
    public Pile updatePileStatus(Long stationId, Long pileId, PileStatusUpdateRequest request, String operatorAccount) {
        if (stationId == null || stationId <= 0L) {
            throw new BusinessException("VALIDATION_ERROR", "充电站不存在", HttpStatus.BAD_REQUEST);
        }

        Pile pile = pileRepository.findById(pileId == null ? -1L : pileId)
                .filter(existingPile -> existingPile.getStationId().equals(stationId))
                .orElseThrow(() -> new BusinessException("PILE_NOT_FOUND", "充电桩不存在", HttpStatus.NOT_FOUND));

        Long targetStatusItemId = requirePositiveId(request.pileStatusItemId(), "请选择新状态");
        SystemDictItem targetStatus = dictItemRepository
                .findByIdAndTypeTypeCodeAndItemStatus(targetStatusItemId, DictionaryTypeCode.PILE_STATUS, DictionaryStatus.ENABLED)
                .orElseThrow(() -> new BusinessException("PILE_STATUS_NOT_FOUND", "目标状态不存在或已停用", HttpStatus.BAD_REQUEST));

        Long fromStatusItemId = pile.getPileStatusItemId();
        pile.setPileStatusItemId(targetStatus.getId());
        pileRepository.save(pile);

        PileStatusLog statusLog = new PileStatusLog();
        statusLog.setPileId(pile.getId());
        statusLog.setFromStatusItemId(fromStatusItemId);
        statusLog.setToStatusItemId(targetStatus.getId());
        statusLog.setRemark(trimToNull(request.remark()));
        statusLog.setOperatorAccount(trimToNull(operatorAccount));
        pileStatusLogRepository.save(statusLog);

        return pile;
    }

    private Long requirePositiveId(Long value, String message) {
        if (value == null || value <= 0L) {
            throw new BusinessException("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
        }
        return value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalizedValue = value.trim();
        return normalizedValue.isEmpty() ? null : normalizedValue;
    }

    private Pile clonePile(Pile source) {
        Pile clone = new Pile();
        clone.setPileCode(source.getPileCode());
        clone.setStationId(source.getStationId());
        clone.setChargeType(source.getChargeType());
        clone.setGunCount(source.getGunCount());
        clone.setPileStatusItemId(source.getPileStatusItemId());
        return clone;
    }

    private ChargeType resolveChargeType(String chargeType) {
        if (chargeType == null || chargeType.trim().isEmpty()) {
            throw new BusinessException("VALIDATION_ERROR", "请选择充电类型", HttpStatus.BAD_REQUEST);
        }
        try {
            return ChargeType.valueOf(chargeType.trim());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("VALIDATION_ERROR", "充电类型不正确", HttpStatus.BAD_REQUEST);
        }
    }

    private Integer requireValidGunCount(Integer gunCount) {
        if (gunCount == null || gunCount < 1) {
            throw new BusinessException("VALIDATION_ERROR", "充电枪数量至少为1", HttpStatus.BAD_REQUEST);
        }
        if (gunCount > 64) {
            throw new BusinessException("VALIDATION_ERROR", "充电枪数量不能超过64", HttpStatus.BAD_REQUEST);
        }
        return gunCount;
    }

    private Long resolveDefaultStatusItemId() {
        SystemDictItem defaultStatus = dictItemRepository
                .findByTypeTypeCodeAndItemNameAndItemStatus(
                        DictionaryTypeCode.PILE_STATUS,
                        DEFAULT_PILE_STATUS_NAME,
                        DictionaryStatus.ENABLED
                )
                .orElseThrow(() -> new BusinessException("PILE_STATUS_NOT_FOUND", "默认充电桩状态不存在", HttpStatus.BAD_REQUEST));
        return defaultStatus.getId();
    }
}
