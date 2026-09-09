package com.changsha.zhongdian.station;

import com.changsha.zhongdian.AuthenticatedWebTestSupport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
public class StationMeterAccountNumberIntegrationTest extends AuthenticatedWebTestSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateStationWithMeterAccountNumber() throws Exception {
        String token = loginAsBusinessAdmin();

        String createRequest = """
                {
                    "stationName": "测试充电站-电表户号",
                    "platformItemId": 11,
                    "platformStationCode": "TEST-METER-001",
                    "address": "测试地址",
                    "contactName": "测试联系人",
                    "contactPhone": "13800138000",
                    "meterAccountNumber": "0101234567890"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/stations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(response);
        Long stationId = jsonNode.get("data").get("id").asLong();

        mockMvc.perform(get("/api/stations/" + stationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.meterAccountNumber").value("0101234567890"));
    }

    @Test
    void shouldUpdateStationMeterAccountNumber() throws Exception {
        String token = loginAsBusinessAdmin();

        String createRequest = """
                {
                    "stationName": "测试充电站-更新电表户号",
                    "platformItemId": 11,
                    "platformStationCode": "TEST-METER-002",
                    "address": "测试地址",
                    "contactName": "测试联系人",
                    "contactPhone": "13800138000",
                    "meterAccountNumber": "0101111111111"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/stations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isOk())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        JsonNode createJsonNode = objectMapper.readTree(createResponse);
        Long stationId = createJsonNode.get("data").get("id").asLong();

        String updateRequest = """
                {
                    "stationName": "测试充电站-更新电表户号",
                    "platformItemId": 11,
                    "platformStationCode": "TEST-METER-002",
                    "address": "测试地址",
                    "contactName": "测试联系人",
                    "contactPhone": "13800138000",
                    "meterAccountNumber": "0102222222222"
                }
                """;

        mockMvc.perform(put("/api/stations/" + stationId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/stations/" + stationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.meterAccountNumber").value("0102222222222"));
    }

    @Test
    void shouldAllowNullMeterAccountNumber() throws Exception {
        String token = loginAsBusinessAdmin();

        String createRequest = """
                {
                    "stationName": "测试充电站-无电表户号",
                    "platformItemId": 11,
                    "platformStationCode": "TEST-METER-003",
                    "address": "测试地址",
                    "contactName": "测试联系人",
                    "contactPhone": "13800138000"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/stations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(response);
        Long stationId = jsonNode.get("data").get("id").asLong();

        mockMvc.perform(get("/api/stations/" + stationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.meterAccountNumber").isEmpty());
    }
}
