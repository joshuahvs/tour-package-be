package apap.ti._5.tour_package_2306165540_be.restdto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CustomerResponseDTO {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private boolean active;
    private Long saldo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
