package apap.ti._5.tour_package_2306165540_be.restdto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementResponseDTO {
    private String id;
    private String username;
    private String email;
    private String fullName;
    private String organizationName;
    private String notes;
    private String roleType;
    private boolean active;
}
