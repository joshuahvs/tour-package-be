// package apap.ti._5.tour_package_2306165540_be.restcontroller;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.validation.BindingResult;
// import org.springframework.validation.FieldError;
// import org.springframework.web.bind.annotation.*;

// import apap.ti._5.tour_package_2306165540_be.restdto.response.BaseResponseDTO;
// import apap.ti._5.tour_package_2306165540_be.restdto.response.EndUserResponseDTO;

// import java.util.Date;
// import java.util.List;
// import java.util.UUID;

// @RestController
// @RequestMapping("/api/end-users")
// public class EndUserRestController {

//     private final EndUserRestService endUserRestService;

//     public EndUserRestController(EndUserRestService endUserRestService) {
//         this.endUserRestService = endUserRestService;
//     }

//     public static final String GET_ALL_END_USERS = "";
//     public static final String GET_END_USERS_BY_ROLE = "/role/{roleType}";
//     public static final String GET_ALL_CUSTOMERS = "/customers";
//     public static final String GET_END_USER_DETAIL = "/{identifier}";
//     public static final String CREATE_END_USER = "/create";
//     public static final String UPDATE_END_USER = "/{id}/edit";
//     public static final String DELETE_END_USER = "/{id}/delete";

//     // GET / - Get all EndUsers (Superadmin only)
//     @GetMapping(GET_ALL_END_USERS)
//     public ResponseEntity<BaseResponseDTO<List<EndUserResponseDTO>>> getAllEndUsers() {
        
//         var baseResponseDTO = new BaseResponseDTO<List<EndUserResponseDTO>>();

//         try {
//             List<EndUserResponseDTO> endUsers = endUserRestService.getAllEndUsers();

//             baseResponseDTO.setStatus(HttpStatus.OK.value());
//             baseResponseDTO.setData(endUsers);
//             baseResponseDTO.setMessage("End users retrieved successfully");
//             baseResponseDTO.setTimestamp(new Date());
//             return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

//         } catch (Exception e) {
//             baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//             baseResponseDTO.setMessage("Failed to retrieve end users: " + e.getMessage());
//             baseResponseDTO.setTimestamp(new Date());
//             return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
//         }
//     }

//     // GET /role/{roleType} - Get EndUsers by role (Superadmin only)
//     @GetMapping(GET_END_USERS_BY_ROLE)
//     public ResponseEntity<BaseResponseDTO<List<EndUserResponseDTO>>> getEndUsersByRole(
//             @PathVariable String roleType) {
        
//         var baseResponseDTO = new BaseResponseDTO<List<EndUserResponseDTO>>();

//         try {
//             List<EndUserResponseDTO> endUsers = endUserRestService.getEndUsersByRole(roleType);

//             baseResponseDTO.setStatus(HttpStatus.OK.value());
//             baseResponseDTO.setData(endUsers);
//             baseResponseDTO.setMessage("End users filtered by role retrieved successfully");
//             baseResponseDTO.setTimestamp(new Date());
//             return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

//         } catch (IllegalArgumentException e) {
//             baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
//             baseResponseDTO.setMessage(e.getMessage());
//             baseResponseDTO.setTimestamp(new Date());
//             return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
//         } catch (Exception e) {
//             baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//             baseResponseDTO.setMessage("Failed to retrieve end users by role: " + e.getMessage());
//             baseResponseDTO.setTimestamp(new Date());
//             return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
//         }
//     }

//     // GET /customers - Get all Customers with search functionality
//     @GetMapping(GET_ALL_CUSTOMERS)
//     public ResponseEntity<BaseResponseDTO<List<CustomerResponseDTO>>> getAllCustomers(
//             @RequestParam(required = false) String name,
//             @RequestParam(required = false) String email) {
        
//         var baseResponseDTO = new BaseResponseDTO<List<CustomerResponseDTO>>();

//         try {
//             List<CustomerResponseDTO> customers = endUserRestService.searchCustomers(name, email);

//             baseResponseDTO.setStatus(HttpStatus.OK.value());
//             baseResponseDTO.setData(customers);
//             baseResponseDTO.setMessage("Customers retrieved successfully");
//             baseResponseDTO.setTimestamp(new Date());
//             return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

//         } catch (Exception e) {
//             baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//             baseResponseDTO.setMessage("Failed to retrieve customers: " + e.getMessage());
//             baseResponseDTO.setTimestamp(new Date());
//             return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
//         }
//     }

//     // GET /{identifier} - Get EndUser detail by id, username, or email
//     @GetMapping(GET_END_USER_DETAIL)
//     public ResponseEntity<BaseResponseDTO<EndUserResponseDTO>> getEndUserDetail(@PathVariable String identifier) {
//         var baseResponseDTO = new BaseResponseDTO<EndUserResponseDTO>();

//         try {
//             EndUserResponseDTO endUser = endUserRestService.getEndUserByIdOrUsernameOrEmail(identifier);

//             if (endUser == null) {
//                 baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
//                 baseResponseDTO.setMessage("EndUser with identifier " + identifier + " not found");
//                 baseResponseDTO.setTimestamp(new Date());
//                 return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
//             }

//             baseResponseDTO.setStatus(HttpStatus.OK.value());
//             baseResponseDTO.setData(endUser);
//             baseResponseDTO.setMessage("EndUser retrieved successfully");
//             baseResponseDTO.setTimestamp(new Date());
//             return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

//         } catch (Exception e) {
//             baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//             baseResponseDTO.setMessage("Failed to retrieve end user: " + e.getMessage());
//             baseResponseDTO.setTimestamp(new Date());
//             return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
//         }
//     }

//     // POST /create - Create new EndUser
//     @PostMapping(CREATE_END_USER)
//     public ResponseEntity<BaseResponseDTO<EndUserResponseDTO>> createEndUser(
//             @Valid @RequestBody CreateEndUserRequestDTO createEndUserRequestDTO,
//             BindingResult bindingResult) {

//         var baseResponseDTO = new BaseResponseDTO<EndUserResponseDTO>();

//         if (bindingResult.hasFieldErrors()) {
//             StringBuilder errorMessages = new StringBuilder();
//             List<FieldError> errors = bindingResult.getFieldErrors();

//             for (FieldError error : errors) {
//                 errorMessages.append(error.getDefaultMessage()).append("; ");
//             }

//             baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
//             baseResponseDTO.setMessage(errorMessages.toString());
//             baseResponseDTO.setTimestamp(new Date());
//             return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
//         }

//         try {
//             EndUserResponseDTO endUser = endUserRestService.createEndUser(createEndUserRequestDTO);

//             baseResponseDTO.setStatus(HttpStatus.CREATED.value());
//             baseResponseDTO.setData(endUser);
//             baseResponseDTO.setMessage("EndUser created successfully");
//             baseResponseDTO.setTimestamp(new Date());
//             return new ResponseEntity<>(baseResponseDTO, HttpStatus.CREATED);

//         } catch (IllegalArgumentException e) {
//             baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
//             baseResponseDTO.setMessage(e.getMessage());
//             baseResponseDTO.setTimestamp(new Date());
//             return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
//         } catch (Exception e) {
//             baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//             baseResponseDTO.setMessage("Failed to create end user: " + e.getMessage());
//             baseResponseDTO.setTimestamp(new Date());
//             return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
//         }
//     }

//     // PUT /{id}/edit - Update EndUser
//     @PutMapping(UPDATE_END_USER)
//     public ResponseEntity<BaseResponseDTO<EndUserResponseDTO>> updateEndUser(
//             @PathVariable UUID id,
//             @Valid @RequestBody UpdateEndUserRequestDTO updateEndUserRequestDTO,
//             BindingResult bindingResult) {

//         var baseResponseDTO = new BaseResponseDTO<EndUserResponseDTO>();

//         if (bindingResult.hasFieldErrors()) {
//             StringBuilder errorMessages = new StringBuilder();
//             List<FieldError> errors = bindingResult.getFieldErrors();

//             for (FieldError error : errors) {
//                 errorMessages.append(error.getDefaultMessage()).append("; ");
//             }

//             baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
//             baseResponseDTO.setMessage(errorMessages.toString());
//             baseResponseDTO.setTimestamp(new Date());
//             return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
//         }

//         try {
//             // Set the ID from path parameter
//             updateEndUserRequestDTO.setId(id);

//             EndUserResponseDTO endUser = endUserRestService.updateEndUser(updateEndUserRequestDTO);

//             baseResponseDTO.setStatus(HttpStatus.OK.value());
//             baseResponseDTO.setData(endUser);
//             baseResponseDTO.setMessage("EndUser updated successfully");
//             baseResponseDTO.setTimestamp(new Date());
//             return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

//         } catch (IllegalArgumentException e) {
//             baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
//             baseResponseDTO.setMessage(e.getMessage());
//             baseResponseDTO.setTimestamp(new Date());
//             return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
//         } catch (RuntimeException e) {
//             baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
//             baseResponseDTO.setMessage(e.getMessage());
//             baseResponseDTO.setTimestamp(new Date());
//             return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
//         } catch (Exception e) {
//             baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//             baseResponseDTO.setMessage("Failed to update end user: " + e.getMessage());
//             baseResponseDTO.setTimestamp(new Date());
//             return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
//         }
//     }

//     // DELETE /{id}/delete - Delete EndUser (Superadmin only)
//     @DeleteMapping(DELETE_END_USER)
//     public ResponseEntity<BaseResponseDTO<?>> deleteEndUser(
//             @PathVariable UUID id) {
        
//         var baseResponseDTO = new BaseResponseDTO<>();

//         try {
//             endUserRestService.deleteEndUser(id);

//             baseResponseDTO.setStatus(HttpStatus.OK.value());
//             baseResponseDTO.setMessage("EndUser with id " + id + " deleted successfully");
//             baseResponseDTO.setTimestamp(new Date());
//             return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

//         } catch (IllegalArgumentException e) {
//             baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
//             baseResponseDTO.setMessage(e.getMessage());
//             baseResponseDTO.setTimestamp(new Date());
//             return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
//         } catch (Exception e) {
//             baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//             baseResponseDTO.setMessage("Failed to delete end user with id " + id + ": " + e.getMessage());
//             baseResponseDTO.setTimestamp(new Date());
//             return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
//         }
//     }
// }