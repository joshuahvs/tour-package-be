package apap.ti._5.tour_package_2306165540_be.restservice;

import apap.ti._5.tour_package_2306165540_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306165540_be.model.profile.EndUser;
import apap.ti._5.tour_package_2306165540_be.model.profile.RoleType;
import apap.ti._5.tour_package_2306165540_be.repository.EndUserRepository;
import apap.ti._5.tour_package_2306165540_be.repository.OrderedQuantityRepository;
import apap.ti._5.tour_package_2306165540_be.restdto.response.ActivityTypeRevenueDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.MonthlyRevenueDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.RevenueStatisticsResponseDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsRestServiceImpl implements StatisticsRestService {

    private final OrderedQuantityRepository orderedQuantityRepository;
    private final EndUserRepository endUserRepository;

    public StatisticsRestServiceImpl(OrderedQuantityRepository orderedQuantityRepository,
            EndUserRepository endUserRepository) {
        this.orderedQuantityRepository = orderedQuantityRepository;
        this.endUserRepository = endUserRepository;
    }

    @Override
    public RevenueStatisticsResponseDTO getRevenueStatistics(Integer year, Integer month) {
        // Authorization: Only Superadmin and Tour Package Vendor can access
        checkStatisticsAuthorization();

        // Validate year is required
        if (year == null) {
            throw new RuntimeException("Year parameter is required");
        }

        // Get all fulfilled ordered quantities (non-deleted, plan status = Fulfilled)
        List<OrderedQuantity> fulfilledOrderedQuantities = orderedQuantityRepository.findAll()
                .stream()
                .filter(oq -> oq.getDeletedAt() == null)
                .filter(oq -> oq.getPlan().getDeletedAt() == null)
                .filter(oq -> "Fulfilled".equalsIgnoreCase(oq.getPlan().getStatus()))
                .collect(Collectors.toList());

        RevenueStatisticsResponseDTO response = new RevenueStatisticsResponseDTO();

        if (month == null) {
            // Return revenue per month for the year
            List<MonthlyRevenueDTO> monthlyRevenues = new ArrayList<>();

            for (int m = 1; m <= 12; m++) {
                final int currentMonth = m;

                List<OrderedQuantity> monthData = fulfilledOrderedQuantities.stream()
                        .filter(oq -> {
                            LocalDateTime startDate = oq.getPlan().getStartDate();
                            return startDate.getYear() == year && startDate.getMonthValue() == currentMonth;
                        })
                        .collect(Collectors.toList());

                long totalRevenue = monthData.stream()
                        .mapToLong(oq -> oq.getPrice() * oq.getOrderedQuota())
                        .sum();

                String period = String.format("%d-%02d", year, m);
                monthlyRevenues.add(new MonthlyRevenueDTO(period, totalRevenue));
            }

            response.setMonthlyRevenues(monthlyRevenues);
            response.setPeriod(year.toString());
        } else {
            // Return detailed revenue for specific month with breakdown
            List<OrderedQuantity> monthData = fulfilledOrderedQuantities.stream()
                    .filter(oq -> {
                        LocalDateTime startDate = oq.getPlan().getStartDate();
                        return startDate.getYear() == year && startDate.getMonthValue() == month;
                    })
                    .collect(Collectors.toList());

            // Calculate total revenue
            long totalRevenue = monthData.stream()
                    .mapToLong(oq -> oq.getPrice() * oq.getOrderedQuota())
                    .sum();

            // Group by activity type
            Map<String, Long> revenueByType = new HashMap<>();
            for (OrderedQuantity oq : monthData) {
                String activityType = oq.getPlan().getActivityType();
                Long revenue = oq.getPrice() * oq.getOrderedQuota();
                revenueByType.merge(activityType, revenue, Long::sum);
            }

            List<ActivityTypeRevenueDTO> breakdown = revenueByType.entrySet().stream()
                    .map(entry -> new ActivityTypeRevenueDTO(entry.getKey(), entry.getValue()))
                    .sorted((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()))
                    .collect(Collectors.toList());

            String period = String.format("%d-%02d", year, month);
            response.setPeriod(period);
            response.setTotalRevenue(totalRevenue);
            response.setRevenueByActivityType(breakdown);
        }

        return response;
    }

    @Override
    public List<MonthlyRevenueDTO> getYearlyRevenue(Integer year) {
        // Authorization: Only Superadmin and Tour Package Vendor can access
        checkStatisticsAuthorization();

        // Get all fulfilled ordered quantities
        List<OrderedQuantity> fulfilledOrderedQuantities = orderedQuantityRepository.findAll()
                .stream()
                .filter(oq -> oq.getDeletedAt() == null)
                .filter(oq -> oq.getPlan().getDeletedAt() == null)
                .filter(oq -> "Fulfilled".equalsIgnoreCase(oq.getPlan().getStatus()))
                .collect(Collectors.toList());

        List<MonthlyRevenueDTO> monthlyRevenues = new ArrayList<>();

        for (int m = 1; m <= 12; m++) {
            final int currentMonth = m;

            List<OrderedQuantity> monthData = fulfilledOrderedQuantities.stream()
                    .filter(oq -> {
                        LocalDateTime startDate = oq.getPlan().getStartDate();
                        return startDate.getYear() == year && startDate.getMonthValue() == currentMonth;
                    })
                    .collect(Collectors.toList());

            long totalRevenue = monthData.stream()
                    .mapToLong(oq -> oq.getPrice() * oq.getOrderedQuota())
                    .sum();

            String period = String.format("%d-%02d", year, m);
            monthlyRevenues.add(new MonthlyRevenueDTO(period, totalRevenue));
        }

        return monthlyRevenues;
    }

    @Override
    public RevenueStatisticsResponseDTO getMonthlyRevenue(Integer year, Integer month) {
        // Authorization: Only Superadmin and Tour Package Vendor can access
        checkStatisticsAuthorization();

        // Get fulfilled ordered quantities for the specific month
        List<OrderedQuantity> monthData = orderedQuantityRepository.findAll()
                .stream()
                .filter(oq -> oq.getDeletedAt() == null)
                .filter(oq -> oq.getPlan().getDeletedAt() == null)
                .filter(oq -> "Fulfilled".equalsIgnoreCase(oq.getPlan().getStatus()))
                .filter(oq -> {
                    LocalDateTime startDate = oq.getPlan().getStartDate();
                    return startDate.getYear() == year && startDate.getMonthValue() == month;
                })
                .collect(Collectors.toList());

        // Calculate total revenue
        long totalRevenue = monthData.stream()
                .mapToLong(oq -> oq.getPrice() * oq.getOrderedQuota())
                .sum();

        // Group by activity type
        Map<String, Long> revenueByType = new HashMap<>();
        for (OrderedQuantity oq : monthData) {
            String activityType = oq.getPlan().getActivityType();
            Long revenue = oq.getPrice() * oq.getOrderedQuota();
            revenueByType.merge(activityType, revenue, Long::sum);
        }

        List<ActivityTypeRevenueDTO> breakdown = revenueByType.entrySet().stream()
                .map(entry -> new ActivityTypeRevenueDTO(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()))
                .collect(Collectors.toList());

        String period = String.format("%d-%02d", year, month);

        RevenueStatisticsResponseDTO response = new RevenueStatisticsResponseDTO();
        response.setPeriod(period);
        response.setTotalRevenue(totalRevenue);
        response.setRevenueByActivityType(breakdown);

        return response;
    }

    private void checkStatisticsAuthorization() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Authentication required");
        }

        String currentUsername = authentication.getName();
        EndUser currentUser = endUserRepository.findByUsernameIgnoreCase(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only Superadmin and Tour Package Vendor can access statistics
        if (currentUser.getRoleType() != RoleType.SUPERADMIN &&
                currentUser.getRoleType() != RoleType.TOUR_PACKAGE_VENDOR) {
            throw new RuntimeException("Only Superadmin and Tour Package Vendor can access revenue statistics");
        }
    }
}
