package com.changsha.zhongdian.station.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.dictionary.entity.DictionaryStatus;
import com.changsha.zhongdian.dictionary.entity.DictionaryTypeCode;
import com.changsha.zhongdian.dictionary.entity.SystemDictItem;
import com.changsha.zhongdian.dictionary.entity.SystemDictType;
import com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository;
import com.changsha.zhongdian.station.dto.StationCreateRequest;
import com.changsha.zhongdian.station.dto.StationUpdateRequest;
import com.changsha.zhongdian.station.entity.Station;
import com.changsha.zhongdian.station.repository.StationRepository;
import org.junit.jupiter.api.Test;

class StationCommandServiceTest {

    @Test
    void createStationShouldGenerateCodeAndUseContractStatusByDefault() {
        StationRepository stationRepository = org.mockito.Mockito.mock(StationRepository.class);
        StationCodeGenerator stationCodeGenerator = org.mockito.Mockito.mock(StationCodeGenerator.class);
        SystemDictItemRepository dictItemRepository = org.mockito.Mockito.mock(SystemDictItemRepository.class);
        StationCommandService commandService = new StationCommandService(
                stationRepository,
                stationCodeGenerator,
                dictItemRepository
        );

        SystemDictItem platformItem = createDictItem(11L, DictionaryTypeCode.PLATFORM, "星络平台");
        SystemDictItem contractStatus = createDictItem(21L, DictionaryTypeCode.STATION_STATUS, "签合同");

        when(dictItemRepository.findByIdAndTypeTypeCodeAndItemStatus(11L, DictionaryTypeCode.PLATFORM, DictionaryStatus.ENABLED))
                .thenReturn(Optional.of(platformItem));
        when(dictItemRepository.findByTypeTypeCodeAndItemNameAndItemStatus(
                DictionaryTypeCode.STATION_STATUS, "签合同", DictionaryStatus.ENABLED
        )).thenReturn(Optional.of(contractStatus));
        when(stationCodeGenerator.generateNextCode()).thenReturn("ST-202608-0001");
        when(stationRepository.existsByPlatformItemIdAndPlatformStationCode(11L, "XL-4301-889")).thenReturn(false);
        when(stationRepository.save(any(Station.class))).thenAnswer(invocation -> {
            Station station = invocation.getArgument(0);
            java.lang.reflect.Field idField = Station.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(station, 1001L);
            return station;
        });

        Long createdId = commandService.createStation(new StationCreateRequest(
                "雨花城际快充港",
                11L,
                "XL-4301-889",
                "长沙市雨花区城际大道南段",
                "张海峰",
                "13800138000"
        ));

        assertNotNull(createdId);
        assertEquals(1001L, createdId);
        verify(stationRepository).save(argThat(station ->
                "ST-202608-0001".equals(station.getStationCode())
                        && "雨花城际快充港".equals(station.getStationName())
                        && Long.valueOf(11L).equals(station.getPlatformItemId())
                        && Long.valueOf(21L).equals(station.getStationStatusItemId())
        ));
    }

    @Test
    void createStationShouldRejectDuplicatedPlatformStationCodeWithinSamePlatform() {
        StationRepository stationRepository = org.mockito.Mockito.mock(StationRepository.class);
        StationCodeGenerator stationCodeGenerator = org.mockito.Mockito.mock(StationCodeGenerator.class);
        SystemDictItemRepository dictItemRepository = org.mockito.Mockito.mock(SystemDictItemRepository.class);
        StationCommandService commandService = new StationCommandService(
                stationRepository,
                stationCodeGenerator,
                dictItemRepository
        );

        SystemDictItem platformItem = createDictItem(11L, DictionaryTypeCode.PLATFORM, "星络平台");
        SystemDictItem contractStatus = createDictItem(21L, DictionaryTypeCode.STATION_STATUS, "签合同");

        when(dictItemRepository.findByIdAndTypeTypeCodeAndItemStatus(11L, DictionaryTypeCode.PLATFORM, DictionaryStatus.ENABLED))
                .thenReturn(Optional.of(platformItem));
        when(dictItemRepository.findByTypeTypeCodeAndItemNameAndItemStatus(
                DictionaryTypeCode.STATION_STATUS, "签合同", DictionaryStatus.ENABLED
        )).thenReturn(Optional.of(contractStatus));
        when(stationRepository.existsByPlatformItemIdAndPlatformStationCode(11L, "XL-4301-889")).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> commandService.createStation(new StationCreateRequest(
                        "雨花城际快充港",
                        11L,
                        "XL-4301-889",
                        "长沙市雨花区城际大道南段",
                        "张海峰",
                        "13800138000"
                ))
        );

        assertEquals("STATION_PLATFORM_CODE_DUPLICATED", exception.getCode());
        assertEquals("同一平台下的平台站点编号不能重复", exception.getMessage());
    }

    @Test
    void updateStationShouldUpdateBaseFieldsAndKeepStationCodeAndStatus() {
        StationRepository stationRepository = org.mockito.Mockito.mock(StationRepository.class);
        StationCodeGenerator stationCodeGenerator = org.mockito.Mockito.mock(StationCodeGenerator.class);
        SystemDictItemRepository dictItemRepository = org.mockito.Mockito.mock(SystemDictItemRepository.class);
        StationCommandService commandService = new StationCommandService(
                stationRepository,
                stationCodeGenerator,
                dictItemRepository
        );

        Station existingStation = new Station();
        existingStation.setStationCode("ST-202608-0001");
        existingStation.setStationName("雨花城际快充港");
        existingStation.setPlatformItemId(11L);
        existingStation.setPlatformStationCode("XL-4301-889");
        existingStation.setAddress("长沙市雨花区城际大道南段");
        existingStation.setContactName("张海峰");
        existingStation.setContactPhone("13800138000");
        existingStation.setStationStatusItemId(21L);
        setStationId(existingStation, 1001L);

        SystemDictItem platformItem = createDictItem(12L, DictionaryTypeCode.PLATFORM, "云快充");

        when(stationRepository.findById(1001L)).thenReturn(Optional.of(existingStation));
        when(dictItemRepository.findByIdAndTypeTypeCodeAndItemStatus(12L, DictionaryTypeCode.PLATFORM, DictionaryStatus.ENABLED))
                .thenReturn(Optional.of(platformItem));
        when(stationRepository.existsByPlatformItemIdAndPlatformStationCodeAndIdNot(12L, "YK-7788", 1001L)).thenReturn(false);
        when(stationRepository.save(any(Station.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Long updatedId = commandService.updateStation(1001L, new StationUpdateRequest(
                "雨花城际快充港（更新）",
                12L,
                "YK-7788",
                "长沙市雨花区城际大道南段 99 号",
                "李工",
                "13800138111"
        ));

        assertEquals(1001L, updatedId);
        verify(stationRepository).save(argThat(station ->
                Long.valueOf(1001L).equals(station.getId())
                        && "ST-202608-0001".equals(station.getStationCode())
                        && "雨花城际快充港（更新）".equals(station.getStationName())
                        && Long.valueOf(12L).equals(station.getPlatformItemId())
                        && "YK-7788".equals(station.getPlatformStationCode())
                        && Long.valueOf(21L).equals(station.getStationStatusItemId())
        ));
    }

    @Test
    void updateStationShouldRejectDuplicatedPlatformStationCodeWithinSamePlatform() {
        StationRepository stationRepository = org.mockito.Mockito.mock(StationRepository.class);
        StationCodeGenerator stationCodeGenerator = org.mockito.Mockito.mock(StationCodeGenerator.class);
        SystemDictItemRepository dictItemRepository = org.mockito.Mockito.mock(SystemDictItemRepository.class);
        StationCommandService commandService = new StationCommandService(
                stationRepository,
                stationCodeGenerator,
                dictItemRepository
        );

        Station existingStation = new Station();
        existingStation.setStationCode("ST-202608-0001");
        existingStation.setStationName("雨花城际快充港");
        existingStation.setPlatformItemId(11L);
        existingStation.setStationStatusItemId(21L);
        setStationId(existingStation, 1001L);

        SystemDictItem platformItem = createDictItem(11L, DictionaryTypeCode.PLATFORM, "星络平台");

        when(stationRepository.findById(1001L)).thenReturn(Optional.of(existingStation));
        when(dictItemRepository.findByIdAndTypeTypeCodeAndItemStatus(11L, DictionaryTypeCode.PLATFORM, DictionaryStatus.ENABLED))
                .thenReturn(Optional.of(platformItem));
        when(stationRepository.existsByPlatformItemIdAndPlatformStationCodeAndIdNot(11L, "XL-4301-889", 1001L)).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> commandService.updateStation(1001L, new StationUpdateRequest(
                        "雨花城际快充港",
                        11L,
                        "XL-4301-889",
                        "长沙市雨花区城际大道南段",
                        "张海峰",
                        "13800138000"
                ))
        );

        assertEquals("STATION_PLATFORM_CODE_DUPLICATED", exception.getCode());
        assertEquals("同一平台下的平台站点编号不能重复", exception.getMessage());
    }

    private void setStationId(Station station, Long id) {
        try {
            java.lang.reflect.Field idField = Station.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(station, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("测试数据构造失败", exception);
        }
    }

    private SystemDictItem createDictItem(Long id, DictionaryTypeCode typeCode, String itemName) {
        SystemDictType type = new SystemDictType();
        type.setTypeCode(typeCode);
        type.setTypeName(typeCode.name());
        type.setTypeStatus(DictionaryStatus.ENABLED);
        type.setSortOrder(10);

        SystemDictItem item = new SystemDictItem();
        item.setType(type);
        item.setItemCode(itemName);
        item.setItemName(itemName);
        item.setItemStatus(DictionaryStatus.ENABLED);
        item.setSortOrder(10);
        try {
            java.lang.reflect.Field idField = SystemDictItem.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(item, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("测试数据构造失败", exception);
        }
        return item;
    }
}
