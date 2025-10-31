package apap.ti._5.tour_package_2306165540_be.restservice;

import apap.ti._5.tour_package_2306165540_be.restdto.request.CreatePlanRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.request.UpdatePlanRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PlanDetailResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PlanResponseDTO;

import java.util.UUID;

public interface PlanRestService {
    PlanResponseDTO createPlan(String packageId, CreatePlanRequestDTO requestDTO);

    PlanDetailResponseDTO getPlanDetail(UUID planId);

    PlanDetailResponseDTO updatePlan(UUID planId, UpdatePlanRequestDTO requestDTO);
}
