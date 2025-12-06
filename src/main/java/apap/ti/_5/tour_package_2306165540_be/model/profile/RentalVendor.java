package apap.ti._5.tour_package_2306165540_be.model.profile;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("RENTAL_VENDOR")
@Getter
@Setter
public class RentalVendor extends EndUser {
    @Override
    public RoleType getRoleType() {
        return RoleType.RENTAL_VENDOR;
    }

    @Column(name = "phone", length = 30)
    private String phone;

    @ElementCollection
    @CollectionTable(name = "rental_vendor_locations", joinColumns = @JoinColumn(name = "end_user_id"))
    @Column(name = "location", length = 160)
    private List<String> listOfLocations;
}
