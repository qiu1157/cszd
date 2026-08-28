package com.changsha.zhongdian.dictionary.controller;

import java.util.List;

import com.changsha.zhongdian.auth.model.LoginUser;
import com.changsha.zhongdian.auth.service.AuthService;
import com.changsha.zhongdian.common.api.ApiResponse;
import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.dictionary.dto.SystemDictItemListItemResponse;
import com.changsha.zhongdian.dictionary.dto.SystemDictTypeListItemResponse;
import com.changsha.zhongdian.dictionary.service.SystemDictionaryQueryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system-dictionaries")
public class SystemDictionaryController {

    private final AuthService authService;
    private final SystemDictionaryQueryService queryService;

    public SystemDictionaryController(AuthService authService, SystemDictionaryQueryService queryService) {
        this.authService = authService;
        this.queryService = queryService;
    }

    @GetMapping("/types")
    public ApiResponse<List<SystemDictTypeListItemResponse>> listTypes(HttpServletRequest request) {
        requireSystemAdmin(request);
        return ApiResponse.success(queryService.listTypes());
    }

    @GetMapping("/{typeCode}/items")
    public ApiResponse<List<SystemDictItemListItemResponse>> listItems(
            @PathVariable String typeCode,
            HttpServletRequest request
    ) {
        requireSystemAdmin(request);
        return ApiResponse.success(queryService.listItems(typeCode));
    }

    private void requireSystemAdmin(HttpServletRequest request) {
        LoginUser currentUser = authService.getCurrentUser(request);
        if (!"SYSTEM_ADMIN".equals(currentUser.role())) {
            throw new BusinessException("FORBIDDEN", "仅系统管理员可执行该操作", HttpStatus.FORBIDDEN);
        }
    }
}
