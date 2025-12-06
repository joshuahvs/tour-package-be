package apap.ti._5.tour_package_2306165540_be.model.profile;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("INSURANCE_PROVIDER")
public class InsuranceProvider extends EndUser {
    @Override
    public RoleType getRoleType() {
        return RoleType.INSURANCE_PROVIDER;
    }
}
