package com.changsha.zhongdian.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.changsha.zhongdian.AuthenticatedWebTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

// 为什么：继承无数据库测试基类统一提供登录与 Repository mock，本类聚焦用户管理接口的角色鉴权
class UserControllerAuthorizationTest extends AuthenticatedWebTestSupport {

    @Test
    void businessAdminShouldNotCreateBusinessAdmin() throws Exception {
        String token = loginAsBusinessAdmin();

        mockMvc.perform(post("/api/users/business-admins")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "13800138000",
                                  "name": "李丹",
                                  "initialPassword": "Init@123456"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void businessAdminShouldNotListBusinessAdmins() throws Exception {
        String token = loginAsBusinessAdmin();

        mockMvc.perform(get("/api/users/business-admins")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void businessAdminShouldNotDisableBusinessAdmin() throws Exception {
        String token = loginAsBusinessAdmin();

        mockMvc.perform(post("/api/users/business-admins/13800138000/disable")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void businessAdminShouldNotResetBusinessAdminPassword() throws Exception {
        String token = loginAsBusinessAdmin();

        mockMvc.perform(post("/api/users/business-admins/13800138000/reset-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "newPassword": "Reset123"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void preflightRequestShouldPassWithoutAuthorization() throws Exception {
        mockMvc.perform(options("/api/users/business-admins")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());
    }
}
