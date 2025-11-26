package apap.ti._5.tour_package_2306165540_be.restdto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class UserProfileResponseDTO {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String gender;
    private String role;
    private String roleDisplayName;
    private Long saldo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TopUpTransactionResponseDTO> topUpTransactions;
}
