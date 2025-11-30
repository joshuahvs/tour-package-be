package apap.ti._5.tour_package_2306165540_be.restservice;

import apap.ti._5.tour_package_2306165540_be.model.Activity;
import apap.ti._5.tour_package_2306165540_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306165540_be.model.Plan;
import apap.ti._5.tour_package_2306165540_be.model.profile.EndUser;
import apap.ti._5.tour_package_2306165540_be.model.profile.AccommodationOwner;
import apap.ti._5.tour_package_2306165540_be.model.profile.EndUser;
import apap.ti._5.tour_package_2306165540_be.model.profile.FlightAirline;
import apap.ti._5.tour_package_2306165540_be.model.profile.InsuranceProvider;
import apap.ti._5.tour_package_2306165540_be.model.profile.RentalVendor;
import apap.ti._5.tour_package_2306165540_be.model.profile.RoleType;
import apap.ti._5.tour_package_2306165540_be.model.profile.SuperAdmin;
import apap.ti._5.tour_package_2306165540_be.model.profile.TourPackageVendor;
import apap.ti._5.tour_package_2306165540_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306165540_be.repository.EndUserRepository;
import apap.ti._5.tour_package_2306165540_be.restdto.request.CreateActivityRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.request.UpdateActivityRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.ActivityResponseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityRestServiceImplTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private EndUserRepository endUserRepository;

    @InjectMocks
    private ActivityRestServiceImpl service;

    @AfterEach
    void cleanupSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getAllActivities applies filters and sorts by start date")
    void getAllActivities_filtersAndSorts() {
        Activity match = activity(
                "ACT-1",
                "Flight",
                "CGK",
                "DPS",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(3));
        Activity nonMatchType = activity(
                "ACT-2",
                "Accommodation",
                "CGK",
                "DPS",
                match.getStartDate(),
                match.getEndDate());
        Activity nonMatchDate = activity(
                "ACT-3",
                "Flight",
                "CGK",
                "DPS",
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(6));

        when(activityRepository.findAll()).thenReturn(List.of(nonMatchType, match, nonMatchDate));

        List<ActivityResponseDTO> result = service.getAllActivities(
                "Flight",
                "CGK",
                "DPS",
                match.getStartDate().minusHours(1).toString(),
                match.getEndDate().plusHours(1).toString(),
                "ACT-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("ACT-1");
    }

    @Test
    @DisplayName("getAllActivities throws when end date precedes start date")
    void getAllActivities_invalidDateRange() {
        assertThatThrownBy(
                () -> service.getAllActivities(null, null, null, "2025-01-05T10:00", "2025-01-04T10:00", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End date cannot be before start date");
    }

    @Test
    @DisplayName("createActivity saves entity for authorized role")
    void createActivity_superadminHappyPath() {
        mockAuthenticatedUser(RoleType.SUPERADMIN);
        lenient().when(activityRepository.findAll()).thenReturn(List.of());
        when(activityRepository.save(any(Activity.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateActivityRequestDTO request = new CreateActivityRequestDTO();
        request.setActivityName("Eco Flight");
        request.setActivityItem("Ticket");
        request.setActivityType("Flight");
        request.setCapacity(120);
        request.setPrice(1_200_000L);
        request.setStartDate(LocalDateTime.now().plusDays(2));
        request.setEndDate(LocalDateTime.now().plusDays(3));
        request.setStartLocation("CGK");
        request.setEndLocation("DPS");

        ActivityResponseDTO created = service.createActivity(request);
        assertThat(created.getActivityName()).isEqualTo("Eco Flight");

        ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
        verify(activityRepository).save(captor.capture());
        assertThat(captor.getValue().getActivityName()).isEqualTo("Eco Flight");
    }

    @Test
    @DisplayName("createActivity enforces vendor-specific activity type rules")
    void createActivity_disallowsWrongVendorType() {
        mockAuthenticatedUser(RoleType.RENTAL_VENDOR);

        CreateActivityRequestDTO request = new CreateActivityRequestDTO();
        request.setActivityName("Luxury Flight");
        request.setActivityItem("Ticket");
        request.setActivityType("Flight");
        request.setCapacity(10);
        request.setPrice(1L);
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setEndDate(LocalDateTime.now().plusDays(2));
        request.setStartLocation("A");
        request.setEndLocation("B");

        assertThatThrownBy(() -> service.createActivity(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Rental Vendor");
    }

    @Test
    @DisplayName("updateActivity blocks modifications when fulfilled orders exist")
    void updateActivity_preventsFulfilledOrders() {
        mockAuthenticatedUser(RoleType.SUPERADMIN);
        Activity activity = activity("ACT-99", "Flight", "A", "B", LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(3));
        Plan plan = new Plan();
        plan.setStatus("Fulfilled");
        OrderedQuantity oq = new OrderedQuantity();
        oq.setPlan(plan);
        activity.setOrderedQuantities(List.of(oq));
        when(activityRepository.findById("ACT-99")).thenReturn(Optional.of(activity));

        UpdateActivityRequestDTO request = new UpdateActivityRequestDTO();
        request.setPrice(2_000_000L);

        assertThatThrownBy(() -> service.updateActivity("ACT-99", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("fulfilled orders");
    }

    @Test
    @DisplayName("deleteActivity rejects when unfulfilled orders remain")
    void deleteActivity_preventsUnfulfilledOrders() {
        mockAuthenticatedUser(RoleType.SUPERADMIN);
        Activity activity = activity("ACT-100", "Flight", "A", "B", LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        Plan plan = new Plan();
        plan.setStatus("Pending");
        OrderedQuantity oq = new OrderedQuantity();
        oq.setPlan(plan);
        activity.setOrderedQuantities(List.of(oq));
        when(activityRepository.findById("ACT-100")).thenReturn(Optional.of(activity));

        assertThatThrownBy(() -> service.deleteActivity("ACT-100"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("unfulfilled");
    }

    @Test
    @DisplayName("getActivityById blocks deleted activity detail access")
    void getActivityById_rejectsDeletedActivity() {
        Activity activity = activity("ACT-DEL", "Flight", "A", "B",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        activity.setIsDeleted(true);
        when(activityRepository.findById("ACT-DEL")).thenReturn(Optional.of(activity));

        assertThatThrownBy(() -> service.getActivityById("ACT-DEL"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("deleted");
    }

    @Test
    @DisplayName("updateActivity rejects start date in the past")
    void updateActivity_rejectsPastStartDate() {
        mockAuthenticatedUser(RoleType.SUPERADMIN);
        Activity activity = activity("ACT-PAST", "Flight", "A", "B",
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(6));
        when(activityRepository.findById("ACT-PAST")).thenReturn(Optional.of(activity));

        UpdateActivityRequestDTO request = new UpdateActivityRequestDTO();
        request.setStartDate(LocalDateTime.now().minusDays(1));

        assertThatThrownBy(() -> service.updateActivity("ACT-PAST", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Start date cannot be in the past");
    }

    @Test
    @DisplayName("updateActivity enforces ownership for non-superadmin")
    void updateActivity_requiresOwnership() {
        mockAuthenticatedUser(RoleType.TOUR_PACKAGE_VENDOR);
        Activity activity = activity("ACT-OWN", "Flight", "A", "B",
                LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4));
        activity.setCreatorId(UUID.randomUUID().toString());
        when(activityRepository.findById("ACT-OWN")).thenReturn(Optional.of(activity));

        UpdateActivityRequestDTO request = new UpdateActivityRequestDTO();
        request.setPrice(2_000_000L);

        assertThatThrownBy(() -> service.updateActivity("ACT-OWN", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("only modify activities you created");
    }

    @Test
    @DisplayName("deleteActivity soft deletes when no blocking orders")
    void deleteActivity_marksAsDeleted() {
        mockAuthenticatedUser(RoleType.SUPERADMIN);
        Activity activity = activity("ACT-DEL-OK", "Flight", "A", "B",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        when(activityRepository.findById("ACT-DEL-OK")).thenReturn(Optional.of(activity));

        service.deleteActivity("ACT-DEL-OK");

        ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
        verify(activityRepository).save(captor.capture());
        assertThat(captor.getValue().getIsDeleted()).isTrue();
    }

    private Activity activity(String id, String type, String start, String end, LocalDateTime startDate,
            LocalDateTime endDate) {
        Activity activity = new Activity();
        activity.setId(id);
        activity.setActivityType(type);
        activity.setStartLocation(start);
        activity.setEndLocation(end);
        activity.setStartDate(startDate);
        activity.setEndDate(endDate);
        activity.setActivityName(id);
        activity.setIsDeleted(false);
        activity.setCreatorId(UUID.randomUUID().toString());
        return activity;
    }

    private void mockAuthenticatedUser(RoleType roleType) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("test-user", null,
                List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        EndUser user = switch (roleType) {
            case SUPERADMIN -> new SuperAdmin();
            case ACCOMMODATION_OWNER -> new AccommodationOwner();
            case FLIGHT_AIRLINE -> new FlightAirline();
            case INSURANCE_PROVIDER -> new InsuranceProvider();
            case TOUR_PACKAGE_VENDOR -> new TourPackageVendor();
            case RENTAL_VENDOR -> new RentalVendor();
            default -> throw new IllegalArgumentException("Unsupported role for test: " + roleType);
        };
        user.setId(UUID.randomUUID());
        user.setUsername("test-user");
        when(endUserRepository.findByUsernameIgnoreCase("test-user"))
                .thenReturn(Optional.of(user));
    }
}
