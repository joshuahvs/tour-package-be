package apap.ti._5.tour_package_2306165540_be.restservice;

import apap.ti._5.tour_package_2306165540_be.model.Activity;
import apap.ti._5.tour_package_2306165540_be.model.profile.EndUser;
import apap.ti._5.tour_package_2306165540_be.model.profile.RoleType;
import apap.ti._5.tour_package_2306165540_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306165540_be.repository.EndUserRepository;
import apap.ti._5.tour_package_2306165540_be.restdto.request.CreateActivityRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.request.UpdateActivityRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.ActivityResponseDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ActivityRestServiceImpl implements ActivityRestService {

    private final ActivityRepository activityRepository;
    private final EndUserRepository endUserRepository;

    public ActivityRestServiceImpl(ActivityRepository activityRepository, EndUserRepository endUserRepository) {
        this.activityRepository = activityRepository;
        this.endUserRepository = endUserRepository;
    }

    @Override
    public List<ActivityResponseDTO> getAllActivities(String activityType, String startLocation, String endLocation,
            String startDateParam, String endDateParam, String search) {
        LocalDateTime startDate = parseDateParam(startDateParam);
        LocalDateTime endDate = parseDateParam(endDateParam);

        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        List<Activity> activities = activityRepository.findAll();

        return activities.stream()
                .filter(this::filterByDeleted) // Always filter out deleted activities
                .filter(activity -> filterByType(activity, activityType))
                .filter(activity -> filterByStartLocation(activity, startLocation))
                .filter(activity -> filterByEndLocation(activity, endLocation))
                .filter(activity -> filterByStartDate(activity, startDate))
                .filter(activity -> filterByEndDate(activity, endDate))
                .filter(activity -> filterBySearch(activity, search))
                .sorted(Comparator.comparing(Activity::getStartDate, Comparator.nullsLast(LocalDateTime::compareTo)))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ActivityResponseDTO getActivityById(String id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found with id: " + id));

        // Only non-deleted activities can be viewed in detail
        if (activity.getIsDeleted() != null && activity.getIsDeleted()) {
            throw new RuntimeException("Activity has been deleted");
        }

        return convertToDTO(activity);
    }

    @Override
    public ActivityResponseDTO createActivity(CreateActivityRequestDTO requestDTO) {
        // Validate all required fields
        validateRequiredFields(requestDTO);

        // Authorization check based on vendor type
        checkCreateActivityAuthorization(requestDTO.getActivityType());

        // Validate business rules
        validateBusinessRules(requestDTO);

        // Generate ActivityID: ACT-{YYYYMMDD}-{XXX}
        String activityId = generateActivityId();

        // Create Activity entity
        Activity activity = new Activity();
        activity.setId(activityId);
        activity.setActivityName(requestDTO.getActivityName());
        activity.setActivityItem(requestDTO.getActivityItem());
        activity.setCapacity(requestDTO.getCapacity());
        activity.setPrice(requestDTO.getPrice());
        activity.setActivityType(requestDTO.getActivityType());
        activity.setStartDate(requestDTO.getStartDate());
        activity.setEndDate(requestDTO.getEndDate());
        activity.setStartLocation(requestDTO.getStartLocation());
        activity.setEndLocation(requestDTO.getEndLocation());
        activity.setIsDeleted(false);

        // Set creator ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        EndUser currentUser = endUserRepository.findByUsernameIgnoreCase(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
        activity.setCreatorId(currentUser.getId().toString());

        Activity savedActivity = activityRepository.save(activity);
        return convertToDTO(savedActivity);
    }

    @Override
    public ActivityResponseDTO updateActivity(String id, UpdateActivityRequestDTO requestDTO) {
        // Find activity
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found with id: " + id));

        // Check if activity is deleted
        if (activity.getIsDeleted() != null && activity.getIsDeleted()) {
            throw new RuntimeException("Cannot update a deleted activity");
        }

        // Check ownership - vendor can only update their own activities
        checkActivityOwnership(activity);

        // Validate that no fulfilled OrderedActivities exist
        // Check the plan status of each OrderedQuantity
        if (activity.getOrderedQuantities() != null && !activity.getOrderedQuantities().isEmpty()) {
            boolean hasFulfilledOrders = activity.getOrderedQuantities().stream()
                    .filter(oq -> oq.getPlan() != null)
                    .anyMatch(oq -> "Fulfilled".equalsIgnoreCase(oq.getPlan().getStatus()));

            if (hasFulfilledOrders) {
                throw new RuntimeException("Cannot update activity with fulfilled orders");
            }
        }

        // Validate business rules for fields that are being updated
        if (requestDTO.getPrice() != null && requestDTO.getPrice() <= 0) {
            throw new RuntimeException("Price must be greater than 0");
        }

        if (requestDTO.getCapacity() != null && requestDTO.getCapacity() <= 0) {
            throw new RuntimeException("Capacity must be greater than 0");
        }

        if (requestDTO.getStartDate() != null && requestDTO.getEndDate() != null) {
            if (!requestDTO.getEndDate().isAfter(requestDTO.getStartDate())) {
                throw new RuntimeException("End date must be after start date");
            }
        } else if (requestDTO.getStartDate() != null && activity.getEndDate() != null) {
            if (!activity.getEndDate().isAfter(requestDTO.getStartDate())) {
                throw new RuntimeException("End date must be after start date");
            }
        } else if (requestDTO.getEndDate() != null && activity.getStartDate() != null) {
            if (!requestDTO.getEndDate().isAfter(activity.getStartDate())) {
                throw new RuntimeException("End date must be after start date");
            }
        }

        if (requestDTO.getStartDate() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (requestDTO.getStartDate().isBefore(now)) {
                throw new RuntimeException("Start date cannot be in the past");
            }
        }

        // Update allowed fields (activityType CANNOT be updated)
        if (requestDTO.getActivityName() != null && !requestDTO.getActivityName().trim().isEmpty()) {
            activity.setActivityName(requestDTO.getActivityName());
        }

        if (requestDTO.getActivityItem() != null && !requestDTO.getActivityItem().trim().isEmpty()) {
            activity.setActivityItem(requestDTO.getActivityItem());
        }

        if (requestDTO.getCapacity() != null) {
            activity.setCapacity(requestDTO.getCapacity());
        }

        if (requestDTO.getPrice() != null) {
            activity.setPrice(requestDTO.getPrice());
        }

        if (requestDTO.getStartDate() != null) {
            activity.setStartDate(requestDTO.getStartDate());
        }

        if (requestDTO.getEndDate() != null) {
            activity.setEndDate(requestDTO.getEndDate());
        }

        if (requestDTO.getStartLocation() != null && !requestDTO.getStartLocation().trim().isEmpty()) {
            activity.setStartLocation(requestDTO.getStartLocation());
        }

        if (requestDTO.getEndLocation() != null && !requestDTO.getEndLocation().trim().isEmpty()) {
            activity.setEndLocation(requestDTO.getEndLocation());
        }

        Activity updatedActivity = activityRepository.save(activity);
        return convertToDTO(updatedActivity);
    }

    @Override
    public void deleteActivity(String id) {
        // Find activity
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found with id: " + id));

        // Check if already deleted
        if (activity.getIsDeleted() != null && activity.getIsDeleted()) {
            throw new RuntimeException("Activity is already deleted");
        }

        // Check ownership - vendor can only delete their own activities
        checkActivityOwnership(activity);

        // Check OrderedActivities status
        // Check the plan status of each OrderedQuantity
        if (activity.getOrderedQuantities() != null && !activity.getOrderedQuantities().isEmpty()) {
            boolean hasUnfulfilledOrders = activity.getOrderedQuantities().stream()
                    .filter(oq -> oq.getPlan() != null)
                    .anyMatch(oq -> !"Fulfilled".equalsIgnoreCase(oq.getPlan().getStatus()));

            if (hasUnfulfilledOrders) {
                throw new RuntimeException("Cannot delete activity with unfulfilled orders");
            }
        }

        // Soft delete - set isDeleted to true
        activity.setIsDeleted(true);
        activityRepository.save(activity);
    }

    // Helper method for ownership check
    private void checkActivityOwnership(Activity activity) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Authentication required");
        }

        String currentUsername = authentication.getName();
        EndUser currentUser = endUserRepository.findByUsernameIgnoreCase(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user is the creator of the activity
        if (!activity.getCreatorId().equals(currentUser.getId().toString())) {
            throw new RuntimeException("You can only modify activities you created");
        }
    }

    // Helper methods
    private void validateRequiredFields(CreateActivityRequestDTO requestDTO) {
        if (requestDTO.getActivityName() == null || requestDTO.getActivityName().trim().isEmpty()) {
            throw new RuntimeException("Activity name is required");
        }
        if (requestDTO.getActivityItem() == null || requestDTO.getActivityItem().trim().isEmpty()) {
            throw new RuntimeException("Activity item is required");
        }
        if (requestDTO.getActivityType() == null || requestDTO.getActivityType().trim().isEmpty()) {
            throw new RuntimeException("Activity type is required");
        }
        if (requestDTO.getStartDate() == null) {
            throw new RuntimeException("Start date is required");
        }
        if (requestDTO.getEndDate() == null) {
            throw new RuntimeException("End date is required");
        }
        if (requestDTO.getStartLocation() == null || requestDTO.getStartLocation().trim().isEmpty()) {
            throw new RuntimeException("Start location is required");
        }
        if (requestDTO.getEndLocation() == null || requestDTO.getEndLocation().trim().isEmpty()) {
            throw new RuntimeException("End location is required");
        }
    }

    private void validateBusinessRules(CreateActivityRequestDTO requestDTO) {
        // Validate startDate < endDate
        if (!requestDTO.getEndDate().isAfter(requestDTO.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        // Validate price > 0
        if (requestDTO.getPrice() == null || requestDTO.getPrice() <= 0) {
            throw new RuntimeException("Price must be greater than 0");
        }

        // Validate capacity > 0
        if (requestDTO.getCapacity() <= 0) {
            throw new RuntimeException("Capacity must be greater than 0");
        }

        // Validate startDate >= now
        LocalDateTime now = LocalDateTime.now();
        if (requestDTO.getStartDate().isBefore(now)) {
            throw new RuntimeException("Start date cannot be in the past");
        }
    }

    private void checkCreateActivityAuthorization(String activityType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Authentication required");
        }

        String currentUsername = authentication.getName();
        EndUser currentUser = endUserRepository.findByUsernameIgnoreCase(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        RoleType userRole = currentUser.getRoleType();

        // Tour Package Vendor can create all types
        if (userRole == RoleType.TOUR_PACKAGE_VENDOR) {
            return;
        }

        // Flight Airline can only create Flight activities
        if (userRole == RoleType.FLIGHT_AIRLINE) {
            if (!"Flight".equalsIgnoreCase(activityType)) {
                throw new RuntimeException("Flight Airline can only create Flight activities");
            }
            return;
        }

        // Accommodation Owner can only create Accommodation activities
        if (userRole == RoleType.ACCOMMODATION_OWNER) {
            if (!"Accommodation".equalsIgnoreCase(activityType)) {
                throw new RuntimeException("Accommodation Owner can only create Accommodation activities");
            }
            return;
        }

        // Rental Vendor can only create Vehicle Rental activities
        if (userRole == RoleType.RENTAL_VENDOR) {
            if (!"Vehicle Rental".equalsIgnoreCase(activityType)) {
                throw new RuntimeException("Rental Vendor can only create Vehicle Rental activities");
            }
            return;
        }

        throw new RuntimeException("You do not have permission to create activities");
    }

    private String generateActivityId() {
        // Format: ACT-{YYYYMMDD}-{XXX}
        LocalDateTime now = LocalDateTime.now();
        String dateStr = String.format("%04d%02d%02d",
                now.getYear(), now.getMonthValue(), now.getDayOfMonth());

        // Get count of activities created today
        String datePrefix = "ACT-" + dateStr + "-";
        long todayCount = activityRepository.findAll().stream()
                .filter(activity -> activity.getId().startsWith(datePrefix))
                .count();

        int count = (int) todayCount + 1;
        String countStr = String.format("%03d", count);

        return datePrefix + countStr;
    }

    private boolean filterByDeleted(Activity activity) {
        // Always exclude deleted activities (isDeleted = FALSE only)
        return activity.getIsDeleted() == null || !activity.getIsDeleted();
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

    private boolean filterByStartLocation(Activity activity, String startLocation) {
        if (startLocation == null || startLocation.isBlank()) {
            return true;
        }
        if (activity.getStartLocation() == null) {
            return false;
        }
        return activity.getStartLocation().equalsIgnoreCase(startLocation.trim());
    }

    private boolean filterByEndLocation(Activity activity, String endLocation) {
        if (endLocation == null || endLocation.isBlank()) {
            return true;
        }
        if (activity.getEndLocation() == null) {
            return false;
        }
        return activity.getEndLocation().equalsIgnoreCase(endLocation.trim());
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
                || (activity.getActivityItem() != null && activity.getActivityItem().toLowerCase().contains(keyword));
    }

    private LocalDateTime parseDateParam(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(rawDate.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "Invalid date format. Please use ISO-8601 format (yyyy-MM-dd'T'HH:mm:ss)");
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
        dto.setCreatorId(activity.getCreatorId());
        dto.setStartDate(activity.getStartDate());
        dto.setEndDate(activity.getEndDate());
        dto.setStartLocation(activity.getStartLocation());
        dto.setEndLocation(activity.getEndLocation());
        dto.setIsDeleted(activity.getIsDeleted());
        return dto;
    }
}
