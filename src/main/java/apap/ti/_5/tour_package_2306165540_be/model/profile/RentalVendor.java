package apap.ti._5.tour_package_2306165540_be.model.profile;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("RENTAL_VENDOR")
public class RentalVendor extends EndUser {
    @Override
    public RoleType getRoleType() {
        return RoleType.RENTAL_VENDOR;
    }

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "locationList", nullable = false)
    private List<String> listOfLocations;
}
