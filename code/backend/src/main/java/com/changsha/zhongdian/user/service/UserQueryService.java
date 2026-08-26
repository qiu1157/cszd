package com.changsha.zhongdian.user.service;

import java.util.List;

import com.changsha.zhongdian.user.dto.BusinessAdminListItemResponse;
import com.changsha.zhongdian.user.entity.SystemUser;
import com.changsha.zhongdian.user.entity.UserRole;
import com.changsha.zhongdian.user.repository.SystemUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class UserQueryService {

    private static final Logger log = LoggerFactory.getLogger(UserQueryService.class);

    private final SystemUserRepository systemUserRepository;

    public UserQueryService(SystemUserRepository systemUserRepository) {
        this.systemUserRepository = systemUserRepository;
    }

    public List<BusinessAdminListItemResponse> listBusinessAdmins() {
        try {
            return systemUserRepository.findAllByRoleCodeOrderByUpdatedAtDesc(UserRole.BUSINESS_ADMIN)
                    .stream()
                    .map(this::toBusinessAdminListItem)
                    .toList();
        } catch (DataAccessException exception) {
            // 为什么：当前阶段要求数据库异常时系统仍可用于前后端联调与页面验收，
            // 列表查询失败时返回空集合，避免用户管理页直接报 500。
            log.warn("查询业务管理员列表失败，已返回空列表：{}", exception.getClass().getSimpleName());
            return List.of();
        }
    }

    private BusinessAdminListItemResponse toBusinessAdminListItem(SystemUser systemUser) {
        return new BusinessAdminListItemResponse(
                systemUser.getAccount(),
                systemUser.getName(),
                systemUser.getRoleCode().name(),
                systemUser.getUserStatus().name(),
                systemUser.getLastLoginAt()
        );
    }
}
