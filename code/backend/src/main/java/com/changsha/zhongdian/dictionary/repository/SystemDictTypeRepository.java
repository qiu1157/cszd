package com.changsha.zhongdian.dictionary.repository;

import java.util.List;
import java.util.Optional;

import com.changsha.zhongdian.dictionary.entity.DictionaryTypeCode;
import com.changsha.zhongdian.dictionary.entity.SystemDictType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemDictTypeRepository extends JpaRepository<SystemDictType, Long> {

    Optional<SystemDictType> findByTypeCode(DictionaryTypeCode typeCode);

    List<SystemDictType> findAllByOrderBySortOrderAscTypeCodeAsc();
}
