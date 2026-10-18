package com.changsha.zhongdian.shareholder.repository;

import java.util.Collection;
import java.util.List;

import com.changsha.zhongdian.shareholder.entity.Shareholder;
import com.changsha.zhongdian.shareholder.entity.ShareholderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareholderRepository extends JpaRepository<Shareholder, Long> {

    // 股东名称为业务主键：手工新增与批量导入都依赖该查询做名称查重（数据库唯一索引为兜底）
    boolean existsByShareholderName(String shareholderName);

    // 批量导入存在性校验：一次 IN 查询拉取文件内所有名称的已有记录，避免逐名查询
    List<Shareholder> findByShareholderNameIn(Collection<String> shareholderNames);

    List<Shareholder> findAllByOrderByUpdatedAtDesc();

    List<Shareholder> findAllByShareholderNameContainingOrderByUpdatedAtDesc(String shareholderName);

    List<Shareholder> findAllByShareholderStatusOrderByUpdatedAtDesc(ShareholderStatus shareholderStatus);

    List<Shareholder> findAllByShareholderNameContainingAndShareholderStatusOrderByUpdatedAtDesc(
            String shareholderName,
            ShareholderStatus shareholderStatus
    );

    // 关联股东候选列表按名称升序：选择场景按名称查找股东，不按更新时间排序
    List<Shareholder> findAllByShareholderStatusOrderByShareholderNameAsc(ShareholderStatus shareholderStatus);

    long countByShareholderStatus(ShareholderStatus shareholderStatus);
}