package com.changsha.zhongdian.pile.service;

import java.util.List;

import com.changsha.zhongdian.dictionary.entity.DictionaryStatus;
import com.changsha.zhongdian.dictionary.entity.DictionaryTypeCode;
import com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository;
import com.changsha.zhongdian.pile.dto.PileStatusOptionResponse;
import org.springframework.stereotype.Service;

@Service
public class PileFormOptionQueryService {

    private final SystemDictItemRepository itemRepository;

    public PileFormOptionQueryService(SystemDictItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public List<PileStatusOptionResponse> listStatusOptions() {
        // 充电桩状态下拉统一取 PILE_STATUS 启用字典项，保证与站点状态下拉同一数据口径
        return itemRepository.findAllByTypeTypeCodeAndItemStatusOrderBySortOrderAscItemNameAsc(
                        DictionaryTypeCode.PILE_STATUS,
                        DictionaryStatus.ENABLED
                ).stream()
                .map(item -> new PileStatusOptionResponse(
                        item.getId(),
                        item.getItemCode(),
                        item.getItemName()
                ))
                .toList();
    }
}
