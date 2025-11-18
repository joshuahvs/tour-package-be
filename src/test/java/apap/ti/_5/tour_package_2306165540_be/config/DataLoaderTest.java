package apap.ti._5.tour_package_2306165540_be.config;

import apap.ti._5.tour_package_2306165540_be.model.Activity;
import apap.ti._5.tour_package_2306165540_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306165540_be.model.Package;
import apap.ti._5.tour_package_2306165540_be.model.Plan;
import apap.ti._5.tour_package_2306165540_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306165540_be.repository.OrderedQuantityRepository;
import apap.ti._5.tour_package_2306165540_be.repository.PackageRepository;
import apap.ti._5.tour_package_2306165540_be.repository.PlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

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

    private DataLoader dataLoader;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        dataLoader = new DataLoader(activityRepository, packageRepository, planRepository, orderedQuantityRepository);
    }

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
        assertThat(activities).hasSize(10 + 3 * 120);

        List<Package> initialPackages = (List<Package>) packageCaptor.getAllValues().get(0);
        assertThat(initialPackages).hasSize(4);
        assertThat(initialPackages)
                .extracting(Package::getStatus)
                .containsExactlyInAnyOrder("PROCESSED", "PROCESSED", "PROCESSED", "PENDING");

        List<Package> finalPackages = (List<Package>) packageCaptor.getAllValues().get(1);
        assertThat(finalPackages)
                .allMatch(pkg -> pkg.getPrice() >= 0L, "package price should be non-negative after seeding");

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
