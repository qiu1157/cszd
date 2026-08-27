package com.changsha.zhongdian.user.controller;

import java.util.List;

import com.changsha.zhongdian.auth.model.LoginUser;
import com.changsha.zhongdian.auth.service.AuthService;
import com.changsha.zhongdian.common.api.ApiResponse;
import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.user.dto.BusinessAdminListItemResponse;
import com.changsha.zhongdian.user.dto.CreateBusinessAdminRequest;
import com.changsha.zhongdian.user.service.BusinessAdminCommandService;
import com.changsha.zhongdian.user.service.UserQueryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;
    private final BusinessAdminCommandService businessAdminCommandService;
    private final UserQueryService userQueryService;

    public UserController(
            AuthService authService,
            BusinessAdminCommandService businessAdminCommandService,
            UserQueryService userQueryService
    ) {
        this.authService = authService;
        this.businessAdminCommandService = businessAdminCommandService;
        this.userQueryService = userQueryService;
    }

    @PostMapping("/business-admins")
    public ApiResponse<Void> createBusinessAdmin(
            @Valid @RequestBody CreateBusinessAdminRequest request,
            HttpServletRequest httpServletRequest
    ) {
        requireSystemAdmin(httpServletRequest);
        businessAdminCommandService.createBusinessAdmin(request.phone(), request.name(), request.initialPassword());
        return ApiResponse.success(null);
    }

    @GetMapping("/business-admins")
    public ApiResponse<List<BusinessAdminListItemResponse>> listBusinessAdmins(HttpServletRequest httpServletRequest) {
        requireSystemAdmin(httpServletRequest);
        return ApiResponse.success(userQueryService.listBusinessAdmins());
    }

    private void requireSystemAdmin(HttpServletRequest httpServletRequest) {
        LoginUser currentUser = authService.getCurrentUser(httpServletRequest);
        if (!"SYSTEM_ADMIN".equals(currentUser.role())) {
            throw new BusinessException("FORBIDDEN", "仅系统管理员可执行该操作", HttpStatus.FORBIDDEN);
        }
    }
}
