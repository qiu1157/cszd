package com.changsha.zhongdian.pile.service;

import java.util.List;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.dictionary.entity.DictionaryStatus;
import com.changsha.zhongdian.dictionary.entity.DictionaryTypeCode;
import com.changsha.zhongdian.dictionary.entity.SystemDictItem;
import com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository;
import com.changsha.zhongdian.pile.dto.PileListItemResponse;
import com.changsha.zhongdian.pile.dto.PileStatisticsResponse;
import com.changsha.zhongdian.pile.entity.ChargeType;
import com.changsha.zhongdian.pile.repository.PileListItemProjection;
import com.changsha.zhongdian.pile.repository.PileRepository;
import com.changsha.zhongdian.station.repository.StationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class PileQueryService {

    private final PileRepository pileRepository;
    private final StationRepository stationRepository;
    private final PileCodeGenerator pileCodeGenerator;
    private final SystemDictItemRepository dictItemRepository;

    public PileQueryService(
            PileRepository pileRepository,
            StationRepository stationRepository,
            PileCodeGenerator pileCodeGenerator,
            SystemDictItemRepository dictItemRepository
    ) {
        this.pileRepository = pileRepository;
        this.stationRepository = stationRepository;
        this.pileCodeGenerator = pileCodeGenerator;
        this.dictItemRepository = dictItemRepository;
    }

    public List<PileListItemResponse> listPilesByStationId(Long stationId) {
        requireExistingStation(stationId);

        return pileRepository.findAllListItemsByStationIdOrderByUpdatedAtDesc(stationId).stream()
                .map(this::toListItemResponse)
                .toList();
    }

    public String nextPileCode(Long stationId) {
        requireExistingStation(stationId);
        return pileCodeGenerator.generateNextCode();
    }

    public PileStatisticsResponse getPileStatistics() {
        long totalCount = pileRepository.count();
        long fastCount = pileRepository.countByChargeType(ChargeType.FAST);
        long slowCount = pileRepository.countByChargeType(ChargeType.SLOW);

        List<SystemDictItem> pileStatusItems = dictItemRepository
                .findAllByTypeTypeCodeAndItemStatusOrderBySortOrderAscItemNameAsc(
                        DictionaryTypeCode.PILE_STATUS,
                        DictionaryStatus.ENABLED
                );

        List<PileStatisticsResponse.PileStatusCount> statusCounts = pileStatusItems.stream()
                .map(item -> {
                    long count = pileRepository.countByPileStatusItemId(item.getId());
                    return new PileStatisticsResponse.PileStatusCount(
                            item.getId(),
                            item.getItemCode(),
                            item.getItemName(),
                            count
                    );
                })
                .toList();

        return new PileStatisticsResponse(totalCount, fastCount, slowCount, statusCounts);
    }

    private void requireExistingStation(Long stationId) {
        if (stationId == null || stationId <= 0L) {
            throw new BusinessException("VALIDATION_ERROR", "充电站不存在", HttpStatus.BAD_REQUEST);
        }

        // 为什么：桩列表挂在站点维度，站点不存在时与详情接口口径一致返回 404，
        // 避免出现“幽灵站点的空列表”让前端误以为站点正常
        if (!stationRepository.existsById(stationId)) {
            throw new BusinessException("STATION_NOT_FOUND", "充电站不存在", HttpStatus.NOT_FOUND);
        }
    }

    private PileListItemResponse toListItemResponse(PileListItemProjection projection) {
        return new PileListItemResponse(
                projection.getId(),
                projection.getPileCode(),
                projection.getChargeType(),
                toChargeTypeName(projection.getChargeType()),
                projection.getGunCount(),
                projection.getPileStatusName(),
                projection.getUpdatedAt()
        );
    }

    private String toChargeTypeName(String chargeType) {
        // 充电类型为应用层枚举口径，直接在服务层翻译为业务可读名称
        if (ChargeType.FAST.name().equals(chargeType)) {
            return "快充";
        }
        if (ChargeType.SLOW.name().equals(chargeType)) {
            return "慢充";
        }
        return chargeType == null ? "--" : chargeType;
    }
}
