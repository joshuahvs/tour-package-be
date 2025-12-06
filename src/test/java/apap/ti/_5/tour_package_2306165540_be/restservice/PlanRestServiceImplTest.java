package apap.ti._5.tour_package_2306165540_be.restservice;

import apap.ti._5.tour_package_2306165540_be.model.Activity;
import apap.ti._5.tour_package_2306165540_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306165540_be.model.Package;
import apap.ti._5.tour_package_2306165540_be.model.Plan;
import apap.ti._5.tour_package_2306165540_be.model.profile.Customer;
import apap.ti._5.tour_package_2306165540_be.model.profile.EndUser;
import apap.ti._5.tour_package_2306165540_be.model.profile.SuperAdmin;
import apap.ti._5.tour_package_2306165540_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306165540_be.repository.EndUserRepository;
import apap.ti._5.tour_package_2306165540_be.repository.OrderedQuantityRepository;
import apap.ti._5.tour_package_2306165540_be.repository.PackageRepository;
import apap.ti._5.tour_package_2306165540_be.repository.PlanRepository;
import apap.ti._5.tour_package_2306165540_be.restdto.request.AddOrderedQuantityRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.request.CreatePlanRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.request.UpdatePlanRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.request.UpdateOrderedQuantityRequestDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PlanRestServiceImplTest {

    @Mock
    private PlanRepository planRepository;
    @Mock
    private PackageRepository packageRepository;
    @Mock
    private OrderedQuantityRepository orderedQuantityRepository;
    @Mock
    private EndUserRepository endUserRepository;
    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private PlanRestServiceImpl service;

    private Package pendingPackage;
    private Package processedPackage;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        pendingPackage = new Package();
        pendingPackage.setId("PKG-001");
        pendingPackage.setStatus("PENDING");
        pendingPackage.setQuota(10);
        pendingPackage.setStartDate(LocalDateTime.now().minusDays(1));
        pendingPackage.setEndDate(LocalDateTime.now().plusDays(10));
        pendingPackage.setUserId(UUID.randomUUID().toString());

        processedPackage = new Package();
        processedPackage.setId("PKG-002");
        processedPackage.setStatus("Waiting for Payment");
        processedPackage.setQuota(10);
        processedPackage.setUserId(UUID.randomUUID().toString());

        mockAuthenticatedSuperAdmin();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createPlan_happyPath_pendingPackage() {
        // Given a pending package and valid request
        when(packageRepository.findById("PKG-001")).thenReturn(Optional.of(pendingPackage));
        CreatePlanRequestDTO req = new CreatePlanRequestDTO();
        req.setPlanName("My Plan");
        req.setActivityType("Flight");
        req.setStartDate(pendingPackage.getStartDate().plusHours(1));
        req.setEndDate(pendingPackage.getStartDate().plusHours(2));
        req.setStartLocation("CGK");
        req.setEndLocation("DPS");

        when(planRepository.save(any(Plan.class))).thenAnswer(inv -> inv.getArgument(0));

        var dto = service.createPlan("PKG-001", req);
        assertEquals("My Plan", dto.getPlanName());
        assertEquals("Unfulfilled", dto.getStatus());
    }

    @Test
    void createPlan_shouldReject_nonPendingPackage() {
        when(packageRepository.findById("PKG-002")).thenReturn(Optional.of(processedPackage));
        CreatePlanRequestDTO req = new CreatePlanRequestDTO();
        req.setPlanName("X");
        req.setActivityType("Flight");
        req.setStartDate(LocalDateTime.now());
        req.setEndDate(LocalDateTime.now().plusHours(1));
        req.setStartLocation("A");
        req.setEndLocation("B");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.createPlan("PKG-002", req));
        assertTrue(ex.getMessage().toLowerCase().contains("pending"));
    }

    @Test
    void updatePlan_shouldUpdate_whenStatusUnfulfilled() {
        Plan plan = new Plan();
        plan.setId(UUID.randomUUID());
        plan.setStatus("Unfulfilled");
        plan.setPackageEntity(pendingPackage);
        when(planRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(planRepository.save(any(Plan.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdatePlanRequestDTO req = new UpdatePlanRequestDTO();
        req.setPlanName("Updated Name");
        req.setStartDate(pendingPackage.getStartDate().plusHours(1));
        req.setEndDate(pendingPackage.getStartDate().plusHours(2));
        req.setStartLocation("11");
        req.setEndLocation("11");

        var dto = service.updatePlan(plan.getId(), req);
        assertEquals("Updated Name", dto.getPlanName());
    }

    @Test
    void updatePlan_shouldReject_whenStatusNotUnfulfilled() {
        Plan plan = new Plan();
        plan.setId(UUID.randomUUID());
        plan.setPackageEntity(pendingPackage);
        plan.setStatus("FULFILLED");
        OrderedQuantity oq = new OrderedQuantity();
        oq.setId(UUID.randomUUID());
        oq.setPlan(plan);
        plan.getOrderedQuantities().add(oq);
        when(planRepository.findById(plan.getId())).thenReturn(Optional.of(plan));

        UpdatePlanRequestDTO req = new UpdatePlanRequestDTO();
        req.setPlanName("name");
        req.setStartDate(pendingPackage.getStartDate().plusHours(1));
        req.setEndDate(pendingPackage.getStartDate().plusHours(2));
        req.setStartLocation("A");
        req.setEndLocation("A");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.updatePlan(plan.getId(), req));
        assertTrue(ex.getMessage().toLowerCase().contains("unfulfilled"));
    }

    @Test
    void deletePlan_shouldSoftDelete_whenPackagePending() {
        UUID planId = UUID.randomUUID();
        Plan plan = new Plan();
        plan.setId(planId);
        plan.setPackageEntity(pendingPackage);
        plan.setStatus("Unfulfilled");

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));

        service.deletePlan(planId);

        ArgumentCaptor<Plan> captor = ArgumentCaptor.forClass(Plan.class);
        verify(planRepository).save(captor.capture());
        assertNotNull(captor.getValue().getDeletedAt());
    }

    @Test
    void deletePlan_shouldThrow_whenPackageNotPending() {
        UUID planId = UUID.randomUUID();
        Plan plan = new Plan();
        plan.setId(planId);
        plan.setPackageEntity(processedPackage);
        plan.setStatus("Fulfilled");
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.deletePlan(planId));
        assertTrue(ex.getMessage().toLowerCase().contains("unfulfilled"));
        verify(planRepository, never()).save(any());
    }

    @Test
    void updateOrderedQuantity_shouldRecalculatePriceAndStatus() {
        // Arrange a plan with package quota 10 and one ordered quantity capacity 10
        Plan plan = new Plan();
        plan.setId(UUID.randomUUID());
        plan.setPackageEntity(pendingPackage);
        plan.setPrice(0L);
        plan.setStatus("Unfulfilled");

        OrderedQuantity oq = new OrderedQuantity();
        oq.setId(UUID.randomUUID());
        oq.setPlan(plan);
        oq.setQuota(10);
        oq.setPrice(1000L);
        oq.setOrderedQuota(3);
        plan.getOrderedQuantities().add(oq);

        UpdateOrderedQuantityRequestDTO req = new UpdateOrderedQuantityRequestDTO();
        req.setOrderedQuantity(10); // make it equal to package quota

        when(orderedQuantityRepository.findById(oq.getId())).thenReturn(Optional.of(oq));
        when(planRepository.save(any(Plan.class))).thenAnswer(inv -> inv.getArgument(0));
        when(planRepository.findById(plan.getId())).thenReturn(Optional.of(plan));

        var result = service.updateOrderedQuantity(oq.getId(), req);

        assertEquals(10_000L, result.getTotalPrice());
        assertEquals("Fulfilled", result.getStatus());
        verify(orderedQuantityRepository).save(any());
        verify(planRepository, atLeastOnce()).save(any());
    }

    @Test
    void updateOrderedQuantity_shouldValidateCapacityAndQuota() {
        // Plan quota 5, ordered capacity 3
        pendingPackage.setQuota(5);
        Plan plan = new Plan();
        plan.setId(UUID.randomUUID());
        plan.setPackageEntity(pendingPackage);
        plan.setStatus("Unfulfilled");

        OrderedQuantity oq = new OrderedQuantity();
        oq.setId(UUID.randomUUID());
        oq.setPlan(plan);
        oq.setQuota(3);
        oq.setPrice(1000L);
        oq.setOrderedQuota(2);
        plan.getOrderedQuantities().add(oq);

        UpdateOrderedQuantityRequestDTO req = new UpdateOrderedQuantityRequestDTO();
        req.setOrderedQuantity(6); // exceeds package quota AND capacity

        when(orderedQuantityRepository.findById(oq.getId())).thenReturn(Optional.of(oq));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.updateOrderedQuantity(oq.getId(), req));
        assertTrue(
                ex.getMessage().toLowerCase().contains("capacity") || ex.getMessage().toLowerCase().contains("quota"));
    }

    @Test
    void addOrderedQuantity_happyPath_shouldUpdatePriceAndStatus() {
        // current plan in pending package
        pendingPackage.setQuota(5);
        Plan current = new Plan();
        current.setId(UUID.randomUUID());
        current.setActivityType("Flight");
        current.setPackageEntity(pendingPackage);
        current.setStartDate(LocalDateTime.now());
        current.setEndDate(LocalDateTime.now().plusHours(2));
        current.setStartLocation("CGK");
        current.setEndLocation("DPS");
        current.setStatus("Unfulfilled");

        // activity plan that matches constraints
        Package pkg2 = new Package();
        pkg2.setId("PKG-2");
        pkg2.setQuota(10);
        Plan activityPlan = new Plan();
        activityPlan.setId(UUID.randomUUID());
        activityPlan.setPackageEntity(pkg2);
        activityPlan.setPlanName("Some Flight");
        activityPlan.setActivityType("Flight");
        activityPlan.setStartDate(current.getStartDate());
        activityPlan.setEndDate(current.getEndDate());
        activityPlan.setStartLocation("CGK");
        activityPlan.setEndLocation("DPS");
        activityPlan.setPrice(2000L);

        when(planRepository.findById(current.getId())).thenReturn(Optional.of(current));
        when(planRepository.findById(activityPlan.getId())).thenReturn(Optional.of(activityPlan));
        when(planRepository.save(any(Plan.class))).thenAnswer(inv -> inv.getArgument(0));

        AddOrderedQuantityRequestDTO req = new AddOrderedQuantityRequestDTO();
        req.setActivityId(activityPlan.getId());
        req.setOrderedQuantity(5);

        var result = service.addOrderedQuantity(current.getId(), req);
        assertEquals(10_000L, result.getTotalPrice());
        assertEquals("Fulfilled", result.getStatus());
    }

    @Test
    void deleteOrderedQuantity_softDeleteAndRecalculate() {
        pendingPackage.setQuota(10);
        Plan plan = new Plan();
        plan.setId(UUID.randomUUID());
        plan.setPackageEntity(pendingPackage);
        plan.setStatus("Unfulfilled");

        OrderedQuantity oq = new OrderedQuantity();
        oq.setId(UUID.randomUUID());
        oq.setPlan(plan);
        oq.setQuota(10);
        oq.setPrice(1000L);
        oq.setOrderedQuota(4);
        plan.getOrderedQuantities().add(oq);

        when(orderedQuantityRepository.findById(oq.getId())).thenReturn(Optional.of(oq));
        when(planRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(planRepository.save(any(Plan.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.deleteOrderedQuantity(oq.getId());
        assertNotNull(oq.getDeletedAt());
        assertEquals(0L, result.getTotalPrice());
        assertEquals("Unfulfilled", result.getStatus());
    }

    @Test
    void getAvailablePlansForActivity_shouldFilterCorrectly() {
        Plan current = new Plan();
        current.setId(UUID.randomUUID());
        current.setActivityType("Flight");
        current.setStartDate(LocalDateTime.of(2025, 1, 1, 8, 0));
        current.setEndDate(LocalDateTime.of(2025, 1, 1, 10, 0));
        current.setStartLocation("11");
        current.setEndLocation("12");

        Activity candidateGood = new Activity();
        candidateGood.setId("ACT-1");
        candidateGood.setActivityType("Flight");
        candidateGood.setActivityName("Flight 1");
        candidateGood.setStartDate(current.getStartDate());
        candidateGood.setEndDate(current.getEndDate());
        candidateGood.setStartLocation("11");
        candidateGood.setEndLocation("12");
        candidateGood.setCapacity(10);
        candidateGood.setPrice(1000L);

        Activity badType = new Activity();
        badType.setId("ACT-2");
        badType.setActivityType("Accommodation");
        badType.setActivityName("Hotel");
        badType.setStartDate(current.getStartDate());
        badType.setEndDate(current.getEndDate());
        badType.setStartLocation("11");
        badType.setEndLocation("11");

        when(planRepository.findById(current.getId())).thenReturn(Optional.of(current));
        when(activityRepository.findAll()).thenReturn(java.util.List.of(candidateGood, badType));

        var list = service.getAvailablePlansForActivity(current.getId());
        assertEquals(1, list.size());
        assertEquals(candidateGood.getActivityType(), list.get(0).getActivityType());
    }

    @Test
    void getAllPlansByPackage_denies_customer_not_owner() {
        Plan plan = new Plan();
        plan.setId(UUID.randomUUID());
        plan.setPlanName("Denied Plan");
        plan.setActivityType("Flight");
        plan.setStatus("Unfulfilled");
        plan.setStartDate(LocalDateTime.now());
        plan.setEndDate(LocalDateTime.now().plusHours(2));
        plan.setStartLocation("CGK");
        plan.setEndLocation("DPS");
        plan.setPackageEntity(pendingPackage);
        pendingPackage.getPlans().add(plan);

        pendingPackage.setUserId(UUID.randomUUID().toString());
        when(packageRepository.findById(pendingPackage.getId())).thenReturn(Optional.of(pendingPackage));

        mockAuthenticatedCustomer(UUID.randomUUID(), "cust-non-owner");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getAllPlansByPackage(pendingPackage.getId()));
        assertTrue(ex.getMessage().toLowerCase().contains("permission"));
    }

    @Test
    void getAllPlansByPackage_allows_owner_customer() {
        UUID ownerId = UUID.randomUUID();
        pendingPackage.setUserId(ownerId.toString());

        Plan plan = new Plan();
        plan.setId(UUID.randomUUID());
        plan.setPlanName("Owner Plan");
        plan.setActivityType("Flight");
        plan.setStatus("Unfulfilled");
        plan.setStartDate(LocalDateTime.now());
        plan.setEndDate(LocalDateTime.now().plusHours(1));
        plan.setStartLocation("CGK");
        plan.setEndLocation("DPS");
        plan.setPackageEntity(pendingPackage);
        pendingPackage.getPlans().add(plan);

        when(packageRepository.findById(pendingPackage.getId())).thenReturn(Optional.of(pendingPackage));
        mockAuthenticatedCustomer(ownerId, "cust-owner");

        var result = service.getAllPlansByPackage(pendingPackage.getId());
        assertEquals(1, result.size());
        assertEquals(plan.getId(), result.get(0).getId());
    }

    @Test
    void getPlanDetail_requires_authentication() {
        SecurityContextHolder.clearContext();

        Plan plan = new Plan();
        plan.setId(UUID.randomUUID());
        plan.setPlanName("Detail Plan");
        plan.setActivityType("Flight");
        plan.setStatus("Unfulfilled");
        plan.setPrice(0L);
        plan.setStartDate(LocalDateTime.now());
        plan.setEndDate(LocalDateTime.now().plusHours(1));
        plan.setStartLocation("CGK");
        plan.setEndLocation("DPS");

        Package pkg = new Package();
        pkg.setId("PKG-DTL");
        pkg.setUserId(UUID.randomUUID().toString());
        pkg.setPackageName("Pkg");
        plan.setPackageEntity(pkg);

        when(planRepository.findById(plan.getId())).thenReturn(Optional.of(plan));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.getPlanDetail(plan.getId()));
        assertTrue(ex.getMessage().toLowerCase().contains("permission"));
    }

    @Test
    void addOrderedQuantity_rejects_activity_type_mismatch() {
        Plan current = new Plan();
        current.setId(UUID.randomUUID());
        current.setPlanName("Flight Plan");
        current.setActivityType("Flight");
        current.setStatus("Unfulfilled");
        current.setStartDate(LocalDateTime.now());
        current.setEndDate(LocalDateTime.now().plusHours(2));
        current.setStartLocation("CGK");
        current.setEndLocation("DPS");
        current.setPackageEntity(pendingPackage);

        Plan activityPlan = new Plan();
        activityPlan.setId(UUID.randomUUID());
        activityPlan.setActivityType("Accommodation");
        activityPlan.setPlanName("Hotel");
        activityPlan.setStartDate(current.getStartDate());
        activityPlan.setEndDate(current.getEndDate());
        activityPlan.setStartLocation("CGK");
        activityPlan.setEndLocation("CGK");
        Package source = new Package();
        source.setQuota(10);
        activityPlan.setPackageEntity(source);
        activityPlan.setPrice(1000L);

        when(planRepository.findById(current.getId())).thenReturn(Optional.of(current));
        when(planRepository.findById(activityPlan.getId())).thenReturn(Optional.of(activityPlan));

        AddOrderedQuantityRequestDTO req = new AddOrderedQuantityRequestDTO();
        req.setActivityId(activityPlan.getId());
        req.setOrderedQuantity(1);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.addOrderedQuantity(current.getId(), req));
        assertTrue(ex.getMessage().toLowerCase().contains("activity type"));
    }

    @Test
    void addOrderedQuantity_rejects_when_total_exceeds_quota() {
        pendingPackage.setQuota(5);
        Plan current = new Plan();
        current.setId(UUID.randomUUID());
        current.setPlanName("Flight Plan");
        current.setActivityType("Flight");
        current.setStatus("Unfulfilled");
        current.setStartDate(LocalDateTime.now());
        current.setEndDate(LocalDateTime.now().plusHours(2));
        current.setStartLocation("CGK");
        current.setEndLocation("DPS");
        current.setPackageEntity(pendingPackage);

        OrderedQuantity existing = new OrderedQuantity();
        existing.setId(UUID.randomUUID());
        existing.setPlan(current);
        existing.setOrderedQuota(4);
        existing.setPrice(500L);
        existing.setQuota(10);
        current.getOrderedQuantities().add(existing);

        Plan activityPlan = new Plan();
        activityPlan.setId(UUID.randomUUID());
        activityPlan.setActivityType("Flight");
        activityPlan.setPlanName("Flight Option");
        activityPlan.setStartDate(current.getStartDate());
        activityPlan.setEndDate(current.getEndDate());
        activityPlan.setStartLocation("CGK");
        activityPlan.setEndLocation("DPS");
        Package source = new Package();
        source.setQuota(10);
        activityPlan.setPackageEntity(source);
        activityPlan.setPrice(1000L);

        when(planRepository.findById(current.getId())).thenReturn(Optional.of(current));
        when(planRepository.findById(activityPlan.getId())).thenReturn(Optional.of(activityPlan));

        AddOrderedQuantityRequestDTO req = new AddOrderedQuantityRequestDTO();
        req.setActivityId(activityPlan.getId());
        req.setOrderedQuantity(2);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.addOrderedQuantity(current.getId(), req));
        assertTrue(ex.getMessage().toLowerCase().contains("total ordered quantity"));
    }

    private void mockAuthenticatedSuperAdmin() {
        SuperAdmin admin = new SuperAdmin();
        admin.setId(UUID.randomUUID());
        admin.setUsername("test-user");
        mockAuthenticatedUser(admin);
    }

    private void mockAuthenticatedCustomer(UUID customerId, String username) {
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setUsername(username);
        mockAuthenticatedUser(customer);
    }

    private void mockAuthenticatedUser(EndUser user) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user.getUsername(), null,
                java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(endUserRepository.findByUsernameIgnoreCase(user.getUsername()))
                .thenReturn(Optional.of(user));
    }
}
