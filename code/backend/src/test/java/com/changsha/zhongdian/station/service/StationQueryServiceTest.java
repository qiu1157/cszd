package com.changsha.zhongdian.station.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import com.changsha.zhongdian.station.dto.StationDetailResponse;
import com.changsha.zhongdian.station.dto.StationListItemResponse;
import com.changsha.zhongdian.station.repository.StationDetailProjection;
import com.changsha.zhongdian.station.repository.StationListItemProjection;
import com.changsha.zhongdian.station.repository.StationRepository;
import org.junit.jupiter.api.Test;

class StationQueryServiceTest {

    @Test
    void listStationsShouldMapRepositoryProjectionToResponse() {
        StationRepository stationRepository = org.mockito.Mockito.mock(StationRepository.class);
        when(stationRepository.findAllListItemsOrderByUpdatedAtDesc()).thenReturn(List.of(
                new TestStationListItemProjection(
                        1001L,
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

        com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository dictItemRepository = org.mockito.Mockito.mock(com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository.class);
        StationQueryService stationQueryService = new StationQueryService(stationRepository, dictItemRepository);

        List<StationListItemResponse> response = stationQueryService.listStations();

        assertEquals(1, response.size());
        assertEquals(1001L, response.get(0).id());
        assertEquals("ST-202608-0018", response.get(0).stationCode());
        assertEquals("星络平台", response.get(0).platformName());
        assertEquals("运营中", response.get(0).stationStatusName());
        assertEquals(LocalDateTime.of(2026, 8, 29, 10, 26), response.get(0).updatedAt());
    }

    @Test
    void listStationsShouldReturnEmptyListWhenRepositoryHasNoData() {
        StationRepository stationRepository = org.mockito.Mockito.mock(StationRepository.class);
        com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository dictItemRepository = org.mockito.Mockito.mock(com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository.class);
        when(stationRepository.findAllListItemsOrderByUpdatedAtDesc()).thenReturn(List.of());

        StationQueryService stationQueryService = new StationQueryService(stationRepository, dictItemRepository);

        assertTrue(stationQueryService.listStations().isEmpty());
    }

    @Test
    void listStationsShouldTrimKeywordAndUseNameSearchWhenKeywordProvided() {
        StationRepository stationRepository = org.mockito.Mockito.mock(StationRepository.class);
        com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository dictItemRepository = org.mockito.Mockito.mock(com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository.class);
        when(stationRepository.findAllListItemsByStationNameContainingOrderByUpdatedAtDesc("雨花")).thenReturn(List.of(
                new TestStationListItemProjection(
                        1001L,
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

        StationQueryService stationQueryService = new StationQueryService(stationRepository, dictItemRepository);

        List<StationListItemResponse> response = stationQueryService.listStations("  雨花  ");

        assertEquals(1, response.size());
        assertEquals("雨花城际快充港", response.get(0).stationName());
    }

    @Test
    void listStationsShouldUsePlatformFilterWhenPlatformItemIdProvided() {
        StationRepository stationRepository = org.mockito.Mockito.mock(StationRepository.class);
        com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository dictItemRepository = org.mockito.Mockito.mock(com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository.class);
        when(stationRepository.findAllListItemsByPlatformItemIdOrderByUpdatedAtDesc(11L)).thenReturn(List.of(
                new TestStationListItemProjection(
                        1002L,
                        "ST-202608-0002",
                        "望城综合能源站",
                        "手工验收平台",
                        "PT-330011",
                        "李工",
                        "13800138001",
                        "签合同",
                        LocalDateTime.of(2026, 8, 30, 8, 40)
                )
        ));

        StationQueryService stationQueryService = new StationQueryService(stationRepository, dictItemRepository);

        List<StationListItemResponse> response = stationQueryService.listStations(null, 11L);

        assertEquals(1, response.size());
        assertEquals("望城综合能源站", response.get(0).stationName());
    }

    @Test
    void listStationsShouldUseStatusFilterWhenStationStatusItemIdProvided() {
        StationRepository stationRepository = org.mockito.Mockito.mock(StationRepository.class);
        when(stationRepository.findAllListItemsByStationStatusItemIdOrderByUpdatedAtDesc(21L)).thenReturn(List.of(
                new TestStationListItemProjection(
                        1003L,
                        "ST-202608-0003",
                        "河西智慧充电站",
                        "手工验收平台",
                        "PT-330021",
                        "王工",
                        "13800138002",
                        "运营中",
                        LocalDateTime.of(2026, 8, 30, 9, 10)
                )
        ));

        com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository dictItemRepository = org.mockito.Mockito.mock(com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository.class);
        StationQueryService stationQueryService = new StationQueryService(stationRepository, dictItemRepository);

        List<StationListItemResponse> response = stationQueryService.listStations(null, null, 21L);

        assertEquals(1, response.size());
        assertEquals("河西智慧充电站", response.get(0).stationName());
    }

    @Test
    void getStationDetailShouldMapRepositoryProjectionToResponse() {
        StationRepository stationRepository = org.mockito.Mockito.mock(StationRepository.class);
        com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository dictItemRepository = org.mockito.Mockito.mock(com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository.class);
        when(stationRepository.findDetailById(1001L)).thenReturn(java.util.Optional.of(
                new TestStationDetailProjection(
                        1001L,
                        "ST-202608-0001",
                        "雨花城际快充港",
                        11L,
                        "星络平台",
                        "XL-4301-889",
                        "长沙市雨花区城际大道南段",
                        "张海峰",
                        "13800138000",
                        "0101234567890",
                        21L,
                        "签合同"
                )
        ));

        StationQueryService stationQueryService = new StationQueryService(stationRepository, dictItemRepository);

        StationDetailResponse response = stationQueryService.getStationDetail(1001L);

        assertNotNull(response);
        assertEquals(1001L, response.id());
        assertEquals("ST-202608-0001", response.stationCode());
        assertEquals("雨花城际快充港", response.stationName());
        assertEquals("长沙市雨花区城际大道南段", response.address());
        assertEquals("张海峰", response.contactName());
        assertEquals("13800138000", response.contactPhone());
        assertEquals("0101234567890", response.meterAccountNumber());
        assertEquals(21L, response.stationStatusItemId());
        assertEquals("签合同", response.stationStatusName());
    }

    private static final class TestStationListItemProjection implements StationListItemProjection {

        private final Long id;
        private final String stationCode;
        private final String stationName;
        private final String platformName;
        private final String platformStationCode;
        private final String contactName;
        private final String contactPhone;
        private final String stationStatusName;
        private final LocalDateTime updatedAt;

        private TestStationListItemProjection(
                Long id,
                String stationCode,
                String stationName,
                String platformName,
                String platformStationCode,
                String contactName,
                String contactPhone,
                String stationStatusName,
                LocalDateTime updatedAt
        ) {
            this.id = id;
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
        public Long getId() {
            return id;
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

    private static final class TestStationDetailProjection implements StationDetailProjection {

        private final Long id;
        private final String stationCode;
        private final String stationName;
        private final Long platformItemId;
        private final String platformName;
        private final String platformStationCode;
        private final String address;
        private final String contactName;
        private final String contactPhone;
        private final String meterAccountNumber;
        private final Long stationStatusItemId;
        private final String stationStatusName;

        private TestStationDetailProjection(
                Long id,
                String stationCode,
                String stationName,
                Long platformItemId,
                String platformName,
                String platformStationCode,
                String address,
                String contactName,
                String contactPhone,
                String meterAccountNumber,
                Long stationStatusItemId,
                String stationStatusName
        ) {
            this.id = id;
            this.stationCode = stationCode;
            this.stationName = stationName;
            this.platformItemId = platformItemId;
            this.platformName = platformName;
            this.platformStationCode = platformStationCode;
            this.address = address;
            this.contactName = contactName;
            this.contactPhone = contactPhone;
            this.meterAccountNumber = meterAccountNumber;
            this.stationStatusItemId = stationStatusItemId;
            this.stationStatusName = stationStatusName;
        }

        @Override
        public Long getId() {
            return id;
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
        public Long getPlatformItemId() {
            return platformItemId;
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
        public String getAddress() {
            return address;
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
        public String getMeterAccountNumber() {
            return meterAccountNumber;
        }

        @Override
        public Long getStationStatusItemId() {
            return stationStatusItemId;
        }

        @Override
        public String getStationStatusName() {
            return stationStatusName;
        }
    }
}
