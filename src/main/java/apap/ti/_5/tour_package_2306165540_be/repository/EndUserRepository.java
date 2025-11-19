package apap.ti._5.tour_package_2306165540_be.repository;

import apap.ti._5.tour_package_2306165540_be.model.profile.EndUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EndUserRepository extends JpaRepository<EndUser, UUID> {
    Optional<EndUser> findByUsernameIgnoreCase(String username);

    Optional<EndUser> findByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, UUID id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

    List<EndUser> findAllByActiveIsTrueOrderByUsernameAsc();

    List<EndUser> findAllByOrderByUsernameAsc();
}
