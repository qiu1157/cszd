package com.changsha.zhongdian.pile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import com.changsha.zhongdian.AuthenticatedWebTestSupport;
import com.changsha.zhongdian.common.exception.BusinessException;
import com.changsha.zhongdian.pile.dto.PileCreateRequest;
import com.changsha.zhongdian.pile.dto.PileListItemResponse;
import com.changsha.zhongdian.pile.dto.PileStatusOptionResponse;
import com.changsha.zhongdian.pile.dto.PileUpdateRequest;
import com.changsha.zhongdian.pile.entity.Pile;
import com.changsha.zhongdian.pile.service.PileCommandService;
import com.changsha.zhongdian.pile.service.PileFormOptionQueryService;
import com.changsha.zhongdian.pile.service.PileQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

// 为什么：继承无数据库测试基类统一提供登录与 Repository mock，本类聚焦桩接口的 Web 层行为
class PileControllerTest extends AuthenticatedWebTestSupport {

    @MockBean
    private PileQueryService pileQueryService;

    @MockBean
    private PileCommandService pileCommandService;

    @MockBean
    private PileFormOptionQueryService pileFormOptionQueryService;

    @Test
    void listPilesShouldReturnPileRowsForAuthenticatedUser() throws Exception {
        when(pileQueryService.listPilesByStationId(1001L)).thenReturn(List.of(
                new PileListItemResponse(
                        21L,
                        "PL-202609-0001",
                        "FAST",
                        "快充",
                        2,
                        "运营中",
                        LocalDateTime.of(2026, 9, 1, 10, 0)
                )
        ));
        String token = loginAsBusinessAdmin();

        mockMvc.perform(get("/api/stations/1001/piles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].pileCode").value("PL-202609-0001"))
                .andExpect(jsonPath("$.data[0].chargeType").value("FAST"))
                .andExpect(jsonPath("$.data[0].chargeTypeName").value("快充"))
                .andExpect(jsonPath("$.data[0].gunCount").value(2))
                .andExpect(jsonPath("$.data[0].pileStatusName").value("运营中"));
    }

    @Test
    void listPilesShouldRejectAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/stations/1001/piles"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void listPilesShouldReturnNotFoundWhenStationMissing() throws Exception {
        when(pileQueryService.listPilesByStationId(eq(999L)))
                .thenThrow(new BusinessException("STATION_NOT_FOUND", "充电站不存在", HttpStatus.NOT_FOUND));
        String token = loginAsBusinessAdmin();

        mockMvc.perform(get("/api/stations/999/piles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("STATION_NOT_FOUND"));
    }

    @Test
    void nextPileCodeShouldReturnGeneratedCodeForAuthenticatedUser() throws Exception {
        when(pileQueryService.nextPileCode(1001L)).thenReturn("PL-202609-0001");
        String token = loginAsBusinessAdmin();

        mockMvc.perform(get("/api/stations/1001/piles/next-code")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pileCode").value("PL-202609-0001"));
    }

    @Test
    void createPileShouldReturnSuccessForAuthenticatedUser() throws Exception {
        Pile mockPile = new Pile();
        try {
            java.lang.reflect.Field idField = Pile.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mockPile, 21L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        when(pileCommandService.createPile(eq(1001L), any(PileCreateRequest.class))).thenReturn(mockPile);
        String token = loginAsBusinessAdmin();

        mockMvc.perform(post("/api/stations/1001/piles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "chargeType": "FAST",
                                  "gunCount": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(21L));
    }

    @Test
    void createPileShouldRejectAnonymousRequest() throws Exception {
        mockMvc.perform(post("/api/stations/1001/piles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "chargeType": "FAST",
                                  "gunCount": 2
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void createPileShouldRejectInvalidGunCount() throws Exception {
        String token = loginAsBusinessAdmin();

        mockMvc.perform(post("/api/stations/1001/piles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "chargeType": "FAST",
                                  "gunCount": 0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatePileShouldReturnSuccessForAuthenticatedUser() throws Exception {
        Pile mockPile = new Pile();
        try {
            java.lang.reflect.Field idField = Pile.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mockPile, 21L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        when(pileCommandService.updatePile(eq(1001L), eq(21L), any(PileUpdateRequest.class)))
                .thenReturn(mockPile);
        String token = loginAsBusinessAdmin();

        mockMvc.perform(put("/api/stations/1001/piles/21")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "chargeType": "SLOW",
                                  "gunCount": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(21L));
    }

    @Test
    void updatePileShouldRejectAnonymousRequest() throws Exception {
        mockMvc.perform(put("/api/stations/1001/piles/21")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "chargeType": "SLOW",
                                  "gunCount": 1
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void updatePileShouldReturnNotFoundWhenPileMissing() throws Exception {
        when(pileCommandService.updatePile(eq(1001L), eq(99L), any(PileUpdateRequest.class)))
                .thenThrow(new BusinessException("PILE_NOT_FOUND", "充电桩不存在", HttpStatus.NOT_FOUND));
        String token = loginAsBusinessAdmin();

        mockMvc.perform(put("/api/stations/1001/piles/99")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "chargeType": "SLOW",
                                  "gunCount": 1
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PILE_NOT_FOUND"));
    }

    @Test
    void listPileStatusOptionsShouldReturnEnabledStatusesForAuthenticatedUser() throws Exception {
        when(pileFormOptionQueryService.listStatusOptions()).thenReturn(List.of(
                new PileStatusOptionResponse(31L, "IN_OPERATION", "运营中"),
                new PileStatusOptionResponse(32L, "ABNORMAL", "运营异常"),
                new PileStatusOptionResponse(33L, "DISABLED", "停用")
        ));
        String token = loginAsBusinessAdmin();

        mockMvc.perform(get("/api/stations/1001/piles/status-options")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].itemId").value(31L))
                .andExpect(jsonPath("$.data[0].itemName").value("运营中"))
                .andExpect(jsonPath("$.data[2].itemName").value("停用"));
    }

    @Test
    void listPileStatusOptionsShouldRejectAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/stations/1001/piles/status-options"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void updatePileStatusShouldReturnSuccessForAuthenticatedUser() throws Exception {
        Pile mockPile = new Pile();
        try {
            java.lang.reflect.Field idField = Pile.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mockPile, 21L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        when(pileCommandService.updatePileStatus(eq(1001L), eq(21L), any(), anyString()))
                .thenReturn(mockPile);
        String token = loginAsBusinessAdmin();

        mockMvc.perform(put("/api/stations/1001/piles/21/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pileStatusItemId": 33,
                                  "remark": "设备检修停用"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("充电桩状态切换成功"))
                .andExpect(jsonPath("$.data").value(21L));
    }

    @Test
    void updatePileStatusShouldRejectAnonymousRequest() throws Exception {
        mockMvc.perform(put("/api/stations/1001/piles/21/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pileStatusItemId": 33
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void updatePileStatusShouldRejectMissingStatusItemId() throws Exception {
        String token = loginAsBusinessAdmin();

        mockMvc.perform(put("/api/stations/1001/piles/21/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "remark": "缺少状态"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatePileStatusShouldReturnNotFoundWhenPileMissing() throws Exception {
        when(pileCommandService.updatePileStatus(eq(1001L), eq(99L), any(), anyString()))
                .thenThrow(new BusinessException("PILE_NOT_FOUND", "充电桩不存在", HttpStatus.NOT_FOUND));
        String token = loginAsBusinessAdmin();

        mockMvc.perform(put("/api/stations/1001/piles/99/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pileStatusItemId": 33
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PILE_NOT_FOUND"));
    }
}
