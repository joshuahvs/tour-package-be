package apap.ti._5.tour_package_2306165540_be.restservice;

import apap.ti._5.tour_package_2306165540_be.restdto.request.AddOrderedQuantityRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.request.CreatePlanRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.request.UpdatePlanRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.request.UpdateOrderedQuantityRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PlanDetailResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PlanResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PlanRestService {
    PlanResponseDTO createPlan(String packageId, CreatePlanRequestDTO requestDTO);

    PlanDetailResponseDTO getPlanDetail(UUID planId);

    PlanDetailResponseDTO updatePlan(UUID planId, UpdatePlanRequestDTO requestDTO);

    PlanDetailResponseDTO addOrderedQuantity(UUID planId, AddOrderedQuantityRequestDTO requestDTO);

    PlanDetailResponseDTO updateOrderedQuantity(UUID orderedQuantityId, UpdateOrderedQuantityRequestDTO requestDTO);

    PlanDetailResponseDTO deleteOrderedQuantity(UUID orderedQuantityId);

    void deletePlan(UUID planId);

    List<PlanResponseDTO> getAvailablePlansForActivity(UUID planId);
}