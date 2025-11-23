package apap.ti._5.tour_package_2306165540_be.restdto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class EndUserResponseDTO {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String role;
    private String roleDisplayName;
    private String responsibility;
    private boolean active;
    private String organizationName;
    private String notes;
    private Long saldo; // default 0 for all, meaningful for CUSTOMER
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
