package com.changsha.zhongdian.user.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.changsha.zhongdian.user.entity.SystemUser;
import com.changsha.zhongdian.user.entity.UserRole;
import com.changsha.zhongdian.user.entity.UserStatus;
import com.changsha.zhongdian.user.repository.SystemUserRepository;
import com.changsha.zhongdian.user.service.PasswordHasher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;

class DefaultAdminInitializerTest {

    @Test
    void initializeShouldCreateDefaultAdminWhenMissing() {
        SystemUserRepository userRepository = org.mockito.Mockito.mock(SystemUserRepository.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<SystemUserRepository> repositoryProvider = org.mockito.Mockito.mock(ObjectProvider.class);
        PasswordHasher passwordHasher = org.mockito.Mockito.mock(PasswordHasher.class);
        when(repositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(userRepository.existsByAccount("admin")).thenReturn(false);
        when(passwordHasher.hash("Init@123456")).thenReturn("hashed-admin-password");

        DefaultAdminInitializer initializer = new DefaultAdminInitializer(
                repositoryProvider,
                passwordHasher,
                "Init@123456"
        );

        initializer.initializeDefaultAdmin();

        ArgumentCaptor<SystemUser> userCaptor = ArgumentCaptor.forClass(SystemUser.class);
        verify(userRepository).save(userCaptor.capture());

        SystemUser createdUser = userCaptor.getValue();
        assertEquals("admin", createdUser.getAccount());
        assertEquals("系统管理员", createdUser.getName());
        assertEquals("hashed-admin-password", createdUser.getPasswordHash());
        assertEquals(UserRole.SYSTEM_ADMIN, createdUser.getRoleCode());
        assertEquals(UserStatus.ENABLED, createdUser.getUserStatus());
        assertTrue(createdUser.isDefaultAdmin());
        assertNull(createdUser.getPhone());
    }

    @Test
    void initializeShouldSkipWhenRepositoryIsUnavailable() {
        @SuppressWarnings("unchecked")
        ObjectProvider<SystemUserRepository> repositoryProvider = org.mockito.Mockito.mock(ObjectProvider.class);
        PasswordHasher passwordHasher = org.mockito.Mockito.mock(PasswordHasher.class);
        when(repositoryProvider.getIfAvailable()).thenReturn(null);

        DefaultAdminInitializer initializer = new DefaultAdminInitializer(
                repositoryProvider,
                passwordHasher,
                "123456"
        );

        initializer.initializeDefaultAdmin();

        verify(passwordHasher, never()).hash(any());
    }

    @Test
    void initializeShouldUseDefaultPasswordWhenEnvPasswordIsBlank() {
        SystemUserRepository userRepository = org.mockito.Mockito.mock(SystemUserRepository.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<SystemUserRepository> repositoryProvider = org.mockito.Mockito.mock(ObjectProvider.class);
        PasswordHasher passwordHasher = org.mockito.Mockito.mock(PasswordHasher.class);
        when(repositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(userRepository.existsByAccount("admin")).thenReturn(false);
        when(passwordHasher.hash("123456")).thenReturn("hashed-default-admin-password");

        DefaultAdminInitializer initializer = new DefaultAdminInitializer(
                repositoryProvider,
                passwordHasher,
                "   "
        );

        initializer.initializeDefaultAdmin();

        ArgumentCaptor<SystemUser> userCaptor = ArgumentCaptor.forClass(SystemUser.class);
        verify(userRepository).save(userCaptor.capture());
        verify(passwordHasher).hash("123456");

        SystemUser createdUser = userCaptor.getValue();
        assertEquals("admin", createdUser.getAccount());
        assertEquals("hashed-default-admin-password", createdUser.getPasswordHash());
        assertTrue(createdUser.isDefaultAdmin());
    }

    @Test
    void initializeShouldSkipWhenNonDefaultAdminAlreadyExists() {
        SystemUserRepository userRepository = org.mockito.Mockito.mock(SystemUserRepository.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<SystemUserRepository> repositoryProvider = org.mockito.Mockito.mock(ObjectProvider.class);
        PasswordHasher passwordHasher = org.mockito.Mockito.mock(PasswordHasher.class);
        SystemUser existingAdmin = new SystemUser();
        existingAdmin.setAccount("admin");
        existingAdmin.setName("系统管理员");
        existingAdmin.setRoleCode(UserRole.SYSTEM_ADMIN);
        existingAdmin.setUserStatus(UserStatus.ENABLED);
        existingAdmin.setDefaultAdmin(false);
        when(repositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(userRepository.existsByAccount("admin")).thenReturn(true);
        when(userRepository.findByAccount("admin")).thenReturn(java.util.Optional.of(existingAdmin));

        DefaultAdminInitializer initializer = new DefaultAdminInitializer(
                repositoryProvider,
                passwordHasher,
                "Init@123456"
        );

        initializer.initializeDefaultAdmin();

        verify(userRepository).existsByAccount("admin");
        verify(userRepository, never()).save(any());
        verify(passwordHasher, never()).hash(any());
    }

    @Test
    void initializeShouldResetPasswordWhenDefaultAdminAlreadyExists() {
        SystemUserRepository userRepository = org.mockito.Mockito.mock(SystemUserRepository.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<SystemUserRepository> repositoryProvider = org.mockito.Mockito.mock(ObjectProvider.class);
        PasswordHasher passwordHasher = org.mockito.Mockito.mock(PasswordHasher.class);
        SystemUser existingAdmin = new SystemUser();
        existingAdmin.setAccount("admin");
        existingAdmin.setName("系统管理员");
        existingAdmin.setPasswordHash("old-hash");
        existingAdmin.setRoleCode(UserRole.SYSTEM_ADMIN);
        existingAdmin.setUserStatus(UserStatus.ENABLED);
        existingAdmin.setDefaultAdmin(true);
        when(repositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(userRepository.existsByAccount("admin")).thenReturn(true);
        when(userRepository.findByAccount("admin")).thenReturn(java.util.Optional.of(existingAdmin));
        when(passwordHasher.hash("123456")).thenReturn("hashed-reset-password");

        DefaultAdminInitializer initializer = new DefaultAdminInitializer(
                repositoryProvider,
                passwordHasher,
                ""
        );

        initializer.initializeDefaultAdmin();

        verify(passwordHasher).hash("123456");
        verify(userRepository).save(existingAdmin);
        assertEquals("hashed-reset-password", existingAdmin.getPasswordHash());
        assertTrue(existingAdmin.isDefaultAdmin());
    }
}
