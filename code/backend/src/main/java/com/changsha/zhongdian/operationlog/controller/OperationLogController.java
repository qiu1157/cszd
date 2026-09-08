package com.changsha.zhongdian.operationlog.controller;

import com.changsha.zhongdian.common.api.ApiResponse;
import com.changsha.zhongdian.operationlog.dto.TimelineEventDTO;
import com.changsha.zhongdian.operationlog.service.OperationLogQueryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/operation-logs")
public class OperationLogController {
    
    private final OperationLogQueryService operationLogQueryService;
    
    public OperationLogController(OperationLogQueryService operationLogQueryService) {
        this.operationLogQueryService = operationLogQueryService;
    }
    
    @GetMapping("/timeline/station/{stationId}")
    public ApiResponse<List<TimelineEventDTO>> getStationTimeline(@PathVariable Long stationId) {
        List<TimelineEventDTO> timeline = operationLogQueryService.getStationTimeline(stationId);
        return ApiResponse.success(timeline);
    }
    
    @GetMapping("/timeline/shareholder/{shareholderId}")
    public ApiResponse<List<TimelineEventDTO>> getShareholderTimeline(
            @PathVariable Long shareholderId,
            @RequestParam(required = false) Long stationId
    ) {
        List<TimelineEventDTO> timeline;
        if (stationId != null) {
            timeline = operationLogQueryService.getShareholderTimelineByStation(shareholderId, stationId);
        } else {
            timeline = operationLogQueryService.getShareholderTimeline(shareholderId);
        }
        return ApiResponse.success(timeline);
    }
}
