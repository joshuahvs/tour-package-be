package apap.ti._5.tour_package_2306165540_be.restcontroller;

import apap.ti._5.tour_package_2306165540_be.model.profile.Customer;
import apap.ti._5.tour_package_2306165540_be.model.profile.EndUser;
import apap.ti._5.tour_package_2306165540_be.model.profile.SuperAdmin;
import apap.ti._5.tour_package_2306165540_be.repository.EndUserRepository;
import apap.ti._5.tour_package_2306165540_be.restdto.response.CustomerResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.EndUserResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.UserProfileResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restservice.EndUserRestService;
import apap.ti._5.tour_package_2306165540_be.security.jwt.JwtTokenFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EndUserRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class EndUserRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EndUserRestService endUserRestService;

    @MockBean
    private EndUserRepository endUserRepository;

    @MockBean
    private JwtTokenFilter jwtTokenFilter;

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllEndUsers_returnsData() throws Exception {
        EndUserResponseDTO dto = new EndUserResponseDTO();
        dto.setUsername("alice");
        when(endUserRestService.getAllEndUsers()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/end-users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].username").value("alice"));
    }

    @Test
    void getAllEndUsers_handlesException() throws Exception {
        when(endUserRestService.getAllEndUsers()).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/end-users"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to retrieve end users: boom"));
    }

    @Test
    void getEndUsersByRole_invalidRole() throws Exception {
        when(endUserRestService.getEndUsersByRole("invalid"))
                .thenThrow(new IllegalArgumentException("Role not found"));

        mockMvc.perform(get("/api/end-users/role/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Role not found"));
    }

    @Test
    void getEndUsersByRole_success() throws Exception {
        EndUserResponseDTO dto = new EndUserResponseDTO();
        dto.setUsername("bob");
        when(endUserRestService.getEndUsersByRole("CUSTOMER"))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/end-users/role/CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].username").value("bob"));
    }

    @Test
    void getEndUserDetail_notFound() throws Exception {
        when(endUserRestService.getEndUserByIdOrUsernameOrEmail("missing")).thenReturn(null);

        mockMvc.perform(get("/api/end-users/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("EndUser with identifier missing not found"));
    }

    @Test
    void getEndUserDetail_success() throws Exception {
        EndUserResponseDTO dto = new EndUserResponseDTO();
        dto.setUsername("alice");
        when(endUserRestService.getEndUserByIdOrUsernameOrEmail("alice"))
                .thenReturn(dto);

        mockMvc.perform(get("/api/end-users/alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("alice"));
    }

    @Test
    void getMyProfile_returnsProfile() throws Exception {
        UserProfileResponseDTO profile = new UserProfileResponseDTO();
        profile.setUsername("alice");
        when(endUserRestService.getUserProfile("alice"))
                .thenReturn(profile);
        setAuthentication("alice", "ROLE_CUSTOMER");

        mockMvc.perform(get("/api/end-users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("alice"));
    }

    @Test
    void getMyProfile_notFound() throws Exception {
        when(endUserRestService.getUserProfile("alice"))
                .thenReturn(null);
        setAuthentication("alice", "ROLE_CUSTOMER");

        mockMvc.perform(get("/api/end-users/profile"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserProfileByIdentifier_success() throws Exception {
        UserProfileResponseDTO profile = new UserProfileResponseDTO();
        profile.setUsername("bob");
        when(endUserRestService.getUserProfile("bob"))
                .thenReturn(profile);

        mockMvc.perform(get("/api/end-users/profile/bob"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("bob"));
    }

    @Test
    void getUserProfileByIdentifier_notFound() throws Exception {
        when(endUserRestService.getUserProfile("bob"))
                .thenReturn(null);

        mockMvc.perform(get("/api/end-users/profile/bob"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void getAllCustomers_success() throws Exception {
        CustomerResponseDTO customer = new CustomerResponseDTO();
        customer.setUsername("cust");
        when(endUserRestService.searchCustomers("cust", "mail"))
                .thenReturn(List.of(customer));

        mockMvc.perform(get("/api/end-users/customers")
                .param("name", "cust")
                .param("email", "mail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].username").value("cust"));
    }

    @Test
    void getAllCustomers_handlesException() throws Exception {
        when(endUserRestService.searchCustomers(null, null))
                .thenThrow(new RuntimeException("err"));

        mockMvc.perform(get("/api/end-users/customers"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to retrieve customers: err"));
    }

    @Test
    void createEndUser_validationErrors() throws Exception {
        mockMvc.perform(post("/api/end-users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEndUser_success() throws Exception {
        EndUserResponseDTO dto = new EndUserResponseDTO();
        dto.setUsername("bob");
        when(endUserRestService.createEndUser(any())).thenReturn(dto);

        mockMvc.perform(post("/api/end-users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"username\":\"bob\"," +
                        "\"email\":\"bob@example.com\"," +
                        "\"fullName\":\"Bob\"," +
                        "\"password\":\"Secret1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username").value("bob"));
    }

    @Test
    void updateEndUser_setsIdFromPath() throws Exception {
        UUID id = UUID.randomUUID();
        EndUserResponseDTO dto = new EndUserResponseDTO();
        dto.setId(id);
        when(endUserRestService.updateEndUser(any())).thenReturn(dto);

        mockMvc.perform(put("/api/end-users/" + id + "/edit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"id\":\"" + id + "\"," +
                        "\"username\":\"updated\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<apap.ti._5.tour_package_2306165540_be.restdto.request.UpdateEndUserRequestDTO> captor = ArgumentCaptor
                .forClass(apap.ti._5.tour_package_2306165540_be.restdto.request.UpdateEndUserRequestDTO.class);
        verify(endUserRestService).updateEndUser(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(id);
    }

    @Test
    void deleteEndUser_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new IllegalArgumentException("not found")).when(endUserRestService).deleteEndUser(id);

        mockMvc.perform(delete("/api/end-users/" + id + "/delete"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEndUser_success() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/end-users/" + id + "/delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("EndUser with id " + id + " deleted successfully"));
        verify(endUserRestService).deleteEndUser(id);
    }

    @Test
    void getUsersForManagement_forbiddenForNonSuperadmin() throws Exception {
        setAuthentication("user", "ROLE_CUSTOMER");
        when(endUserRepository.findByUsernameIgnoreCase("user"))
                .thenReturn(Optional.of(buildCustomer("user")));

        mockMvc.perform(get("/api/end-users/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsersForManagement_filtersByRole() throws Exception {
        setAuthentication("admin", "ROLE_SUPERADMIN");
        EndUser superAdmin = new SuperAdmin();
        superAdmin.setId(UUID.randomUUID());
        superAdmin.setUsername("admin");
        when(endUserRepository.findByUsernameIgnoreCase("admin"))
                .thenReturn(Optional.of(superAdmin));
        EndUser vendor = new SuperAdmin();
        vendor.setId(UUID.randomUUID());
        vendor.setUsername("vendor1");
        vendor.setEmail("vendor@example.com");
        vendor.setFullName("Vendor One");
        vendor.setOrganizationName("Org");
        vendor.setNotes("note");
        vendor.setActive(true);
        when(endUserRepository.findByRoleType(SuperAdmin.class)).thenReturn(List.of(vendor));

        mockMvc.perform(get("/api/end-users/users").param("role", "SUPERADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].username").value("vendor1"));
    }

    @Test
    void getUsersForManagement_withoutRoleReturnsAll() throws Exception {
        setAuthentication("admin", "ROLE_SUPERADMIN");
        EndUser superAdmin = new SuperAdmin();
        superAdmin.setId(UUID.randomUUID());
        superAdmin.setUsername("admin");
        when(endUserRepository.findByUsernameIgnoreCase("admin"))
                .thenReturn(Optional.of(superAdmin));
        EndUser vendor = new SuperAdmin();
        vendor.setId(UUID.randomUUID());
        vendor.setUsername("vendor1");
        vendor.setEmail("vendor@example.com");
        vendor.setFullName("Vendor One");
        vendor.setOrganizationName("Org");
        vendor.setNotes("note");
        vendor.setActive(true);
        when(endUserRepository.findAll()).thenReturn(List.of(vendor));

        mockMvc.perform(get("/api/end-users/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].username").value("vendor1"));
    }

    @Test
    void getUsersForManagement_invalidRole() throws Exception {
        setAuthentication("admin", "ROLE_SUPERADMIN");
        EndUser superAdmin = new SuperAdmin();
        superAdmin.setId(UUID.randomUUID());
        superAdmin.setUsername("admin");
        when(endUserRepository.findByUsernameIgnoreCase("admin"))
                .thenReturn(Optional.of(superAdmin));

        mockMvc.perform(get("/api/end-users/users").param("role", "UNKNOWN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserByIdForManagement_notFound() throws Exception {
        setAuthentication("admin", "ROLE_SUPERADMIN");
        EndUser superAdmin = new SuperAdmin();
        superAdmin.setId(UUID.randomUUID());
        superAdmin.setUsername("admin");
        when(endUserRepository.findByUsernameIgnoreCase("admin"))
                .thenReturn(Optional.of(superAdmin));
        UUID id = UUID.randomUUID();
        when(endUserRepository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/end-users/users/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserByIdForManagement_success() throws Exception {
        setAuthentication("admin", "ROLE_SUPERADMIN");
        EndUser superAdmin = new SuperAdmin();
        superAdmin.setId(UUID.randomUUID());
        superAdmin.setUsername("admin");
        when(endUserRepository.findByUsernameIgnoreCase("admin"))
                .thenReturn(Optional.of(superAdmin));
        UUID id = UUID.randomUUID();
        EndUser target = new SuperAdmin();
        target.setId(id);
        target.setUsername("target");
        target.setEmail("target@example.com");
        target.setFullName("Target");
        when(endUserRepository.findById(id)).thenReturn(Optional.of(target));

        mockMvc.perform(get("/api/end-users/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("target"));
    }

    @Test
    void deductBalance_validatesAmount() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(post("/api/end-users/" + id + "/deduct-balance")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deductBalance_callsService() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(post("/api/end-users/" + id + "/deduct-balance")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Saldo berhasil dipotong."));
        verify(endUserRestService).deductBalance(id, 100d);
    }

    private void setAuthentication(String username, String role) {
        var auth = new UsernamePasswordAuthenticationToken(
                username, null, List.of(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private EndUser buildCustomer(String username) {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setUsername(username);
        customer.setEmail(username + "@example.com");
        customer.setFullName(username);
        customer.setPassword("encoded");
        customer.setActive(true);
        return customer;
    }
}
