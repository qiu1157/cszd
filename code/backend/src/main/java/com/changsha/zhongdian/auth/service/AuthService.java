package com.changsha.zhongdian.auth.service;

import com.changsha.zhongdian.auth.dto.LoginRequest;
import com.changsha.zhongdian.auth.dto.LoginResponse;
import com.changsha.zhongdian.auth.model.LoginUser;
import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.user.repository.SystemUserRepository;
import com.changsha.zhongdian.user.service.SystemUserAuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public static final String CURRENT_USER_REQUEST_ATTRIBUTE = "currentLoginUser";

    private final SystemUserAuthenticationService systemUserAuthenticationService;
    private final TokenService tokenService;
    private final ObjectProvider<SystemUserRepository> systemUserRepositoryProvider;

    public AuthService(
            SystemUserAuthenticationService systemUserAuthenticationService,
            TokenService tokenService,
            ObjectProvider<SystemUserRepository> systemUserRepositoryProvider
    ) {
        this.systemUserAuthenticationService = systemUserAuthenticationService;
        this.tokenService = tokenService;
        this.systemUserRepositoryProvider = systemUserRepositoryProvider;
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
            SystemUserRepository systemUserRepository = systemUserRepositoryProvider.getIfAvailable();
            if (systemUserRepository == null) {
                return loginUser;
            }

            return systemUserRepository.findByAccount(loginUser.account())
                    .map(systemUser -> new LoginUser(
                            systemUser.getAccount(),
                            systemUser.getName(),
                            systemUser.getPhone(),
                            systemUser.getRoleCode().name(),
                            systemUser.getUserStatus().name(),
                            systemUser.getLastLoginAt()
                    ))
                    .orElse(loginUser);
        }

        throw new BusinessException("UNAUTHORIZED", "登录状态失效，请重新登录", HttpStatus.UNAUTHORIZED);
    }
}
