package apap.ti._5.tour_package_2306165540_be.restdto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanDetailResponseDTO {
    private UUID id;
    private String planName;
    private String activityType;
    private String status;
    private Long totalPrice;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String startLocation;
    private String endLocation;
    private String packageId;
    private String packageName;
    private List<OrderedQuantityResponseDTO> orderedQuantities;
}
