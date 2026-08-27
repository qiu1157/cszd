package com.changsha.zhongdian.auth.service;

import java.util.Map;
import java.util.Optional;

import com.changsha.zhongdian.auth.model.LoginUser;
public class DevAuthAccountProvider {

    private final Map<String, DevAccount> accounts;

    public DevAuthAccountProvider(String adminAccount, String adminPassword, String businessAccount, String businessPassword) {
        this.accounts = Map.of(
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
