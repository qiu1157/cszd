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
            @Value("${CSZD_DEV_BIZ_ACCOUNT:13800000001}") String businessAccount,
            @Value("${CSZD_DEV_BIZ_PASSWORD:dev-biz-password}") String businessPassword
    ) {
        this.accounts = Map.of(
                // 为什么：Task 2 起，admin 必须由数据库初始化逻辑创建，不能再走内存账号；
                // 这里仅保留开发期业务管理员，避免阻断后续业务模块联调。
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
