package com.changsha.zhongdian.auth.service;

import com.changsha.zhongdian.auth.dto.LoginRequest;
import com.changsha.zhongdian.auth.dto.LoginResponse;
import com.changsha.zhongdian.auth.model.LoginUser;
import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.user.service.SystemUserAuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public static final String CURRENT_USER_REQUEST_ATTRIBUTE = "currentLoginUser";

    private final SystemUserAuthenticationService systemUserAuthenticationService;
    private final TokenService tokenService;

    public AuthService(
            SystemUserAuthenticationService systemUserAuthenticationService,
            TokenService tokenService
    ) {
        this.systemUserAuthenticationService = systemUserAuthenticationService;
        this.tokenService = tokenService;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        LoginUser loginUser = systemUserAuthenticationService.authenticate(loginRequest.account(), loginRequest.password())
                .orElseThrow(() -> new BusinessException("AUTH_INVALID_CREDENTIALS", "账号或密码错误", HttpStatus.UNAUTHORIZED));

        String token = tokenService.issueToken(loginUser);
        return new LoginResponse(token, loginUser);
    }

    public LoginUser getCurrentUser(HttpServletRequest request) {
        Object currentUser = request.getAttribute(CURRENT_USER_REQUEST_ATTRIBUTE);
        if (currentUser instanceof LoginUser loginUser) {
            return loginUser;
        }

        throw new BusinessException("UNAUTHORIZED", "登录状态失效，请重新登录", HttpStatus.UNAUTHORIZED);
    }
}
