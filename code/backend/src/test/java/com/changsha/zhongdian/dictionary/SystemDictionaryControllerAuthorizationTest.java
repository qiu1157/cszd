package com.changsha.zhongdian.dictionary;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
@AutoConfigureMockMvc
class SystemDictionaryControllerAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

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

    private String loginAsBusinessAdmin() throws Exception {
        MvcResult loginResult = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/login")
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
