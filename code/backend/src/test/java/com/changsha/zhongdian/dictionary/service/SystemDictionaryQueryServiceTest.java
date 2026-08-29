package com.changsha.zhongdian.dictionary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.changsha.zhongdian.dictionary.dto.SystemDictItemListItemResponse;
import com.changsha.zhongdian.dictionary.entity.DictionaryStatus;
import com.changsha.zhongdian.dictionary.entity.DictionaryTypeCode;
import com.changsha.zhongdian.dictionary.entity.SystemDictItem;
import com.changsha.zhongdian.dictionary.entity.SystemDictType;
import com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository;
import com.changsha.zhongdian.dictionary.repository.SystemDictTypeRepository;
import org.junit.jupiter.api.Test;

class SystemDictionaryQueryServiceTest {

    @Test
    void listItemsShouldUseSortOrderThenNameOrderingAndExposeItemId() {
        SystemDictTypeRepository typeRepository = org.mockito.Mockito.mock(SystemDictTypeRepository.class);
        SystemDictItemRepository itemRepository = org.mockito.Mockito.mock(SystemDictItemRepository.class);
        SystemDictionaryQueryService queryService = new SystemDictionaryQueryService(typeRepository, itemRepository);

        SystemDictType platformType = new SystemDictType();
        platformType.setTypeCode(DictionaryTypeCode.PLATFORM);
        platformType.setTypeName("平台");
        platformType.setTypeStatus(DictionaryStatus.ENABLED);
        platformType.setSortOrder(10);

        SystemDictItem item = new SystemDictItem();
        item.setType(platformType);
        item.setItemCode("HN_GRID");
        item.setItemName("湖南电网");
        item.setItemStatus(DictionaryStatus.DISABLED);
        item.setSortOrder(20);
        setIdAndUpdatedAt(item, 15L, LocalDateTime.of(2026, 8, 28, 9, 30));

        when(typeRepository.findByTypeCode(DictionaryTypeCode.PLATFORM)).thenReturn(Optional.of(platformType));
        when(itemRepository.findAllByTypeOrderBySortOrderAscItemNameAsc(platformType)).thenReturn(List.of(item));

        List<SystemDictItemListItemResponse> response = queryService.listItems("PLATFORM");

        verify(itemRepository).findAllByTypeOrderBySortOrderAscItemNameAsc(platformType);
        assertEquals(1, response.size());
        assertEquals(15L, response.get(0).id());
        assertEquals("HN_GRID", response.get(0).itemCode());
    }

    private void setIdAndUpdatedAt(SystemDictItem item, Long id, LocalDateTime updatedAt) {
        try {
            java.lang.reflect.Field idField = SystemDictItem.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(item, id);

            java.lang.reflect.Field updatedAtField = SystemDictItem.class.getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(item, updatedAt);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to prepare test data", exception);
        }
    }
}
