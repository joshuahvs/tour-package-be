package apap.ti._5.tour_package_2306165540_be.restdto.request;

import lombok.Data;

@Data
public class UpsertEndUserRequestDTO {
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String password;
    private String organizationName;
    private String notes;
    private String role;
    private Boolean active;
}
