package com.changsha.zhongdian.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class UserControllerAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

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
    void preflightRequestShouldPassWithoutAuthorization() throws Exception {
        mockMvc.perform(options("/api/users/business-admins")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());
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
