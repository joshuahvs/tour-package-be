package apap.ti._5.tour_package_2306165540_be.restservice;

import apap.ti._5.tour_package_2306165540_be.model.Package;
import apap.ti._5.tour_package_2306165540_be.model.Plan;
import apap.ti._5.tour_package_2306165540_be.model.profile.EndUser;
import apap.ti._5.tour_package_2306165540_be.model.profile.RoleType;
import apap.ti._5.tour_package_2306165540_be.repository.EndUserRepository;
import apap.ti._5.tour_package_2306165540_be.repository.PackageRepository;
import apap.ti._5.tour_package_2306165540_be.repository.PlanRepository;
import apap.ti._5.tour_package_2306165540_be.restdto.request.CreatePackageRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PackageDetailResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PackageResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PlanResponseDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PackageRestServiceImpl implements PackageRestService {

    private final PackageRepository packageRepository;
    private final PlanRepository planRepository;
    private final EndUserRepository endUserRepository;

    public PackageRestServiceImpl(PackageRepository packageRepository, PlanRepository planRepository,
            EndUserRepository endUserRepository) {
        this.packageRepository = packageRepository;
        this.planRepository = planRepository;
        this.endUserRepository = endUserRepository;
    }

    @Override
    public List<PackageResponseDTO> getAllPackages() {
        List<Package> packages = packageRepository.findAll();
        return packages.stream()
                .filter(pkg -> !"DELETED".equalsIgnoreCase(pkg.getStatus())) // Filter out deleted packages
                .filter(this::isPackageVisibleToCurrentUser) // Filter based on user role
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
                .filter(this::isPackageVisibleToCurrentUser) // Filter based on user role
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
        // Validate quota > 0
        if (requestDTO.getQuota() <= 0) {
            throw new RuntimeException("Quota must be greater than 0");
        }

        // Validate price > 0 (if provided)
        if (requestDTO.getPrice() != null && requestDTO.getPrice() <= 0) {
            throw new RuntimeException("Price must be greater than 0");
        }

        // Validate startDate >= now
        LocalDateTime now = LocalDateTime.now();
        if (requestDTO.getStartDate().isBefore(now)) {
            throw new RuntimeException("Start date must be in the future or today");
        }

        // Validate startDate < endDate
        if (requestDTO.getEndDate().isBefore(requestDTO.getStartDate()) ||
                requestDTO.getEndDate().isEqual(requestDTO.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        // Generate package ID with format PKG-{YYYYMMDD}-{XXX}
        String packageId = generatePackageId();

        Package packageEntity = toEntity(requestDTO);
        packageEntity.setId(packageId);
        packageEntity.setStatus("PENDING"); // Default status
        packageEntity.setPrice(0L); // Initial price is 0

        Package savedPackage = packageRepository.save(packageEntity);
        return toResponseDTO(savedPackage);
    }

    private String generatePackageId() {
        // Format: PKG-{YYYYMMDD}-{XXX}
        LocalDateTime now = LocalDateTime.now();
        String dateStr = String.format("%04d%02d%02d",
                now.getYear(), now.getMonthValue(), now.getDayOfMonth());

        // Get count of packages created today
        String datePrefix = "PKG-" + dateStr + "-";
        long todayCount = packageRepository.findAll().stream()
                .filter(pkg -> pkg.getId().startsWith(datePrefix))
                .count();

        int count = (int) todayCount + 1;
        String countStr = String.format("%03d", count);

        return datePrefix + countStr;
    }

    @Override
    public PackageResponseDTO updatePackage(String id, CreatePackageRequestDTO requestDTO) {
        Package existingPackage = packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found with id: " + id));

        // Authorization check: Customer only can update their own packages
        // Superadmin and Vendor can update all packages
        if (!canUserEditOrDeletePackage(existingPackage)) {
            throw new RuntimeException("You do not have permission to update this package");
        }

        // Validate: Only allow edit if status is PENDING or PROCESSED
        if (!"PENDING".equalsIgnoreCase(existingPackage.getStatus()) &&
                !"PROCESSED".equalsIgnoreCase(existingPackage.getStatus())) {
            throw new RuntimeException(
                    "Cannot edit package. Only packages with status 'PENDING' or 'PROCESSED' can be edited.");
        }

        // Validate quota > 0
        if (requestDTO.getQuota() <= 0) {
            throw new RuntimeException("Quota must be greater than 0");
        }

        // Validate startDate >= now
        LocalDateTime now = LocalDateTime.now();
        if (requestDTO.getStartDate().isBefore(now)) {
            throw new RuntimeException("Start date must be in the future or today");
        }

        // Validate startDate < endDate
        if (requestDTO.getEndDate().isBefore(requestDTO.getStartDate()) ||
                requestDTO.getEndDate().isEqual(requestDTO.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        // Validate: Package with status PROCESSED cannot change activityIdList
        // This is checked by not allowing plan modifications when status is PROCESSED
        if ("PROCESSED".equalsIgnoreCase(existingPackage.getStatus())) {
            // For PROCESSED packages, only allow editing packageName, startDate, endDate,
            // quota
            // activityIdList changes are not allowed (handled via Plan management)
        }

        // Update package fields (userId and status cannot be changed)
        existingPackage.setPackageName(requestDTO.getPackageName());
        existingPackage.setQuota(requestDTO.getQuota());
        existingPackage.setStartDate(requestDTO.getStartDate());
        existingPackage.setEndDate(requestDTO.getEndDate());
        // Price is calculated automatically, not updated manually

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

        // Authorization check: Customer only can delete their own packages
        // Superadmin and Vendor can delete all packages
        if (!canUserEditOrDeletePackage(packageEntity)) {
            throw new RuntimeException("You do not have permission to delete this package");
        }

        if (!"PENDING".equalsIgnoreCase(packageEntity.getStatus())) {
            throw new RuntimeException("Only packages with status 'PENDING' can be deleted.");
        }

        // Cascade delete: Remove all Plans and OrderedActivities
        List<Plan> plansToDelete = new ArrayList<>(packageEntity.getPlans());

        packageEntity.getPlans().clear();

        packageRepository.save(packageEntity);

        if (!plansToDelete.isEmpty()) {
            planRepository.deleteAll(plansToDelete);
        }

        // Soft delete: Set status to DELETED
        packageEntity.setStatus("DELETED");
        packageRepository.save(packageEntity);
    }

    // Helper methods
    /**
     * Mengecek apakah user dapat edit atau delete package.
     * Customer hanya bisa edit/delete package milik sendiri.
     * Superadmin dan Vendor Tour Package dapat edit/delete semua package.
     */
    private boolean canUserEditOrDeletePackage(Package pkg) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return false; // Deny if no authentication
        }

        String currentUsername = authentication.getName();
        EndUser currentUser = endUserRepository.findByUsernameIgnoreCase(currentUsername)
                .orElse(null);

        if (currentUser == null) {
            return false; // Deny if user not found
        }

        // Superadmin and Vendor can edit/delete all packages
        if (currentUser.getRoleType() == RoleType.SUPERADMIN ||
                currentUser.getRoleType() == RoleType.TOUR_PACKAGE_VENDOR) {
            return true;
        }

        // Customer can only edit/delete their own packages
        if (currentUser.getRoleType() == RoleType.CUSTOMER) {
            return pkg.getUserId().equals(currentUser.getId().toString());
        }

        return false;
    }

    /**
     * Mengecek apakah package visible untuk user yang sedang login.
     * Customer hanya bisa melihat package yang dibuat oleh vendor/admin dan package
     * miliknya sendiri.
     * Vendor/Admin/Superadmin bisa melihat semua package.
     */
    private boolean isPackageVisibleToCurrentUser(Package pkg) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return true; // Allow if no authentication (shouldn't happen with security)
        }

        String currentUsername = authentication.getName();
        EndUser currentUser = endUserRepository.findByUsernameIgnoreCase(currentUsername)
                .orElse(null);

        if (currentUser == null) {
            return true; // Allow if user not found
        }

        // Jika bukan Customer, bisa lihat semua package
        if (currentUser.getRoleType() != RoleType.CUSTOMER) {
            return true;
        }

        // Jika Customer, cek apakah package dibuat oleh dirinya sendiri
        if (pkg.getUserId().equals(currentUser.getId().toString())) {
            return true; // Package milik sendiri
        }

        // Cek apakah package dibuat oleh vendor/admin (bukan Customer lain)
        try {
            java.util.UUID creatorId = java.util.UUID.fromString(pkg.getUserId());
            EndUser packageCreator = endUserRepository.findById(creatorId).orElse(null);

            if (packageCreator == null) {
                return false; // Creator tidak ditemukan
            }

            // Allow jika creator bukan Customer (berarti vendor/admin)
            return packageCreator.getRoleType() != RoleType.CUSTOMER;
        } catch (IllegalArgumentException e) {
            // userId bukan format UUID, tidak bisa validasi creator
            // Default: hide package dari Customer untuk safety
            return false;
        }
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
