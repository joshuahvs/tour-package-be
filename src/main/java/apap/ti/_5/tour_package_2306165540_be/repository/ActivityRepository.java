package apap.ti._5.tour_package_2306165540_be.repository;

import apap.ti._5.tour_package_2306165540_be.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, String> {
}
