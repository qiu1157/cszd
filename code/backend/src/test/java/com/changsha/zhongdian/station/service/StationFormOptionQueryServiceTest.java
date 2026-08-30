package com.changsha.zhongdian.station.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import com.changsha.zhongdian.dictionary.entity.DictionaryStatus;
import com.changsha.zhongdian.dictionary.entity.DictionaryTypeCode;
import com.changsha.zhongdian.dictionary.entity.SystemDictItem;
import com.changsha.zhongdian.dictionary.entity.SystemDictType;
import com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository;
import com.changsha.zhongdian.station.dto.StationPlatformOptionResponse;
import org.junit.jupiter.api.Test;

class StationFormOptionQueryServiceTest {

    @Test
    void listPlatformOptionsShouldReturnEnabledPlatformItemsOrderedBySortAndName() {
        SystemDictItemRepository itemRepository = org.mockito.Mockito.mock(SystemDictItemRepository.class);
        StationFormOptionQueryService queryService = new StationFormOptionQueryService(itemRepository);

        SystemDictType platformType = new SystemDictType();
        platformType.setTypeCode(DictionaryTypeCode.PLATFORM);

        SystemDictItem enabledItem = new SystemDictItem();
        enabledItem.setType(platformType);
        enabledItem.setItemCode("XL");
        enabledItem.setItemName("星络平台");
        enabledItem.setItemStatus(DictionaryStatus.ENABLED);
        enabledItem.setSortOrder(10);

        when(itemRepository.findAllByTypeTypeCodeAndItemStatusOrderBySortOrderAscItemNameAsc(
                DictionaryTypeCode.PLATFORM,
                DictionaryStatus.ENABLED
        )).thenReturn(List.of(enabledItem));

        assertEquals(List.of(
                new StationPlatformOptionResponse(null, "XL", "星络平台")
        ), queryService.listPlatformOptions());
    }
}
