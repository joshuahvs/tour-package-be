package apap.ti._5.tour_package_2306165540_be.restcontroller;

import apap.ti._5.tour_package_2306165540_be.restdto.request.UpdatePlanRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PlanDetailResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PlanResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restservice.PlanRestService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlanEditRestController.class)
class PlanEditRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlanRestService planRestService;

    @Test
    void getEditPlanPage_shouldReturnOk() throws Exception {
        Mockito.when(planRestService.getPlanDetail(any())).thenReturn(new PlanDetailResponseDTO());

        mockMvc.perform(get("/api/plans/" + UUID.randomUUID() + "/edit").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void updatePlan_shouldReturnOk() throws Exception {
        Mockito.when(planRestService.updatePlan(any(), any())).thenReturn(new PlanDetailResponseDTO());

        String body = "{\n  \"planName\": \"X\",\n  \"startDate\": \"2025-11-01T08:00:00\",\n  \"endDate\": \"2025-11-01T10:00:00\",\n  \"startLocation\": \"A\",\n  \"endLocation\": \"B\"\n}";

        mockMvc.perform(put("/api/plans/" + UUID.randomUUID() + "/edit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void getAvailableActivities_shouldReturnOk() throws Exception {
        Mockito.when(planRestService.getAvailablePlansForActivity(any())).thenReturn(List.of(new PlanResponseDTO()));
        mockMvc.perform(
                get("/api/plans/" + UUID.randomUUID() + "/available-activities").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void getEditPlanPage_notFound() throws Exception {
        Mockito.when(planRestService.getPlanDetail(any())).thenThrow(new RuntimeException("Plan not found"));

        mockMvc.perform(get("/api/plans/" + UUID.randomUUID() + "/edit").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePlan_badRequest() throws Exception {
        Mockito.when(planRestService.updatePlan(any(), any())).thenThrow(new RuntimeException("Invalid"));

        String body = "{\n  \"planName\": \"X\",\n  \"startDate\": \"2025-11-01T08:00:00\",\n  \"endDate\": \"2025-11-01T10:00:00\",\n  \"startLocation\": \"A\",\n  \"endLocation\": \"B\"\n}";

        mockMvc.perform(put("/api/plans/" + UUID.randomUUID() + "/edit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAvailableActivities_notFound() throws Exception {
        Mockito.when(planRestService.getAvailablePlansForActivity(any())).thenThrow(new RuntimeException("Not found"));
        mockMvc.perform(
                get("/api/plans/" + UUID.randomUUID() + "/available-activities").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePlan_serverError() throws Exception {
        Mockito.when(planRestService.updatePlan(any(), any())).thenAnswer(inv -> {
            throw new Exception("boom");
        });

        String body = "{\n  \"planName\": \"X\",\n  \"startDate\": \"2025-11-01T08:00:00\",\n  \"endDate\": \"2025-11-01T10:00:00\",\n  \"startLocation\": \"A\",\n  \"endLocation\": \"B\"\n}";

        mockMvc.perform(put("/api/plans/" + UUID.randomUUID() + "/edit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }
}
