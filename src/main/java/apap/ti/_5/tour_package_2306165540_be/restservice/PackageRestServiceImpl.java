package apap.ti._5.tour_package_2306165540_be.restservice;

import apap.ti._5.tour_package_2306165540_be.model.Package;
import apap.ti._5.tour_package_2306165540_be.model.Plan;
import apap.ti._5.tour_package_2306165540_be.repository.PackageRepository;
import apap.ti._5.tour_package_2306165540_be.repository.PlanRepository;
import apap.ti._5.tour_package_2306165540_be.restdto.request.CreatePackageRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PackageDetailResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PackageResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PlanResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PackageRestServiceImpl implements PackageRestService {

    private final PackageRepository packageRepository;
    private final PlanRepository planRepository;

    public PackageRestServiceImpl(PackageRepository packageRepository, PlanRepository planRepository){
        this.packageRepository = packageRepository;
        this.planRepository = planRepository;
    }

    @Override
    public List<PackageResponseDTO> getAllPackages() {
        List<Package> packages = packageRepository.findAll();
        return packages.stream()
                .filter(pkg -> !"DELETED".equalsIgnoreCase(pkg.getStatus())) // Filter out deleted packages
                .map(pkg -> toResponseDTO(pkg))
                .collect(Collectors.toList());
    }

    @Override
    public List<PackageResponseDTO> searchPackagesByName(String name) {
        List<Package> packages = packageRepository.findAll();

        // Filter packages by name and exclude deleted packages
        return packages.stream()
                .filter(pkg -> !"DELETED".equalsIgnoreCase(pkg.getStatus())) // Filter out deleted packages
                .filter(pkg -> pkg.getPackageName().toLowerCase().contains(name.toLowerCase()))
                .map(pkg -> toResponseDTO(pkg))
                .collect(Collectors.toList());
    }

    @Override
    public PackageResponseDTO getPackageById(String id) {
        Package packageEntity = packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found with id: " + id));
        return toResponseDTO(packageEntity);
    }

    @Override
    public PackageDetailResponseDTO getPackageDetailById(String id) {
        Package packageEntity = packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found with id: " + id));
        return toDetailResponseDTO(packageEntity);
    }

    @Override
    public PackageResponseDTO createPackage(CreatePackageRequestDTO requestDTO) {
        // Validate end date is after start date
        if (requestDTO.getEndDate().isBefore(requestDTO.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        // Generate package ID
        String packageId = generatePackageId(requestDTO.getUserId());

        Package packageEntity = toEntity(requestDTO);
        packageEntity.setId(packageId);
        packageEntity.setStatus("PENDING"); // Default status
        packageEntity.setPrice(0L); // Initial price is 0

        Package savedPackage = packageRepository.save(packageEntity);
        return toResponseDTO(savedPackage);
    }

    private String generatePackageId(String userId) {
        // Get count of packages for this user
        List<Package> userPackages = packageRepository.findAll().stream()
                .filter(pkg -> pkg.getUserId().equals(userId))
                .collect(Collectors.toList());

        int count = userPackages.size() + 1;
        String countStr = String.format("%03d", count);

        return "PACK-" + userId + "-" + countStr;
    }

    @Override
    public PackageResponseDTO updatePackage(String id, CreatePackageRequestDTO requestDTO) {
        Package existingPackage = packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found with id: " + id));

        // Validate: Only allow edit if status is PENDING
        if (!"PENDING".equalsIgnoreCase(existingPackage.getStatus())) {
            throw new RuntimeException("Cannot edit package. Only packages with status 'PENDING' can be edited.");
        }

        // Validate: Only allow edit if package doesn't have any plans
        if (existingPackage.getPlans() != null && !existingPackage.getPlans().isEmpty()) {
            throw new RuntimeException("Cannot edit package. Package with existing plans cannot be edited.");
        }

        // Validate: End date must be after start date
        if (requestDTO.getEndDate().isBefore(requestDTO.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        // Update package fields (userId cannot be changed to maintain package ID
        // format)
        existingPackage.setPackageName(requestDTO.getPackageName());
        existingPackage.setQuota(requestDTO.getQuota());
        existingPackage.setStartDate(requestDTO.getStartDate());
        existingPackage.setEndDate(requestDTO.getEndDate());

        Package updatedPackage = packageRepository.save(existingPackage);
        return toResponseDTO(updatedPackage);
    }

    @Override
    public PackageResponseDTO processPackage(String id) {
        Package packageEntity = packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found with id: " + id));

        // Validate: Package must have status PENDING
        if (!"PENDING".equalsIgnoreCase(packageEntity.getStatus())) {
            throw new RuntimeException("Cannot process package. Only packages with status 'PENDING' can be processed.");
        }

        // Validate: All plans must have status FULFILLED
        // Filter out soft-deleted plans (deletedAt != null)
        List<Plan> activePlans = packageEntity.getPlans().stream()
                .filter(plan -> plan.getDeletedAt() == null)
                .collect(Collectors.toList());

        if (activePlans.isEmpty()) {
            throw new RuntimeException("Cannot process package. Package must have at least one plan.");
        }

        boolean allPlansFulfilled = activePlans.stream()
                .allMatch(plan -> "FULFILLED".equalsIgnoreCase(plan.getStatus()));

        if (!allPlansFulfilled) {
            throw new RuntimeException(
                    "Cannot process package. All plans must have status 'FULFILLED' before processing.");
        }

        // Process: Change package status to PROCESSED
        packageEntity.setStatus("PROCESSED");

        // Booking activities: Reduce capacity
        // For each active plan's ordered quantities, reduce the activity capacity
        for (Plan plan : activePlans) {
            if (plan.getOrderedQuantities() == null) {
                continue;
            }

            for (var orderedQty : plan.getOrderedQuantities()) {
                if (orderedQty.getDeletedAt() != null) {
                    continue;
                }

                var legacyActivity = orderedQty.getActivity();

                if (legacyActivity != null) {
                    int newCapacity = legacyActivity.getCapacity() - orderedQty.getOrderedQuota();

                    if (newCapacity < 0) {
                        throw new RuntimeException("Cannot process package. Activity '" +
                                legacyActivity.getActivityName() + "' does not have enough capacity.");
                    }

                    legacyActivity.setCapacity(newCapacity);
                    orderedQty.setQuota(newCapacity);
                    continue;
                }

                var activityPlan = orderedQty.getActivityPlan();

                if (activityPlan == null) {
                    continue;
                }

                Package sourcePackage = activityPlan.getPackageEntity();
                int currentCapacity = sourcePackage.getQuota();
                int newCapacity = currentCapacity - orderedQty.getOrderedQuota();

                if (newCapacity < 0) {
                    throw new RuntimeException("Cannot process package. Activity '" +
                            activityPlan.getPlanName() + "' does not have enough capacity.");
                }

                sourcePackage.setQuota(newCapacity);
                orderedQty.setQuota(newCapacity);
            }
        }

        Package processedPackage = packageRepository.save(packageEntity);
        return toResponseDTO(processedPackage);
    }

    @Override
    public void deletePackage(String id) {
        Package packageEntity = packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found with id: " + id));

        if (!"PENDING".equalsIgnoreCase(packageEntity.getStatus())) {
            throw new RuntimeException("Cannot delete package. Only packages with status 'PENDING' can be deleted.");
        }

        List<Plan> plansToDelete = new ArrayList<>(packageEntity.getPlans());

        packageEntity.getPlans().clear();

        packageRepository.save(packageEntity);

        if (!plansToDelete.isEmpty()) {
            planRepository.deleteAll(plansToDelete);
        }

        packageEntity.setStatus("DELETED");
        packageRepository.save(packageEntity);
    }

    // Mapper methods
    private PackageResponseDTO toResponseDTO(Package packageEntity) {
        PackageResponseDTO dto = new PackageResponseDTO();
        dto.setId(packageEntity.getId());
        dto.setUserId(packageEntity.getUserId());
        dto.setPackageName(packageEntity.getPackageName());
        dto.setQuota(packageEntity.getQuota());
        dto.setPrice(packageEntity.getPrice());
        dto.setStatus(packageEntity.getStatus());
        dto.setStartDate(packageEntity.getStartDate());
        dto.setEndDate(packageEntity.getEndDate());
        return dto;
    }

    private Package toEntity(CreatePackageRequestDTO dto) {
        Package packageEntity = new Package();
        packageEntity.setId(dto.getId());
        packageEntity.setUserId(dto.getUserId());
        packageEntity.setPackageName(dto.getPackageName());
        packageEntity.setQuota(dto.getQuota());
        packageEntity.setPrice(dto.getPrice());
        packageEntity.setStatus(dto.getStatus());
        packageEntity.setStartDate(dto.getStartDate());
        packageEntity.setEndDate(dto.getEndDate());
        return packageEntity;
    }

    private PackageDetailResponseDTO toDetailResponseDTO(Package packageEntity) {
        PackageDetailResponseDTO dto = new PackageDetailResponseDTO();
        dto.setId(packageEntity.getId());
        dto.setUserId(packageEntity.getUserId());
        dto.setPackageName(packageEntity.getPackageName());
        dto.setQuota(packageEntity.getQuota());
        dto.setPrice(packageEntity.getPrice());
        dto.setStatus(packageEntity.getStatus());
        dto.setStartDate(packageEntity.getStartDate());
        dto.setEndDate(packageEntity.getEndDate());

        // Map all plans, filtering out soft deleted ones
        List<PlanResponseDTO> planDTOs = packageEntity.getPlans().stream()
                .filter(plan -> plan.getDeletedAt() == null)
                .map(this::toPlanResponseDTO)
                .collect(Collectors.toList());
        dto.setPlans(planDTOs);

        return dto;
    }

    private PlanResponseDTO toPlanResponseDTO(Plan plan) {
        PlanResponseDTO dto = new PlanResponseDTO();
        dto.setId(plan.getId());
        dto.setPlanName(plan.getActivityType() + " Plan");
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
}
