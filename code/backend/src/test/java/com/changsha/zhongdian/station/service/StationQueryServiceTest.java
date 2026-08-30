package com.changsha.zhongdian.station.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import com.changsha.zhongdian.station.dto.StationListItemResponse;
import com.changsha.zhongdian.station.repository.StationListItemProjection;
import com.changsha.zhongdian.station.repository.StationRepository;
import org.junit.jupiter.api.Test;

class StationQueryServiceTest {

    @Test
    void listStationsShouldMapRepositoryProjectionToResponse() {
        StationRepository stationRepository = org.mockito.Mockito.mock(StationRepository.class);
        when(stationRepository.findAllListItemsOrderByUpdatedAtDesc()).thenReturn(List.of(
                new TestStationListItemProjection(
                        "ST-202608-0018",
                        "星城南服务区超级站",
                        "星络平台",
                        "XL-4301-889",
                        "张海峰",
                        "13800138000",
                        "运营中",
                        LocalDateTime.of(2026, 8, 29, 10, 26)
                )
        ));

        StationQueryService stationQueryService = new StationQueryService(stationRepository);

        List<StationListItemResponse> response = stationQueryService.listStations();

        assertEquals(1, response.size());
        assertEquals("ST-202608-0018", response.get(0).stationCode());
        assertEquals("星络平台", response.get(0).platformName());
        assertEquals("运营中", response.get(0).stationStatusName());
        assertEquals(LocalDateTime.of(2026, 8, 29, 10, 26), response.get(0).updatedAt());
    }

    @Test
    void listStationsShouldReturnEmptyListWhenRepositoryHasNoData() {
        StationRepository stationRepository = org.mockito.Mockito.mock(StationRepository.class);
        when(stationRepository.findAllListItemsOrderByUpdatedAtDesc()).thenReturn(List.of());

        StationQueryService stationQueryService = new StationQueryService(stationRepository);

        assertTrue(stationQueryService.listStations().isEmpty());
    }

    @Test
    void listStationsShouldTrimKeywordAndUseNameSearchWhenKeywordProvided() {
        StationRepository stationRepository = org.mockito.Mockito.mock(StationRepository.class);
        when(stationRepository.findAllListItemsByStationNameContainingOrderByUpdatedAtDesc("雨花")).thenReturn(List.of(
                new TestStationListItemProjection(
                        "ST-202608-0001",
                        "雨花城际快充港",
                        "手工验收平台",
                        "PT-224211",
                        "测试联系人",
                        "13800138000",
                        "签合同",
                        LocalDateTime.of(2026, 8, 29, 22, 42)
                )
        ));

        StationQueryService stationQueryService = new StationQueryService(stationRepository);

        List<StationListItemResponse> response = stationQueryService.listStations("  雨花  ");

        assertEquals(1, response.size());
        assertEquals("雨花城际快充港", response.get(0).stationName());
    }

    private static final class TestStationListItemProjection implements StationListItemProjection {

        private final String stationCode;
        private final String stationName;
        private final String platformName;
        private final String platformStationCode;
        private final String contactName;
        private final String contactPhone;
        private final String stationStatusName;
        private final LocalDateTime updatedAt;

        private TestStationListItemProjection(
                String stationCode,
                String stationName,
                String platformName,
                String platformStationCode,
                String contactName,
                String contactPhone,
                String stationStatusName,
                LocalDateTime updatedAt
        ) {
            this.stationCode = stationCode;
            this.stationName = stationName;
            this.platformName = platformName;
            this.platformStationCode = platformStationCode;
            this.contactName = contactName;
            this.contactPhone = contactPhone;
            this.stationStatusName = stationStatusName;
            this.updatedAt = updatedAt;
        }

        @Override
        public String getStationCode() {
            return stationCode;
        }

        @Override
        public String getStationName() {
            return stationName;
        }

        @Override
        public String getPlatformName() {
            return platformName;
        }

        @Override
        public String getPlatformStationCode() {
            return platformStationCode;
        }

        @Override
        public String getContactName() {
            return contactName;
        }

        @Override
        public String getContactPhone() {
            return contactPhone;
        }

        @Override
        public String getStationStatusName() {
            return stationStatusName;
        }

        @Override
        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }
    }
}
