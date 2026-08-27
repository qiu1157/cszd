package com.changsha.zhongdian.dictionary.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.changsha.zhongdian.dictionary.entity.DictionaryTypeCode;
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
            int size = 0;
            for (Object ignored : items) {
                size++;
            }
            assertEquals(5, size);
            return true;
        }));
    }
}
