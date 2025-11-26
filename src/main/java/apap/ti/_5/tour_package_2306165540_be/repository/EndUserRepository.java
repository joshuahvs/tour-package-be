package apap.ti._5.tour_package_2306165540_be.repository;

import apap.ti._5.tour_package_2306165540_be.model.profile.EndUser;
import apap.ti._5.tour_package_2306165540_be.model.profile.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT u FROM EndUser u WHERE TYPE(u) = :roleType")
    List<EndUser> findByRoleType(@Param("roleType") Class<? extends EndUser> roleType);
}
