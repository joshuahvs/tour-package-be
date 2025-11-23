package apap.ti._5.tour_package_2306165540_be.restdto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateEndUserRequestDTO {
    @NotNull(message = "Id end user wajib diisi.")
    private UUID id;

    @Size(max = 100, message = "Username maksimal 100 karakter.")
    private String username;

    @Size(max = 160, message = "Email maksimal 160 karakter.")
    private String email;

    @Size(max = 160, message = "Nama lengkap maksimal 160 karakter.")
    private String fullName;

    @Size(max = 30, message = "Nomor telepon maksimal 30 karakter.")
    private String phoneNumber;

    @Size(min = 4, message = "Password minimal 4 karakter.")
    private String password;

    @Size(max = 160, message = "Nama organisasi maksimal 160 karakter.")
    private String organizationName;

    @Size(max = 255, message = "Catatan maksimal 255 karakter.")
    private String notes;

    private Boolean active;

    private String role;

    private Long saldo;
}
