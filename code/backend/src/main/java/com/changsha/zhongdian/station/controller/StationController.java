package com.changsha.zhongdian.station.controller;

import java.util.List;

import com.changsha.zhongdian.common.api.ApiResponse;
import com.changsha.zhongdian.station.dto.StationListItemResponse;
import com.changsha.zhongdian.station.dto.StationNextCodeResponse;
import com.changsha.zhongdian.station.service.StationCodeGenerator;
import com.changsha.zhongdian.station.service.StationQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stations")
public class StationController {

    private final StationCodeGenerator stationCodeGenerator;
    private final StationQueryService stationQueryService;

    public StationController(StationCodeGenerator stationCodeGenerator, StationQueryService stationQueryService) {
        this.stationCodeGenerator = stationCodeGenerator;
        this.stationQueryService = stationQueryService;
    }

    @GetMapping
    public ApiResponse<List<StationListItemResponse>> listStations() {
        return ApiResponse.success(stationQueryService.listStations());
    }

    @GetMapping("/next-code")
    public ApiResponse<StationNextCodeResponse> nextCode() {
        return ApiResponse.success(new StationNextCodeResponse(stationCodeGenerator.generateNextCode()));
    }
}
