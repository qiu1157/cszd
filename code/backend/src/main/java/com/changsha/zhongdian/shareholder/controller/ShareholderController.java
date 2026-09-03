package com.changsha.zhongdian.shareholder.controller;

import java.util.List;

import com.changsha.zhongdian.common.api.ApiResponse;
import com.changsha.zhongdian.shareholder.dto.ShareholderListItemResponse;
import com.changsha.zhongdian.shareholder.service.ShareholderQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shareholders")
public class ShareholderController {

    private final ShareholderQueryService shareholderQueryService;

    public ShareholderController(ShareholderQueryService shareholderQueryService) {
        this.shareholderQueryService = shareholderQueryService;
    }

    @GetMapping
    public ApiResponse<List<ShareholderListItemResponse>> listShareholders(
            @RequestParam(name = "shareholderName", required = false) String shareholderName,
            @RequestParam(name = "shareholderStatus", required = false) String shareholderStatus
    ) {
        return ApiResponse.success(shareholderQueryService.listShareholders(shareholderName, shareholderStatus));
    }
}