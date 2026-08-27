package com.changsha.zhongdian.user.service;

import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.user.entity.SystemUser;
import com.changsha.zhongdian.user.entity.UserRole;
import com.changsha.zhongdian.user.entity.UserStatus;
import com.changsha.zhongdian.user.repository.SystemUserRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class BusinessAdminCommandService {

    private final SystemUserRepository systemUserRepository;
    private final PasswordHasher passwordHasher;

    public BusinessAdminCommandService(SystemUserRepository systemUserRepository, PasswordHasher passwordHasher) {
        this.systemUserRepository = systemUserRepository;
        this.passwordHasher = passwordHasher;
    }

    public void createBusinessAdmin(String phone, String name, String initialPassword) {
        String normalizedPhone = requireNonBlank(phone, "手机号不能为空");
        String normalizedName = requireNonBlank(name, "姓名不能为空");
        String normalizedInitialPassword = requireNonBlank(initialPassword, "初始密码不能为空");

        SystemUser systemUser = new SystemUser();
        systemUser.setAccount(normalizedPhone);
        systemUser.setPhone(normalizedPhone);
        systemUser.setName(normalizedName);
        systemUser.setPasswordHash(passwordHasher.hash(normalizedInitialPassword));
        systemUser.setRoleCode(UserRole.BUSINESS_ADMIN);
        systemUser.setUserStatus(UserStatus.ENABLED);
        systemUser.setDefaultAdmin(false);
        systemUser.setLastLoginAt(null);

        try {
            systemUserRepository.save(systemUser);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException("USER_ALREADY_EXISTS", "账号已存在", HttpStatus.CONFLICT);
        } catch (DataAccessException exception) {
            throw new BusinessException("USER_SAVE_FAILED", "当前数据库不可用，暂时无法新增业务管理员", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
        }
        return value.trim();
    }
}
