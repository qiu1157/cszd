package com.changsha.zhongdian.auth.controller;

import com.changsha.zhongdian.auth.dto.ChangePasswordRequest;
import com.changsha.zhongdian.auth.dto.LoginRequest;
import com.changsha.zhongdian.auth.dto.LoginResponse;
import com.changsha.zhongdian.auth.model.LoginUser;
import com.changsha.zhongdian.auth.service.AuthService;
import com.changsha.zhongdian.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ApiResponse.success("登录成功", authService.login(loginRequest));
    }

    @GetMapping("/me")
    public ApiResponse<LoginUser> currentUser(HttpServletRequest request) {
        return ApiResponse.success(authService.getCurrentUser(request));
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpServletRequest
    ) {
        authService.changePassword(httpServletRequest, request);
        return ApiResponse.success("密码修改成功", null);
    }
}
