package com.changsha.zhongdian.dictionary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.dictionary.entity.DictionaryStatus;
import com.changsha.zhongdian.dictionary.entity.DictionaryTypeCode;
import com.changsha.zhongdian.dictionary.entity.SystemDictItem;
import com.changsha.zhongdian.dictionary.entity.SystemDictType;
import com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository;
import com.changsha.zhongdian.dictionary.repository.SystemDictTypeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;

class SystemDictionaryCommandServiceTest {

    @Test
    void createItemShouldPersistNormalizedFieldsAndDefaultSortOrder() {
        SystemDictTypeRepository typeRepository = org.mockito.Mockito.mock(SystemDictTypeRepository.class);
        SystemDictItemRepository itemRepository = org.mockito.Mockito.mock(SystemDictItemRepository.class);
        SystemDictionaryCommandService commandService = new SystemDictionaryCommandService(typeRepository, itemRepository);

        SystemDictType platformType = createType(DictionaryTypeCode.PLATFORM);
        when(typeRepository.findByTypeCode(DictionaryTypeCode.PLATFORM)).thenReturn(Optional.of(platformType));

        commandService.createItem("PLATFORM", "  STATE_GRID  ", "  国网  ", "ENABLED", null);

        ArgumentCaptor<SystemDictItem> itemCaptor = ArgumentCaptor.forClass(SystemDictItem.class);
        verify(itemRepository).save(itemCaptor.capture());

        SystemDictItem savedItem = itemCaptor.getValue();
        assertEquals(platformType, savedItem.getType());
        assertEquals("STATE_GRID", savedItem.getItemCode());
        assertEquals("国网", savedItem.getItemName());
        assertEquals(DictionaryStatus.ENABLED, savedItem.getItemStatus());
        assertEquals(0, savedItem.getSortOrder());
    }

    @Test
    void updateItemShouldModifyExistingItemWithinCurrentType() {
        SystemDictTypeRepository typeRepository = org.mockito.Mockito.mock(SystemDictTypeRepository.class);
        SystemDictItemRepository itemRepository = org.mockito.Mockito.mock(SystemDictItemRepository.class);
        SystemDictionaryCommandService commandService = new SystemDictionaryCommandService(typeRepository, itemRepository);

        SystemDictType platformType = createType(DictionaryTypeCode.PLATFORM);
        SystemDictItem existingItem = new SystemDictItem();
        existingItem.setType(platformType);
        existingItem.setItemCode("STATE_GRID");
        existingItem.setItemName("国网");
        existingItem.setItemStatus(DictionaryStatus.ENABLED);
        existingItem.setSortOrder(10);

        when(typeRepository.findByTypeCode(DictionaryTypeCode.PLATFORM)).thenReturn(Optional.of(platformType));
        when(itemRepository.findByIdAndType(8L, platformType)).thenReturn(Optional.of(existingItem));

        commandService.updateItem(8L, "PLATFORM", "  HN_GRID  ", "  湖南电网  ", "DISABLED", 25);

        assertEquals("HN_GRID", existingItem.getItemCode());
        assertEquals("湖南电网", existingItem.getItemName());
        assertEquals(DictionaryStatus.DISABLED, existingItem.getItemStatus());
        assertEquals(25, existingItem.getSortOrder());
        verify(itemRepository).save(existingItem);
    }

    @Test
    void createItemShouldRejectInvalidStatus() {
        SystemDictTypeRepository typeRepository = org.mockito.Mockito.mock(SystemDictTypeRepository.class);
        SystemDictItemRepository itemRepository = org.mockito.Mockito.mock(SystemDictItemRepository.class);
        SystemDictionaryCommandService commandService = new SystemDictionaryCommandService(typeRepository, itemRepository);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> commandService.createItem("PLATFORM", "STATE_GRID", "国网", "ACTIVE", 10)
        );

        assertEquals("VALIDATION_ERROR", exception.getCode());
        assertEquals("状态值不合法", exception.getMessage());
    }

    @Test
    void updateItemShouldRejectUnknownItem() {
        SystemDictTypeRepository typeRepository = org.mockito.Mockito.mock(SystemDictTypeRepository.class);
        SystemDictItemRepository itemRepository = org.mockito.Mockito.mock(SystemDictItemRepository.class);
        SystemDictionaryCommandService commandService = new SystemDictionaryCommandService(typeRepository, itemRepository);

        SystemDictType platformType = createType(DictionaryTypeCode.PLATFORM);
        when(typeRepository.findByTypeCode(DictionaryTypeCode.PLATFORM)).thenReturn(Optional.of(platformType));
        when(itemRepository.findByIdAndType(12L, platformType)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> commandService.updateItem(12L, "PLATFORM", "STATE_GRID", "国网", "ENABLED", 0)
        );

        assertEquals("DICT_ITEM_NOT_FOUND", exception.getCode());
        assertEquals("字典项不存在", exception.getMessage());
    }

    @Test
    void createItemShouldReportConflictWhenCodeOrNameDuplicated() {
        SystemDictTypeRepository typeRepository = org.mockito.Mockito.mock(SystemDictTypeRepository.class);
        SystemDictItemRepository itemRepository = org.mockito.Mockito.mock(SystemDictItemRepository.class);
        SystemDictionaryCommandService commandService = new SystemDictionaryCommandService(typeRepository, itemRepository);

        SystemDictType platformType = createType(DictionaryTypeCode.PLATFORM);
        when(typeRepository.findByTypeCode(DictionaryTypeCode.PLATFORM)).thenReturn(Optional.of(platformType));
        when(itemRepository.save(org.mockito.ArgumentMatchers.any(SystemDictItem.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> commandService.createItem("PLATFORM", "STATE_GRID", "国网", "ENABLED", 0)
        );

        assertEquals("DICT_ITEM_DUPLICATED", exception.getCode());
        assertEquals("同一字典类型下编码或名称已存在", exception.getMessage());
    }

    @Test
    void updateItemShouldReportReadableMessageWhenDatabaseUnavailable() {
        SystemDictTypeRepository typeRepository = org.mockito.Mockito.mock(SystemDictTypeRepository.class);
        SystemDictItemRepository itemRepository = org.mockito.Mockito.mock(SystemDictItemRepository.class);
        SystemDictionaryCommandService commandService = new SystemDictionaryCommandService(typeRepository, itemRepository);

        SystemDictType platformType = createType(DictionaryTypeCode.PLATFORM);
        SystemDictItem existingItem = new SystemDictItem();
        existingItem.setType(platformType);
        existingItem.setItemCode("STATE_GRID");
        existingItem.setItemName("国网");
        existingItem.setItemStatus(DictionaryStatus.ENABLED);
        existingItem.setSortOrder(10);

        when(typeRepository.findByTypeCode(DictionaryTypeCode.PLATFORM)).thenReturn(Optional.of(platformType));
        when(itemRepository.findByIdAndType(9L, platformType)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(existingItem)).thenThrow(new DataAccessResourceFailureException("db down"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> commandService.updateItem(9L, "PLATFORM", "STATE_GRID", "国网", "ENABLED", 0)
        );

        assertEquals("DICT_ITEM_SAVE_FAILED", exception.getCode());
        assertEquals("当前数据库不可用，暂时无法保存字典项", exception.getMessage());
    }

    private SystemDictType createType(DictionaryTypeCode typeCode) {
        SystemDictType type = new SystemDictType();
        type.setTypeCode(typeCode);
        type.setTypeName("平台");
        type.setTypeStatus(DictionaryStatus.ENABLED);
        type.setSortOrder(10);
        return type;
    }
}
