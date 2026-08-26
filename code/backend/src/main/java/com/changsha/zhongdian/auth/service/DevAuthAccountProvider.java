package com.changsha.zhongdian.auth.service;

import java.util.Map;
import java.util.Optional;

import com.changsha.zhongdian.auth.model.LoginUser;
import org.springframework.stereotype.Component;

@Component
public class DevAuthAccountProvider {

    private final Map<String, DevAccount> accounts = Map.of(
            "admin", new DevAccount("admin", "123456", "系统管理员", "SYSTEM_ADMIN"),
            // 为什么：里程碑 A 先用开发期内置账号打通完整鉴权链路，里程碑 B 再切换到数据库用户表。
            "13800138000", new DevAccount("13800138000", "123456", "业务管理员", "BUSINESS_ADMIN")
    );

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
