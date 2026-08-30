package com.changsha.zhongdian.station.service;

import java.util.List;

import com.changsha.zhongdian.dictionary.entity.DictionaryStatus;
import com.changsha.zhongdian.dictionary.entity.DictionaryTypeCode;
import com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository;
import com.changsha.zhongdian.station.dto.StationPlatformOptionResponse;
import org.springframework.stereotype.Service;

@Service
public class StationFormOptionQueryService {

    private final SystemDictItemRepository itemRepository;

    public StationFormOptionQueryService(SystemDictItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public List<StationPlatformOptionResponse> listPlatformOptions() {
        return itemRepository.findAllByTypeTypeCodeAndItemStatusOrderBySortOrderAscItemNameAsc(
                        DictionaryTypeCode.PLATFORM,
                        DictionaryStatus.ENABLED
                ).stream()
                .map(item -> new StationPlatformOptionResponse(
                        item.getId(),
                        item.getItemCode(),
                        item.getItemName()
                ))
                .toList();
    }
}
