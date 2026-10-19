package com.changsha.zhongdian.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.changsha.zhongdian.AuthenticatedWebTestSupport;
import com.changsha.zhongdian.user.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class AuthControllerTest extends AuthenticatedWebTestSupport {

    @Test
    void loginShouldReturnTokenAndCurrentUserProfile() throws Exception {
        stubDatabaseUser("13800000001", "dev-biz-password", UserRole.BUSINESS_ADMIN, "业务管理员");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "account": "13800000001",
                                  "password": "dev-biz-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.user.account").value("13800000001"))
                .andExpect(jsonPath("$.data.user.phone").value("13800000001"))
                .andExpect(jsonPath("$.data.user.status").value("ENABLED"))
                .andExpect(jsonPath("$.data.user.lastLoginAt").isNotEmpty())
                .andExpect(jsonPath("$.data.user.role").value("BUSINESS_ADMIN"))
                .andReturn();

        String token = extractToken(loginResult);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.account").value("13800000001"))
                .andExpect(jsonPath("$.data.phone").value("13800000001"))
                .andExpect(jsonPath("$.data.status").value("ENABLED"))
                .andExpect(jsonPath("$.data.lastLoginAt").isNotEmpty())
                .andExpect(jsonPath("$.data.role").value("BUSINESS_ADMIN"));
    }

    @Test
    void loginShouldRejectWhenDatabaseBackedAccountIsUnavailable() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "account": "admin",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTH_INVALID_CREDENTIALS"));
    }

    @Test
    void currentUserShouldRejectAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void loginPreflightShouldPassBehindReverseProxyWhenOriginMatchesForwardedHost() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://192.168.3.52:9080")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type")
                        .header("X-Forwarded-Proto", "http")
                        .header("X-Forwarded-Host", "192.168.3.52:9080")
                        .header("X-Forwarded-Port", "9080"))
                .andExpect(status().isOk());
    }
}
