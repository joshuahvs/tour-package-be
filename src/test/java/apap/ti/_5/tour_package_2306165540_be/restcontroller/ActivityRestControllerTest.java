package apap.ti._5.tour_package_2306165540_be.restcontroller;

import apap.ti._5.tour_package_2306165540_be.restdto.response.ActivityResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restservice.ActivityRestService;
import apap.ti._5.tour_package_2306165540_be.security.jwt.JwtTokenFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ActivityRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class ActivityRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActivityRestService activityRestService;

    @MockBean
    private JwtTokenFilter jwtTokenFilter;

    private ActivityResponseDTO sampleActivity() {
        return new ActivityResponseDTO(
                "ACT-1",
                "Hotel",
                "Room",
                100,
                1_000L,
                "Flight",
                "creator-1",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2),
                "CGK",
                "DPS",
                false);
    }

    @Test
    void getAllActivities_shouldReturnList() throws Exception {
        when(activityRestService.getAllActivities(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(sampleActivity()));

        mockMvc.perform(get("/api/activities").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].id").value("ACT-1"));
    }

    @Test
    void getAllActivities_shouldReturnOk() throws Exception {
        ActivityResponseDTO dto = sampleActivity();
        dto.setActivityType("Flight");
        dto.setActivityItem("Ticket");
        when(activityRestService.getAllActivities(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/activities").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].activityType").value("Flight"));
    }

    @Test
    void getAllActivities_shouldReturnServerError_whenRepositoryThrows() throws Exception {
        when(activityRestService.getAllActivities(any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("DB down"));

        mockMvc.perform(get("/api/activities").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void getAllActivities_shouldReturnBadRequest_whenValidationFails() throws Exception {
        when(activityRestService.getAllActivities(any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Invalid date"));

        mockMvc.perform(get("/api/activities").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid date"));
    }

    @Test
    void getActivityById_shouldReturnOk() throws Exception {
        when(activityRestService.getActivityById("ACT-1")).thenReturn(sampleActivity());

        mockMvc.perform(get("/api/activities/ACT-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("ACT-1"));
    }

    @Test
    void getActivityById_notFound() throws Exception {
        when(activityRestService.getActivityById("missing"))
                .thenThrow(new RuntimeException("Activity not found"));

        mockMvc.perform(get("/api/activities/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Activity not found"));
    }

    @Test
    void getActivityById_serverError() throws Exception {
        when(activityRestService.getActivityById("err"))
                .thenAnswer(inv -> {
                    throw new Exception("boom");
                });

        mockMvc.perform(get("/api/activities/err"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void createActivity_shouldReturnCreated() throws Exception {
        when(activityRestService.createActivity(any())).thenReturn(sampleActivity());

        mockMvc.perform(post("/api/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPayload()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("ACT-1"));
    }

    @Test
    void createActivity_forbiddenWhenVendorMismatch() throws Exception {
        when(activityRestService.createActivity(any()))
                .thenThrow(new RuntimeException("User can only create their own activity"));

        mockMvc.perform(post("/api/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPayload()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User can only create their own activity"));
    }

    @Test
    void createActivity_badRequestOnRuntimeError() throws Exception {
        when(activityRestService.createActivity(any()))
                .thenThrow(new RuntimeException("Validation failed"));

        mockMvc.perform(post("/api/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPayload()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createActivity_serverError() throws Exception {
        when(activityRestService.createActivity(any()))
                .thenAnswer(inv -> {
                    throw new Exception("boom");
                });

        mockMvc.perform(post("/api/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPayload()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateActivity_shouldReturnOk() throws Exception {
        when(activityRestService.updateActivity(any(), any())).thenReturn(sampleActivity());

        mockMvc.perform(put("/api/activities/ACT-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("ACT-1"));
    }

    @Test
    void updateActivity_notFound() throws Exception {
        when(activityRestService.updateActivity(any(), any()))
                .thenThrow(new RuntimeException("Activity not found"));

        mockMvc.perform(put("/api/activities/ACT-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateActivity_forbiddenOnOwnershipMismatch() throws Exception {
        when(activityRestService.updateActivity(any(), any()))
                .thenThrow(new RuntimeException("You can only modify your own activity"));

        mockMvc.perform(put("/api/activities/ACT-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload()))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateActivity_badRequest() throws Exception {
        when(activityRestService.updateActivity(any(), any()))
                .thenThrow(new RuntimeException("Capacity invalid"));

        mockMvc.perform(put("/api/activities/ACT-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateActivity_serverError() throws Exception {
        when(activityRestService.updateActivity(any(), any()))
                .thenAnswer(inv -> {
                    throw new Exception("boom");
                });

        mockMvc.perform(put("/api/activities/ACT-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteActivity_shouldReturnOk() throws Exception {
        doNothing().when(activityRestService).deleteActivity("ACT-1");

        mockMvc.perform(delete("/api/activities/ACT-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully deleted activity"));
        verify(activityRestService).deleteActivity("ACT-1");
    }

    @Test
    void deleteActivity_notFound() throws Exception {
        doThrow(new RuntimeException("Activity not found"))
                .when(activityRestService).deleteActivity("ACT-1");

        mockMvc.perform(delete("/api/activities/ACT-1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteActivity_forbidden() throws Exception {
        doThrow(new RuntimeException("You can only modify"))
                .when(activityRestService).deleteActivity("ACT-1");

        mockMvc.perform(delete("/api/activities/ACT-1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteActivity_badRequest() throws Exception {
        doThrow(new RuntimeException("Cannot delete"))
                .when(activityRestService).deleteActivity("ACT-1");

        mockMvc.perform(delete("/api/activities/ACT-1"))
                .andExpect(status().isBadRequest());
    }

    private String createPayload() {
        return "{" +
                "\"activityName\":\"Snorkeling\"," +
                "\"activityItem\":\"Boat\"," +
                "\"capacity\":10," +
                "\"price\":1500000," +
                "\"activityType\":\"TOUR\"," +
                "\"startLocation\":\"CGK\"," +
                "\"endLocation\":\"DPS\"}";
    }

    private String updatePayload() {
        return "{" +
                "\"activityItem\":\"Guide\"," +
                "\"capacity\":5," +
                "\"price\":2000000}";
    }
}
