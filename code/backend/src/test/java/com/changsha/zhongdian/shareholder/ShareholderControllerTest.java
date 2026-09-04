package com.changsha.zhongdian.shareholder;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import com.changsha.zhongdian.AuthenticatedWebTestSupport;
import com.changsha.zhongdian.shareholder.dto.ShareholderListItemResponse;
import com.changsha.zhongdian.shareholder.service.ShareholderCommandService;
import com.changsha.zhongdian.shareholder.service.ShareholderQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

// 为什么：继承无数据库测试基类统一提供登录与 Repository mock，本类额外 mock 股东查询/命令服务以聚焦 Web 层行为
class ShareholderControllerTest extends AuthenticatedWebTestSupport {

    @MockBean
    private ShareholderQueryService shareholderQueryService;

    @MockBean
    private ShareholderCommandService shareholderCommandService;

    @Test
    void listShareholdersShouldReturnShareholderRowsForAuthenticatedUser() throws Exception {
        when(shareholderQueryService.listShareholders(null, null)).thenReturn(List.of(
                new ShareholderListItemResponse(
                        2001L,
                        "长沙城新投资有限公司",
                        "刘可",
                        "13977882211",
                        "ENABLED",
                        LocalDateTime.of(2026, 9, 4, 9, 40)
                )
        ));
        String token = loginAsBusinessAdmin();

        mockMvc.perform(get("/api/shareholders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].shareholderName").value("长沙城新投资有限公司"))
                .andExpect(jsonPath("$.data[0].contactName").value("刘可"))
                .andExpect(jsonPath("$.data[0].shareholderStatus").value("ENABLED"));
    }

    @Test
    void listShareholdersShouldPassNameKeywordAndStatusToQueryService() throws Exception {
        when(shareholderQueryService.listShareholders("城新", "ENABLED")).thenReturn(List.of());
        String token = loginAsBusinessAdmin();

        mockMvc.perform(get("/api/shareholders")
                        .param("shareholderName", "城新")
                        .param("shareholderStatus", "ENABLED")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(shareholderQueryService).listShareholders("城新", "ENABLED");
    }

    @Test
    void listShareholdersShouldRejectAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/shareholders"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void createShareholderShouldReturnNewIdForAuthenticatedUser() throws Exception {
        when(shareholderCommandService.createShareholder(org.mockito.ArgumentMatchers.any())).thenReturn(3001L);
        String token = loginAsBusinessAdmin();

        mockMvc.perform(post("/api/shareholders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "shareholderName": "长沙城新能源合伙企业",
                                  "contactName": "刘可",
                                  "contactPhone": "13977882211",
                                  "shareholderStatus": "ENABLED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(3001))
                .andExpect(jsonPath("$.message").value("新增股东成功"));
    }

    @Test
    void createShareholderShouldRejectAnonymousRequest() throws Exception {
        mockMvc.perform(post("/api/shareholders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "shareholderName": "长沙城新能源合伙企业",
                                  "shareholderStatus": "ENABLED"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
}