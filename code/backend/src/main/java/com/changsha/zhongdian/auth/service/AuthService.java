package com.changsha.zhongdian.auth.service;

import com.changsha.zhongdian.auth.dto.ChangePasswordRequest;
import com.changsha.zhongdian.auth.dto.LoginRequest;
import com.changsha.zhongdian.auth.dto.LoginResponse;
import com.changsha.zhongdian.auth.model.LoginUser;
import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.user.entity.SystemUser;
import com.changsha.zhongdian.user.repository.SystemUserRepository;
import com.changsha.zhongdian.user.service.PasswordHasher;
import com.changsha.zhongdian.user.service.SystemUserAuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public static final String CURRENT_USER_REQUEST_ATTRIBUTE = "currentLoginUser";
    private static final String CHANGE_PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)\\S{6,20}$";

    private final SystemUserAuthenticationService systemUserAuthenticationService;
    private final TokenService tokenService;
    private final ObjectProvider<SystemUserRepository> systemUserRepositoryProvider;
    private final PasswordHasher passwordHasher;

    public AuthService(
            SystemUserAuthenticationService systemUserAuthenticationService,
            TokenService tokenService,
            ObjectProvider<SystemUserRepository> systemUserRepositoryProvider,
            PasswordHasher passwordHasher
    ) {
        this.systemUserAuthenticationService = systemUserAuthenticationService;
        this.tokenService = tokenService;
        this.systemUserRepositoryProvider = systemUserRepositoryProvider;
        this.passwordHasher = passwordHasher;
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

    public void changePassword(HttpServletRequest request, ChangePasswordRequest changePasswordRequest) {
        LoginUser currentUser = getCurrentUser(request);
        SystemUserRepository systemUserRepository = systemUserRepositoryProvider.getIfAvailable();
        if (systemUserRepository == null) {
            throw new BusinessException("USER_PASSWORD_UPDATE_FAILED", "当前数据库不可用，暂时无法修改密码", HttpStatus.SERVICE_UNAVAILABLE);
        }

        String currentPassword = requireNonBlank(changePasswordRequest.currentPassword(), "请输入旧密码");
        String newPassword = requireNonBlank(changePasswordRequest.newPassword(), "请输入新密码");
        String confirmPassword = requireNonBlank(changePasswordRequest.confirmPassword(), "请输入确认新密码");

        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException("VALIDATION_ERROR", "确认密码与新密码不一致", HttpStatus.BAD_REQUEST);
        }
        if (newPassword.equals(currentPassword)) {
            throw new BusinessException("VALIDATION_ERROR", "新密码不能与旧密码相同", HttpStatus.BAD_REQUEST);
        }
        if (!newPassword.matches(CHANGE_PASSWORD_PATTERN)) {
            throw new BusinessException("VALIDATION_ERROR", "请输入6-20位且包含字母和数字的新密码", HttpStatus.BAD_REQUEST);
        }

        SystemUser systemUser = systemUserRepository.findByAccount(currentUser.account())
                .orElseThrow(() -> new BusinessException("UNAUTHORIZED", "登录状态失效，请重新登录", HttpStatus.UNAUTHORIZED));

        if (!passwordHasher.matches(currentPassword, systemUser.getPasswordHash())) {
            throw new BusinessException("AUTH_INVALID_CREDENTIALS", "旧密码不正确", HttpStatus.UNAUTHORIZED);
        }

        systemUser.setPasswordHash(passwordHasher.hash(newPassword));
        systemUserRepository.save(systemUser);
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
        }
        return value.trim();
    }
}
