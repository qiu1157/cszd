package com.changsha.zhongdian.shareholding.controller;

import java.util.List;

import com.changsha.zhongdian.auth.model.LoginUser;
import com.changsha.zhongdian.auth.service.AuthService;
import com.changsha.zhongdian.common.api.ApiResponse;
import com.changsha.zhongdian.shareholding.dto.ShareholdingCreateRequest;
import com.changsha.zhongdian.shareholding.dto.ShareholdingEquityAdjustmentRequest;
import com.changsha.zhongdian.shareholding.dto.StationShareholdingListItemResponse;
import com.changsha.zhongdian.shareholding.service.ShareholdingCommandService;
import com.changsha.zhongdian.shareholding.service.ShareholdingQueryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 站点维度持股关系接口：与充电桩一致采用 /api/stations/{stationId}/ 子资源路径表达归属；
// 股东详情侧的“股东-站点”查询仍由 ShareholderController 承载，两侧按入口视角划分职责
@RestController
@RequestMapping("/api/stations")
public class ShareholdingController {

    private final ShareholdingQueryService shareholdingQueryService;
    private final ShareholdingCommandService shareholdingCommandService;
    private final AuthService authService;

    public ShareholdingController(
            ShareholdingQueryService shareholdingQueryService,
            ShareholdingCommandService shareholdingCommandService,
            AuthService authService
    ) {
        this.shareholdingQueryService = shareholdingQueryService;
        this.shareholdingCommandService = shareholdingCommandService;
        this.authService = authService;
    }

    @GetMapping("/{stationId}/shareholdings")
    public ApiResponse<List<StationShareholdingListItemResponse>> listStationShareholdings(@PathVariable Long stationId) {
        return ApiResponse.success(shareholdingQueryService.getStationShareholdings(stationId));
    }

    @PostMapping("/{stationId}/shareholdings")
    public ApiResponse<Long> createShareholding(
            @PathVariable Long stationId,
            @Valid @RequestBody ShareholdingCreateRequest request
    ) {
        return ApiResponse.success("关联股东成功", shareholdingCommandService.createShareholding(stationId, request));
    }

    // 退股是状态流转而非删除（spec 8.2）：用 PUT 子资源动作表达，路径仍锚定站点归属
    @PutMapping("/{stationId}/shareholdings/{shareholdingId}/exit")
    public ApiResponse<Long> exitShareholding(
            @PathVariable Long stationId,
            @PathVariable Long shareholdingId
    ) {
        return ApiResponse.success("退股成功", shareholdingCommandService.exitShareholding(stationId, shareholdingId));
    }

    // 股权调整是批量更新站点下持股中关系的占比（spec 8.3），用 PUT 子资源集合动作表达
    @PutMapping("/{stationId}/shareholdings/equity-adjustment")
    public ApiResponse<Long> adjustEquity(
            @PathVariable Long stationId,
            @Valid @RequestBody ShareholdingEquityAdjustmentRequest request,
            HttpServletRequest httpServletRequest
    ) {
        LoginUser currentUser = authService.getCurrentUser(httpServletRequest);
        return ApiResponse.success(
                "股权调整成功",
                shareholdingCommandService.adjustEquity(stationId, request, currentUser.account())
        );
    }
}