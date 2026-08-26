package com.changsha.zhongdian.user.repository;

import java.util.Optional;

import com.changsha.zhongdian.user.entity.SystemUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemUserRepository extends JpaRepository<SystemUser, Long> {

    boolean existsByAccount(String account);

    Optional<SystemUser> findByAccount(String account);
}
