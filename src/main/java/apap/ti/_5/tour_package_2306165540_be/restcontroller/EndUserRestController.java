package apap.ti._5.tour_package_2306165540_be.restcontroller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import apap.ti._5.tour_package_2306165540_be.model.profile.AccommodationOwner;
import apap.ti._5.tour_package_2306165540_be.model.profile.Customer;
import apap.ti._5.tour_package_2306165540_be.model.profile.EndUser;
import apap.ti._5.tour_package_2306165540_be.model.profile.FlightAirline;
import apap.ti._5.tour_package_2306165540_be.model.profile.RentalVendor;
import apap.ti._5.tour_package_2306165540_be.model.profile.RoleType;
import apap.ti._5.tour_package_2306165540_be.model.profile.SuperAdmin;
import apap.ti._5.tour_package_2306165540_be.model.profile.TourPackageVendor;
import apap.ti._5.tour_package_2306165540_be.repository.EndUserRepository;
import apap.ti._5.tour_package_2306165540_be.restdto.request.CreateEndUserRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.request.UpdateEndUserRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.BaseResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.CustomerResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.EndUserResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.UserManagementResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.UserProfileResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restservice.EndUserRestService;
import jakarta.validation.Valid;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/end-users")
public class EndUserRestController {

    private final EndUserRestService endUserRestService;
    private final EndUserRepository endUserRepository;

    public EndUserRestController(EndUserRestService endUserRestService, EndUserRepository endUserRepository) {
        this.endUserRestService = endUserRestService;
        this.endUserRepository = endUserRepository;
    }

    public static final String GET_ALL_END_USERS = "";
    public static final String GET_END_USERS_BY_ROLE = "/role/{roleType}";
    public static final String GET_ALL_CUSTOMERS = "/customers";
    public static final String GET_END_USER_DETAIL = "/{identifier}";
    public static final String GET_USER_PROFILE = "/profile/{identifier}";
    public static final String GET_MY_PROFILE = "/profile";
    public static final String CREATE_END_USER = "/create";
    public static final String UPDATE_END_USER = "/{id}/edit";
    public static final String DELETE_END_USER = "/{id}/delete";

    @GetMapping(GET_ALL_END_USERS)
    public ResponseEntity<BaseResponseDTO<List<EndUserResponseDTO>>> getAllEndUsers() {

        var baseResponseDTO = new BaseResponseDTO<List<EndUserResponseDTO>>();

        try {
            List<EndUserResponseDTO> endUsers = endUserRestService.getAllEndUsers();

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(endUsers);
            baseResponseDTO.setMessage("End users retrieved successfully");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception e) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Failed to retrieve end users: " + e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(GET_END_USERS_BY_ROLE)
    public ResponseEntity<BaseResponseDTO<List<EndUserResponseDTO>>> getEndUsersByRole(
            @PathVariable String roleType) {

        var baseResponseDTO = new BaseResponseDTO<List<EndUserResponseDTO>>();

        try {
            List<EndUserResponseDTO> endUsers = endUserRestService.getEndUsersByRole(roleType);

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(endUsers);
            baseResponseDTO.setMessage("End users filtered by role retrieved successfully");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Failed to retrieve end users by role: " + e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(GET_ALL_CUSTOMERS)
    public ResponseEntity<BaseResponseDTO<List<CustomerResponseDTO>>> getAllCustomers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email) {

        var baseResponseDTO = new BaseResponseDTO<List<CustomerResponseDTO>>();

        try {
            List<CustomerResponseDTO> customers = endUserRestService.searchCustomers(name, email);

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(customers);
            baseResponseDTO.setMessage("Customers retrieved successfully");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception e) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Failed to retrieve customers: " + e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(GET_END_USER_DETAIL)
    public ResponseEntity<BaseResponseDTO<EndUserResponseDTO>> getEndUserDetail(@PathVariable String identifier) {
        var baseResponseDTO = new BaseResponseDTO<EndUserResponseDTO>();

        try {
            EndUserResponseDTO endUser = endUserRestService.getEndUserByIdOrUsernameOrEmail(identifier);

            if (endUser == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("EndUser with identifier " + identifier + " not found");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(endUser);
            baseResponseDTO.setMessage("EndUser retrieved successfully");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception e) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Failed to retrieve end user: " + e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(GET_MY_PROFILE)
    public ResponseEntity<BaseResponseDTO<UserProfileResponseDTO>> getMyProfile() {
        var baseResponseDTO = new BaseResponseDTO<UserProfileResponseDTO>();

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            UserProfileResponseDTO profile = endUserRestService.getUserProfile(username);

            if (profile == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("User not found");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(profile);
            baseResponseDTO.setMessage("User profile retrieved successfully");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception e) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Failed to retrieve user profile: " + e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(GET_USER_PROFILE)
    public ResponseEntity<BaseResponseDTO<UserProfileResponseDTO>> getUserProfile(@PathVariable String identifier) {
        var baseResponseDTO = new BaseResponseDTO<UserProfileResponseDTO>();

        try {
            UserProfileResponseDTO profile = endUserRestService.getUserProfile(identifier);

            if (profile == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("User not found");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(profile);
            baseResponseDTO.setMessage("User profile retrieved successfully");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception e) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Failed to retrieve user profile: " + e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(CREATE_END_USER)
    public ResponseEntity<BaseResponseDTO<EndUserResponseDTO>> createEndUser(
            @Valid @RequestBody CreateEndUserRequestDTO createEndUserRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<EndUserResponseDTO>();

        if (bindingResult.hasFieldErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();

            for (FieldError error : errors) {
                errorMessages.append(error.getDefaultMessage()).append("; ");
            }

            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(errorMessages.toString());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }

        try {
            EndUserResponseDTO endUser = endUserRestService.createEndUser(createEndUserRequestDTO);

            baseResponseDTO.setStatus(HttpStatus.CREATED.value());
            baseResponseDTO.setData(endUser);
            baseResponseDTO.setMessage("EndUser created successfully");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Failed to create end user: " + e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(UPDATE_END_USER)
    public ResponseEntity<BaseResponseDTO<EndUserResponseDTO>> updateEndUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEndUserRequestDTO updateEndUserRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<EndUserResponseDTO>();

        if (bindingResult.hasFieldErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();

            for (FieldError error : errors) {
                errorMessages.append(error.getDefaultMessage()).append("; ");
            }

            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(errorMessages.toString());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }

        try {
            // Set the ID from path parameter
            updateEndUserRequestDTO.setId(id);

            EndUserResponseDTO endUser = endUserRestService.updateEndUser(updateEndUserRequestDTO);

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(endUser);
            baseResponseDTO.setMessage("EndUser updated successfully");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
            baseResponseDTO.setMessage(e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Failed to update end user: " + e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(DELETE_END_USER)
    public ResponseEntity<BaseResponseDTO<?>> deleteEndUser(
            @PathVariable UUID id) {

        var baseResponseDTO = new BaseResponseDTO<>();

        try {
            endUserRestService.deleteEndUser(id);

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setMessage("EndUser with id " + id + " deleted successfully");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
            baseResponseDTO.setMessage(e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Failed to delete end user with id " + id + ": " + e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // New endpoints for User Management frontend (accessible at /api/users)
    @GetMapping("/users")
    public ResponseEntity<BaseResponseDTO<List<UserManagementResponseDTO>>> getUsersForManagement(
            @RequestParam(required = false) String role) {

        var baseResponseDTO = new BaseResponseDTO<List<UserManagementResponseDTO>>();

        try {
            // Check authorization - only SUPERADMIN can access
            if (!isSuperAdmin()) {
                baseResponseDTO.setStatus(HttpStatus.FORBIDDEN.value());
                baseResponseDTO.setMessage("You are not authorized to view this page.");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.FORBIDDEN);
            }

            List<EndUser> users;

            if (role != null && !role.isBlank()) {
                // Filter by role - map RoleType to Class type
                Class<? extends EndUser> roleClass = getRoleClass(role.toUpperCase());
                if (roleClass == null) {
                    baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
                    baseResponseDTO.setMessage("Invalid role type: " + role);
                    baseResponseDTO.setTimestamp(new Date());
                    return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
                }
                users = endUserRepository.findByRoleType(roleClass);
            } else {
                // Get all users
                users = endUserRepository.findAll();
            }

            List<UserManagementResponseDTO> userDTOs = users.stream()
                    .map(this::convertToUserManagementDTO)
                    .collect(Collectors.toList());

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(userDTOs);
            baseResponseDTO.setMessage("Users retrieved successfully");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception e) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Failed to retrieve users: " + e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<BaseResponseDTO<UserManagementResponseDTO>> getUserByIdForManagement(
            @PathVariable String id) {

        var baseResponseDTO = new BaseResponseDTO<UserManagementResponseDTO>();

        try {
            // Check authorization - only SUPERADMIN can access
            if (!isSuperAdmin()) {
                baseResponseDTO.setStatus(HttpStatus.FORBIDDEN.value());
                baseResponseDTO.setMessage("You are not authorized to view this page.");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.FORBIDDEN);
            }

            EndUser user = endUserRepository.findById(UUID.fromString(id))
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

            UserManagementResponseDTO userDTO = convertToUserManagementDTO(user);

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(userDTO);
            baseResponseDTO.setMessage("User retrieved successfully");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (RuntimeException e) {
            baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
            baseResponseDTO.setMessage(e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Failed to retrieve user: " + e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isSuperAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return false;
        }

        String currentUsername = authentication.getName();
        EndUser currentUser = endUserRepository.findByUsernameIgnoreCase(currentUsername)
                .orElse(null);

        return currentUser != null && currentUser.getRoleType() == RoleType.SUPERADMIN;
    }

    private Class<? extends EndUser> getRoleClass(String roleType) {
        return switch (roleType) {
            case "SUPERADMIN" -> SuperAdmin.class;
            case "TOUR_PACKAGE_VENDOR" -> TourPackageVendor.class;
            case "FLIGHT_AIRLINE" -> FlightAirline.class;
            case "ACCOMMODATION_OWNER" -> AccommodationOwner.class;
            case "RENTAL_VENDOR" -> RentalVendor.class;
            case "CUSTOMER" -> Customer.class;
            default -> null;
        };
    }

    private UserManagementResponseDTO convertToUserManagementDTO(EndUser user) {
        UserManagementResponseDTO dto = new UserManagementResponseDTO();
        dto.setId(user.getId().toString());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setOrganizationName(user.getOrganizationName());
        dto.setNotes(user.getNotes());
        dto.setRoleType(user.getRoleType().name());
        dto.setActive(user.isActive());
        return dto;
    }
}