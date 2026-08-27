package com.changsha.zhongdian.auth.service;

import java.util.Map;
import java.util.Optional;

import com.changsha.zhongdian.auth.model.LoginUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DevAuthAccountProvider {

    private final Map<String, DevAccount> accounts;

    public DevAuthAccountProvider(
            @Value("${CSZD_DEV_ADMIN_ACCOUNT:admin}") String adminAccount,
            @Value("${CSZD_DEV_ADMIN_PASSWORD:123456}") String adminPassword,
            @Value("${CSZD_DEV_BIZ_ACCOUNT:13800000001}") String businessAccount,
            @Value("${CSZD_DEV_BIZ_PASSWORD:dev-biz-password}") String businessPassword
    ) {
        this.accounts = Map.of(
                // 为什么：当数据库暂不可用时，里程碑 B 的系统管理员入口仍需可用，
                // 以便继续联调用户管理与平台管理页面；数据库恢复后，AuthService 会优先走正式数据库认证。
                adminAccount, new DevAccount(adminAccount, adminPassword, "系统管理员", "SYSTEM_ADMIN"),
                businessAccount, new DevAccount(businessAccount, businessPassword, "业务管理员", "BUSINESS_ADMIN")
        );
    }

    public Optional<LoginUser> validate(String account, String password) {
        DevAccount matchedAccount = accounts.get(account);
        if (matchedAccount == null || !matchedAccount.password().equals(password)) {
            return Optional.empty();
        }

        return Optional.of(new LoginUser(
                matchedAccount.account(),
                matchedAccount.name(),
                matchedAccount.role()
        ));
    }

    private record DevAccount(
            String account,
            String password,
            String name,
            String role
    ) {
    }
}
