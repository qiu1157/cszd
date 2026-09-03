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
}