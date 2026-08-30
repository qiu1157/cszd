package com.changsha.zhongdian.station.controller;

import java.util.List;

import com.changsha.zhongdian.common.api.ApiResponse;
import com.changsha.zhongdian.station.dto.StationCreateRequest;
import com.changsha.zhongdian.station.dto.StationDetailResponse;
import com.changsha.zhongdian.station.dto.StationListItemResponse;
import com.changsha.zhongdian.station.dto.StationNextCodeResponse;
import com.changsha.zhongdian.station.dto.StationPlatformOptionResponse;
import com.changsha.zhongdian.station.dto.StationStatusOptionResponse;
import com.changsha.zhongdian.station.dto.StationUpdateRequest;
import com.changsha.zhongdian.station.service.StationCodeGenerator;
import com.changsha.zhongdian.station.service.StationCommandService;
import com.changsha.zhongdian.station.service.StationFormOptionQueryService;
import com.changsha.zhongdian.station.service.StationQueryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stations")
public class StationController {

    private final StationCodeGenerator stationCodeGenerator;
    private final StationQueryService stationQueryService;
    private final StationFormOptionQueryService stationFormOptionQueryService;
    private final StationCommandService stationCommandService;

    public StationController(
            StationCodeGenerator stationCodeGenerator,
            StationQueryService stationQueryService,
            StationFormOptionQueryService stationFormOptionQueryService,
            StationCommandService stationCommandService
    ) {
        this.stationCodeGenerator = stationCodeGenerator;
        this.stationQueryService = stationQueryService;
        this.stationFormOptionQueryService = stationFormOptionQueryService;
        this.stationCommandService = stationCommandService;
    }

    @GetMapping
    public ApiResponse<List<StationListItemResponse>> listStations(
            @RequestParam(name = "stationName", required = false) String stationName,
            @RequestParam(name = "platformItemId", required = false) Long platformItemId,
            @RequestParam(name = "stationStatusItemId", required = false) Long stationStatusItemId
    ) {
        if (stationStatusItemId != null) {
            return ApiResponse.success(stationQueryService.listStations(stationName, platformItemId, stationStatusItemId));
        }
        return ApiResponse.success(stationQueryService.listStations(stationName, platformItemId));
    }

    @GetMapping("/{stationId}")
    public ApiResponse<StationDetailResponse> getStationDetail(@PathVariable("stationId") Long stationId) {
        return ApiResponse.success(stationQueryService.getStationDetail(stationId));
    }

    @PostMapping
    public ApiResponse<Long> createStation(@Valid @RequestBody StationCreateRequest request) {
        return ApiResponse.success("新增充电站成功", stationCommandService.createStation(request));
    }

    @PutMapping("/{stationId}")
    public ApiResponse<Long> updateStation(
            @PathVariable("stationId") Long stationId,
            @Valid @RequestBody StationUpdateRequest request
    ) {
        return ApiResponse.success("更新充电站成功", stationCommandService.updateStation(stationId, request));
    }

    @GetMapping("/platform-options")
    public ApiResponse<List<StationPlatformOptionResponse>> listPlatformOptions() {
        return ApiResponse.success(stationFormOptionQueryService.listPlatformOptions());
    }

    @GetMapping("/status-options")
    public ApiResponse<List<StationStatusOptionResponse>> listStatusOptions() {
        return ApiResponse.success(stationFormOptionQueryService.listStatusOptions());
    }

    @GetMapping("/next-code")
    public ApiResponse<StationNextCodeResponse> nextCode() {
        return ApiResponse.success(new StationNextCodeResponse(stationCodeGenerator.generateNextCode()));
    }
}
