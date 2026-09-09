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
import com.changsha.zhongdian.station.dto.StationStatusUpdateRequest;
import com.changsha.zhongdian.station.dto.StationUpdateRequest;
import com.changsha.zhongdian.station.entity.Station;
import com.changsha.zhongdian.station.repository.StationRepository;
import com.changsha.zhongdian.station.repository.StationStatusLogRepository;
import org.junit.jupiter.api.Test;

class StationCommandServiceTest {

    @Test
    void createStationShouldGenerateCodeAndUseContractStatusByDefault() {
        StationRepository stationRepository = org.mockito.Mockito.mock(StationRepository.class);
        StationCodeGenerator stationCodeGenerator = org.mockito.Mockito.mock(StationCodeGenerator.class);
        SystemDictItemRepository dictItemRepository = org.mockito.Mockito.mock(SystemDictItemRepository.class);
        StationStatusLogRepository stationStatusLogRepository = org.mockito.Mockito.mock(StationStatusLogRepository.class);
        StationCommandService commandService = new StationCommandService(
                stationRepository,
                stationCodeGenerator,
                dictItemRepository,
                stationStatusLogRepository
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

        Station result = commandService.createStation(new StationCreateRequest(
                "雨花城际快充港",
                11L,
                "XL-4301-889",
                "长沙市雨花区城际大道南段",
                "张海峰",
                "13800138000",
                "0101234567890"
        ));

        assertNotNull(result);
        assertEquals(1001L, result.getId());
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
        StationStatusLogRepository stationStatusLogRepository = org.mockito.Mockito.mock(StationStatusLogRepository.class);
        StationCommandService commandService = new StationCommandService(
                stationRepository,
                stationCodeGenerator,
                dictItemRepository,
                stationStatusLogRepository
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
                        "13800138000",
                        "0101234567890"
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
        StationStatusLogRepository stationStatusLogRepository = org.mockito.Mockito.mock(StationStatusLogRepository.class);
        StationCommandService commandService = new StationCommandService(
                stationRepository,
                stationCodeGenerator,
                dictItemRepository,
                stationStatusLogRepository
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

        Station result = commandService.updateStation(1001L, new StationUpdateRequest(
                "雨花城际快充港（更新）",
                12L,
                "YK-7788",
                "长沙市雨花区城际大道南段 99 号",
                "李工",
                "13800138111",
                "0101234567891"
        ));

        assertEquals(1001L, result.getId());
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
        StationStatusLogRepository stationStatusLogRepository = org.mockito.Mockito.mock(StationStatusLogRepository.class);
        StationCommandService commandService = new StationCommandService(
                stationRepository,
                stationCodeGenerator,
                dictItemRepository,
                stationStatusLogRepository
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
                        "13800138000",
                        "0101234567890"
                ))
        );

        assertEquals("STATION_PLATFORM_CODE_DUPLICATED", exception.getCode());
        assertEquals("同一平台下的平台站点编号不能重复", exception.getMessage());
    }

    @Test
    void updateStationStatusShouldSwitchStatusAndWriteLog() {
        StationRepository stationRepository = org.mockito.Mockito.mock(StationRepository.class);
        StationCodeGenerator stationCodeGenerator = org.mockito.Mockito.mock(StationCodeGenerator.class);
        SystemDictItemRepository dictItemRepository = org.mockito.Mockito.mock(SystemDictItemRepository.class);
        StationStatusLogRepository stationStatusLogRepository = org.mockito.Mockito.mock(StationStatusLogRepository.class);
        StationCommandService commandService = new StationCommandService(
                stationRepository,
                stationCodeGenerator,
                dictItemRepository,
                stationStatusLogRepository
        );

        Station existingStation = new Station();
        existingStation.setStationCode("ST-202608-0001");
        existingStation.setStationName("雨花城际快充港");
        existingStation.setStationStatusItemId(21L);
        setStationId(existingStation, 1001L);

        SystemDictItem targetStatus = createDictItem(22L, DictionaryTypeCode.STATION_STATUS, "运营中");

        when(stationRepository.findById(1001L)).thenReturn(Optional.of(existingStation));
        when(dictItemRepository.findByIdAndTypeTypeCodeAndItemStatus(22L, DictionaryTypeCode.STATION_STATUS, DictionaryStatus.ENABLED))
                .thenReturn(Optional.of(targetStatus));
        when(stationRepository.save(any(Station.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Station result = commandService.updateStationStatus(
                1001L,
                new StationStatusUpdateRequest(22L, " 设备检修完成，恢复运营 "),
                "13800000001"
        );

        assertEquals(1001L, result.getId());
        verify(stationRepository).save(argThat(station ->
                Long.valueOf(1001L).equals(station.getId())
                        && Long.valueOf(22L).equals(station.getStationStatusItemId())
        ));
        verify(stationStatusLogRepository).save(argThat(log ->
                Long.valueOf(1001L).equals(log.getStationId())
                        && Long.valueOf(21L).equals(log.getFromStatusItemId())
                        && Long.valueOf(22L).equals(log.getToStatusItemId())
                        && "设备检修完成，恢复运营".equals(log.getRemark())
                        && "13800000001".equals(log.getOperatorAccount())
        ));
    }

    @Test
    void updateStationStatusShouldAllowSwitchingToDisabledStatus() {
        // 为什么：第一期不提供充电站物理删除，"停用"是站点唯一的下线手段（spec 8.2），
        // 此处防回归确保任意状态均可切换到停用，且停用动作完整落入状态变更日志。
        StationRepository stationRepository = org.mockito.Mockito.mock(StationRepository.class);
        StationCodeGenerator stationCodeGenerator = org.mockito.Mockito.mock(StationCodeGenerator.class);
        SystemDictItemRepository dictItemRepository = org.mockito.Mockito.mock(SystemDictItemRepository.class);
        StationStatusLogRepository stationStatusLogRepository = org.mockito.Mockito.mock(StationStatusLogRepository.class);
        StationCommandService commandService = new StationCommandService(
                stationRepository,
                stationCodeGenerator,
                dictItemRepository,
                stationStatusLogRepository
        );

        Station existingStation = new Station();
        existingStation.setStationCode("ST-202608-0001");
        existingStation.setStationName("雨花城际快充港");
        existingStation.setStationStatusItemId(22L);
        setStationId(existingStation, 1001L);

        SystemDictItem disabledStatus = createDictItem(25L, DictionaryTypeCode.STATION_STATUS, "停用");

        when(stationRepository.findById(1001L)).thenReturn(Optional.of(existingStation));
        when(dictItemRepository.findByIdAndTypeTypeCodeAndItemStatus(25L, DictionaryTypeCode.STATION_STATUS, DictionaryStatus.ENABLED))
                .thenReturn(Optional.of(disabledStatus));
        when(stationRepository.save(any(Station.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Station result = commandService.updateStationStatus(
                1001L,
                new StationStatusUpdateRequest(25L, null),
                "13800000001"
        );

        assertEquals(1001L, result.getId());
        verify(stationRepository).save(argThat(station ->
                Long.valueOf(1001L).equals(station.getId())
                        && Long.valueOf(25L).equals(station.getStationStatusItemId())
        ));
        verify(stationStatusLogRepository).save(argThat(log ->
                Long.valueOf(1001L).equals(log.getStationId())
                        && Long.valueOf(22L).equals(log.getFromStatusItemId())
                        && Long.valueOf(25L).equals(log.getToStatusItemId())
                        && log.getRemark() == null
                        && "13800000001".equals(log.getOperatorAccount())
        ));
    }

    @Test
    void updateStationStatusShouldRejectMissingStation() {
        StationRepository stationRepository = org.mockito.Mockito.mock(StationRepository.class);
        StationCodeGenerator stationCodeGenerator = org.mockito.Mockito.mock(StationCodeGenerator.class);
        SystemDictItemRepository dictItemRepository = org.mockito.Mockito.mock(SystemDictItemRepository.class);
        StationStatusLogRepository stationStatusLogRepository = org.mockito.Mockito.mock(StationStatusLogRepository.class);
        StationCommandService commandService = new StationCommandService(
                stationRepository,
                stationCodeGenerator,
                dictItemRepository,
                stationStatusLogRepository
        );

        when(stationRepository.findById(9999L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> commandService.updateStationStatus(9999L, new StationStatusUpdateRequest(22L, null), "13800000001")
        );

        assertEquals("STATION_NOT_FOUND", exception.getCode());
        assertEquals("充电站不存在", exception.getMessage());
    }

    @Test
    void updateStationStatusShouldRejectInvalidTargetStatus() {
        StationRepository stationRepository = org.mockito.Mockito.mock(StationRepository.class);
        StationCodeGenerator stationCodeGenerator = org.mockito.Mockito.mock(StationCodeGenerator.class);
        SystemDictItemRepository dictItemRepository = org.mockito.Mockito.mock(SystemDictItemRepository.class);
        StationStatusLogRepository stationStatusLogRepository = org.mockito.Mockito.mock(StationStatusLogRepository.class);
        StationCommandService commandService = new StationCommandService(
                stationRepository,
                stationCodeGenerator,
                dictItemRepository,
                stationStatusLogRepository
        );

        Station existingStation = new Station();
        existingStation.setStationCode("ST-202608-0001");
        existingStation.setStationStatusItemId(21L);
        setStationId(existingStation, 1001L);

        when(stationRepository.findById(1001L)).thenReturn(Optional.of(existingStation));
        when(dictItemRepository.findByIdAndTypeTypeCodeAndItemStatus(99L, DictionaryTypeCode.STATION_STATUS, DictionaryStatus.ENABLED))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> commandService.updateStationStatus(1001L, new StationStatusUpdateRequest(99L, null), "13800000001")
        );

        assertEquals("STATION_STATUS_NOT_FOUND", exception.getCode());
        assertEquals("目标状态不存在或已停用", exception.getMessage());
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
