package com.changsha.zhongdian.dictionary.service;

import java.util.List;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.dictionary.dto.SystemDictItemListItemResponse;
import com.changsha.zhongdian.dictionary.dto.SystemDictTypeListItemResponse;
import com.changsha.zhongdian.dictionary.entity.DictionaryTypeCode;
import com.changsha.zhongdian.dictionary.entity.SystemDictItem;
import com.changsha.zhongdian.dictionary.entity.SystemDictType;
import com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository;
import com.changsha.zhongdian.dictionary.repository.SystemDictTypeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class SystemDictionaryQueryService {

    private final SystemDictTypeRepository typeRepository;
    private final SystemDictItemRepository itemRepository;

    public SystemDictionaryQueryService(
            SystemDictTypeRepository typeRepository,
            SystemDictItemRepository itemRepository
    ) {
        this.typeRepository = typeRepository;
        this.itemRepository = itemRepository;
    }

    public List<SystemDictTypeListItemResponse> listTypes() {
        return typeRepository.findAllByOrderBySortOrderAscTypeCodeAsc().stream()
                .map(type -> new SystemDictTypeListItemResponse(
                        type.getTypeCode().name(),
                        type.getTypeName(),
                        type.getTypeStatus().name(),
                        type.getSortOrder()
                ))
                .toList();
    }

    public List<SystemDictItemListItemResponse> listItems(String typeCode) {
        DictionaryTypeCode normalizedTypeCode;
        try {
            normalizedTypeCode = DictionaryTypeCode.valueOf(typeCode);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("DICT_TYPE_NOT_FOUND", "字典类型不存在", HttpStatus.NOT_FOUND);
        }

        SystemDictType type = typeRepository.findByTypeCode(normalizedTypeCode)
                .orElseThrow(() -> new BusinessException("DICT_TYPE_NOT_FOUND", "字典类型不存在", HttpStatus.NOT_FOUND));

        return itemRepository.findAllByTypeOrderBySortOrderAscItemNameAsc(type).stream()
                .map(this::toItemResponse)
                .toList();
    }

    private SystemDictItemListItemResponse toItemResponse(SystemDictItem item) {
        return new SystemDictItemListItemResponse(
                item.getId(),
                item.getItemCode(),
                item.getItemName(),
                item.getItemStatus().name(),
                item.getSortOrder(),
                item.getUpdatedAt()
        );
    }
}
