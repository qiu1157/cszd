package com.changsha.zhongdian.user.controller;

import java.util.List;

import com.changsha.zhongdian.common.api.ApiResponse;
import com.changsha.zhongdian.user.dto.BusinessAdminListItemResponse;
import com.changsha.zhongdian.user.service.UserQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserQueryService userQueryService;

    public UserController(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    @GetMapping("/business-admins")
    public ApiResponse<List<BusinessAdminListItemResponse>> listBusinessAdmins() {
        return ApiResponse.success(userQueryService.listBusinessAdmins());
    }
}
