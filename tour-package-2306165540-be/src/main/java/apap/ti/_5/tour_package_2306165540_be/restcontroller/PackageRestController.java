package apap.ti._5.tour_package_2306165540_be.restcontroller;

import apap.ti._5.tour_package_2306165540_be.restdto.BaseResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.request.CreatePackageRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PackageDetailResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PackageResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restservice.PackageRestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/package")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PackageRestController {

    private final PackageRestService packageRestService;

    @GetMapping
    public ResponseEntity<BaseResponseDTO<List<PackageResponseDTO>>> getAllPackages(
            @RequestParam(required = false) String name) {
        try {
            List<PackageResponseDTO> packageDTOs;

            if (name != null && !name.trim().isEmpty()) {
                packageDTOs = packageRestService.searchPackagesByName(name);
            } else {
                packageDTOs = packageRestService.getAllPackages();
            }

            BaseResponseDTO<List<PackageResponseDTO>> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Successfully retrieved packages");
            response.setData(packageDTOs);
            response.setTimestamp(new Date());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            BaseResponseDTO<List<PackageResponseDTO>> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to retrieve packages: " + e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponseDTO<PackageResponseDTO>> getPackageById(@PathVariable String id) {
        try {
            PackageResponseDTO packageDTO = packageRestService.getPackageById(id);

            BaseResponseDTO<PackageResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Successfully retrieved package");
            response.setData(packageDTO);
            response.setTimestamp(new Date());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            BaseResponseDTO<PackageResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setMessage(e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            BaseResponseDTO<PackageResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to retrieve package: " + e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<BaseResponseDTO<PackageDetailResponseDTO>> getPackageDetailById(@PathVariable String id) {
        try {
            PackageDetailResponseDTO packageDTO = packageRestService.getPackageDetailById(id);

            BaseResponseDTO<PackageDetailResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Successfully retrieved package detail");
            response.setData(packageDTO);
            response.setTimestamp(new Date());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            BaseResponseDTO<PackageDetailResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setMessage(e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            BaseResponseDTO<PackageDetailResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to retrieve package detail: " + e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping
    public ResponseEntity<BaseResponseDTO<PackageResponseDTO>> createPackage(
            @RequestBody CreatePackageRequestDTO requestDTO) {
        try {
            PackageResponseDTO packageDTO = packageRestService.createPackage(requestDTO);

            BaseResponseDTO<PackageResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.CREATED.value());
            response.setMessage("Successfully created package");
            response.setData(packageDTO);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            BaseResponseDTO<PackageResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to create package: " + e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponseDTO<PackageResponseDTO>> updatePackage(
            @PathVariable String id,
            @RequestBody CreatePackageRequestDTO requestDTO) {
        try {
            PackageResponseDTO packageDTO = packageRestService.updatePackage(id, requestDTO);

            BaseResponseDTO<PackageResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Successfully updated package");
            response.setData(packageDTO);
            response.setTimestamp(new Date());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            BaseResponseDTO<PackageResponseDTO> response = new BaseResponseDTO<>();

            // Check if it's a validation error or not found error
            if (e.getMessage().contains("not found")) {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
            }

            response.setMessage(e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            BaseResponseDTO<PackageResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to update package: " + e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponseDTO<Void>> deletePackage(@PathVariable String id) {
        try {
            packageRestService.deletePackage(id);

            BaseResponseDTO<Void> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Successfully deleted package");
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            BaseResponseDTO<Void> response = new BaseResponseDTO<>();

            // Check if it's a validation error (not PENDING status)
            if (e.getMessage().contains("Only packages with status 'PENDING'")) {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.setMessage(e.getMessage());
                response.setData(null);
                response.setTimestamp(new Date());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Package not found
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setMessage(e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            BaseResponseDTO<Void> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to delete package: " + e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
