package apap.ti._5.tour_package_2306165540_be.restservice;

import apap.ti._5.tour_package_2306165540_be.restdto.request.CreatePackageRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PackageDetailResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PackageResponseDTO;

import java.util.List;

public interface PackageRestService {
    List<PackageResponseDTO> getAllPackages();

    List<PackageResponseDTO> searchPackagesByName(String name);

    PackageResponseDTO getPackageById(String id);

    PackageDetailResponseDTO getPackageDetailById(String id);

    PackageResponseDTO createPackage(CreatePackageRequestDTO requestDTO);

    PackageResponseDTO updatePackage(String id, CreatePackageRequestDTO requestDTO);

    PackageResponseDTO processPackage(String id);

    PackageResponseDTO confirmPackagePayment(String id);

    void deletePackage(String id);
}
