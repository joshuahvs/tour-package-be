package apap.ti._5.tour_package_2306165540_be.restservice;

import apap.ti._5.tour_package_2306165540_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306165540_be.model.Plan;
import apap.ti._5.tour_package_2306165540_be.repository.OrderedQuantityRepository;
import apap.ti._5.tour_package_2306165540_be.restdto.response.ActivityTypeRevenueDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.RevenueStatisticsResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsRestServiceImpl implements StatisticsRestService {

    private final OrderedQuantityRepository orderedQuantityRepository;

    @Override
    public RevenueStatisticsResponseDTO getRevenueStatistics(Integer year, Integer month) {
        // Get all ordered quantities (non-deleted)
        List<OrderedQuantity> allOrderedQuantities = orderedQuantityRepository.findAll()
                .stream()
                .filter(oq -> oq.getDeletedAt() == null)
                .filter(oq -> oq.getPlan().getDeletedAt() == null)
                .collect(Collectors.toList());

        // Filter by year and month if provided
        if (year != null) {
            allOrderedQuantities = allOrderedQuantities.stream()
                    .filter(oq -> {
                        Plan plan = oq.getPlan();
                        LocalDateTime startDate = plan.getStartDate();
                        return startDate.getYear() == year;
                    })
                    .collect(Collectors.toList());
        }

        if (month != null && year != null) {
            allOrderedQuantities = allOrderedQuantities.stream()
                    .filter(oq -> {
                        Plan plan = oq.getPlan();
                        LocalDateTime startDate = plan.getStartDate();
                        return startDate.getMonthValue() == month;
                    })
                    .collect(Collectors.toList());
        }

        // Group by activity type and sum revenue
        Map<String, Long> revenueByType = new HashMap<>();

        for (OrderedQuantity oq : allOrderedQuantities) {
            String activityType = oq.getPlan().getActivityType();
            Long revenue = oq.getPrice() * oq.getOrderedQuota();

            revenueByType.merge(activityType, revenue, Long::sum);
        } // Convert to DTO list
        List<ActivityTypeRevenueDTO> revenueList = revenueByType.entrySet().stream()
                .map(entry -> new ActivityTypeRevenueDTO(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue())) // Sort by revenue descending
                .collect(Collectors.toList());

        RevenueStatisticsResponseDTO response = new RevenueStatisticsResponseDTO();
        response.setRevenueByActivityType(revenueList);

        return response;
    }
}
