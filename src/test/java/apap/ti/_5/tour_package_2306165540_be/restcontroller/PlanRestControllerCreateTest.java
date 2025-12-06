package apap.ti._5.tour_package_2306165540_be.restcontroller;

import apap.ti._5.tour_package_2306165540_be.restdto.request.CreatePlanRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PlanResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restservice.PlanRestService;
import apap.ti._5.tour_package_2306165540_be.security.jwt.JwtTokenFilter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlanRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlanRestControllerCreateTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlanRestService planRestService;

    @MockBean
    private JwtTokenFilter jwtTokenFilter;

    @Test
    void createPlan_shouldReturnCreated() throws Exception {
        Mockito.when(planRestService.createPlan(any(), any())).thenReturn(new PlanResponseDTO());

        String body = "{\n  \"planName\": \"X\",\n  \"activityType\": \"Flight\",\n  \"startDate\": \"2025-11-01T08:00:00\",\n  \"endDate\": \"2025-11-01T10:00:00\",\n  \"startLocation\": \"A\",\n  \"endLocation\": \"B\"\n}";

        mockMvc.perform(post("/api/packages/PKG-1/plans/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201));
    }

    @Test
    void createPlan_shouldReturnBadRequest_whenServiceThrows() throws Exception {
        Mockito.when(planRestService.createPlan(any(), any())).thenThrow(new RuntimeException("Invalid"));

        String body = "{\n  \"planName\": \"X\",\n  \"activityType\": \"Flight\",\n  \"startDate\": \"2025-11-01T08:00:00\",\n  \"endDate\": \"2025-11-01T10:00:00\",\n  \"startLocation\": \"A\",\n  \"endLocation\": \"B\"\n}";

        mockMvc.perform(post("/api/packages/PKG-1/plans/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCreatePlanPage_shouldReturnOk() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/api/packages/PKG-1/plans/create"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }
}
