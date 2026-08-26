package com.changsha.zhongdian.user.bootstrap;

import com.changsha.zhongdian.user.entity.SystemUser;
import com.changsha.zhongdian.user.entity.UserRole;
import com.changsha.zhongdian.user.entity.UserStatus;
import com.changsha.zhongdian.user.repository.SystemUserRepository;
import com.changsha.zhongdian.user.service.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DefaultAdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultAdminInitializer.class);
    private static final String DEFAULT_ADMIN_ACCOUNT = "admin";

    private final ObjectProvider<SystemUserRepository> systemUserRepositoryProvider;
    private final PasswordHasher passwordHasher;
    private final String defaultAdminPassword;

    public DefaultAdminInitializer(
            ObjectProvider<SystemUserRepository> systemUserRepositoryProvider,
            PasswordHasher passwordHasher,
            @Value("${CSZD_DEFAULT_ADMIN_PASSWORD:}") String defaultAdminPassword
    ) {
        this.systemUserRepositoryProvider = systemUserRepositoryProvider;
        this.passwordHasher = passwordHasher;
        this.defaultAdminPassword = defaultAdminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        initializeDefaultAdmin();
    }

    void initializeDefaultAdmin() {
        if (!StringUtils.hasText(defaultAdminPassword)) {
            log.warn("未检测到 CSZD_DEFAULT_ADMIN_PASSWORD，已跳过默认系统管理员初始化");
            return;
        }

        SystemUserRepository systemUserRepository = systemUserRepositoryProvider.getIfAvailable();
        if (systemUserRepository == null) {
            log.warn("当前未启用系统用户仓库，已跳过默认系统管理员初始化");
            return;
        }

        try {
            if (systemUserRepository.existsByAccount(DEFAULT_ADMIN_ACCOUNT)) {
                log.info("默认系统管理员账号已存在，跳过初始化");
                return;
            }

            SystemUser defaultAdmin = new SystemUser();
            defaultAdmin.setAccount(DEFAULT_ADMIN_ACCOUNT);
            defaultAdmin.setName("系统管理员");
            defaultAdmin.setPasswordHash(passwordHasher.hash(defaultAdminPassword));
            defaultAdmin.setRoleCode(UserRole.SYSTEM_ADMIN);
            defaultAdmin.setUserStatus(UserStatus.ENABLED);
            defaultAdmin.setDefaultAdmin(true);

            systemUserRepository.save(defaultAdmin);
            log.info("已初始化默认系统管理员账号：admin");
        } catch (DataAccessException exception) {
            // 为什么：项目当前仍要求数据库异常时后端至少可启动并返回健康检查，
            // 因此初始化失败时只记录告警，不直接中断服务启动。
            log.warn("初始化默认系统管理员失败，已跳过本次初始化：{}", exception.getClass().getSimpleName());
        }
    }
}
