package apap.ti._5.tour_package_2306165540_be.restdto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddOrderedQuantityRequestDTO {
    private UUID activityId;
    private Integer orderedQuantity;
}
