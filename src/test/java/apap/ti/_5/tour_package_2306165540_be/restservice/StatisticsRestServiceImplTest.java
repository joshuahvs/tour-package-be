package apap.ti._5.tour_package_2306165540_be.restservice;

import apap.ti._5.tour_package_2306165540_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306165540_be.model.Plan;
import apap.ti._5.tour_package_2306165540_be.model.profile.Customer;
import apap.ti._5.tour_package_2306165540_be.model.profile.EndUser;
import apap.ti._5.tour_package_2306165540_be.model.profile.SuperAdmin;
import apap.ti._5.tour_package_2306165540_be.model.profile.TourPackageVendor;
import apap.ti._5.tour_package_2306165540_be.repository.OrderedQuantityRepository;
import apap.ti._5.tour_package_2306165540_be.repository.EndUserRepository;
import apap.ti._5.tour_package_2306165540_be.restdto.response.ActivityTypeRevenueDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.MonthlyRevenueDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.RevenueStatisticsResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class StatisticsRestServiceImplTest {

    @Mock
    private OrderedQuantityRepository orderedQuantityRepository;

    @Mock
    private EndUserRepository endUserRepository;

    @InjectMocks
    private StatisticsRestServiceImpl service;

    private Plan planFlight;
    private Plan planHotel;
    private Plan planCruise;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        planFlight = new Plan();
        planFlight.setId(UUID.randomUUID());
        planFlight.setActivityType("Flight");
        planFlight.setStartDate(LocalDateTime.of(2025, 11, 1, 8, 0));
        planFlight.setEndDate(LocalDateTime.of(2025, 11, 1, 10, 0));
        planFlight.setStatus("Fulfilled");

        planHotel = new Plan();
        planHotel.setId(UUID.randomUUID());
        planHotel.setActivityType("Accommodation");
        planHotel.setStartDate(LocalDateTime.of(2025, 11, 2, 8, 0));
        planHotel.setEndDate(LocalDateTime.of(2025, 11, 3, 8, 0));
        planHotel.setStatus("Fulfilled");

        planCruise = new Plan();
        planCruise.setId(UUID.randomUUID());
        planCruise.setActivityType("Cruise");
        planCruise.setStartDate(LocalDateTime.of(2025, 10, 5, 9, 0));
        planCruise.setEndDate(LocalDateTime.of(2025, 10, 6, 9, 0));
        planCruise.setStatus("Fulfilled");

        mockAuthenticatedSuperAdmin();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
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

        RevenueStatisticsResponseDTO yearly = service.getRevenueStatistics(2025, null);
        assertEquals("2025", yearly.getPeriod());
        assertEquals(12, yearly.getMonthlyRevenues().size());
        assertTrue(yearly.getMonthlyRevenues().stream()
                .anyMatch(entry -> entry.getPeriod().equals("2025-11") && entry.getTotalRevenue() == 5000));

        RevenueStatisticsResponseDTO nov2025 = service.getRevenueStatistics(2025, 11);
        assertEquals(2, nov2025.getRevenueByActivityType().size());
    }

    @Test
    void getRevenueStatistics_allows_vendor_role() {
        mockAuthenticatedTourVendor();
        when(orderedQuantityRepository.findAll()).thenReturn(List.of());

        RevenueStatisticsResponseDTO result = service.getRevenueStatistics(2025, null);
        assertEquals("2025", result.getPeriod());
        assertEquals(12, result.getMonthlyRevenues().size());
    }

    @Test
    void getRevenueStatistics_requires_authentication_and_year() {
        SecurityContextHolder.clearContext();
        assertThrows(RuntimeException.class, () -> service.getRevenueStatistics(2025, null),
                "Expected authentication requirement");

        mockAuthenticatedCustomer();
        assertThrows(RuntimeException.class, () -> service.getRevenueStatistics(2025, null),
                "Customer should not access statistics");

        mockAuthenticatedSuperAdmin();
        assertThrows(RuntimeException.class, () -> service.getRevenueStatistics(null, null),
                "Year parameter is mandatory");
    }

    @Test
    void getYearlyRevenue_sums_each_month() {
        OrderedQuantity oq1 = fulfilledOrder(planFlight, 1000L, 3);
        OrderedQuantity oq2 = fulfilledOrder(planHotel, 500L, 4);
        oq2.setDeletedAt(null);

        when(orderedQuantityRepository.findAll()).thenReturn(List.of(oq1, oq2));

        List<MonthlyRevenueDTO> revenues = service.getYearlyRevenue(2025);
        assertEquals(12, revenues.size());

        MonthlyRevenueDTO november = revenues.stream()
                .filter(entry -> entry.getPeriod().equals("2025-11"))
                .findFirst()
                .orElseThrow();
        assertEquals(5000L, november.getTotalRevenue());
    }

    @Test
    void getMonthlyRevenue_filters_deleted_and_sorts_breakdown() {
        OrderedQuantity oq1 = fulfilledOrder(planFlight, 1000L, 3);
        OrderedQuantity oq2 = fulfilledOrder(planHotel, 500L, 4);
        OrderedQuantity oqDeleted = fulfilledOrder(planHotel, 200L, 1);
        oqDeleted.setDeletedAt(LocalDateTime.now());

        OrderedQuantity otherMonth = fulfilledOrder(planCruise, 999L, 10);

        when(orderedQuantityRepository.findAll()).thenReturn(List.of(oq1, oq2, oqDeleted, otherMonth));

        RevenueStatisticsResponseDTO nov = service.getMonthlyRevenue(2025, 11);
        assertEquals("2025-11", nov.getPeriod());
        assertEquals(5000L, nov.getTotalRevenue());

        List<ActivityTypeRevenueDTO> breakdown = nov.getRevenueByActivityType();
        assertEquals(2, breakdown.size());
        assertEquals("Flight", breakdown.get(0).getActivityType());
        assertEquals(3000L, breakdown.get(0).getTotalRevenue());
        assertEquals("Accommodation", breakdown.get(1).getActivityType());
        assertEquals(2000L, breakdown.get(1).getTotalRevenue());
    }

    private void mockAuthenticatedSuperAdmin() {
        SuperAdmin admin = new SuperAdmin();
        admin.setId(UUID.randomUUID());
        admin.setUsername("stats-user");
        mockAuthenticatedUser(admin);
    }

    private void mockAuthenticatedCustomer() {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setUsername("customer-user");
        mockAuthenticatedUser(customer);
    }

    private void mockAuthenticatedTourVendor() {
        TourPackageVendor vendor = new TourPackageVendor();
        vendor.setId(UUID.randomUUID());
        vendor.setUsername("vendor-user");
        mockAuthenticatedUser(vendor);
    }

    private void mockAuthenticatedUser(EndUser user) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user.getUsername(), null,
                List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(endUserRepository.findByUsernameIgnoreCase(user.getUsername()))
                .thenReturn(Optional.of(user));
    }

    private OrderedQuantity fulfilledOrder(Plan plan, long price, int quota) {
        OrderedQuantity oq = new OrderedQuantity();
        oq.setId(UUID.randomUUID());
        oq.setPlan(plan);
        oq.setPrice(price);
        oq.setOrderedQuota(quota);
        return oq;
    }
}
