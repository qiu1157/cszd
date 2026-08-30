package com.changsha.zhongdian.station.controller;

import java.util.List;

import com.changsha.zhongdian.common.api.ApiResponse;
import com.changsha.zhongdian.station.dto.StationCreateRequest;
import com.changsha.zhongdian.station.dto.StationListItemResponse;
import com.changsha.zhongdian.station.dto.StationNextCodeResponse;
import com.changsha.zhongdian.station.dto.StationPlatformOptionResponse;
import com.changsha.zhongdian.station.service.StationCodeGenerator;
import com.changsha.zhongdian.station.service.StationCommandService;
import com.changsha.zhongdian.station.service.StationFormOptionQueryService;
import com.changsha.zhongdian.station.service.StationQueryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
            @RequestParam(name = "platformItemId", required = false) Long platformItemId
    ) {
        return ApiResponse.success(stationQueryService.listStations(stationName, platformItemId));
    }

    @PostMapping
    public ApiResponse<Long> createStation(@Valid @RequestBody StationCreateRequest request) {
        return ApiResponse.success("新增充电站成功", stationCommandService.createStation(request));
    }

    @GetMapping("/platform-options")
    public ApiResponse<List<StationPlatformOptionResponse>> listPlatformOptions() {
        return ApiResponse.success(stationFormOptionQueryService.listPlatformOptions());
    }

    @GetMapping("/next-code")
    public ApiResponse<StationNextCodeResponse> nextCode() {
        return ApiResponse.success(new StationNextCodeResponse(stationCodeGenerator.generateNextCode()));
    }
}
