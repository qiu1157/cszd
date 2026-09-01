package com.changsha.zhongdian.pile.controller;

import java.util.List;

import com.changsha.zhongdian.common.api.ApiResponse;
import com.changsha.zhongdian.pile.dto.PileListItemResponse;
import com.changsha.zhongdian.pile.service.PileQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 为什么：桩是站点的子资源，桩相关接口统一挂 /api/stations/{stationId}/piles 路径，
// 由 pile 模块的 Controller 承载，避免 StationController 随桩能力扩展持续膨胀
@RestController
@RequestMapping("/api/stations")
public class PileController {

    private final PileQueryService pileQueryService;

    public PileController(PileQueryService pileQueryService) {
        this.pileQueryService = pileQueryService;
    }

    @GetMapping("/{stationId}/piles")
    public ApiResponse<List<PileListItemResponse>> listPiles(@PathVariable("stationId") Long stationId) {
        return ApiResponse.success(pileQueryService.listPilesByStationId(stationId));
    }
}
