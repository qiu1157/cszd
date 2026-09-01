package com.changsha.zhongdian.pile.controller;

import java.util.List;

import com.changsha.zhongdian.auth.model.LoginUser;
import com.changsha.zhongdian.auth.service.AuthService;
import com.changsha.zhongdian.common.api.ApiResponse;
import com.changsha.zhongdian.pile.dto.PileCreateRequest;
import com.changsha.zhongdian.pile.dto.PileListItemResponse;
import com.changsha.zhongdian.pile.dto.PileNextCodeResponse;
import com.changsha.zhongdian.pile.dto.PileStatusOptionResponse;
import com.changsha.zhongdian.pile.dto.PileStatusUpdateRequest;
import com.changsha.zhongdian.pile.dto.PileUpdateRequest;
import com.changsha.zhongdian.pile.service.PileCommandService;
import com.changsha.zhongdian.pile.service.PileFormOptionQueryService;
import com.changsha.zhongdian.pile.service.PileQueryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 为什么：桩是站点的子资源，桩相关接口统一挂 /api/stations/{stationId}/piles 路径，
// 由 pile 模块的 Controller 承载，避免 StationController 随桩能力扩展持续膨胀
@RestController
@RequestMapping("/api/stations")
public class PileController {

    private final PileQueryService pileQueryService;
    private final PileCommandService pileCommandService;
    private final PileFormOptionQueryService pileFormOptionQueryService;
    private final AuthService authService;

    public PileController(
            PileQueryService pileQueryService,
            PileCommandService pileCommandService,
            PileFormOptionQueryService pileFormOptionQueryService,
            AuthService authService
    ) {
        this.pileQueryService = pileQueryService;
        this.pileCommandService = pileCommandService;
        this.pileFormOptionQueryService = pileFormOptionQueryService;
        this.authService = authService;
    }

    @GetMapping("/{stationId}/piles")
    public ApiResponse<List<PileListItemResponse>> listPiles(@PathVariable("stationId") Long stationId) {
        return ApiResponse.success(pileQueryService.listPilesByStationId(stationId));
    }

    @GetMapping("/{stationId}/piles/next-code")
    public ApiResponse<PileNextCodeResponse> nextPileCode(@PathVariable("stationId") Long stationId) {
        return ApiResponse.success(new PileNextCodeResponse(pileQueryService.nextPileCode(stationId)));
    }

    @PostMapping("/{stationId}/piles")
    public ApiResponse<Long> createPile(
            @PathVariable("stationId") Long stationId,
            @Valid @RequestBody PileCreateRequest request
    ) {
        return ApiResponse.success("新增充电桩成功", pileCommandService.createPile(stationId, request));
    }

    @PutMapping("/{stationId}/piles/{pileId}")
    public ApiResponse<Long> updatePile(
            @PathVariable("stationId") Long stationId,
            @PathVariable("pileId") Long pileId,
            @Valid @RequestBody PileUpdateRequest request
    ) {
        return ApiResponse.success("更新充电桩成功", pileCommandService.updatePile(stationId, pileId, request));
    }

    @GetMapping("/{stationId}/piles/status-options")
    public ApiResponse<List<PileStatusOptionResponse>> listPileStatusOptions(@PathVariable("stationId") Long stationId) {
        return ApiResponse.success(pileFormOptionQueryService.listStatusOptions());
    }

    @PutMapping("/{stationId}/piles/{pileId}/status")
    public ApiResponse<Long> updatePileStatus(
            @PathVariable("stationId") Long stationId,
            @PathVariable("pileId") Long pileId,
            @Valid @RequestBody PileStatusUpdateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        LoginUser currentUser = authService.getCurrentUser(httpServletRequest);
        return ApiResponse.success(
                "充电桩状态切换成功",
                pileCommandService.updatePileStatus(stationId, pileId, request, currentUser.account())
        );
    }
}
