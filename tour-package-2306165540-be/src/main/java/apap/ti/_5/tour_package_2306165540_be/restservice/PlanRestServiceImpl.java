package apap.ti._5.tour_package_2306165540_be.restservice;

import apap.ti._5.tour_package_2306165540_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306165540_be.model.Package;
import apap.ti._5.tour_package_2306165540_be.model.Plan;
import apap.ti._5.tour_package_2306165540_be.repository.PackageRepository;
import apap.ti._5.tour_package_2306165540_be.repository.PlanRepository;
import apap.ti._5.tour_package_2306165540_be.restdto.request.CreatePlanRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.request.UpdatePlanRequestDTO;
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
        dto.setActivityName(orderedQuantity.getActivity().getActivityName());
        dto.setActivityId(orderedQuantity.getActivity().getId());
        dto.setStartDate(orderedQuantity.getStartDate());
        dto.setEndDate(orderedQuantity.getEndDate());
        dto.setPrice(orderedQuantity.getPrice());
        dto.setQuota(orderedQuantity.getQuota());
        dto.setOrderedQuota(orderedQuantity.getOrderedQuota());
        dto.setTotal(orderedQuantity.getPrice() * orderedQuantity.getOrderedQuota());
        return dto;
    }
}
