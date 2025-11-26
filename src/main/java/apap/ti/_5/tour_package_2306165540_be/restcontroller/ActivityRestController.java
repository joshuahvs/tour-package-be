package apap.ti._5.tour_package_2306165540_be.restcontroller;

import apap.ti._5.tour_package_2306165540_be.restdto.request.CreateActivityRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.request.UpdateActivityRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.ActivityResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.BaseResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restservice.ActivityRestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/activities")
public class ActivityRestController {

    private final ActivityRestService activityRestService;

    public ActivityRestController(ActivityRestService activityRestService) {
        this.activityRestService = activityRestService;
    }

    @GetMapping
    public ResponseEntity<BaseResponseDTO<List<ActivityResponseDTO>>> getAllActivities(
            @RequestParam(name = "activityType", required = false) String activityType,
            @RequestParam(name = "startLocation", required = false) String startLocation,
            @RequestParam(name = "endLocation", required = false) String endLocation,
            @RequestParam(name = "startDate", required = false) String startDateParam,
            @RequestParam(name = "endDate", required = false) String endDateParam,
            @RequestParam(name = "search", required = false) String search) {
        try {
            List<ActivityResponseDTO> activityDTOs = activityRestService.getAllActivities(
                    activityType, startLocation, endLocation, startDateParam, endDateParam, search);

            BaseResponseDTO<List<ActivityResponseDTO>> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Successfully retrieved activities");
            response.setData(activityDTOs);
            response.setTimestamp(new Date());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            BaseResponseDTO<List<ActivityResponseDTO>> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage(ex.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            BaseResponseDTO<List<ActivityResponseDTO>> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to retrieve activities: " + e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponseDTO<ActivityResponseDTO>> getActivityById(@PathVariable String id) {
        try {
            ActivityResponseDTO activity = activityRestService.getActivityById(id);

            BaseResponseDTO<ActivityResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Successfully retrieved activity");
            response.setData(activity);
            response.setTimestamp(new Date());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            BaseResponseDTO<ActivityResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setMessage(e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            BaseResponseDTO<ActivityResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to retrieve activity: " + e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping
    public ResponseEntity<BaseResponseDTO<ActivityResponseDTO>> createActivity(
            @RequestBody CreateActivityRequestDTO requestDTO) {
        try {
            ActivityResponseDTO activity = activityRestService.createActivity(requestDTO);

            BaseResponseDTO<ActivityResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.CREATED.value());
            response.setMessage("Successfully created activity");
            response.setData(activity);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            BaseResponseDTO<ActivityResponseDTO> response = new BaseResponseDTO<>();

            if (e.getMessage().contains("permission") || e.getMessage().contains("can only create")) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setMessage(e.getMessage());
                response.setData(null);
                response.setTimestamp(new Date());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage(e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            BaseResponseDTO<ActivityResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to create activity: " + e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponseDTO<ActivityResponseDTO>> updateActivity(
            @PathVariable String id,
            @RequestBody UpdateActivityRequestDTO requestDTO) {
        try {
            ActivityResponseDTO activity = activityRestService.updateActivity(id, requestDTO);

            BaseResponseDTO<ActivityResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Successfully updated activity");
            response.setData(activity);
            response.setTimestamp(new Date());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            BaseResponseDTO<ActivityResponseDTO> response = new BaseResponseDTO<>();

            if (e.getMessage().contains("not found")) {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.setMessage(e.getMessage());
                response.setData(null);
                response.setTimestamp(new Date());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (e.getMessage().contains("can only modify") || e.getMessage().contains("Authentication required")) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setMessage(e.getMessage());
                response.setData(null);
                response.setTimestamp(new Date());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage(e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            BaseResponseDTO<ActivityResponseDTO> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to update activity: " + e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponseDTO<Void>> deleteActivity(@PathVariable String id) {
        try {
            activityRestService.deleteActivity(id);

            BaseResponseDTO<Void> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Successfully deleted activity");
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            BaseResponseDTO<Void> response = new BaseResponseDTO<>();

            if (e.getMessage().contains("not found")) {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.setMessage(e.getMessage());
                response.setData(null);
                response.setTimestamp(new Date());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (e.getMessage().contains("can only modify") || e.getMessage().contains("Authentication required")) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setMessage(e.getMessage());
                response.setData(null);
                response.setTimestamp(new Date());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage(e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            BaseResponseDTO<Void> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to delete activity: " + e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
