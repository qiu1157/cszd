package com.changsha.zhongdian.dictionary.repository;

import java.util.List;
import java.util.Optional;

import com.changsha.zhongdian.dictionary.entity.DictionaryStatus;
import com.changsha.zhongdian.dictionary.entity.DictionaryTypeCode;
import com.changsha.zhongdian.dictionary.entity.SystemDictItem;
import com.changsha.zhongdian.dictionary.entity.SystemDictType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemDictItemRepository extends JpaRepository<SystemDictItem, Long> {

    long countByType(SystemDictType type);

    List<SystemDictItem> findAllByTypeOrderBySortOrderAscItemNameAsc(SystemDictType type);

    List<SystemDictItem> findAllByTypeTypeCodeAndItemStatusOrderBySortOrderAscItemNameAsc(
            DictionaryTypeCode typeCode,
            DictionaryStatus itemStatus
    );

    Optional<SystemDictItem> findByIdAndTypeTypeCodeAndItemStatus(
            Long id,
            DictionaryTypeCode typeCode,
            DictionaryStatus itemStatus
    );

    Optional<SystemDictItem> findByTypeTypeCodeAndItemNameAndItemStatus(
            DictionaryTypeCode typeCode,
            String itemName,
            DictionaryStatus itemStatus
    );

    Optional<SystemDictItem> findByIdAndType(Long id, SystemDictType type);
}
