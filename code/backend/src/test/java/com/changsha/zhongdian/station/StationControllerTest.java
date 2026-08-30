package com.changsha.zhongdian.station;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import com.changsha.zhongdian.station.dto.StationListItemResponse;
import com.changsha.zhongdian.station.dto.StationPlatformOptionResponse;
import com.changsha.zhongdian.station.service.StationCodeGenerator;
import com.changsha.zhongdian.station.service.StationCommandService;
import com.changsha.zhongdian.station.service.StationFormOptionQueryService;
import com.changsha.zhongdian.station.service.StationQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
@AutoConfigureMockMvc
class StationControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
        when(stationQueryService.listStations()).thenReturn(List.of(
                new StationListItemResponse(
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

    private String loginAsBusinessAdmin() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "account": "13800000001",
                                  "password": "dev-biz-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        return responseBody.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }
}
