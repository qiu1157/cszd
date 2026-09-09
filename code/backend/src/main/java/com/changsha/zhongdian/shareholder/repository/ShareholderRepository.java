package com.changsha.zhongdian.shareholder.repository;

import java.util.List;

import com.changsha.zhongdian.shareholder.entity.Shareholder;
import com.changsha.zhongdian.shareholder.entity.ShareholderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareholderRepository extends JpaRepository<Shareholder, Long> {

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