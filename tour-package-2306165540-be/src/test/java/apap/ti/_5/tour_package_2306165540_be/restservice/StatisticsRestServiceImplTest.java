package apap.ti._5.tour_package_2306165540_be.restservice;

import apap.ti._5.tour_package_2306165540_be.model.Activity;
import apap.ti._5.tour_package_2306165540_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306165540_be.model.Plan;
import apap.ti._5.tour_package_2306165540_be.repository.OrderedQuantityRepository;
import apap.ti._5.tour_package_2306165540_be.restdto.response.RevenueStatisticsResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class StatisticsRestServiceImplTest {

    @Mock
    private OrderedQuantityRepository orderedQuantityRepository;

    @InjectMocks
    private StatisticsRestServiceImpl service;

    private Plan planFlight;
    private Plan planHotel;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        planFlight = new Plan();
        planFlight.setId(UUID.randomUUID());
        planFlight.setActivityType("Flight");
        planFlight.setStartDate(LocalDateTime.of(2025, 11, 1, 8, 0));
        planFlight.setEndDate(LocalDateTime.of(2025, 11, 1, 10, 0));

        planHotel = new Plan();
        planHotel.setId(UUID.randomUUID());
        planHotel.setActivityType("Accommodation");
        planHotel.setStartDate(LocalDateTime.of(2025, 11, 2, 8, 0));
        planHotel.setEndDate(LocalDateTime.of(2025, 11, 3, 8, 0));
    }

    @Test
    void getRevenueStatistics_shouldGroupAndSumByType_andRespectFilters() {
        OrderedQuantity oq1 = new OrderedQuantity();
        oq1.setId(UUID.randomUUID());
        oq1.setPlan(planFlight);
        oq1.setPrice(1000L);
        oq1.setOrderedQuota(3);

        OrderedQuantity oq2 = new OrderedQuantity();
        oq2.setId(UUID.randomUUID());
        oq2.setPlan(planHotel);
        oq2.setPrice(500L);
        oq2.setOrderedQuota(4);

        // Deleted item should be ignored
        OrderedQuantity oqDeleted = new OrderedQuantity();
        oqDeleted.setId(UUID.randomUUID());
        oqDeleted.setPlan(planHotel);
        oqDeleted.setPrice(10L);
        oqDeleted.setOrderedQuota(100);
        oqDeleted.setDeletedAt(LocalDateTime.now());

        when(orderedQuantityRepository.findAll()).thenReturn(List.of(oq1, oq2, oqDeleted));

        RevenueStatisticsResponseDTO all = service.getRevenueStatistics(null, null);
        assertEquals(2, all.getRevenueByActivityType().size());
        assertTrue(all.getRevenueByActivityType().stream()
                .anyMatch(e -> e.getActivityType().equals("Flight") && e.getTotalRevenue() == 3000));
        assertTrue(all.getRevenueByActivityType().stream()
                .anyMatch(e -> e.getActivityType().equals("Accommodation") && e.getTotalRevenue() == 2000));

        RevenueStatisticsResponseDTO nov2025 = service.getRevenueStatistics(2025, 11);
        assertEquals(2, nov2025.getRevenueByActivityType().size());
    }
}
