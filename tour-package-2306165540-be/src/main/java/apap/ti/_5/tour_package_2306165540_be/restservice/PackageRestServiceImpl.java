package apap.ti._5.tour_package_2306165540_be.restservice;

import apap.ti._5.tour_package_2306165540_be.model.Package;
import apap.ti._5.tour_package_2306165540_be.model.Plan;
import apap.ti._5.tour_package_2306165540_be.repository.PackageRepository;
import apap.ti._5.tour_package_2306165540_be.restdto.request.CreatePackageRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PackageDetailResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PackageResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PlanResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PackageRestServiceImpl implements PackageRestService {

    private final PackageRepository packageRepository;

    @Override
    public List<PackageResponseDTO> getAllPackages() {
        List<Package> packages = packageRepository.findAll();
        return packages.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PackageResponseDTO> searchPackagesByName(String name) {
        List<Package> packages = packageRepository.findAll();

        // Filter packages by name in Java
        return packages.stream()
                .filter(pkg -> pkg.getPackageName().toLowerCase().contains(name.toLowerCase()))
                .map(this::toResponseDTO)
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

        existingPackage.setUserId(requestDTO.getUserId());
        existingPackage.setPackageName(requestDTO.getPackageName());
        existingPackage.setQuota(requestDTO.getQuota());
        existingPackage.setPrice(requestDTO.getPrice());
        existingPackage.setStatus(requestDTO.getStatus());
        existingPackage.setStartDate(requestDTO.getStartDate());
        existingPackage.setEndDate(requestDTO.getEndDate());

        Package updatedPackage = packageRepository.save(existingPackage);
        return toResponseDTO(updatedPackage);
    }

    @Override
    public void deletePackage(String id) {
        Package packageEntity = packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found with id: " + id));
        packageRepository.delete(packageEntity);
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

        // Map plans
        List<PlanResponseDTO> planDTOs = packageEntity.getPlans().stream()
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
