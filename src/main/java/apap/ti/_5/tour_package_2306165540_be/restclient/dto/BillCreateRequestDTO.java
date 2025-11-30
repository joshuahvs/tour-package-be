package apap.ti._5.tour_package_2306165540_be.restclient.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillCreateRequestDTO {
    private UUID customerId;
    private String serviceName;
    private String serviceReferenceId;
    private String description;
    private Double amount;
    private LocalDateTime dueDate;
}
