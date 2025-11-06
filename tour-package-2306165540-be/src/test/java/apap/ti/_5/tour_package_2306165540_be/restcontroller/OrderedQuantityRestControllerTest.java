package apap.ti._5.tour_package_2306165540_be.restcontroller;

import apap.ti._5.tour_package_2306165540_be.restdto.request.AddOrderedQuantityRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.request.UpdateOrderedQuantityRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PlanDetailResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restservice.PlanRestService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderedQuantityRestController.class)
class OrderedQuantityRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlanRestService planRestService;

    @Test
    void addOrderedQuantity_shouldReturnOk() throws Exception {
        Mockito.when(planRestService.addOrderedQuantity(any(), any())).thenReturn(new PlanDetailResponseDTO());

        String body = "{\n  \"activityId\": \"" + UUID.randomUUID() + "\",\n  \"orderedQuantity\": 3\n}";
        mockMvc.perform(post("/api/ordered-activities/create?planId=" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void updateOrderedQuantity_shouldReturnOk() throws Exception {
        Mockito.when(planRestService.updateOrderedQuantity(any(), any())).thenReturn(new PlanDetailResponseDTO());

        String body = "{\n  \"orderedQuantity\": 5\n}";
        mockMvc.perform(put("/api/ordered-activities/" + UUID.randomUUID() + "/edit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void deleteOrderedQuantity_shouldReturnOk() throws Exception {
        Mockito.when(planRestService.deleteOrderedQuantity(any())).thenReturn(new PlanDetailResponseDTO());

        mockMvc.perform(delete("/api/ordered-activities/" + UUID.randomUUID() + "/delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void addOrderedQuantity_shouldReturnBadRequest_whenServiceThrows() throws Exception {
        Mockito.when(planRestService.addOrderedQuantity(any(), any())).thenThrow(new RuntimeException("Invalid"));

        String body = "{\n  \"activityId\": \"" + UUID.randomUUID() + "\",\n  \"orderedQuantity\": 0\n}";
        mockMvc.perform(post("/api/ordered-activities/create?planId=" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateOrderedQuantity_shouldReturnBadRequest_whenServiceThrows() throws Exception {
        Mockito.when(planRestService.updateOrderedQuantity(any(), any())).thenThrow(new RuntimeException("Too many"));

        String body = "{\n  \"orderedQuantity\": -1\n}";
        mockMvc.perform(put("/api/ordered-activities/" + UUID.randomUUID() + "/edit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteOrderedQuantity_shouldReturnBadRequest_whenServiceThrows() throws Exception {
        Mockito.when(planRestService.deleteOrderedQuantity(any())).thenThrow(new RuntimeException("Cannot delete"));

        mockMvc.perform(delete("/api/ordered-activities/" + UUID.randomUUID() + "/delete"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addOrderedQuantity_serverError() throws Exception {
        Mockito.when(planRestService.addOrderedQuantity(any(), any())).thenAnswer(inv -> {
            throw new Exception("boom");
        });
        String body = "{\n  \"activityId\": \"" + UUID.randomUUID() + "\",\n  \"orderedQuantity\": 1\n}";
        mockMvc.perform(post("/api/ordered-activities/create?planId=" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void updateOrderedQuantity_serverError() throws Exception {
        Mockito.when(planRestService.updateOrderedQuantity(any(), any())).thenAnswer(inv -> {
            throw new Exception("boom");
        });
        String body = "{\n  \"orderedQuantity\": 2\n}";
        mockMvc.perform(put("/api/ordered-activities/" + UUID.randomUUID() + "/edit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void deleteOrderedQuantity_serverError() throws Exception {
        Mockito.when(planRestService.deleteOrderedQuantity(any())).thenAnswer(inv -> {
            throw new Exception("boom");
        });
        mockMvc.perform(delete("/api/ordered-activities/" + UUID.randomUUID() + "/delete"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }
}
