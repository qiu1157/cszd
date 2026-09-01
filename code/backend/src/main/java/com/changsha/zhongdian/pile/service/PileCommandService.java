package com.changsha.zhongdian.pile.service;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.dictionary.entity.DictionaryStatus;
import com.changsha.zhongdian.dictionary.entity.DictionaryTypeCode;
import com.changsha.zhongdian.dictionary.entity.SystemDictItem;
import com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository;
import com.changsha.zhongdian.pile.dto.PileCreateRequest;
import com.changsha.zhongdian.pile.dto.PileUpdateRequest;
import com.changsha.zhongdian.pile.entity.ChargeType;
import com.changsha.zhongdian.pile.entity.Pile;
import com.changsha.zhongdian.pile.repository.PileRepository;
import com.changsha.zhongdian.station.repository.StationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class PileCommandService {

    private static final String DEFAULT_PILE_STATUS_NAME = "运营中";

    private final PileRepository pileRepository;
    private final PileCodeGenerator pileCodeGenerator;
    private final StationRepository stationRepository;
    private final SystemDictItemRepository dictItemRepository;

    public PileCommandService(
            PileRepository pileRepository,
            PileCodeGenerator pileCodeGenerator,
            StationRepository stationRepository,
            SystemDictItemRepository dictItemRepository
    ) {
        this.pileRepository = pileRepository;
        this.pileCodeGenerator = pileCodeGenerator;
        this.stationRepository = stationRepository;
        this.dictItemRepository = dictItemRepository;
    }

    public Long createPile(Long stationId, PileCreateRequest request) {
        if (stationId == null || stationId <= 0L) {
            throw new BusinessException("VALIDATION_ERROR", "充电站不存在", HttpStatus.BAD_REQUEST);
        }

        // 为什么：桩必须挂在真实站点下，站点不存在时与详情/列表接口口径一致返回 404
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

        return pileRepository.save(pile).getId();
    }

    public Long updatePile(Long stationId, Long pileId, PileUpdateRequest request) {
        if (stationId == null || stationId <= 0L) {
            throw new BusinessException("VALIDATION_ERROR", "充电站不存在", HttpStatus.BAD_REQUEST);
        }

        ChargeType chargeType = resolveChargeType(request.chargeType());
        Integer gunCount = requireValidGunCount(request.gunCount());

        Pile pile = pileRepository.findById(pileId == null ? -1L : pileId)
                .filter(existingPile -> existingPile.getStationId().equals(stationId))
                .orElseThrow(() -> new BusinessException("PILE_NOT_FOUND", "充电桩不存在", HttpStatus.NOT_FOUND));

        // 为什么：编辑表单不提供状态控件，桩状态变更必须走状态切换接口并记录日志，
        // 与站点“编辑与状态变更分离”的口径一致
        pile.setChargeType(chargeType);
        pile.setGunCount(gunCount);

        return pileRepository.save(pile).getId();
    }

    private ChargeType resolveChargeType(String chargeType) {
        if (chargeType == null || chargeType.trim().isEmpty()) {
            throw new BusinessException("VALIDATION_ERROR", "请选择充电类型", HttpStatus.BAD_REQUEST);
        }
        try {
            // 充电类型为应用层枚举口径，统一在这里收敛解析，避免脏值入库
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
        // 为什么：新增桩默认运营中，与站点“新增默认签合同”同构，由后端统一解析启用字典项，
        // 前端表单不提供状态选择，避免录入期出现非默认状态
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
