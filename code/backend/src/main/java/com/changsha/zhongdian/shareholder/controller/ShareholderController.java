package com.changsha.zhongdian.shareholder.controller;

import java.util.List;

import com.changsha.zhongdian.common.api.ApiResponse;
import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.shareholding.dto.ShareholderStationListItemResponse;
import com.changsha.zhongdian.shareholding.service.ShareholdingQueryService;
import com.changsha.zhongdian.shareholder.dto.ShareholderCreateRequest;
import com.changsha.zhongdian.shareholder.dto.ShareholderDetailResponse;
import com.changsha.zhongdian.shareholder.dto.ShareholderLinkOptionResponse;
import com.changsha.zhongdian.shareholder.dto.ShareholderListItemResponse;
import com.changsha.zhongdian.shareholder.dto.ShareholderUpdateRequest;
import com.changsha.zhongdian.shareholder.service.ShareholderCommandService;
import com.changsha.zhongdian.shareholder.service.ShareholderQueryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shareholders")
public class ShareholderController {

    private final ShareholderQueryService shareholderQueryService;
    private final ShareholderCommandService shareholderCommandService;
    private final ShareholdingQueryService shareholdingQueryService;

    public ShareholderController(ShareholderQueryService shareholderQueryService,
                                 ShareholderCommandService shareholderCommandService,
                                 ShareholdingQueryService shareholdingQueryService) {
        this.shareholderQueryService = shareholderQueryService;
        this.shareholderCommandService = shareholderCommandService;
        this.shareholdingQueryService = shareholdingQueryService;
    }

    @GetMapping
    public ApiResponse<List<ShareholderListItemResponse>> listShareholders(
            @RequestParam(name = "shareholderName", required = false) String shareholderName,
            @RequestParam(name = "shareholderStatus", required = false) String shareholderStatus
    ) {
        return ApiResponse.success(shareholderQueryService.listShareholders(shareholderName, shareholderStatus));
    }

    // E-8 停用股东不可新建持股关联的候选接口：固定只返回启用主档；
    // 字面路径优先于 /{shareholderId} 变量路径，不会被详情路由吞掉
    @GetMapping("/link-options")
    public ApiResponse<List<ShareholderLinkOptionResponse>> listLinkableShareholderOptions() {
        return ApiResponse.success(shareholderQueryService.listLinkableShareholderOptions());
    }

    @GetMapping("/{shareholderId}")
    public ApiResponse<ShareholderDetailResponse> getShareholderDetail(@PathVariable Long shareholderId) {
        return ApiResponse.success(shareholderQueryService.getShareholderDetail(shareholderId));
    }

    // 股东详情的关联站点子资源：URL 挂股东前缀表达归属关系，数据由持股关系模块提供
    @GetMapping("/{shareholderId}/stations")
    public ApiResponse<List<ShareholderStationListItemResponse>> getShareholderStations(@PathVariable Long shareholderId) {
        return ApiResponse.success(shareholdingQueryService.getShareholderStations(shareholderId));
    }

    @PostMapping
    public ApiResponse<Long> createShareholder(@Valid @RequestBody ShareholderCreateRequest request) {
        return ApiResponse.success("新增股东成功", shareholderCommandService.createShareholder(request));
    }

    @PutMapping("/{shareholderId}")
    public ApiResponse<Long> updateShareholder(
            @PathVariable Long shareholderId,
            @Valid @RequestBody ShareholderUpdateRequest request
    ) {
        return ApiResponse.success("更新股东成功", shareholderCommandService.updateShareholder(shareholderId, request));
    }

    // 为什么动作放在路径而不是请求体：enable/disable 即操作语义本身，与字典项状态切换接口保持同一形态
    @PostMapping("/{shareholderId}/{action}")
    public ApiResponse<Void> updateShareholderStatus(
            @PathVariable Long shareholderId,
            @PathVariable String action
    ) {
        shareholderCommandService.updateShareholderStatus(shareholderId, resolveTargetStatus(action));
        return ApiResponse.success("股东状态更新成功", null);
    }

    // 为什么在 Web 层解析：非法动作路径直接拦截为明确错误，不进入业务层
    private String resolveTargetStatus(String action) {
        return switch (action) {
            case "enable" -> "ENABLED";
            case "disable" -> "DISABLED";
            default -> throw new BusinessException("VALIDATION_ERROR", "状态操作不合法", HttpStatus.BAD_REQUEST);
        };
    }
}