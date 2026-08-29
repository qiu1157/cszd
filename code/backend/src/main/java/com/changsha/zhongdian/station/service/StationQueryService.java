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
        return stationRepository.findAllListItemsOrderByUpdatedAtDesc().stream()
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
    }
}
