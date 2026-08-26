package com.changsha.zhongdian.auth.interceptor;

import com.changsha.zhongdian.auth.service.AuthService;
import com.changsha.zhongdian.auth.service.TokenService;
import com.changsha.zhongdian.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;

    public AuthInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new BusinessException("UNAUTHORIZED", "请先登录", HttpStatus.UNAUTHORIZED);
        }

        String token = authorizationHeader.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            throw new BusinessException("UNAUTHORIZED", "请先登录", HttpStatus.UNAUTHORIZED);
        }

        request.setAttribute(
                AuthService.CURRENT_USER_REQUEST_ATTRIBUTE,
                tokenService.getLoginUser(token)
                        .orElseThrow(() -> new BusinessException("UNAUTHORIZED", "登录状态失效，请重新登录", HttpStatus.UNAUTHORIZED))
        );
        return true;
    }
}
