package apap.ti._5.tour_package_2306165540_be.restservice;

import apap.ti._5.tour_package_2306165540_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306165540_be.model.Package;
import apap.ti._5.tour_package_2306165540_be.model.Plan;
import apap.ti._5.tour_package_2306165540_be.repository.OrderedQuantityRepository;
import apap.ti._5.tour_package_2306165540_be.repository.PackageRepository;
import apap.ti._5.tour_package_2306165540_be.repository.PlanRepository;
import apap.ti._5.tour_package_2306165540_be.restdto.request.AddOrderedQuantityRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.request.CreatePlanRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.request.UpdatePlanRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.request.UpdateOrderedQuantityRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.OrderedQuantityResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PlanDetailResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PlanResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanRestServiceImpl implements PlanRestService {

    private final PlanRepository planRepository;
    private final PackageRepository packageRepository;
    private final OrderedQuantityRepository orderedQuantityRepository;

    @Override
    public PlanResponseDTO createPlan(String packageId, CreatePlanRequestDTO requestDTO) {
        // Get the package
        Package packageEntity = packageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found with id: " + packageId));

        // Validate: Package must have status "Pending"
        if (!"PENDING".equalsIgnoreCase(packageEntity.getStatus())) {
            throw new RuntimeException("Cannot create plan. Package must have status 'Pending'.");
        }

        // Validate: EndDate tidak boleh lebih dahulu daripada startDate
        if (requestDTO.getEndDate().isBefore(requestDTO.getStartDate()) ||
                requestDTO.getEndDate().isEqual(requestDTO.getStartDate())) {
            throw new RuntimeException("End date must be after start date.");
        }

        // Validate: StartDate tidak boleh lebih dahulu daripada startDate Package
        if (requestDTO.getStartDate().isBefore(packageEntity.getStartDate())) {
            throw new RuntimeException("Plan start date cannot be before package start date (" +
                    packageEntity.getStartDate() + ").");
        }

        // Validate: EndDate tidak boleh setelah EndDate Package
        if (requestDTO.getEndDate().isAfter(packageEntity.getEndDate())) {
            throw new RuntimeException("Plan end date cannot be after package end date (" +
                    packageEntity.getEndDate() + ").");
        }

        // Validate: StartLocation dan endLocation untuk ActivityType Accommodation
        // harus sama
        if ("Accommodation".equalsIgnoreCase(requestDTO.getActivityType())) {
            if (!requestDTO.getStartLocation().equals(requestDTO.getEndLocation())) {
                throw new RuntimeException(
                        "Start location and end location must be the same for Accommodation activity type.");
            }
        }

        // Create new Plan
        Plan plan = new Plan();
        plan.setId(UUID.randomUUID());
        plan.setPackageEntity(packageEntity);
        plan.setPlanName(requestDTO.getPlanName());
        plan.setActivityType(requestDTO.getActivityType());
        plan.setStartDate(requestDTO.getStartDate());
        plan.setEndDate(requestDTO.getEndDate());
        plan.setStartLocation(requestDTO.getStartLocation());
        plan.setEndLocation(requestDTO.getEndLocation());
        plan.setPrice(0L); // Initial price is 0
        plan.setStatus("Unfulfilled"); // Status otomatis "Unfulfilled"

        // Save plan
        Plan savedPlan = planRepository.save(plan);

        // Convert to DTO
        return toPlanResponseDTO(savedPlan);
    }

    @Override
    public PlanDetailResponseDTO getPlanDetail(UUID planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found with id: " + planId));

        return toPlanDetailResponseDTO(plan);
    }

    @Override
    public PlanDetailResponseDTO updatePlan(UUID planId, UpdatePlanRequestDTO requestDTO) {
        // Get the plan
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found with id: " + planId));

        // Validate: Plan tidak boleh memiliki OrderedQuantity
        if (plan.getOrderedQuantities() != null && !plan.getOrderedQuantities().isEmpty()) {
            throw new RuntimeException("Cannot edit plan. Plan must not have any ordered activities.");
        }

        // Validate: Package harus memiliki status "Pending"
        if (!"PENDING".equalsIgnoreCase(plan.getPackageEntity().getStatus())) {
            throw new RuntimeException("Cannot edit plan. Package must have status 'Pending'.");
        }

        // Validate: EndDate tidak boleh lebih dahulu daripada startDate
        if (requestDTO.getEndDate().isBefore(requestDTO.getStartDate()) ||
                requestDTO.getEndDate().isEqual(requestDTO.getStartDate())) {
            throw new RuntimeException("End date must be after start date.");
        }

        // Validate: StartDate tidak boleh lebih dahulu daripada startDate Package
        if (requestDTO.getStartDate().isBefore(plan.getPackageEntity().getStartDate())) {
            throw new RuntimeException("Plan start date cannot be before package start date (" +
                    plan.getPackageEntity().getStartDate() + ").");
        }

        // Validate: EndDate tidak boleh setelah EndDate Package
        if (requestDTO.getEndDate().isAfter(plan.getPackageEntity().getEndDate())) {
            throw new RuntimeException("Plan end date cannot be after package end date (" +
                    plan.getPackageEntity().getEndDate() + ").");
        }

        // Validate: StartLocation dan endLocation untuk ActivityType Accommodation
        // harus sama
        if ("Accommodation".equalsIgnoreCase(plan.getActivityType())) {
            if (!requestDTO.getStartLocation().equals(requestDTO.getEndLocation())) {
                throw new RuntimeException(
                        "Start location and end location must be the same for Accommodation activity type.");
            }
        }

        // Update plan fields
        plan.setPlanName(requestDTO.getPlanName());
        plan.setStartDate(requestDTO.getStartDate());
        plan.setEndDate(requestDTO.getEndDate());
        plan.setStartLocation(requestDTO.getStartLocation());
        plan.setEndLocation(requestDTO.getEndLocation());

        // Save updated plan
        Plan updatedPlan = planRepository.save(plan);

        return toPlanDetailResponseDTO(updatedPlan);
    }

    private PlanResponseDTO toPlanResponseDTO(Plan plan) {
        PlanResponseDTO dto = new PlanResponseDTO();
        dto.setId(plan.getId());
        dto.setPlanName(plan.getPlanName());
        dto.setPrice(plan.getPrice());
        dto.setActivityType(plan.getActivityType());
        dto.setStatus(plan.getStatus());
        dto.setStartDate(plan.getStartDate());
        dto.setEndDate(plan.getEndDate());
        dto.setStartLocation(plan.getStartLocation());
        dto.setEndLocation(plan.getEndLocation());
        dto.setActivitiesCount(plan.getOrderedQuantities() != null ? plan.getOrderedQuantities().size() : 0);
        return dto;
    }

    private PlanDetailResponseDTO toPlanDetailResponseDTO(Plan plan) {
        PlanDetailResponseDTO dto = new PlanDetailResponseDTO();
        dto.setId(plan.getId());
        dto.setPlanName(plan.getPlanName());
        dto.setActivityType(plan.getActivityType());
        dto.setStatus(plan.getStatus());
        dto.setTotalPrice(plan.getPrice());
        dto.setStartDate(plan.getStartDate());
        dto.setEndDate(plan.getEndDate());
        dto.setStartLocation(plan.getStartLocation());
        dto.setEndLocation(plan.getEndLocation());
        dto.setPackageId(plan.getPackageEntity().getId());
        dto.setPackageName(plan.getPackageEntity().getPackageName());

        // Convert ordered quantities
        List<OrderedQuantityResponseDTO> orderedQuantityDTOs = plan.getOrderedQuantities().stream()
                .map(this::toOrderedQuantityResponseDTO)
                .collect(Collectors.toList());
        dto.setOrderedQuantities(orderedQuantityDTOs);

        return dto;
    }

    private OrderedQuantityResponseDTO toOrderedQuantityResponseDTO(OrderedQuantity orderedQuantity) {
        OrderedQuantityResponseDTO dto = new OrderedQuantityResponseDTO();
        dto.setId(orderedQuantity.getId());
        // Prefer plan-as-activity if present, otherwise fall back to legacy Activity
        if (orderedQuantity.getActivityPlan() != null) {
            dto.setActivityName(orderedQuantity.getActivityPlan().getPlanName());
            dto.setActivityId(orderedQuantity.getActivityPlan().getId().toString());
        } else if (orderedQuantity.getActivity() != null) {
            dto.setActivityName(orderedQuantity.getActivity().getActivityName());
            dto.setActivityId(orderedQuantity.getActivity().getId());
        } else {
            dto.setActivityName("-");
            dto.setActivityId(null);
        }
        dto.setStartDate(orderedQuantity.getStartDate());
        dto.setEndDate(orderedQuantity.getEndDate());
        dto.setPrice(orderedQuantity.getPrice());
        dto.setQuota(orderedQuantity.getQuota());
        dto.setOrderedQuota(orderedQuantity.getOrderedQuota());
        dto.setTotal(orderedQuantity.getPrice() * orderedQuantity.getOrderedQuota());
        return dto;
    }

    @Override
    public PlanDetailResponseDTO addOrderedQuantity(UUID planId, AddOrderedQuantityRequestDTO requestDTO) {
        // Get current Plan
        Plan currentPlan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan with id " + planId + " not found"));

        // Get Package
        Package pkg = currentPlan.getPackageEntity();

        // Validation 1: Package must be PENDING
        if (!"PENDING".equals(pkg.getStatus())) {
            throw new RuntimeException("Cannot add activities. Package status must be PENDING");
        }

        // Get Activity Plan (the plan that will be added as activity)
        Plan activityPlan = planRepository.findById(requestDTO.getActivityId())
                .orElseThrow(() -> new RuntimeException("Activity plan not found"));

        // Validation 2: ActivityType must match
        if (!activityPlan.getActivityType().equals(currentPlan.getActivityType())) {
            throw new RuntimeException("Activity type must match plan activity type");
        }

        // Validation 3: Activity plan start date >= Current plan start date
        if (activityPlan.getStartDate().isBefore(currentPlan.getStartDate())) {
            throw new RuntimeException("Activity start date must be on or after plan start date");
        }

        // Validation 4: Activity plan end date <= Current plan end date
        if (activityPlan.getEndDate().isAfter(currentPlan.getEndDate())) {
            throw new RuntimeException("Activity end date must be on or before plan end date");
        }

        // Validation 5: Start and End locations must match
        if (!activityPlan.getStartLocation().equals(currentPlan.getStartLocation()) ||
                !activityPlan.getEndLocation().equals(currentPlan.getEndLocation())) {
            throw new RuntimeException("Activity start and end locations must match plan locations");
        }

        // Validation 6: Check total ordered quantity <= package quota
        int totalOrderedQuantity = currentPlan.getOrderedQuantities().stream()
                .mapToInt(OrderedQuantity::getOrderedQuota)
                .sum();

        if (totalOrderedQuantity + requestDTO.getOrderedQuantity() > pkg.getQuota()) {
            throw new RuntimeException("Total ordered quantity cannot exceed package quota");
        }

        // Validation 7: Check ordered quantity <= activity plan capacity (use package
        // quota as capacity)
        Package activityPackage = activityPlan.getPackageEntity();
        if (requestDTO.getOrderedQuantity() > activityPackage.getQuota()) {
            throw new RuntimeException("Ordered quantity cannot exceed activity capacity");
        }

        // Create OrderedQuantity
        OrderedQuantity orderedQuantity = new OrderedQuantity();
        orderedQuantity.setId(UUID.randomUUID());
        // Store the selected plan as the activity of this ordered quantity
        orderedQuantity.setActivityPlan(activityPlan);
        // Keep legacy activity null
        orderedQuantity.setActivity(null);
        orderedQuantity.setPlan(currentPlan);
        orderedQuantity.setStartDate(activityPlan.getStartDate());
        orderedQuantity.setEndDate(activityPlan.getEndDate());
        orderedQuantity.setPrice(activityPlan.getPrice());
        orderedQuantity.setQuota(activityPackage.getQuota());
        orderedQuantity.setOrderedQuota(requestDTO.getOrderedQuantity());

        orderedQuantityRepository.save(orderedQuantity);

        // Add to plan
        currentPlan.getOrderedQuantities().add(orderedQuantity);

        // Recalculate total price
        long totalPrice = currentPlan.getOrderedQuantities().stream()
                .mapToLong(oq -> (long) oq.getPrice() * oq.getOrderedQuota())
                .sum();
        currentPlan.setPrice(totalPrice);

        // Check if plan should be marked as Fulfilled
        int newTotalOrderedQuantity = currentPlan.getOrderedQuantities().stream()
                .mapToInt(oq -> oq.getOrderedQuota())
                .sum();

        if (newTotalOrderedQuantity == pkg.getQuota()) {
            currentPlan.setStatus("Fulfilled");
        } else {
            currentPlan.setStatus("Unfulfilled");
        }

        planRepository.save(currentPlan);

        // Return updated plan details
        return getPlanDetail(planId);
    }

    @Override
    public List<PlanResponseDTO> getAvailablePlansForActivity(UUID planId) {
        // Get current plan
        Plan currentPlan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        // Get all plans and filter based on criteria
        List<Plan> allPlans = planRepository.findAll();

        return allPlans.stream()
                .filter(p -> !p.getId().equals(planId)) // Exclude current plan
                .filter(p -> p.getActivityType().equals(currentPlan.getActivityType()))
                .filter(p -> !p.getStartDate().isBefore(currentPlan.getStartDate()))
                .filter(p -> !p.getEndDate().isAfter(currentPlan.getEndDate()))
                .filter(p -> p.getStartLocation().equals(currentPlan.getStartLocation()))
                .filter(p -> p.getEndLocation().equals(currentPlan.getEndLocation()))
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    private PlanResponseDTO convertToResponseDTO(Plan plan) {
        PlanResponseDTO dto = new PlanResponseDTO();
        dto.setId(plan.getId());
        dto.setPlanName(plan.getPlanName());
        dto.setPrice(plan.getPrice());
        dto.setActivityType(plan.getActivityType());
        dto.setStatus(plan.getStatus());
        dto.setStartDate(plan.getStartDate());
        dto.setEndDate(plan.getEndDate());
        dto.setStartLocation(plan.getStartLocation());
        dto.setEndLocation(plan.getEndLocation());
        dto.setActivitiesCount(plan.getOrderedQuantities().size());
        // Set capacity from package quota
        if (plan.getPackageEntity() != null) {
            dto.setCapacity(plan.getPackageEntity().getQuota());
        } else {
            dto.setCapacity(0);
        }
        return dto;
    }

    @Override
    public PlanDetailResponseDTO updateOrderedQuantity(UUID orderedQuantityId,
            UpdateOrderedQuantityRequestDTO requestDTO) {
        // Get the ordered quantity
        OrderedQuantity orderedQuantity = orderedQuantityRepository.findById(orderedQuantityId)
                .orElseThrow(() -> new RuntimeException("Ordered quantity not found with id: " + orderedQuantityId));

        // Get the plan
        Plan plan = orderedQuantity.getPlan();
        Package pkg = plan.getPackageEntity();

        // Validation 1: Package status must be PENDING
        if (!"PENDING".equals(pkg.getStatus())) {
            throw new RuntimeException("Cannot edit ordered activity. Package status must be PENDING");
        }

        // Validation 2: New ordered quantity must be at least 1
        if (requestDTO.getOrderedQuantity() < 1) {
            throw new RuntimeException("Ordered quantity must be at least 1");
        }

        // Validation 3: Check if new ordered quantity exceeds activity capacity
        int activityCapacity = orderedQuantity.getQuota();
        if (requestDTO.getOrderedQuantity() > activityCapacity) {
            throw new RuntimeException("Ordered quantity cannot exceed activity capacity (" + activityCapacity + ")");
        }

        // Validation 4: Check total ordered quantity doesn't exceed package quota
        int currentOrderedQuantity = orderedQuantity.getOrderedQuota();
        int otherOrderedQuantities = plan.getOrderedQuantities().stream()
                .filter(oq -> !oq.getId().equals(orderedQuantityId))
                .mapToInt(OrderedQuantity::getOrderedQuota)
                .sum();

        if (otherOrderedQuantities + requestDTO.getOrderedQuantity() > pkg.getQuota()) {
            throw new RuntimeException("Total ordered quantity cannot exceed package quota (" + pkg.getQuota() + ")");
        }

        // Update the ordered quantity
        orderedQuantity.setOrderedQuota(requestDTO.getOrderedQuantity());
        orderedQuantityRepository.save(orderedQuantity);

        // Recalculate total price
        long totalPrice = plan.getOrderedQuantities().stream()
                .mapToLong(oq -> (long) oq.getPrice() * oq.getOrderedQuota())
                .sum();
        plan.setPrice(totalPrice);

        // Update plan status based on total ordered quantity
        int totalOrderedQuantity = plan.getOrderedQuantities().stream()
                .mapToInt(OrderedQuantity::getOrderedQuota)
                .sum();

        if (totalOrderedQuantity == pkg.getQuota()) {
            plan.setStatus("Fulfilled");
        } else {
            plan.setStatus("Unfulfilled");
        }

        planRepository.save(plan);

        // Return updated plan details
        return getPlanDetail(plan.getId());
    }
}
