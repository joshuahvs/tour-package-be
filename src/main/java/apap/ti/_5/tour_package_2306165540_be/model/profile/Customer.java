package apap.ti._5.tour_package_2306165540_be.model.profile;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("CUSTOMER")
@Getter
@Setter
public class Customer extends EndUser {
    @Override
    public RoleType getRoleType() {
        return RoleType.CUSTOMER;
    }

    // Saldo hanya untuk Customer; kolom harus nullable agar baris non-CUSTOMER
    // tidak gagal
    @Column(name = "saldo")
    private Long saldo = 0L;

}
