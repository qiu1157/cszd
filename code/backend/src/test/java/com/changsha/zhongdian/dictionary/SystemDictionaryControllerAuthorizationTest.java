package com.changsha.zhongdian.dictionary;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.changsha.zhongdian.AuthenticatedWebTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

// 为什么：继承无数据库测试基类统一提供登录与 Repository mock，本类聚焦系统字典接口的角色鉴权
class SystemDictionaryControllerAuthorizationTest extends AuthenticatedWebTestSupport {

    @Test
    void businessAdminShouldNotAccessSystemDictionaryTypes() throws Exception {
        String token = loginAsBusinessAdmin();

        mockMvc.perform(get("/api/system-dictionaries/types")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void businessAdminShouldNotAccessSystemDictionaryItems() throws Exception {
        String token = loginAsBusinessAdmin();

        mockMvc.perform(get("/api/system-dictionaries/PLATFORM/items")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void businessAdminShouldNotCreateSystemDictionaryItem() throws Exception {
        String token = loginAsBusinessAdmin();

        mockMvc.perform(post("/api/system-dictionaries/PLATFORM/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "itemCode": "STATE_GRID",
                                  "itemName": "国网",
                                  "status": "ENABLED",
                                  "sortOrder": 10
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void businessAdminShouldNotUpdateSystemDictionaryItem() throws Exception {
        String token = loginAsBusinessAdmin();

        mockMvc.perform(put("/api/system-dictionaries/PLATFORM/items/8")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "itemCode": "STATE_GRID",
                                  "itemName": "国网",
                                  "status": "DISABLED",
                                  "sortOrder": 20
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void businessAdminShouldNotChangeSystemDictionaryItemStatus() throws Exception {
        String token = loginAsBusinessAdmin();

        mockMvc.perform(post("/api/system-dictionaries/PLATFORM/items/8/enable")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }
}
