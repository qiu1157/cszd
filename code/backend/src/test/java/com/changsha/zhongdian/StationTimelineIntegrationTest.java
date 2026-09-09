import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
public class StationTimelineIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testStationTimelineForST_202608_0001() throws Exception {
        String loginPayload = "{\"account\":\"admin\",\"password\":\"Admin@123456\"}";
        
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.token").exists())
            .andReturn();
        
        String responseBody = loginResult.getResponse().getContentAsString();
        String token = extractToken(responseBody);
        
        System.out.println("=== 测试充电站 ST-202608-0001 的时间线接口 ===");
        System.out.println("Token: " + token);
        
        MvcResult timelineResult = mockMvc.perform(get("/api/operation-logs/timeline/station/2")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();
        
        String timelineResponse = timelineResult.getResponse().getContentAsString();
        System.out.println("Timeline Response: " + timelineResponse);
    }
    
    private String extractToken(String jsonResponse) {
        int tokenStart = jsonResponse.indexOf("\"token\":\"") + 9;
        int tokenEnd = jsonResponse.indexOf("\"", tokenStart);
        return jsonResponse.substring(tokenStart, tokenEnd);
    }
}
