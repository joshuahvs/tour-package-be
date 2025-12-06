package apap.ti._5.tour_package_2306165540_be.restcontroller;

import apap.ti._5.tour_package_2306165540_be.restdto.response.ActivityTypeRevenueDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.MonthlyRevenueDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.RevenueStatisticsResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restservice.StatisticsRestService;
import apap.ti._5.tour_package_2306165540_be.security.jwt.JwtTokenFilter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
@AutoConfigureMockMvc(addFilters = false)
class StatisticsRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatisticsRestService statisticsRestService;

    @MockBean
    private JwtTokenFilter jwtTokenFilter;

    @Test
    void getRevenueStatistics_shouldReturnOk() throws Exception {
        RevenueStatisticsResponseDTO dto = new RevenueStatisticsResponseDTO(
                "2025",
                1_000_000L,
                List.of(new MonthlyRevenueDTO("2025-01", 100_000L)),
                List.of(new ActivityTypeRevenueDTO("Flight", 500_000L)));
        Mockito.when(statisticsRestService.getRevenueStatistics(2025, null)).thenReturn(dto);

        mockMvc.perform(get("/api/statistics/revenue")
                .param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.period").value("2025"))
                .andExpect(jsonPath("$.data.revenueByActivityType[0].activityType").value("Flight"));
    }

    @Test
    void getRevenueStatistics_forbiddenForNonSuperadmin() throws Exception {
        Mockito.when(statisticsRestService.getRevenueStatistics(2025, null))
                .thenThrow(new RuntimeException("Only Superadmin can access"));

        mockMvc.perform(get("/api/statistics/revenue")
                .param("year", "2025"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only Superadmin can access"));
    }

    @Test
    void getRevenueStatistics_badRequestOnRuntime() throws Exception {
        Mockito.when(statisticsRestService.getRevenueStatistics(2025, 2))
                .thenThrow(new RuntimeException("Month must be between 1 and 12"));

        mockMvc.perform(get("/api/statistics/revenue")
                .param("year", "2025")
                .param("month", "2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStatistics_shouldReturnServerError_whenServiceThrows() throws Exception {
        Mockito.when(statisticsRestService.getRevenueStatistics(any(), any()))
                .thenAnswer(inv -> {
                    throw new RuntimeException("Boom");
                });

        mockMvc.perform(get("/api/statistics/revenue")
                .param("year", "2025")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getYearlyRevenue_success() throws Exception {
        List<MonthlyRevenueDTO> data = List.of(new MonthlyRevenueDTO("2025-01", 100_000L));
        Mockito.when(statisticsRestService.getYearlyRevenue(2025)).thenReturn(data);

        mockMvc.perform(get("/api/statistics/revenue/yearly/2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].period").value("2025-01"));
    }

    @Test
    void getYearlyRevenue_forbidden() throws Exception {
        Mockito.when(statisticsRestService.getYearlyRevenue(2025))
                .thenThrow(new RuntimeException("Only Superadmin allowed"));

        mockMvc.perform(get("/api/statistics/revenue/yearly/2025"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getYearlyRevenue_badRequest() throws Exception {
        Mockito.when(statisticsRestService.getYearlyRevenue(2025))
                .thenThrow(new RuntimeException("Invalid year"));

        mockMvc.perform(get("/api/statistics/revenue/yearly/2025"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMonthlyRevenue_success() throws Exception {
        RevenueStatisticsResponseDTO dto = new RevenueStatisticsResponseDTO(
                "2025-02",
                200_000L,
                null,
                List.of(new ActivityTypeRevenueDTO("Hotel", 80_000L)));
        Mockito.when(statisticsRestService.getMonthlyRevenue(2025, 2)).thenReturn(dto);

        mockMvc.perform(get("/api/statistics/revenue/monthly/2025/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.period").value("2025-02"));
    }

    @Test
    void getMonthlyRevenue_forbidden() throws Exception {
        Mockito.when(statisticsRestService.getMonthlyRevenue(2025, 2))
                .thenThrow(new RuntimeException("Only Superadmin"));

        mockMvc.perform(get("/api/statistics/revenue/monthly/2025/2"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMonthlyRevenue_badRequest() throws Exception {
        Mockito.when(statisticsRestService.getMonthlyRevenue(2025, 13))
                .thenThrow(new RuntimeException("Invalid month"));

        mockMvc.perform(get("/api/statistics/revenue/monthly/2025/13"))
                .andExpect(status().isBadRequest());
    }
}
