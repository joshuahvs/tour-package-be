package apap.ti._5.tour_package_2306165540_be.restcontroller;

import apap.ti._5.tour_package_2306165540_be.model.Activity;
import apap.ti._5.tour_package_2306165540_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306165540_be.restdto.response.ActivityResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.BaseResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activities")
public class ActivityRestController {

    private final ActivityRepository activityRepository;

    public ActivityRestController(ActivityRepository activityRepostory){
        this.activityRepository = activityRepostory;
    }

    @GetMapping
    public ResponseEntity<BaseResponseDTO<List<ActivityResponseDTO>>> getAllActivities(
            @RequestParam(name = "activityType", required = false) String activityType,
            @RequestParam(name = "startDate", required = false) String startDateParam,
            @RequestParam(name = "endDate", required = false) String endDateParam,
            @RequestParam(name = "search", required = false) String search) {
        try {
            LocalDateTime startDate = parseDateParam(startDateParam);
            LocalDateTime endDate = parseDateParam(endDateParam);

            if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
                throw new IllegalArgumentException("End date cannot be before start date");
            }

            List<Activity> activities = activityRepository.findAll();
            List<ActivityResponseDTO> activityDTOs = activities.stream()
                    .filter(activity -> filterByType(activity, activityType))
                    .filter(activity -> filterByStartDate(activity, startDate))
                    .filter(activity -> filterByEndDate(activity, endDate))
                    .filter(activity -> filterBySearch(activity, search))
                    .sorted(Comparator.comparing(Activity::getStartDate,
                            Comparator.nullsLast(LocalDateTime::compareTo)))
                    .map(activity -> convertToDTO(activity))
                    .collect(Collectors.toList());

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

    private boolean filterByType(Activity activity, String desiredType) {
        if (desiredType == null || desiredType.isBlank()) {
            return true;
        }
        if (activity.getActivityType() == null) {
            return false;
        }
        return activity.getActivityType().equalsIgnoreCase(desiredType.trim());
    }

    private boolean filterByStartDate(Activity activity, LocalDateTime startDate) {
        if (startDate == null || activity.getStartDate() == null) {
            return true;
        }
        return !activity.getStartDate().isBefore(startDate);
    }

    private boolean filterByEndDate(Activity activity, LocalDateTime endDate) {
        if (endDate == null || activity.getEndDate() == null) {
            return true;
        }
        return !activity.getEndDate().isAfter(endDate);
    }

    private boolean filterBySearch(Activity activity, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String keyword = search.trim().toLowerCase();
        return (activity.getActivityName() != null && activity.getActivityName().toLowerCase().contains(keyword))
                || (activity.getActivityItem() != null && activity.getActivityItem().toLowerCase().contains(keyword))
                || (activity.getStartLocation() != null && activity.getStartLocation().toLowerCase().contains(keyword))
                || (activity.getEndLocation() != null && activity.getEndLocation().toLowerCase().contains(keyword));
    }

    private LocalDateTime parseDateParam(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(rawDate.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format. Please use ISO-8601 format (yyyy-MM-dd'T'HH:mm)");
        }
    }
}
