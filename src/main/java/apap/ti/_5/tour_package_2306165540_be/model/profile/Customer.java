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

    @Column(name = "saldo", nullable = false)
    private Long saldo = 0L;
}
