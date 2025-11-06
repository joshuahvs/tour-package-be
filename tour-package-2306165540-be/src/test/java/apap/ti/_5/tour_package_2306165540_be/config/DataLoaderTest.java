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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;

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

    @Test
    void run_shouldCreateData_whenEmpty() throws Exception {
        when(activityRepository.count()).thenReturn(0L);

        dataLoader.run();

        verify(activityRepository, times(1)).saveAll(Mockito.<List<Activity>>any());
        verify(packageRepository, times(1)).saveAll(Mockito.<List<Package>>any());
        verify(planRepository, times(1)).saveAll(Mockito.<List<Plan>>any());
        verify(orderedQuantityRepository, times(1)).saveAll(Mockito.<List<OrderedQuantity>>any());
    }
}
