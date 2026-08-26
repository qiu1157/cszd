package com.changsha.zhongdian.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.changsha.zhongdian.user.dto.BusinessAdminListItemResponse;
import com.changsha.zhongdian.user.entity.SystemUser;
import com.changsha.zhongdian.user.entity.UserRole;
import com.changsha.zhongdian.user.entity.UserStatus;
import com.changsha.zhongdian.user.repository.SystemUserRepository;
import org.junit.jupiter.api.Test;

class UserQueryServiceTest {

    @Test
    void listBusinessAdminsShouldOnlyReturnBusinessAdminRows() {
        SystemUserRepository userRepository = org.mockito.Mockito.mock(SystemUserRepository.class);
        SystemUser systemUser = new SystemUser();
        systemUser.setAccount("13800138000");
        systemUser.setName("李丹");
        systemUser.setRoleCode(UserRole.BUSINESS_ADMIN);
        systemUser.setUserStatus(UserStatus.ENABLED);

        when(userRepository.findAllByRoleCodeOrderByUpdatedAtDesc(UserRole.BUSINESS_ADMIN))
                .thenReturn(List.of(systemUser));

        UserQueryService userQueryService = new UserQueryService(userRepository);

        List<BusinessAdminListItemResponse> result = userQueryService.listBusinessAdmins();
        BusinessAdminListItemResponse firstItem = result.get(0);

        assertEquals(1, result.size());
        assertEquals("13800138000", firstItem.account());
        assertEquals("李丹", firstItem.name());
        assertEquals("BUSINESS_ADMIN", firstItem.role());
        assertEquals("ENABLED", firstItem.status());
        assertNull(firstItem.lastLoginAt());

        verify(userRepository).findAllByRoleCodeOrderByUpdatedAtDesc(UserRole.BUSINESS_ADMIN);
    }
}
