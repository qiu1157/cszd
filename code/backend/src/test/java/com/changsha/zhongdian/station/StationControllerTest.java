package com.changsha.zhongdian.station;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import com.changsha.zhongdian.AuthenticatedWebTestSupport;
import com.changsha.zhongdian.station.dto.StationDetailResponse;
import com.changsha.zhongdian.station.dto.StationListItemResponse;
import com.changsha.zhongdian.station.dto.StationPlatformOptionResponse;
import com.changsha.zhongdian.station.dto.StationStatusOptionResponse;
import com.changsha.zhongdian.station.service.StationCodeGenerator;
import com.changsha.zhongdian.station.service.StationCommandService;
import com.changsha.zhongdian.station.service.StationFormOptionQueryService;
import com.changsha.zhongdian.station.service.StationQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

// 为什么：继承无数据库测试基类统一提供登录与 Repository mock，本类只额外 mock 站点服务以聚焦 Web 层行为
class StationControllerTest extends AuthenticatedWebTestSupport {

    @MockBean
    private StationCodeGenerator stationCodeGenerator;

    @MockBean
    private StationQueryService stationQueryService;

    @MockBean
    private StationFormOptionQueryService stationFormOptionQueryService;

    @MockBean
    private StationCommandService stationCommandService;

    @Test
    void nextCodeShouldReturnGeneratedStationCodeForAuthenticatedUser() throws Exception {
        when(stationCodeGenerator.generateNextCode()).thenReturn("ST-202608-0019");
        String token = loginAsBusinessAdmin();

        mockMvc.perform(get("/api/stations/next-code")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.stationCode").value("ST-202608-0019"));
    }

    @Test
    void nextCodeShouldRejectAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/stations/next-code"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void listStationsShouldReturnStationRowsForAuthenticatedUser() throws Exception {
        // Controller 无筛选时统一走双参查询，stub 与 verify 需与其调用形态对齐
        when(stationQueryService.listStations(null, null)).thenReturn(List.of(
                new StationListItemResponse(
                        1001L,
                        "ST-202608-0018",
                        "星城南服务区超级站",
                        "星络平台",
                        "XL-4301-889",
                        "张海峰",
                        "13800138000",
                        "运营中",
                        LocalDateTime.of(2026, 8, 29, 10, 26)
                )
        ));
        String token = loginAsBusinessAdmin();

        mockMvc.perform(get("/api/stations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].stationCode").value("ST-202608-0018"))
                .andExpect(jsonPath("$.data[0].stationName").value("星城南服务区超级站"))
                .andExpect(jsonPath("$.data[0].platformName").value("星络平台"))
                .andExpect(jsonPath("$.data[0].stationStatusName").value("运营中"));
    }

    @Test
    void listStationsShouldPassStationNameKeywordToQueryService() throws Exception {
        when(stationQueryService.listStations("雨花", null)).thenReturn(List.of(
                new StationListItemResponse(
                        1001L,
                        "ST-202608-0001",
                        "雨花城际快充港",
                        "手工验收平台",
                        "PT-224211",
                        "测试联系人",
                        "13800138000",
                        "签合同",
                        LocalDateTime.of(2026, 8, 29, 22, 42)
                )
        ));
        String token = loginAsBusinessAdmin();

        mockMvc.perform(get("/api/stations")
                        .param("stationName", "雨花")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].stationName").value("雨花城际快充港"));

        verify(stationQueryService).listStations("雨花", null);
    }

    @Test
    void listStationsShouldPassPlatformItemIdToQueryService() throws Exception {
        when(stationQueryService.listStations(null, 11L)).thenReturn(List.of(
                new StationListItemResponse(
                        1002L,
                        "ST-202608-0002",
                        "望城综合能源站",
                        "手工验收平台",
                        "PT-330011",
                        "李工",
                        "13800138001",
                        "签合同",
                        LocalDateTime.of(2026, 8, 30, 8, 40)
                )
        ));
        String token = loginAsBusinessAdmin();

        mockMvc.perform(get("/api/stations")
                        .param("platformItemId", "11")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].platformName").value("手工验收平台"));

        verify(stationQueryService).listStations(null, 11L);
    }

    @Test
    void listPlatformOptionsShouldReturnEnabledPlatformItemsForAuthenticatedUser() throws Exception {
        when(stationFormOptionQueryService.listPlatformOptions()).thenReturn(List.of(
                new StationPlatformOptionResponse(11L, "XL", "星络平台")
        ));
        String token = loginAsBusinessAdmin();

        mockMvc.perform(get("/api/stations/platform-options")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].itemId").value(11L))
                .andExpect(jsonPath("$.data[0].itemName").value("星络平台"));
    }

    @Test
    void listStatusOptionsShouldReturnEnabledStationStatusItemsForAuthenticatedUser() throws Exception {
        when(stationFormOptionQueryService.listStatusOptions()).thenReturn(List.of(
                new StationStatusOptionResponse(21L, "OPERATING", "运营中")
        ));
        String token = loginAsBusinessAdmin();

        mockMvc.perform(get("/api/stations/status-options")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].itemId").value(21L))
                .andExpect(jsonPath("$.data[0].itemName").value("运营中"));
    }

    @Test
    void listStationsShouldPassStatusItemIdToQueryService() throws Exception {
        when(stationQueryService.listStations(null, null, 21L)).thenReturn(List.of(
                new StationListItemResponse(
                        1003L,
                        "ST-202608-0003",
                        "河西智慧充电站",
                        "手工验收平台",
                        "PT-330021",
                        "王工",
                        "13800138002",
                        "运营中",
                        LocalDateTime.of(2026, 8, 30, 9, 10)
                )
        ));
        String token = loginAsBusinessAdmin();

        mockMvc.perform(get("/api/stations")
                        .param("stationStatusItemId", "21")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].stationStatusName").value("运营中"));

        verify(stationQueryService).listStations(null, null, 21L);
    }

    @Test
    void createStationShouldReturnSuccessForAuthenticatedUser() throws Exception {
        when(stationCommandService.createStation(org.mockito.ArgumentMatchers.any())).thenReturn(1001L);
        String token = loginAsBusinessAdmin();

        mockMvc.perform(post("/api/stations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "stationName": "雨花城际快充港",
                                  "platformItemId": 11,
                                  "platformStationCode": "XL-4301-889",
                                  "address": "长沙市雨花区城际大道南段",
                                  "contactName": "张海峰",
                                  "contactPhone": "13800138000"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(1001L));
    }

    @Test
    void getStationDetailShouldReturnDetailForAuthenticatedUser() throws Exception {
        when(stationQueryService.getStationDetail(1001L)).thenReturn(new StationDetailResponse(
                1001L,
                "ST-202608-0001",
                "雨花城际快充港",
                11L,
                "星络平台",
                "XL-4301-889",
                "长沙市雨花区城际大道南段",
                "张海峰",
                "13800138000",
                21L,
                "签合同"
        ));
        String token = loginAsBusinessAdmin();

        mockMvc.perform(get("/api/stations/1001")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.stationCode").value("ST-202608-0001"))
                .andExpect(jsonPath("$.data.stationStatusName").value("签合同"));
    }

    @Test
    void updateStationShouldReturnSuccessForAuthenticatedUser() throws Exception {
        when(stationCommandService.updateStation(org.mockito.ArgumentMatchers.eq(1001L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(1001L);
        String token = loginAsBusinessAdmin();

        mockMvc.perform(put("/api/stations/1001")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "stationName": "雨花城际快充港（更新）",
                                  "platformItemId": 11,
                                  "platformStationCode": "XL-4301-990",
                                  "address": "长沙市雨花区城际大道南段 99 号",
                                  "contactName": "李工",
                                  "contactPhone": "13800138111"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(1001L));
    }

    @Test
    void updateStationStatusShouldReturnSuccessForAuthenticatedUser() throws Exception {
        when(stationCommandService.updateStationStatus(
                org.mockito.ArgumentMatchers.eq(1001L),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq("13800000001")
        )).thenReturn(1001L);
        String token = loginAsBusinessAdmin();

        mockMvc.perform(put("/api/stations/1001/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "stationStatusItemId": 22,
                                  "remark": "设备检修完成，恢复运营"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(1001L));

        verify(stationCommandService).updateStationStatus(
                org.mockito.ArgumentMatchers.eq(1001L),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq("13800000001")
        );
    }

    @Test
    void updateStationStatusShouldRejectAnonymousRequest() throws Exception {
        mockMvc.perform(put("/api/stations/1001/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "stationStatusItemId": 22,
                                  "remark": "设备检修完成，恢复运营"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void updateStationStatusShouldRejectRemarkOverLimit() throws Exception {
        String token = loginAsBusinessAdmin();

        mockMvc.perform(put("/api/stations/1001/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "stationStatusItemId": 22,
                                  "remark": "%s"
                                }
                                """.formatted("超".repeat(501))))
                .andExpect(status().isBadRequest());
    }
}
