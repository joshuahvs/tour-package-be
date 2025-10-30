package apap.ti._5.tour_package_2306165540_be.restcontroller;

import apap.ti._5.tour_package_2306165540_be.restdto.BaseResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.request.CreatePlanRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PlanResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restservice.PlanRestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlanRestController {

    private final PlanRestService planRestService;

    @PostMapping("/{packageId}/plans/create")
    public ResponseEntity<BaseResponseDTO<PlanResponseDTO>> createPlan(
            @PathVariable String packageId,
            @RequestBody CreatePlanRequestDTO requestDTO) {
        try {
            PlanResponseDTO plan = planRestService.createPlan(packageId, requestDTO);

            BaseResponseDTO<PlanResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.CREATED.value());
            response.setMessage("Successfully created plan");
            response.setData(plan);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            BaseResponseDTO<PlanResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage(e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            BaseResponseDTO<PlanResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to create plan: " + e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{packageId}/plans/create")
    public ResponseEntity<BaseResponseDTO<String>> getCreatePlanPage(@PathVariable String packageId) {
        try {
            BaseResponseDTO<String> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Create plan page for package: " + packageId);
            response.setData(packageId);
            response.setTimestamp(new Date());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            BaseResponseDTO<String> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to load create plan page: " + e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
