package apap.ti._5.tour_package_2306165540_be.restcontroller;

import apap.ti._5.tour_package_2306165540_be.model.Activity;
import apap.ti._5.tour_package_2306165540_be.repository.ActivityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ActivityRestController.class)
class ActivityRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActivityRepository activityRepository;

    @Test
    void getAllActivities_shouldReturnList() throws Exception {
        Activity a = new Activity();
        a.setId("ACT-1");
        a.setActivityName("Hotel");
        a.setPrice(1000L);
        a.setStartDate(LocalDateTime.now());
        a.setEndDate(LocalDateTime.now().plusHours(1));
        when(activityRepository.findAll()).thenReturn(List.of(a));

        mockMvc.perform(get("/api/activities").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].id").value("ACT-1"));
    }

    @Test
    void getAllActivities_shouldReturnOk() throws Exception {
        Activity a = new Activity();
        a.setId("ACT-1");
        a.setActivityName("Economy Flight");
        a.setActivityItem("Ticket");
        a.setCapacity(100);
        a.setPrice(1000000L);
        a.setActivityType("Flight");
        a.setStartDate(LocalDateTime.now());
        a.setEndDate(LocalDateTime.now().plusHours(2));
        a.setStartLocation("CGK");
        a.setEndLocation("DPS");

        when(activityRepository.findAll()).thenReturn(List.of(a));

        mockMvc.perform(get("/api/activities").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].activityType").value("Flight"));
    }

    @Test
    void getAllActivities_shouldReturnServerError_whenRepositoryThrows() throws Exception {
        when(activityRepository.findAll()).thenThrow(new RuntimeException("DB down"));

        mockMvc.perform(get("/api/activities").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }
}
