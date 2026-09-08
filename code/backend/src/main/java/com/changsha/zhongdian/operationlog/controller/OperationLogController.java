package com.changsha.zhongdian.operationlog.controller;

import com.changsha.zhongdian.common.api.ApiResponse;
import com.changsha.zhongdian.operationlog.dto.OperationLogPageDTO;
import com.changsha.zhongdian.operationlog.dto.OperationLogQueryParam;
import com.changsha.zhongdian.operationlog.dto.TimelineEventDTO;
import com.changsha.zhongdian.operationlog.service.OperationLogQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/operation-logs")
public class OperationLogController {
    
    private final OperationLogQueryService operationLogQueryService;
    
    public OperationLogController(OperationLogQueryService operationLogQueryService) {
        this.operationLogQueryService = operationLogQueryService;
    }
    
    @GetMapping
    public ApiResponse<OperationLogPageDTO> queryLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String operatorAccount,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Long stationId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        OperationLogQueryParam param = new OperationLogQueryParam();
        param.setStartTime(startTime);
        param.setEndTime(endTime);
        param.setOperatorAccount(operatorAccount);
        param.setTargetType(targetType);
        param.setStationId(stationId);
        param.setKeyword(keyword);
        param.setPage(page);
        param.setSize(size);
        
        OperationLogPageDTO result = operationLogQueryService.queryLogs(param);
        return ApiResponse.success(result);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<TimelineEventDTO> getLogDetail(@PathVariable Long id) {
        TimelineEventDTO detail = operationLogQueryService.getLogDetail(id);
        if (detail == null) {
            return ApiResponse.failure("NOT_FOUND", "日志不存在");
        }
        return ApiResponse.success(detail);
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
