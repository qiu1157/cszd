package com.changsha.zhongdian.station.controller;

import com.changsha.zhongdian.common.api.ApiResponse;
import com.changsha.zhongdian.station.dto.StationNextCodeResponse;
import com.changsha.zhongdian.station.service.StationCodeGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stations")
public class StationController {

    private final StationCodeGenerator stationCodeGenerator;

    public StationController(StationCodeGenerator stationCodeGenerator) {
        this.stationCodeGenerator = stationCodeGenerator;
    }

    @GetMapping("/next-code")
    public ApiResponse<StationNextCodeResponse> nextCode() {
        return ApiResponse.success(new StationNextCodeResponse(stationCodeGenerator.generateNextCode()));
    }
}
