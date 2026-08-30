package com.changsha.zhongdian.station.service;

import java.util.List;

import com.changsha.zhongdian.station.dto.StationListItemResponse;
import com.changsha.zhongdian.station.repository.StationRepository;
import org.springframework.stereotype.Service;

@Service
public class StationQueryService {

    private final StationRepository stationRepository;

    public StationQueryService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public List<StationListItemResponse> listStations() {
        return listStations(null);
    }

    public List<StationListItemResponse> listStations(String stationNameKeyword) {
        String normalizedKeyword = normalizeKeyword(stationNameKeyword);
        List<StationListItemResponse> stationList = (normalizedKeyword == null
                ? stationRepository.findAllListItemsOrderByUpdatedAtDesc()
                : stationRepository.findAllListItemsByStationNameContainingOrderByUpdatedAtDesc(normalizedKeyword)).stream()
                .map(item -> new StationListItemResponse(
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

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String normalizedKeyword = keyword.trim();
        return normalizedKeyword.isEmpty() ? null : normalizedKeyword;
    }
}
