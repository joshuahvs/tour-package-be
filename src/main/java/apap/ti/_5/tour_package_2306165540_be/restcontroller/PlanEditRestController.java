package apap.ti._5.tour_package_2306165540_be.restcontroller;

import apap.ti._5.tour_package_2306165540_be.restdto.request.UpdatePlanRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.BaseResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PlanDetailResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PlanResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restservice.PlanRestService;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/api/plans")
@CrossOrigin(origins = "*")
public class PlanEditRestController {

    private final PlanRestService planRestService;

    public PlanEditRestController(PlanRestService planRestService){
        this.planRestService = planRestService;
    }

    @GetMapping("/{planId}/edit")
    public ResponseEntity<BaseResponseDTO<PlanDetailResponseDTO>> getEditPlanPage(@PathVariable UUID planId) {
        try {
            PlanDetailResponseDTO plan = planRestService.getPlanDetail(planId);

            BaseResponseDTO<PlanDetailResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Successfully retrieved plan for editing");
            response.setData(plan);
            response.setTimestamp(new Date());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            BaseResponseDTO<PlanDetailResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setMessage(e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            BaseResponseDTO<PlanDetailResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to load edit plan page: " + e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{planId}/edit")
    public ResponseEntity<BaseResponseDTO<PlanDetailResponseDTO>> updatePlan(
            @PathVariable UUID planId,
            @RequestBody UpdatePlanRequestDTO requestDTO) {
        try {
            PlanDetailResponseDTO plan = planRestService.updatePlan(planId, requestDTO);

            BaseResponseDTO<PlanDetailResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Successfully updated plan");
            response.setData(plan);
            response.setTimestamp(new Date());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            BaseResponseDTO<PlanDetailResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage(e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            BaseResponseDTO<PlanDetailResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to update plan: " + e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{planId}/available-activities")
    public ResponseEntity<BaseResponseDTO<List<PlanResponseDTO>>> getAvailableActivities(@PathVariable UUID planId) {
        try {
            List<PlanResponseDTO> availablePlans = planRestService.getAvailablePlansForActivity(planId);

            BaseResponseDTO<List<PlanResponseDTO>> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Successfully retrieved available activities");
            response.setData(availablePlans);
            response.setTimestamp(new Date());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            BaseResponseDTO<List<PlanResponseDTO>> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setMessage(e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            BaseResponseDTO<List<PlanResponseDTO>> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to retrieve available activities: " + e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
