package com.changsha.zhongdian.user.service;

import java.util.Optional;

import com.changsha.zhongdian.auth.model.LoginUser;
import com.changsha.zhongdian.user.entity.SystemUser;
import com.changsha.zhongdian.user.entity.UserStatus;
import com.changsha.zhongdian.user.repository.SystemUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class SystemUserAuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(SystemUserAuthenticationService.class);

    private final ObjectProvider<SystemUserRepository> systemUserRepositoryProvider;
    private final PasswordHasher passwordHasher;

    public SystemUserAuthenticationService(
            ObjectProvider<SystemUserRepository> systemUserRepositoryProvider,
            PasswordHasher passwordHasher
    ) {
        this.systemUserRepositoryProvider = systemUserRepositoryProvider;
        this.passwordHasher = passwordHasher;
    }

    public Optional<LoginUser> authenticate(String account, String password) {
        SystemUserRepository systemUserRepository = systemUserRepositoryProvider.getIfAvailable();
        if (systemUserRepository == null) {
            return Optional.empty();
        }

        try {
            return systemUserRepository.findByAccount(account)
                    // 为什么：停用账号后必须立即阻断登录，避免仅做前端菜单隐藏导致越权。
                    .filter(user -> user.getUserStatus() == UserStatus.ENABLED)
                    .filter(user -> passwordHasher.matches(password, user.getPasswordHash()))
                    .map(this::toLoginUser);
        } catch (DataAccessException exception) {
            // 为什么：当前阶段仍保留“数据库不可用时后端可启动”的容错策略；
            // 这里降级为返回空，避免 DB 暂不可达时连开发期业务管理员账号也无法登录。
            log.warn("读取系统用户失败，已跳过数据库认证：{}", exception.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    private LoginUser toLoginUser(SystemUser systemUser) {
        return new LoginUser(
                systemUser.getAccount(),
                systemUser.getName(),
                systemUser.getRoleCode().name()
        );
    }
}
