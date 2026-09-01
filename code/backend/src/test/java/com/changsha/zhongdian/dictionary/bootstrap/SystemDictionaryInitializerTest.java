package com.changsha.zhongdian.dictionary.bootstrap;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.changsha.zhongdian.dictionary.entity.DictionaryTypeCode;
import com.changsha.zhongdian.dictionary.entity.SystemDictItem;
import com.changsha.zhongdian.dictionary.entity.SystemDictType;
import com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository;
import com.changsha.zhongdian.dictionary.repository.SystemDictTypeRepository;
import org.junit.jupiter.api.Test;

class SystemDictionaryInitializerTest {

    @Test
    void initializeShouldCreateBuiltinTypeWhenMissing() {
        SystemDictTypeRepository typeRepository = org.mockito.Mockito.mock(SystemDictTypeRepository.class);
        SystemDictItemRepository itemRepository = org.mockito.Mockito.mock(SystemDictItemRepository.class);
        when(typeRepository.findByTypeCode(DictionaryTypeCode.PLATFORM)).thenReturn(Optional.empty());

        SystemDictionaryInitializer initializer = new SystemDictionaryInitializer(typeRepository, itemRepository);

        initializer.initializeSystemDictionary();

        verify(typeRepository).save(org.mockito.ArgumentMatchers.argThat(type ->
                type.getTypeCode() == DictionaryTypeCode.PLATFORM && "平台".equals(type.getTypeName())
        ));
    }

    @Test
    void initializeShouldCreateBuiltinStationStatusItems() {
        SystemDictType stationStatusType = new SystemDictType();
        stationStatusType.setTypeCode(DictionaryTypeCode.STATION_STATUS);
        stationStatusType.setTypeName("充电站状态");

        SystemDictTypeRepository typeRepository = org.mockito.Mockito.mock(SystemDictTypeRepository.class);
        SystemDictItemRepository itemRepository = org.mockito.Mockito.mock(SystemDictItemRepository.class);

        when(typeRepository.findByTypeCode(DictionaryTypeCode.STATION_STATUS)).thenReturn(Optional.of(stationStatusType));
        when(itemRepository.countByType(stationStatusType)).thenReturn(0L);

        SystemDictionaryInitializer initializer = new SystemDictionaryInitializer(typeRepository, itemRepository);

        initializer.initializeSystemDictionary();

        verify(itemRepository).saveAll(org.mockito.ArgumentMatchers.argThat(items -> {
            // 为什么：初始化器会对充电站状态(5 项)与充电桩状态(3 项)各调一次 saveAll，
            // matcher 会应用到每一次调用；返回布尔值精确过滤目标调用，
            // 若在此处直接断言，非目标调用(3 项)会先抛 AssertionError 使 verify 失败。
            int size = 0;
            boolean containsDisabled = false;
            for (Object item : items) {
                size++;
                if (item instanceof SystemDictItem dictItem && "停用".equals(dictItem.getItemName())) {
                    containsDisabled = true;
                }
            }
            return size == 5 && containsDisabled;
        }));
    }
}
