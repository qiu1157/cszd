package com.changsha.zhongdian.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import com.changsha.zhongdian.auth.model.LoginUser;
import com.changsha.zhongdian.user.entity.SystemUser;
import com.changsha.zhongdian.user.entity.UserRole;
import com.changsha.zhongdian.user.entity.UserStatus;
import com.changsha.zhongdian.user.repository.SystemUserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;

class SystemUserAuthenticationServiceTest {

    @Test
    void authenticateShouldUpdateLastLoginTimeWhenPasswordMatches() {
        @SuppressWarnings("unchecked")
        ObjectProvider<SystemUserRepository> repositoryProvider = org.mockito.Mockito.mock(ObjectProvider.class);
        SystemUserRepository userRepository = org.mockito.Mockito.mock(SystemUserRepository.class);
        PasswordHasher passwordHasher = org.mockito.Mockito.mock(PasswordHasher.class);
        SystemUser systemUser = new SystemUser();
        systemUser.setAccount("admin");
        systemUser.setName("系统管理员");
        systemUser.setPasswordHash("hashed-password");
        systemUser.setRoleCode(UserRole.SYSTEM_ADMIN);
        systemUser.setUserStatus(UserStatus.ENABLED);

        when(repositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(userRepository.findByAccount("admin")).thenReturn(Optional.of(systemUser));
        when(passwordHasher.matches("Init@123456", "hashed-password")).thenReturn(true);

        SystemUserAuthenticationService authenticationService = new SystemUserAuthenticationService(
                repositoryProvider,
                passwordHasher
        );

        Optional<LoginUser> loginUser = authenticationService.authenticate("admin", "Init@123456");

        assertTrue(loginUser.isPresent());
        assertEquals("admin", loginUser.get().account());
        assertNotNull(systemUser.getLastLoginAt());

        ArgumentCaptor<SystemUser> userCaptor = ArgumentCaptor.forClass(SystemUser.class);
        verify(userRepository).save(userCaptor.capture());
        assertNotNull(userCaptor.getValue().getLastLoginAt());
    }

    @Test
    void authenticateShouldSkipDisabledUser() {
        @SuppressWarnings("unchecked")
        ObjectProvider<SystemUserRepository> repositoryProvider = org.mockito.Mockito.mock(ObjectProvider.class);
        SystemUserRepository userRepository = org.mockito.Mockito.mock(SystemUserRepository.class);
        PasswordHasher passwordHasher = org.mockito.Mockito.mock(PasswordHasher.class);
        SystemUser systemUser = new SystemUser();
        systemUser.setAccount("admin");
        systemUser.setPasswordHash("hashed-password");
        systemUser.setRoleCode(UserRole.SYSTEM_ADMIN);
        systemUser.setUserStatus(UserStatus.DISABLED);
        systemUser.setLastLoginAt(LocalDateTime.of(2026, 8, 1, 8, 0));

        when(repositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(userRepository.findByAccount("admin")).thenReturn(Optional.of(systemUser));

        SystemUserAuthenticationService authenticationService = new SystemUserAuthenticationService(
                repositoryProvider,
                passwordHasher
        );

        Optional<LoginUser> loginUser = authenticationService.authenticate("admin", "Init@123456");

        assertFalse(loginUser.isPresent());
        assertEquals(LocalDateTime.of(2026, 8, 1, 8, 0), systemUser.getLastLoginAt());
        verify(userRepository, org.mockito.Mockito.never()).save(any());
    }
}
