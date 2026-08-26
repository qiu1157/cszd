package com.changsha.zhongdian.user.repository;

import java.util.List;
import java.util.Optional;

import com.changsha.zhongdian.user.entity.SystemUser;
import com.changsha.zhongdian.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemUserRepository extends JpaRepository<SystemUser, Long> {

    boolean existsByAccount(String account);

    Optional<SystemUser> findByAccount(String account);

    List<SystemUser> findAllByRoleCodeOrderByUpdatedAtDesc(UserRole roleCode);
}
