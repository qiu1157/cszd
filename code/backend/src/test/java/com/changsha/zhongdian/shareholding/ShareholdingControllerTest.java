package com.changsha.zhongdian.shareholding;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.changsha.zhongdian.AuthenticatedWebTestSupport;
import com.changsha.zhongdian.shareholding.dto.ShareholdingCreateRequest;
import com.changsha.zhongdian.shareholding.dto.StationShareholdingListItemResponse;
import com.changsha.zhongdian.shareholding.entity.HoldingStatus;
import com.changsha.zhongdian.shareholding.entity.Shareholding;
import com.changsha.zhongdian.shareholding.service.ShareholdingCommandService;
import com.changsha.zhongdian.shareholding.service.ShareholdingQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

// 为什么：继承无数据库测试基类统一提供登录与 Repository mock，本类额外 mock 持股查询/命令服务以聚焦 Web 层行为
class ShareholdingControllerTest extends AuthenticatedWebTestSupport {

    @MockBean
    private ShareholdingQueryService shareholdingQueryService;

    @MockBean
    private ShareholdingCommandService shareholdingCommandService;

    @Test
    void listStationShareholdingsShouldReturnRelationsForAuthenticatedUser() throws Exception {
        when(shareholdingQueryService.getStationShareholdings(2L, null)).thenReturn(List.of(
                new StationShareholdingListItemResponse(
                        9001L,
                        1L,
                        "长沙众电电气服务有限公司",
                        "张伟",
                        "13800000002",
                        new BigDecimal("60"),
                        "ACTIVE",
                        "持股中",
                        LocalDateTime.of(2026, 9, 4, 10, 0)
                )
        ));
        String token = loginAsBusinessAdmin();

        mockMvc.perform(get("/api/stations/2/shareholdings")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].shareholderName").value("长沙众电电气服务有限公司"))
                .andExpect(jsonPath("$.data[0].contactPhone").value("13800000002"))
                .andExpect(jsonPath("$.data[0].ownershipPercentage").value(60))
                .andExpect(jsonPath("$.data[0].holdingStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.data[0].holdingStatusName").value("持股中"));

        verify(shareholdingQueryService).getStationShareholdings(2L, null);
    }

    @Test
    void listStationShareholdingsShouldRejectAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/stations/2/shareholdings"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void shouldPassActiveStatusToQueryService() throws Exception {
        when(shareholdingQueryService.getStationShareholdings(1L, HoldingStatus.ACTIVE)).thenReturn(List.of());
        String token = loginAsBusinessAdmin();

        mockMvc.perform(get("/api/stations/1/shareholdings")
                        .param("holdingStatus", "ACTIVE")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(shareholdingQueryService).getStationShareholdings(1L, HoldingStatus.ACTIVE);
    }

    @Test
    void shouldRejectInvalidHoldingStatus() throws Exception {
        String token = loginAsBusinessAdmin();

        mockMvc.perform(get("/api/stations/1/shareholdings")
                        .param("holdingStatus", "INVALID")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("持股状态参数非法，仅支持 ACTIVE / EXITED"));
    }

    @Test
    void createShareholdingShouldReturnIdForAuthenticatedUser() throws Exception {
        ShareholdingCreateRequest request = new ShareholdingCreateRequest(
                1L,
                new BigDecimal("30"),
                "ACTIVE"
        );
        Shareholding mockShareholding = new Shareholding();
        try {
            java.lang.reflect.Field idField = Shareholding.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mockShareholding, 9001L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        when(shareholdingCommandService.createShareholding(2L, request)).thenReturn(mockShareholding);
        String token = loginAsBusinessAdmin();

        mockMvc.perform(post("/api/stations/2/shareholdings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "shareholderId": 1,
                                  "ownershipPercentage": 30,
                                  "holdingStatus": "ACTIVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("关联股东成功"))
                .andExpect(jsonPath("$.data").value(9001));

        verify(shareholdingCommandService).createShareholding(2L, request);
    }

    @Test
    void createShareholdingShouldRejectAnonymousRequest() throws Exception {
        mockMvc.perform(post("/api/stations/2/shareholdings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "shareholderId": 1,
                                  "ownershipPercentage": 30,
                                  "holdingStatus": "ACTIVE"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void exitShareholdingShouldReturnIdForAuthenticatedUser() throws Exception {
        Shareholding mockShareholding = new Shareholding();
        try {
            java.lang.reflect.Field idField = Shareholding.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mockShareholding, 9001L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        when(shareholdingCommandService.exitShareholding(2L, 9001L)).thenReturn(mockShareholding);
        String token = loginAsBusinessAdmin();

        mockMvc.perform(put("/api/stations/2/shareholdings/9001/exit")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("退股成功"))
                .andExpect(jsonPath("$.data").value(9001));

        verify(shareholdingCommandService).exitShareholding(2L, 9001L);
    }

    @Test
    void exitShareholdingShouldRejectAnonymousRequest() throws Exception {
        mockMvc.perform(put("/api/stations/2/shareholdings/9001/exit"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
}