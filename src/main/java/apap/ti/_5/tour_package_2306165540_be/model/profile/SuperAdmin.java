package apap.ti._5.tour_package_2306165540_be.model.profile;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SUPERADMIN")
public class SuperAdmin extends EndUser {
    @Override
    public RoleType getRoleType() {
        return RoleType.SUPERADMIN;
    }
}
