package apap.ti._5.tour_package_2306165540_be.restdto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderedQuantityResponseDTO {
    private UUID id;
    private String activityName;
    private String activityId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long price;
    private int quota;
    private int orderedQuota;
    private Long total;
}
