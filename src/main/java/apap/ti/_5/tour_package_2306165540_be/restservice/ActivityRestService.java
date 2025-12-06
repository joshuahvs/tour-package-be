package apap.ti._5.tour_package_2306165540_be.restservice;

import apap.ti._5.tour_package_2306165540_be.restdto.request.CreateActivityRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.request.UpdateActivityRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.ActivityResponseDTO;

import java.util.List;

public interface ActivityRestService {
    List<ActivityResponseDTO> getAllActivities(String activityType, String startLocation, String endLocation,
            String startDateParam, String endDateParam, String search);

    ActivityResponseDTO getActivityById(String id);

    ActivityResponseDTO createActivity(CreateActivityRequestDTO requestDTO);

    ActivityResponseDTO updateActivity(String id, UpdateActivityRequestDTO requestDTO);

    void deleteActivity(String id);
}