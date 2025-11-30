package apap.ti._5.tour_package_2306165540_be.restcontroller;

import apap.ti._5.tour_package_2306165540_be.restdto.response.EndUserResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.RoleResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restservice.ProfileRestService;
import apap.ti._5.tour_package_2306165540_be.security.jwt.JwtTokenFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProfileRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfileRestService profileRestService;

    @MockBean
    private JwtTokenFilter jwtTokenFilter;

    @Test
    void upsertUser_success() throws Exception {
        EndUserResponseDTO dto = new EndUserResponseDTO();
        dto.setUsername("jane");
        when(profileRestService.upsertEndUser(any())).thenReturn(dto);

        mockMvc.perform(post("/api/profile/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("jane"));
    }

    @Test
    void upsertUser_validationError() throws Exception {
        when(profileRestService.upsertEndUser(any()))
                .thenThrow(new IllegalArgumentException("Username required"));

        mockMvc.perform(post("/api/profile/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPayload()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username required"));
    }

    @Test
    void upsertUser_serverError() throws Exception {
        when(profileRestService.upsertEndUser(any()))
                .thenAnswer(inv -> {
                    throw new RuntimeException("boom");
                });

        mockMvc.perform(post("/api/profile/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPayload()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getUsers_success() throws Exception {
        EndUserResponseDTO dto = new EndUserResponseDTO();
        dto.setUsername("john");
        when(profileRestService.getEndUsers(false)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/profile/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].username").value("john"));
    }

    @Test
    void getUsers_serverError() throws Exception {
        when(profileRestService.getEndUsers(true))
                .thenThrow(new RuntimeException("down"));

        mockMvc.perform(get("/api/profile/users").param("includeInactive", "true"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Gagal mengambil data user: down"));
    }

    @Test
    void getUserByUsername_success() throws Exception {
        EndUserResponseDTO dto = new EndUserResponseDTO();
        dto.setUsername("alice");
        when(profileRestService.getEndUserByUsername("alice")).thenReturn(dto);

        mockMvc.perform(get("/api/profile/users/alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("alice"));
    }

    @Test
    void getUserByUsername_notFound() throws Exception {
        when(profileRestService.getEndUserByUsername("ghost"))
                .thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(get("/api/profile/users/ghost"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void getUserByUsername_serverError() throws Exception {
        when(profileRestService.getEndUserByUsername("ghost"))
                .thenAnswer(inv -> {
                    throw new RuntimeException("boom");
                });

        mockMvc.perform(get("/api/profile/users/ghost"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deactivateUser_success() throws Exception {
        doNothing().when(profileRestService).deactivateEndUser("alice");

        mockMvc.perform(delete("/api/profile/users/alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User berhasil dinonaktifkan."));
    }

    @Test
    void deactivateUser_notFound() throws Exception {
        doThrow(new IllegalArgumentException("not found"))
                .when(profileRestService).deactivateEndUser("ghost");

        mockMvc.perform(delete("/api/profile/users/ghost"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deactivateUser_serverError() throws Exception {
        doThrow(new RuntimeException("boom"))
                .when(profileRestService).deactivateEndUser("ghost");

        mockMvc.perform(delete("/api/profile/users/ghost"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getRoles_success() throws Exception {
        RoleResponseDTO role = new RoleResponseDTO("SUPERADMIN", "Super Admin", "Manage platform");
        when(profileRestService.getRoleDefinitions()).thenReturn(List.of(role));

        mockMvc.perform(get("/api/profile/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("SUPERADMIN"));
    }

    @Test
    void getRoles_serverError() throws Exception {
        when(profileRestService.getRoleDefinitions())
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/profile/roles"))
                .andExpect(status().isInternalServerError());
    }

    private String userPayload() {
        return "{" +
                "\"username\":\"jane\"," +
                "\"email\":\"jane@example.com\"," +
                "\"fullName\":\"Jane Doe\"}";
    }
}
