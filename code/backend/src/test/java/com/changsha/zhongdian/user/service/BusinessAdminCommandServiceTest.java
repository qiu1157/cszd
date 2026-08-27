package com.changsha.zhongdian.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.user.entity.SystemUser;
import com.changsha.zhongdian.user.entity.UserRole;
import com.changsha.zhongdian.user.entity.UserStatus;
import com.changsha.zhongdian.user.repository.SystemUserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataAccessResourceFailureException;

class BusinessAdminCommandServiceTest {

    @Test
    void createBusinessAdminShouldPersistEnabledUserWithPhoneAsAccount() {
        SystemUserRepository userRepository = org.mockito.Mockito.mock(SystemUserRepository.class);
        PasswordHasher passwordHasher = org.mockito.Mockito.mock(PasswordHasher.class);

        when(passwordHasher.hash("Init@123456")).thenReturn("hashed-password");

        BusinessAdminCommandService commandService = new BusinessAdminCommandService(userRepository, passwordHasher);

        commandService.createBusinessAdmin("13800138000", "李丹", "Init@123456");

        ArgumentCaptor<SystemUser> userCaptor = ArgumentCaptor.forClass(SystemUser.class);
        verify(userRepository).save(userCaptor.capture());

        SystemUser savedUser = userCaptor.getValue();
        assertEquals("13800138000", savedUser.getAccount());
        assertEquals("13800138000", savedUser.getPhone());
        assertEquals("李丹", savedUser.getName());
        assertEquals("hashed-password", savedUser.getPasswordHash());
        assertEquals(UserRole.BUSINESS_ADMIN, savedUser.getRoleCode());
        assertEquals(UserStatus.ENABLED, savedUser.getUserStatus());
        assertNull(savedUser.getLastLoginAt());
    }

    @Test
    void createBusinessAdminShouldRejectBlankPhone() {
        SystemUserRepository userRepository = org.mockito.Mockito.mock(SystemUserRepository.class);
        PasswordHasher passwordHasher = org.mockito.Mockito.mock(PasswordHasher.class);
        BusinessAdminCommandService commandService = new BusinessAdminCommandService(userRepository, passwordHasher);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> commandService.createBusinessAdmin("   ", "李丹", "Init@123456")
        );

        assertEquals("VALIDATION_ERROR", exception.getCode());
        assertEquals("手机号不能为空", exception.getMessage());
    }

    @Test
    void createBusinessAdminShouldRejectInvalidPhoneFormat() {
        SystemUserRepository userRepository = org.mockito.Mockito.mock(SystemUserRepository.class);
        PasswordHasher passwordHasher = org.mockito.Mockito.mock(PasswordHasher.class);
        BusinessAdminCommandService commandService = new BusinessAdminCommandService(userRepository, passwordHasher);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> commandService.createBusinessAdmin("12345", "李丹", "Init@123456")
        );

        assertEquals("VALIDATION_ERROR", exception.getCode());
        assertEquals("请输入正确的手机号", exception.getMessage());
    }

    @Test
    void createBusinessAdminShouldReportReadableMessageWhenDatabaseUnavailable() {
        SystemUserRepository userRepository = org.mockito.Mockito.mock(SystemUserRepository.class);
        PasswordHasher passwordHasher = org.mockito.Mockito.mock(PasswordHasher.class);

        when(passwordHasher.hash("Init@123456")).thenReturn("hashed-password");
        when(userRepository.save(org.mockito.ArgumentMatchers.any(SystemUser.class)))
                .thenThrow(new DataAccessResourceFailureException("db down"));

        BusinessAdminCommandService commandService = new BusinessAdminCommandService(userRepository, passwordHasher);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> commandService.createBusinessAdmin("13800138000", "李丹", "Init@123456")
        );

        assertEquals("USER_SAVE_FAILED", exception.getCode());
        assertEquals("当前数据库不可用，暂时无法新增业务管理员", exception.getMessage());
    }

    @Test
    void updateBusinessAdminStatusShouldDisableBusinessAdmin() {
        SystemUserRepository userRepository = org.mockito.Mockito.mock(SystemUserRepository.class);
        PasswordHasher passwordHasher = org.mockito.Mockito.mock(PasswordHasher.class);
        BusinessAdminCommandService commandService = new BusinessAdminCommandService(userRepository, passwordHasher);

        SystemUser systemUser = new SystemUser();
        systemUser.setAccount("13800138000");
        systemUser.setRoleCode(UserRole.BUSINESS_ADMIN);
        systemUser.setUserStatus(UserStatus.ENABLED);
        when(userRepository.findByAccount("13800138000")).thenReturn(Optional.of(systemUser));

        commandService.updateBusinessAdminStatus("13800138000", UserStatus.DISABLED);

        assertEquals(UserStatus.DISABLED, systemUser.getUserStatus());
        verify(userRepository).save(systemUser);
    }

    @Test
    void updateBusinessAdminStatusShouldEnableBusinessAdmin() {
        SystemUserRepository userRepository = org.mockito.Mockito.mock(SystemUserRepository.class);
        PasswordHasher passwordHasher = org.mockito.Mockito.mock(PasswordHasher.class);
        BusinessAdminCommandService commandService = new BusinessAdminCommandService(userRepository, passwordHasher);

        SystemUser systemUser = new SystemUser();
        systemUser.setAccount("13800139000");
        systemUser.setRoleCode(UserRole.BUSINESS_ADMIN);
        systemUser.setUserStatus(UserStatus.DISABLED);
        when(userRepository.findByAccount("13800139000")).thenReturn(Optional.of(systemUser));

        commandService.updateBusinessAdminStatus("13800139000", UserStatus.ENABLED);

        assertEquals(UserStatus.ENABLED, systemUser.getUserStatus());
        verify(userRepository).save(systemUser);
    }

    @Test
    void resetBusinessAdminPasswordShouldHashAndPersistNewPassword() {
        SystemUserRepository userRepository = org.mockito.Mockito.mock(SystemUserRepository.class);
        PasswordHasher passwordHasher = org.mockito.Mockito.mock(PasswordHasher.class);
        BusinessAdminCommandService commandService = new BusinessAdminCommandService(userRepository, passwordHasher);

        SystemUser systemUser = new SystemUser();
        systemUser.setAccount("13800139000");
        systemUser.setRoleCode(UserRole.BUSINESS_ADMIN);
        systemUser.setPasswordHash("old-hash");
        when(userRepository.findByAccount("13800139000")).thenReturn(Optional.of(systemUser));
        when(passwordHasher.hash("Reset123")).thenReturn("new-hash");

        commandService.resetBusinessAdminPassword("13800139000", "Reset123");

        assertEquals("new-hash", systemUser.getPasswordHash());
        verify(userRepository).save(systemUser);
    }

    @Test
    void resetBusinessAdminPasswordShouldRejectInvalidPasswordFormat() {
        SystemUserRepository userRepository = org.mockito.Mockito.mock(SystemUserRepository.class);
        PasswordHasher passwordHasher = org.mockito.Mockito.mock(PasswordHasher.class);
        BusinessAdminCommandService commandService = new BusinessAdminCommandService(userRepository, passwordHasher);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> commandService.resetBusinessAdminPassword("13800139000", "123456")
        );

        assertEquals("VALIDATION_ERROR", exception.getCode());
        assertEquals("请输入6-20位且包含字母和数字的新密码", exception.getMessage());
    }
}
