package com.changsha.zhongdian.station.service;

import java.util.List;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.station.dto.StationDetailResponse;
import com.changsha.zhongdian.station.dto.StationListItemResponse;
import com.changsha.zhongdian.station.repository.StationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class StationQueryService {

    private final StationRepository stationRepository;

    public StationQueryService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public List<StationListItemResponse> listStations() {
        return listStations(null, null);
    }

    public List<StationListItemResponse> listStations(String stationNameKeyword) {
        return listStations(stationNameKeyword, null);
    }

    public List<StationListItemResponse> listStations(String stationNameKeyword, Long platformItemId) {
        String normalizedKeyword = normalizeKeyword(stationNameKeyword);
        Long normalizedPlatformItemId = normalizePlatformItemId(platformItemId);

        List<StationListItemResponse> stationList = resolveStationProjections(normalizedKeyword, normalizedPlatformItemId).stream()
                .map(item -> new StationListItemResponse(
                        item.getId(),
                        item.getStationCode(),
                        item.getStationName(),
                        item.getPlatformName(),
                        item.getPlatformStationCode(),
                        item.getContactName(),
                        item.getContactPhone(),
                        item.getStationStatusName(),
                        item.getUpdatedAt()
                ))
                .toList();
        return stationList;
    }

    public List<StationListItemResponse> listStations(
            String stationNameKeyword,
            Long platformItemId,
            Long stationStatusItemId
    ) {
        String normalizedKeyword = normalizeKeyword(stationNameKeyword);
        Long normalizedPlatformItemId = normalizePlatformItemId(platformItemId);
        Long normalizedStationStatusItemId = normalizePlatformItemId(stationStatusItemId);

        List<StationListItemResponse> stationList = resolveStationProjections(
                normalizedKeyword,
                normalizedPlatformItemId,
                normalizedStationStatusItemId
        ).stream()
                .map(item -> new StationListItemResponse(
                        item.getId(),
                        item.getStationCode(),
                        item.getStationName(),
                        item.getPlatformName(),
                        item.getPlatformStationCode(),
                        item.getContactName(),
                        item.getContactPhone(),
                        item.getStationStatusName(),
                        item.getUpdatedAt()
                ))
                .toList();
        return stationList;
    }

    public StationDetailResponse getStationDetail(Long stationId) {
        if (stationId == null || stationId <= 0L) {
            throw new BusinessException("VALIDATION_ERROR", "充电站不存在", HttpStatus.BAD_REQUEST);
        }

        com.changsha.zhongdian.station.repository.StationDetailProjection detail = stationRepository.findDetailById(stationId)
                .orElseThrow(() -> new BusinessException("STATION_NOT_FOUND", "充电站不存在", HttpStatus.NOT_FOUND));

        return new StationDetailResponse(
                detail.getId(),
                detail.getStationCode(),
                detail.getStationName(),
                detail.getPlatformItemId(),
                detail.getPlatformName(),
                detail.getPlatformStationCode(),
                detail.getAddress(),
                detail.getContactName(),
                detail.getContactPhone(),
                detail.getStationStatusItemId(),
                detail.getStationStatusName()
        );
    }

    private List<com.changsha.zhongdian.station.repository.StationListItemProjection> resolveStationProjections(
            String stationNameKeyword,
            Long platformItemId
    ) {
        if (stationNameKeyword != null && platformItemId != null) {
            return stationRepository.findAllListItemsByStationNameContainingAndPlatformItemIdOrderByUpdatedAtDesc(
                    stationNameKeyword,
                    platformItemId
            );
        }
        if (stationNameKeyword != null) {
            return stationRepository.findAllListItemsByStationNameContainingOrderByUpdatedAtDesc(stationNameKeyword);
        }
        if (platformItemId != null) {
            return stationRepository.findAllListItemsByPlatformItemIdOrderByUpdatedAtDesc(platformItemId);
        }
        return stationRepository.findAllListItemsOrderByUpdatedAtDesc();
    }

    private List<com.changsha.zhongdian.station.repository.StationListItemProjection> resolveStationProjections(
            String stationNameKeyword,
            Long platformItemId,
            Long stationStatusItemId
    ) {
        if (stationStatusItemId == null) {
            return resolveStationProjections(stationNameKeyword, platformItemId);
        }
        if (stationNameKeyword == null && platformItemId == null) {
            return stationRepository.findAllListItemsByStationStatusItemIdOrderByUpdatedAtDesc(stationStatusItemId);
        }
        return stationRepository.findAllListItemsByFiltersOrderByUpdatedAtDesc(
                stationNameKeyword,
                platformItemId,
                stationStatusItemId
        );
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String normalizedKeyword = keyword.trim();
        return normalizedKeyword.isEmpty() ? null : normalizedKeyword;
    }

    private Long normalizePlatformItemId(Long platformItemId) {
        if (platformItemId == null || platformItemId <= 0L) {
            return null;
        }
        return platformItemId;
    }
}
