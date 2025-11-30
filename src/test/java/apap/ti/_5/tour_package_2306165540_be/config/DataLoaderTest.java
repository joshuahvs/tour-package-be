package apap.ti._5.tour_package_2306165540_be.config;

import apap.ti._5.tour_package_2306165540_be.model.Activity;
import apap.ti._5.tour_package_2306165540_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306165540_be.model.Package;
import apap.ti._5.tour_package_2306165540_be.model.Plan;
import apap.ti._5.tour_package_2306165540_be.model.profile.AccommodationOwner;
import apap.ti._5.tour_package_2306165540_be.model.profile.EndUser;
import apap.ti._5.tour_package_2306165540_be.model.profile.FlightAirline;
import apap.ti._5.tour_package_2306165540_be.model.profile.RentalVendor;
import apap.ti._5.tour_package_2306165540_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306165540_be.repository.EndUserRepository;
import apap.ti._5.tour_package_2306165540_be.repository.OrderedQuantityRepository;
import apap.ti._5.tour_package_2306165540_be.repository.PackageRepository;
import apap.ti._5.tour_package_2306165540_be.repository.PlanRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class DataLoaderTest {

        @Mock
        private ActivityRepository activityRepository;
        @Mock
        private PackageRepository packageRepository;
        @Mock
        private PlanRepository planRepository;
        @Mock
        private OrderedQuantityRepository orderedQuantityRepository;
        @Mock
        private EndUserRepository endUserRepository;
        @Mock
        private PasswordEncoder passwordEncoder;

        private DataLoader dataLoader;
        private AutoCloseable mocks;
        private final Map<String, EndUser> userStore = new HashMap<>();

        @BeforeEach
        void setup() {
                mocks = MockitoAnnotations.openMocks(this);

                userStore.clear();
                seedExistingUser("flightairline", new FlightAirline());
                seedExistingUser("accommodationowner", new AccommodationOwner());
                seedExistingUser("rentalvendor", new RentalVendor());

                lenient().when(endUserRepository.findByUsernameIgnoreCase(anyString())).thenAnswer(invocation -> {
                        String username = ((String) invocation.getArgument(0)).toLowerCase();
                        return Optional.ofNullable(userStore.get(username));
                });

                lenient().when(endUserRepository.save(any(EndUser.class))).thenAnswer(invocation -> {
                        EndUser saved = invocation.getArgument(0);
                        if (saved.getUsername() != null) {
                                userStore.put(saved.getUsername().toLowerCase(), saved);
                        }
                        if (saved.getId() == null) {
                                saved.setId(UUID.randomUUID());
                        }
                        return saved;
                });

                lenient().when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

                dataLoader = new DataLoader(activityRepository, packageRepository,
                                planRepository, orderedQuantityRepository, endUserRepository, passwordEncoder);
        }

        @AfterEach
        void tearDown() throws Exception {
                if (mocks != null) {
                        mocks.close();
                }
        }

        private void seedExistingUser(String username, EndUser user) {
                user.setId(UUID.randomUUID());
                user.setUsername(username);
                userStore.put(username.toLowerCase(), user);
        }

        // @BeforeEach
        // void setup() {
        // MockitoAnnotations.openMocks(this);
        // dataLoader = new DataLoader(activityRepository, packageRepository,
        // planRepository, orderedQuantityRepository);
        // }

        @Test
        void run_shouldSkip_whenActivitiesExist() throws Exception {
                when(activityRepository.count()).thenReturn(1L);
                dataLoader.run();
                verify(activityRepository, never()).saveAll(anyList());
                verify(packageRepository, never()).saveAll(anyList());
                verify(planRepository, never()).saveAll(anyList());
                verify(orderedQuantityRepository, never()).saveAll(anyList());
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        void run_shouldSeedData_whenDatabaseEmpty() throws Exception {
                when(activityRepository.count()).thenReturn(0L);

                var activityCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
                var packageCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
                var planCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
                var orderedQuantityCaptor = org.mockito.ArgumentCaptor.forClass(List.class);

                dataLoader.run();

                verify(activityRepository).saveAll(activityCaptor.capture());
                verify(packageRepository, times(2)).saveAll(packageCaptor.capture());
                verify(planRepository, times(2)).saveAll(planCaptor.capture());
                verify(orderedQuantityRepository).saveAll(orderedQuantityCaptor.capture());

                List<Activity> activities = activityCaptor.getValue();
                assertThat(activities).isNotEmpty();
                assertThat(activities).hasSize(10 + 3 * 20);

                List<Package> initialPackages = (List<Package>) packageCaptor.getAllValues().get(0);
                assertThat(initialPackages).hasSize(4);
                assertThat(initialPackages)
                                .extracting(Package::getStatus)
                                .containsExactlyInAnyOrder("Waiting for Payment", "Waiting for Payment",
                                                "Waiting for Payment",
                                                "PENDING");

                List<Package> finalPackages = (List<Package>) packageCaptor.getAllValues().get(1);
                assertThat(finalPackages)
                                .allMatch(pkg -> pkg.getPrice() >= 0L,
                                                "package price should be non-negative after seeding");

                List<Plan> seededPlans = (List<Plan>) planCaptor.getAllValues().get(0);
                assertThat(seededPlans).hasSize(8);
                assertThat(seededPlans)
                                .extracting(Plan::getStatus)
                                .contains("FULFILLED", "UNFULFILLED");

                List<Plan> refreshedPlans = (List<Plan>) planCaptor.getAllValues().get(1);
                assertThat(refreshedPlans)
                                .allMatch(plan -> plan.getPrice() != null && plan.getPrice() >= 0L,
                                                "plan price should be calculated after ordered quantities");

                List<OrderedQuantity> orderedQuantities = orderedQuantityCaptor.getValue();
                assertThat(orderedQuantities).hasSize(8);
                assertThat(orderedQuantities)
                                .allMatch(oq -> oq.getOrderedQuota() > 0 && oq.getPrice() != null && oq.getPrice() > 0,
                                                "ordered quantities should have positive quota and price");
        }
}
