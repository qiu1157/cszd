package com.changsha.zhongdian.auth.service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.changsha.zhongdian.auth.model.LoginUser;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final Map<String, LoginUser> tokenStore = new ConcurrentHashMap<>();

    public String issueToken(LoginUser loginUser) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(token, loginUser);
        return token;
    }

    public Optional<LoginUser> getLoginUser(String token) {
        return Optional.ofNullable(tokenStore.get(token));
    }
}
