package apap.ti._5.tour_package_2306165540_be.model.profile;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("TOUR_PACKAGE_VENDOR")
public class TourPackageVendor extends EndUser {
    @Override
    public RoleType getRoleType() {
        return RoleType.TOUR_PACKAGE_VENDOR;
    }
}
