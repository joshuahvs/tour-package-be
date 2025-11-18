package apap.ti._5.tour_package_2306165540_be.restcontroller;

import apap.ti._5.tour_package_2306165540_be.restdto.request.AddOrderedQuantityRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.request.UpdateOrderedQuantityRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.BaseResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PlanDetailResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restservice.PlanRestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/api/ordered-activities")
@CrossOrigin(origins = "*")
public class OrderedQuantityRestController {

    private final PlanRestService planRestService;

    public OrderedQuantityRestController(PlanRestService planRestService){
        this.planRestService = planRestService;
    }

    @PostMapping("/create")
    public ResponseEntity<BaseResponseDTO<PlanDetailResponseDTO>> addOrderedQuantity(
            @RequestParam UUID planId,
            @RequestBody AddOrderedQuantityRequestDTO requestDTO) {
        try {
            PlanDetailResponseDTO plan = planRestService.addOrderedQuantity(planId, requestDTO);

            BaseResponseDTO<PlanDetailResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Successfully added activity to plan");
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
            response.setMessage("Failed to add activity to plan: " + e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}/edit")
    public ResponseEntity<BaseResponseDTO<PlanDetailResponseDTO>> updateOrderedQuantity(
            @PathVariable UUID id,
            @RequestBody UpdateOrderedQuantityRequestDTO requestDTO) {
        try {
            PlanDetailResponseDTO plan = planRestService.updateOrderedQuantity(id, requestDTO);

            BaseResponseDTO<PlanDetailResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Successfully updated ordered activity quantity");
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
            response.setMessage("Failed to update ordered activity: " + e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<BaseResponseDTO<PlanDetailResponseDTO>> deleteOrderedQuantity(@PathVariable UUID id) {
        try {
            PlanDetailResponseDTO plan = planRestService.deleteOrderedQuantity(id);

            BaseResponseDTO<PlanDetailResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Successfully removed activity from plan");
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
            response.setMessage("Failed to remove activity from plan: " + e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
