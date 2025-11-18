package apap.ti._5.tour_package_2306165540_be.restcontroller;

import apap.ti._5.tour_package_2306165540_be.restdto.response.ActivityTypeRevenueDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.RevenueStatisticsResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restservice.StatisticsRestService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatisticsRestController.class)
class StatisticsRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatisticsRestService statisticsRestService;

    @Test
    void getStatistics_shouldReturnOkWithData() throws Exception {
        RevenueStatisticsResponseDTO dto = new RevenueStatisticsResponseDTO(
                List.of(new ActivityTypeRevenueDTO("Flight", 1000L)));
        Mockito.when(statisticsRestService.getRevenueStatistics(any(), any())).thenReturn(dto);

        mockMvc.perform(get("/api/statistics").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.revenueByActivityType[0].activityType").value("Flight"));
    }

    @Test
    void getStatistics_shouldReturnServerError_whenServiceThrows() throws Exception {
        Mockito.when(statisticsRestService.getRevenueStatistics(any(), any())).thenThrow(new RuntimeException("Boom"));

        mockMvc.perform(get("/api/statistics").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }
}
