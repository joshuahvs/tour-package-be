package apap.ti._5.tour_package_2306165540_be.restdto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueStatisticsResponseDTO {
    private List<ActivityTypeRevenueDTO> revenueByActivityType;
}
