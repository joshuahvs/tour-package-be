package apap.ti._5.tour_package_2306165540_be.restdto.response;

import lombok.Data;

import java.time.Instant;

@Data
public class LoginResponseDTO {
    private String token;
    private String tokenType = "Bearer";
    private Instant expiresAt;
    private EndUserResponseDTO user;
}
