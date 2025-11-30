package apap.ti._5.tour_package_2306165540_be.restservice;

import apap.ti._5.tour_package_2306165540_be.model.Activity;
import apap.ti._5.tour_package_2306165540_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306165540_be.model.Package;
import apap.ti._5.tour_package_2306165540_be.model.Plan;
import apap.ti._5.tour_package_2306165540_be.repository.PackageRepository;
import apap.ti._5.tour_package_2306165540_be.repository.PlanRepository;
import apap.ti._5.tour_package_2306165540_be.restdto.request.CreatePackageRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PackageDetailResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PackageResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PackageRestServiceImplTest {

    @Mock
    private PackageRepository packageRepository;

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private PackageRestServiceImpl service;

    private Package pkg(String id, String userId, String name, String status) {
        Package p = new Package();
        p.setId(id);
        p.setUserId(userId);
        p.setPackageName(name);
        p.setQuota(2);
        p.setPrice(1_000_000L);
        p.setStatus(status);
        p.setStartDate(LocalDateTime.now());
        p.setEndDate(LocalDateTime.now().plusDays(1));
        p.setPlans(new ArrayList<>());
        return p;
    }

    private Plan plan(String status) {
        Plan pl = new Plan();
        pl.setId(UUID.randomUUID());
        pl.setStatus(status);
        pl.setActivityType("Flight");
        pl.setPrice(100L);
        pl.setStartDate(LocalDateTime.now());
        pl.setEndDate(LocalDateTime.now().plusHours(1));
        pl.setStartLocation("A");
        pl.setEndLocation("B");
        pl.setOrderedQuantities(new ArrayList<>());
        return pl;
    }

    @Test
    @DisplayName("getAllPackages filters out DELETED")
    void getAllPackages_filtersDeleted() {
        when(packageRepository.findAll()).thenReturn(List.of(
                pkg("PKG-1", "u1", "A", "PENDING"),
                pkg("PKG-2", "u1", "B", "DELETED")));

        List<PackageResponseDTO> out = service.getAllPackages();
        assertThat(out).hasSize(1);
        assertThat(out.get(0).getId()).isEqualTo("PKG-1");
    }

    @Test
    @DisplayName("searchPackagesByName filters by name and excludes DELETED")
    void searchPackagesByName_filters() {
        when(packageRepository.findAll()).thenReturn(List.of(
                pkg("PKG-1", "u1", "Honey Bali", "PENDING"),
                pkg("PKG-2", "u1", "Other", "PENDING"),
                pkg("PKG-3", "u1", "Honey Lombok", "DELETED")));

        List<PackageResponseDTO> out = service.searchPackagesByName("honey");
        assertThat(out).extracting(PackageResponseDTO::getId).containsExactly("PKG-1");
    }

    @Test
    @DisplayName("getPackageById returns or throws when not found")
    void getPackageById_cases() {
        when(packageRepository.findById("PKG-1")).thenReturn(Optional.of(pkg("PKG-1", "u1", "A", "PENDING")));
        assertThat(service.getPackageById("PKG-1").getId()).isEqualTo("PKG-1");

        when(packageRepository.findById("PKG-404")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getPackageById("PKG-404")).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getPackageDetailById filters soft-deleted plans")
    void getPackageDetailById_filtersSoftDeletedPlans() {
        Package p = pkg("PKG-9", "u1", "A", "PENDING");
        Plan active = plan("FULFILLED");
        Plan deleted = plan("FULFILLED");
        deleted.setDeletedAt(LocalDateTime.now());
        p.getPlans().addAll(List.of(active, deleted));

        when(packageRepository.findById("PKG-9")).thenReturn(Optional.of(p));

        PackageDetailResponseDTO out = service.getPackageDetailById("PKG-9");
        assertThat(out.getPlans()).hasSize(1);
        assertThat(out.getPlans().get(0).getId()).isEqualTo(active.getId());
    }

    // @Test
    // @DisplayName("createPackage validates dates, generates id, sets defaults and
    // saves")
    // void createPackage_happyPath_and_dateValidation() {
    // // invalid dates -> throws
    // CreatePackageRequestDTO bad = new CreatePackageRequestDTO(null, "u1", "A", 2,
    // 0L, null,
    // LocalDateTime.of(2025, 1, 2, 0, 0), LocalDateTime.of(2025, 1, 1, 0, 0));
    // assertThatThrownBy(() ->
    // service.createPackage(bad)).isInstanceOf(RuntimeException.class)
    // .hasMessageContaining("End date must be after start date");

    // // happy path
    // CreatePackageRequestDTO req = new CreatePackageRequestDTO(null, "u1", "A", 2,
    // 0L, null,
    // LocalDateTime.of(2025, 1, 1, 0, 0), LocalDateTime.of(2025, 1, 2, 0, 0));

    // // existing 2 packages for user -> expect suffix 003
    // when(packageRepository.findAll()).thenReturn(List.of(
    // pkg("PACK-u1-001", "u1", "X", "PENDING"),
    // pkg("PACK-u1-002", "u1", "Y", "PENDING")));

    // ArgumentCaptor<Package> captor = ArgumentCaptor.forClass(Package.class);
    // when(packageRepository.save(any(Package.class))).thenAnswer(inv ->
    // inv.getArgument(0));

    // PackageResponseDTO out = service.createPackage(req);
    // verify(packageRepository).save(captor.capture());
    // Package saved = captor.getValue();
    // assertThat(saved.getId()).isEqualTo("PACK-u1-003");
    // assertThat(saved.getStatus()).isEqualTo("PENDING");
    // assertThat(saved.getPrice()).isZero();
    // assertThat(out.getId()).isEqualTo("PACK-u1-003");
    // }

    // @Test
    // @DisplayName("updatePackage enforces rules and updates fields")
    // void updatePackage_rules_and_update() {
    // Package existing = pkg("PKG-1", "u1", "Old", "PENDING");
    // when(packageRepository.findById("PKG-1")).thenReturn(Optional.of(existing));

    // // with plans -> cannot edit
    // Plan pl = plan("ANY");
    // existing.getPlans().add(pl);
    // CreatePackageRequestDTO reqWithPlan = new CreatePackageRequestDTO(null, "u1",
    // "New", 3, 0L, null,
    // LocalDateTime.now(), LocalDateTime.now().plusDays(1));
    // assertThatThrownBy(() -> service.updatePackage("PKG-1", reqWithPlan))
    // .isInstanceOf(RuntimeException.class)
    // .hasMessageContaining("cannot be edited");

    // // remove plans; change status not PENDING -> cannot edit
    // existing.getPlans().clear();
    // existing.setStatus("Waiting for Payment");
    // assertThatThrownBy(() -> service.updatePackage("PKG-1", reqWithPlan))
    // .isInstanceOf(RuntimeException.class)
    // .hasMessageContaining("Only packages with status 'PENDING'");

    // // set PENDING again; invalid date -> throws
    // existing.setStatus("PENDING");
    // CreatePackageRequestDTO badDate = new CreatePackageRequestDTO(null, "u1",
    // "New", 3, 0L, null,
    // LocalDateTime.of(2025, 1, 2, 0, 0), LocalDateTime.of(2025, 1, 1, 0, 0));
    // assertThatThrownBy(() -> service.updatePackage("PKG-1", badDate))
    // .isInstanceOf(RuntimeException.class)
    // .hasMessageContaining("End date must be after start date");

    // // happy path
    // CreatePackageRequestDTO good = new CreatePackageRequestDTO(null, "u1", "New",
    // 3, 0L, null,
    // LocalDateTime.of(2025, 1, 1, 0, 0), LocalDateTime.of(2025, 1, 3, 0, 0));
    // when(packageRepository.save(any(Package.class))).thenAnswer(inv ->
    // inv.getArgument(0));

    // PackageResponseDTO out = service.updatePackage("PKG-1", good);
    // assertThat(out.getPackageName()).isEqualTo("New");
    // assertThat(out.getQuota()).isEqualTo(3);
    // assertThat(existing.getStartDate()).isEqualTo(good.getStartDate());
    // assertThat(existing.getEndDate()).isEqualTo(good.getEndDate());
    // }

    @Test
    @DisplayName("processPackage enforces status, plans, fulfillment and capacity; happy path reduces capacity")
    void processPackage_rules_and_success() {
        Package p = pkg("PKG-1", "u1", "A", "PENDING");

        // not found
        when(packageRepository.findById("PKG-404")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.processPackage("PKG-404")).isInstanceOf(RuntimeException.class);

        // empty plans -> throws
        when(packageRepository.findById("PKG-1")).thenReturn(Optional.of(p));
        assertThatThrownBy(() -> service.processPackage("PKG-1")).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("at least one plan");

        // add plan not fulfilled -> throws
        Plan notFulfilled = plan("PENDING");
        p.getPlans().add(notFulfilled);
        assertThatThrownBy(() -> service.processPackage("PKG-1")).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("All plans must have status 'FULFILLED'");

        // fulfilled but over-capacity -> throws
        notFulfilled.setStatus("FULFILLED");
        Activity act = new Activity();
        act.setId("ACT-1");
        act.setCapacity(1);
        act.setActivityName("Hotel");
        OrderedQuantity oq = new OrderedQuantity();
        oq.setActivity(act);
        oq.setOrderedQuota(5);
        notFulfilled.getOrderedQuantities().add(oq);
        assertThatThrownBy(() -> service.processPackage("PKG-1")).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("does not have enough capacity");

        // reset for happy path: sufficient capacity
        act.setCapacity(10);
        oq.setOrderedQuota(3);
        // Previous call may have mutated package status to Waiting for Payment before
        // throwing;
        // reset to PENDING
        p.setStatus("PENDING");
        when(packageRepository.save(any(Package.class))).thenAnswer(inv -> inv.getArgument(0));

        PackageResponseDTO out = service.processPackage("PKG-1");
        assertThat(out.getStatus()).isEqualTo("Waiting for Payment");
        assertThat(act.getCapacity()).isEqualTo(7); // reduced
    }

    @Test
    @DisplayName("deletePackage enforces status, clears plans and marks DELETED")
    void deletePackage_rules() {
        Package p = pkg("PKG-1", "u1", "A", "PENDING");
        Plan pl = plan("ANY");
        p.getPlans().add(pl);

        when(packageRepository.findById("PKG-404")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deletePackage("PKG-404")).isInstanceOf(RuntimeException.class);

        // not pending -> throws
        Package nonPending = pkg("PKG-2", "u1", "B", "Waiting for Payment");
        when(packageRepository.findById("PKG-2")).thenReturn(Optional.of(nonPending));
        assertThatThrownBy(() -> service.deletePackage("PKG-2")).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only packages with status 'PENDING'");

        // happy path
        when(packageRepository.findById("PKG-1")).thenReturn(Optional.of(p));
        when(packageRepository.save(any(Package.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(planRepository).deleteAll(anyList());

        service.deletePackage("PKG-1");
        assertThat(p.getStatus()).isEqualTo("DELETED");
        verify(planRepository).deleteAll(anyList());
    }
}
