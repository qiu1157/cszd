package com.changsha.zhongdian.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.changsha.zhongdian.auth.dto.ChangePasswordRequest;
import com.changsha.zhongdian.auth.model.LoginUser;
import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.user.entity.SystemUser;
import com.changsha.zhongdian.user.entity.UserRole;
import com.changsha.zhongdian.user.entity.UserStatus;
import com.changsha.zhongdian.user.repository.SystemUserRepository;
import com.changsha.zhongdian.user.service.PasswordHasher;
import com.changsha.zhongdian.user.service.SystemUserAuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

class AuthServiceTest {

    @Test
    void changePasswordShouldRejectWhenCurrentPasswordIsIncorrect() {
        SystemUser systemUser = new SystemUser();
        systemUser.setAccount("13800000001");
        systemUser.setName("李丹");
        systemUser.setPasswordHash("stored-hash");
        systemUser.setPhone("13800000001");
        systemUser.setRoleCode(UserRole.BUSINESS_ADMIN);
        systemUser.setUserStatus(UserStatus.ENABLED);

        @SuppressWarnings("unchecked")
        ObjectProvider<SystemUserRepository> repositoryProvider = org.mockito.Mockito.mock(ObjectProvider.class);
        SystemUserRepository userRepository = org.mockito.Mockito.mock(SystemUserRepository.class);
        PasswordHasher passwordHasher = org.mockito.Mockito.mock(PasswordHasher.class);
        SystemUserAuthenticationService authenticationService = org.mockito.Mockito.mock(SystemUserAuthenticationService.class);
        TokenService tokenService = org.mockito.Mockito.mock(TokenService.class);
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);

        when(repositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(userRepository.findByAccount("13800000001")).thenReturn(Optional.of(systemUser));
        when(passwordHasher.matches("wrong-password", "stored-hash")).thenReturn(false);
        when(request.getAttribute(AuthService.CURRENT_USER_REQUEST_ATTRIBUTE)).thenReturn(new LoginUser(
                "13800000001",
                "李丹",
                "13800000001",
                "BUSINESS_ADMIN",
                "ENABLED",
                null
        ));

        AuthService authService = new AuthService(authenticationService, tokenService, repositoryProvider, passwordHasher);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.changePassword(request, new ChangePasswordRequest("wrong-password", "Reset123", "Reset123"))
        );

        assertEquals("AUTH_INVALID_CREDENTIALS", exception.getCode());
        assertEquals("旧密码不正确", exception.getMessage());
    }

    @Test
    void changePasswordShouldHashAndPersistNewPassword() {
        SystemUser systemUser = new SystemUser();
        systemUser.setAccount("admin");
        systemUser.setName("系统管理员");
        systemUser.setPasswordHash("stored-hash");
        systemUser.setRoleCode(UserRole.SYSTEM_ADMIN);
        systemUser.setUserStatus(UserStatus.ENABLED);

        @SuppressWarnings("unchecked")
        ObjectProvider<SystemUserRepository> repositoryProvider = org.mockito.Mockito.mock(ObjectProvider.class);
        SystemUserRepository userRepository = org.mockito.Mockito.mock(SystemUserRepository.class);
        PasswordHasher passwordHasher = org.mockito.Mockito.mock(PasswordHasher.class);
        SystemUserAuthenticationService authenticationService = org.mockito.Mockito.mock(SystemUserAuthenticationService.class);
        TokenService tokenService = org.mockito.Mockito.mock(TokenService.class);
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);

        when(repositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(userRepository.findByAccount("admin")).thenReturn(Optional.of(systemUser));
        when(passwordHasher.matches("Old123", "stored-hash")).thenReturn(true);
        when(passwordHasher.hash("New123")).thenReturn("new-hash");
        when(request.getAttribute(AuthService.CURRENT_USER_REQUEST_ATTRIBUTE)).thenReturn(new LoginUser(
                "admin",
                "系统管理员",
                null,
                "SYSTEM_ADMIN",
                "ENABLED",
                null
        ));

        AuthService authService = new AuthService(authenticationService, tokenService, repositoryProvider, passwordHasher);

        authService.changePassword(request, new ChangePasswordRequest("Old123", "New123", "New123"));

        assertEquals("new-hash", systemUser.getPasswordHash());
        verify(userRepository).save(systemUser);
    }
}
