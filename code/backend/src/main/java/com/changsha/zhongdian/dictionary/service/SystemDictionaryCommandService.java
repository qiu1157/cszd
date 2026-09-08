package com.changsha.zhongdian.dictionary.service;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.dictionary.entity.DictionaryStatus;
import com.changsha.zhongdian.dictionary.entity.DictionaryTypeCode;
import com.changsha.zhongdian.dictionary.entity.SystemDictItem;
import com.changsha.zhongdian.dictionary.entity.SystemDictType;
import com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository;
import com.changsha.zhongdian.dictionary.repository.SystemDictTypeRepository;
import com.changsha.zhongdian.operationlog.annotation.LogOperation;
import com.changsha.zhongdian.operationlog.entity.OperationTargetType;
import com.changsha.zhongdian.operationlog.entity.OperationType;
import com.changsha.zhongdian.operationlog.util.BeforeStateHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class SystemDictionaryCommandService {

    private final SystemDictTypeRepository typeRepository;
    private final SystemDictItemRepository itemRepository;

    public SystemDictionaryCommandService(
            SystemDictTypeRepository typeRepository,
            SystemDictItemRepository itemRepository
    ) {
        this.typeRepository = typeRepository;
        this.itemRepository = itemRepository;
    }

    @LogOperation(
        targetType = OperationTargetType.SYSTEM_DICT,
        operationType = OperationType.CREATE,
        targetIdSpEL = "#result.itemCode",
        targetNameSpEL = "#result.itemName",
        remarkSpEL = "#result.type.typeName + ' - ' + #result.itemName"
    )
    public SystemDictItem createItem(String typeCode, String itemCode, String itemName, String status, Integer sortOrder) {
        SystemDictType type = findType(typeCode);

        SystemDictItem item = new SystemDictItem();
        item.setType(type);
        item.setItemCode(requireNonBlank(itemCode, "编码不能为空"));
        item.setItemName(requireNonBlank(itemName, "名称不能为空"));
        item.setItemStatus(parseStatus(status));
        item.setSortOrder(normalizeSortOrder(sortOrder));

        return saveItem(item);
    }

    @LogOperation(
        targetType = OperationTargetType.SYSTEM_DICT,
        operationType = OperationType.UPDATE,
        targetIdSpEL = "#result.itemCode",
        targetNameSpEL = "#result.itemName",
        remarkSpEL = "#result.type.typeName + ' - ' + #result.itemName",
        captureBeforeState = true
    )
    public SystemDictItem updateItem(Long itemId, String typeCode, String itemCode, String itemName, String status, Integer sortOrder) {
        SystemDictType type = findType(typeCode);
        SystemDictItem item = itemRepository.findByIdAndType(itemId, type)
                .orElseThrow(() -> new BusinessException("DICT_ITEM_NOT_FOUND", "字典项不存在", HttpStatus.NOT_FOUND));

        BeforeStateHolder.set(cloneDictItem(item));

        item.setItemCode(requireNonBlank(itemCode, "编码不能为空"));
        item.setItemName(requireNonBlank(itemName, "名称不能为空"));
        item.setItemStatus(parseStatus(status));
        item.setSortOrder(normalizeSortOrder(sortOrder));

        return saveItem(item);
    }

    @LogOperation(
        targetType = OperationTargetType.SYSTEM_DICT,
        operationType = OperationType.ENABLE,
        targetIdSpEL = "#result.itemCode",
        targetNameSpEL = "#result.itemName",
        remarkSpEL = "#result.type.typeName + ' - ' + (#status == 'ENABLED' ? '启用' : '停用')"
    )
    public SystemDictItem updateItemStatus(Long itemId, String typeCode, String status) {
        SystemDictType type = findType(typeCode);
        SystemDictItem item = itemRepository.findByIdAndType(itemId, type)
                .orElseThrow(() -> new BusinessException("DICT_ITEM_NOT_FOUND", "字典项不存在", HttpStatus.NOT_FOUND));

        item.setItemStatus(parseStatus(status));
        return saveItem(item);
    }

    private SystemDictType findType(String typeCode) {
        DictionaryTypeCode normalizedTypeCode;
        try {
            normalizedTypeCode = DictionaryTypeCode.valueOf(requireNonBlank(typeCode, "字典类型不能为空"));
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("DICT_TYPE_NOT_FOUND", "字典类型不存在", HttpStatus.NOT_FOUND);
        }

        return typeRepository.findByTypeCode(normalizedTypeCode)
                .orElseThrow(() -> new BusinessException("DICT_TYPE_NOT_FOUND", "字典类型不存在", HttpStatus.NOT_FOUND));
    }

    private DictionaryStatus parseStatus(String status) {
        String normalizedStatus = requireNonBlank(status, "状态不能为空");
        try {
            return DictionaryStatus.valueOf(normalizedStatus);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("VALIDATION_ERROR", "状态值不合法", HttpStatus.BAD_REQUEST);
        }
    }

    private Integer normalizeSortOrder(Integer sortOrder) {
        return sortOrder == null ? 0 : sortOrder;
    }

    private SystemDictItem saveItem(SystemDictItem item) {
        try {
            return itemRepository.save(item);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException("DICT_ITEM_DUPLICATED", "同一字典类型下编码或名称已存在", HttpStatus.CONFLICT);
        } catch (DataAccessException exception) {
            throw new BusinessException("DICT_ITEM_SAVE_FAILED", "当前数据库不可用，暂时无法保存字典项", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
        }
        return value.trim();
    }
    
    private SystemDictItem cloneDictItem(SystemDictItem source) {
        SystemDictItem clone = new SystemDictItem();
        clone.setItemCode(source.getItemCode());
        clone.setItemName(source.getItemName());
        clone.setItemStatus(source.getItemStatus());
        clone.setSortOrder(source.getSortOrder());
        return clone;
    }
}
