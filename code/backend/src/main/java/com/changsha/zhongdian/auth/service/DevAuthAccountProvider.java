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
            @Value("${CSZD_DEV_ADMIN_PASSWORD:dev-admin-password}") String adminPassword,
            @Value("${CSZD_DEV_BIZ_ACCOUNT:13800000001}") String businessAccount,
            @Value("${CSZD_DEV_BIZ_PASSWORD:dev-biz-password}") String businessPassword
    ) {
        this.accounts = Map.of(
                "admin", new DevAccount("admin", adminPassword, "系统管理员", "SYSTEM_ADMIN"),
                // 为什么：开发期账号可以保留，但密码必须来自本地环境变量或安全占位值，不能在仓库中写真实口令。
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
