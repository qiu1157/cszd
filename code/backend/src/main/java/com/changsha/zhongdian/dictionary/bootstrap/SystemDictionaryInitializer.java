package com.changsha.zhongdian.dictionary.bootstrap;

import java.util.List;
import java.util.Optional;

import com.changsha.zhongdian.dictionary.entity.DictionaryStatus;
import com.changsha.zhongdian.dictionary.entity.DictionaryTypeCode;
import com.changsha.zhongdian.dictionary.entity.SystemDictItem;
import com.changsha.zhongdian.dictionary.entity.SystemDictType;
import com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository;
import com.changsha.zhongdian.dictionary.repository.SystemDictTypeRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SystemDictionaryInitializer implements ApplicationRunner {

    private final SystemDictTypeRepository typeRepository;
    private final SystemDictItemRepository itemRepository;

    public SystemDictionaryInitializer(
            SystemDictTypeRepository typeRepository,
            SystemDictItemRepository itemRepository
    ) {
        this.typeRepository = typeRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        initializeSystemDictionary();
    }

    void initializeSystemDictionary() {
        SystemDictType platformType = ensureType(DictionaryTypeCode.PLATFORM, "平台", 10);
        SystemDictType stationStatusType = ensureType(DictionaryTypeCode.STATION_STATUS, "充电站状态", 20);
        SystemDictType pileStatusType = ensureType(DictionaryTypeCode.PILE_STATUS, "充电桩状态", 30);

        if (itemRepository.countByType(platformType) == 0L) {
            // 平台数据允许为空，由系统管理员后续维护。
        }
        if (itemRepository.countByType(stationStatusType) == 0L) {
            itemRepository.saveAll(List.of(
                    createItem(stationStatusType, "CONTRACT_SIGNED", "签合同", 10),
                    createItem(stationStatusType, "UNDER_CONSTRUCTION", "建设中", 20),
                    createItem(stationStatusType, "OPERATING", "运营中", 30),
                    createItem(stationStatusType, "OPERATING_EXCEPTION", "运营异常", 40),
                    createItem(stationStatusType, "DISABLED", "停用", 50)
            ));
        }
        if (itemRepository.countByType(pileStatusType) == 0L) {
            itemRepository.saveAll(List.of(
                    createItem(pileStatusType, "OPERATING", "运营中", 10),
                    createItem(pileStatusType, "OPERATING_EXCEPTION", "运营异常", 20),
                    createItem(pileStatusType, "DISABLED", "停用", 30)
            ));
        }
    }

    private SystemDictType ensureType(DictionaryTypeCode typeCode, String typeName, int sortOrder) {
        Optional<SystemDictType> existingType = safeFindType(typeCode);
        return existingType.orElseGet(() -> {
            SystemDictType type = new SystemDictType();
            type.setTypeCode(typeCode);
            type.setTypeName(typeName);
            type.setTypeStatus(DictionaryStatus.ENABLED);
            type.setSortOrder(sortOrder);
            return typeRepository.save(type);
        });
    }

    private Optional<SystemDictType> safeFindType(DictionaryTypeCode typeCode) {
        Optional<SystemDictType> result = typeRepository.findByTypeCode(typeCode);
        return result == null ? Optional.empty() : result;
    }

    private SystemDictItem createItem(SystemDictType type, String itemCode, String itemName, int sortOrder) {
        SystemDictItem item = new SystemDictItem();
        item.setType(type);
        item.setItemCode(itemCode);
        item.setItemName(itemName);
        item.setItemStatus(DictionaryStatus.ENABLED);
        item.setSortOrder(sortOrder);
        return item;
    }
}
