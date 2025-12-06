package apap.ti._5.tour_package_2306165540_be.restdto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateEndUserRequestDTO {
    @NotBlank(message = "Username wajib diisi.")
    @Size(max = 100, message = "Username maksimal 100 karakter.")
    private String username;

    @NotBlank(message = "Email wajib diisi.")
    @Email(message = "Format email tidak valid.")
    @Size(max = 160, message = "Email maksimal 160 karakter.")
    private String email;

    @NotBlank(message = "Nama lengkap wajib diisi.")
    @Size(max = 160, message = "Nama lengkap maksimal 160 karakter.")
    private String fullName;

    @Size(max = 20, message = "Gender maksimal 20 karakter.")
    private String gender;

    @Size(max = 30, message = "Nomor telepon maksimal 30 karakter.")
    private String phoneNumber;

    @NotBlank(message = "Password wajib diisi.")
    @Size(min = 4, message = "Password minimal 4 karakter.")
    private String password;

    @Size(max = 160, message = "Nama organisasi maksimal 160 karakter.")
    private String organizationName;

    @Size(max = 255, message = "Catatan maksimal 255 karakter.")
    private String notes;

    private String role;

    private Boolean active;

    /**
     * Nilai saldo hanya akan digunakan bila role adalah CUSTOMER. Jika null maka
     * default 0.
     */
    private Long saldo;
}
