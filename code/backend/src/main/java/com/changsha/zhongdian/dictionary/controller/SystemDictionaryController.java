package com.changsha.zhongdian.dictionary.controller;

import java.util.List;

import com.changsha.zhongdian.auth.model.LoginUser;
import com.changsha.zhongdian.auth.service.AuthService;
import com.changsha.zhongdian.common.api.ApiResponse;
import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.dictionary.dto.SystemDictItemListItemResponse;
import com.changsha.zhongdian.dictionary.dto.SystemDictItemUpsertRequest;
import com.changsha.zhongdian.dictionary.dto.SystemDictTypeListItemResponse;
import com.changsha.zhongdian.dictionary.service.SystemDictionaryCommandService;
import com.changsha.zhongdian.dictionary.service.SystemDictionaryQueryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system-dictionaries")
public class SystemDictionaryController {

    private final AuthService authService;
    private final SystemDictionaryCommandService commandService;
    private final SystemDictionaryQueryService queryService;

    public SystemDictionaryController(
            AuthService authService,
            SystemDictionaryCommandService commandService,
            SystemDictionaryQueryService queryService
    ) {
        this.authService = authService;
        this.commandService = commandService;
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

    @PostMapping("/{typeCode}/items")
    public ApiResponse<Void> createItem(
            @PathVariable String typeCode,
            @Valid @RequestBody SystemDictItemUpsertRequest request,
            HttpServletRequest httpServletRequest
    ) {
        requireSystemAdmin(httpServletRequest);
        commandService.createItem(typeCode, request.itemCode(), request.itemName(), request.status(), request.sortOrder());
        return ApiResponse.success(null);
    }

    @PutMapping("/{typeCode}/items/{itemId}")
    public ApiResponse<Void> updateItem(
            @PathVariable String typeCode,
            @PathVariable Long itemId,
            @Valid @RequestBody SystemDictItemUpsertRequest request,
            HttpServletRequest httpServletRequest
    ) {
        requireSystemAdmin(httpServletRequest);
        commandService.updateItem(itemId, typeCode, request.itemCode(), request.itemName(), request.status(), request.sortOrder());
        return ApiResponse.success(null);
    }

    @PostMapping("/{typeCode}/items/{itemId}/{action}")
    public ApiResponse<Void> updateItemStatus(
            @PathVariable String typeCode,
            @PathVariable Long itemId,
            @PathVariable String action,
            HttpServletRequest httpServletRequest
    ) {
        requireSystemAdmin(httpServletRequest);
        commandService.updateItemStatus(itemId, typeCode, resolveTargetStatus(action));
        return ApiResponse.success(null);
    }

    private void requireSystemAdmin(HttpServletRequest request) {
        LoginUser currentUser = authService.getCurrentUser(request);
        if (!"SYSTEM_ADMIN".equals(currentUser.role())) {
            throw new BusinessException("FORBIDDEN", "仅系统管理员可执行该操作", HttpStatus.FORBIDDEN);
        }
    }

    private String resolveTargetStatus(String action) {
        return switch (action) {
            case "enable" -> "ENABLED";
            case "disable" -> "DISABLED";
            default -> throw new BusinessException("VALIDATION_ERROR", "状态操作不合法", HttpStatus.BAD_REQUEST);
        };
    }
}
