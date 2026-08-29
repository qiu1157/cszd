package com.changsha.zhongdian.station;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.changsha.zhongdian.station.service.StationCodeGenerator;
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
