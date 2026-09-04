package com.changsha.zhongdian.shareholder.controller;

import java.util.List;

import com.changsha.zhongdian.common.api.ApiResponse;
import com.changsha.zhongdian.shareholder.dto.ShareholderCreateRequest;
import com.changsha.zhongdian.shareholder.dto.ShareholderListItemResponse;
import com.changsha.zhongdian.shareholder.service.ShareholderCommandService;
import com.changsha.zhongdian.shareholder.service.ShareholderQueryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shareholders")
public class ShareholderController {

    private final ShareholderQueryService shareholderQueryService;
    private final ShareholderCommandService shareholderCommandService;

    public ShareholderController(ShareholderQueryService shareholderQueryService,
                                 ShareholderCommandService shareholderCommandService) {
        this.shareholderQueryService = shareholderQueryService;
        this.shareholderCommandService = shareholderCommandService;
    }

    @GetMapping
    public ApiResponse<List<ShareholderListItemResponse>> listShareholders(
            @RequestParam(name = "shareholderName", required = false) String shareholderName,
            @RequestParam(name = "shareholderStatus", required = false) String shareholderStatus
    ) {
        return ApiResponse.success(shareholderQueryService.listShareholders(shareholderName, shareholderStatus));
    }

    @PostMapping
    public ApiResponse<Long> createShareholder(@Valid @RequestBody ShareholderCreateRequest request) {
        return ApiResponse.success("新增股东成功", shareholderCommandService.createShareholder(request));
    }
}