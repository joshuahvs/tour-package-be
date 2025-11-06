package apap.ti._5.tour_package_2306165540_be.restcontroller;

import apap.ti._5.tour_package_2306165540_be.model.Activity;
import apap.ti._5.tour_package_2306165540_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306165540_be.restdto.response.ActivityResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.BaseResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ActivityRestController {

    private final ActivityRepository activityRepository;

    @GetMapping
    public ResponseEntity<BaseResponseDTO<List<ActivityResponseDTO>>> getAllActivities() {
        try {
            List<Activity> activities = activityRepository.findAll();
            List<ActivityResponseDTO> activityDTOs = activities.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            BaseResponseDTO<List<ActivityResponseDTO>> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Successfully retrieved all activities");
            response.setData(activityDTOs);
            response.setTimestamp(new Date());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            BaseResponseDTO<List<ActivityResponseDTO>> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to retrieve activities: " + e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private ActivityResponseDTO convertToDTO(Activity activity) {
        ActivityResponseDTO dto = new ActivityResponseDTO();
        dto.setId(activity.getId());
        dto.setActivityName(activity.getActivityName());
        dto.setActivityItem(activity.getActivityItem());
        dto.setCapacity(activity.getCapacity());
        dto.setPrice(activity.getPrice());
        dto.setActivityType(activity.getActivityType());
        dto.setStartDate(activity.getStartDate());
        dto.setEndDate(activity.getEndDate());
        dto.setStartLocation(activity.getStartLocation());
        dto.setEndLocation(activity.getEndLocation());
        return dto;
    }
}
