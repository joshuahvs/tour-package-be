package apap.ti._5.tour_package_2306165540_be.restservice;

import apap.ti._5.tour_package_2306165540_be.restdto.request.CreatePlanRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PlanResponseDTO;

public interface PlanRestService {
    PlanResponseDTO createPlan(String packageId, CreatePlanRequestDTO requestDTO);
}
