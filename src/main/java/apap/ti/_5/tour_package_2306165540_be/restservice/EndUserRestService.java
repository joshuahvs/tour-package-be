package apap.ti._5.tour_package_2306165540_be.restservice;

import apap.ti._5.tour_package_2306165540_be.restdto.request.CreateEndUserRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.request.UpdateEndUserRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.CustomerResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.EndUserResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.UserProfileResponseDTO;

import java.util.List;
import java.util.UUID;

public interface EndUserRestService {
    List<EndUserResponseDTO> getAllEndUsers();

    List<EndUserResponseDTO> getEndUsersByRole(String roleType);

    List<CustomerResponseDTO> searchCustomers(String name, String email);

    EndUserResponseDTO getEndUserByIdOrUsernameOrEmail(String identifier);

    UserProfileResponseDTO getUserProfile(String identifier);

    EndUserResponseDTO createEndUser(CreateEndUserRequestDTO requestDTO);

    EndUserResponseDTO updateEndUser(UpdateEndUserRequestDTO requestDTO);

    void deleteEndUser(UUID id);

    void deductBalance(UUID userId, Double amount);
}
