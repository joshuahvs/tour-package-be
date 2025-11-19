package apap.ti._5.tour_package_2306165540_be.model.profile;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ACCOMMODATION_OWNER")
public class AccommodationOwner extends EndUser {
    @Override
    public RoleType getRoleType() {
        return RoleType.ACCOMMODATION_OWNER;
    }
}
