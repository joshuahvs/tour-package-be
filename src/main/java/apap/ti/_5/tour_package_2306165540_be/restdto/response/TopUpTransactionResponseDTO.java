package apap.ti._5.tour_package_2306165540_be.restdto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TopUpTransactionResponseDTO {
    private UUID id;
    private UUID userId;
    private Long amount;
    private String status;
    private LocalDateTime transactionDate;
    private String description;
}
