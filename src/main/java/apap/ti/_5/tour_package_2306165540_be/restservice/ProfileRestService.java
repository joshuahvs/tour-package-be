package apap.ti._5.tour_package_2306165540_be.restservice;

import apap.ti._5.tour_package_2306165540_be.restdto.request.UpsertEndUserRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.EndUserResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.RoleResponseDTO;

import java.util.List;

public interface ProfileRestService {
    EndUserResponseDTO upsertEndUser(UpsertEndUserRequestDTO requestDTO);

    List<EndUserResponseDTO> getEndUsers(boolean includeInactive);

    EndUserResponseDTO getEndUserByUsername(String username);

    void deactivateEndUser(String username);

    List<RoleResponseDTO> getRoleDefinitions();
}
