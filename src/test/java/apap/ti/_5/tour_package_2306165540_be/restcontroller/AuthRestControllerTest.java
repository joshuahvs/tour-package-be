package apap.ti._5.tour_package_2306165540_be.restcontroller;

import apap.ti._5.tour_package_2306165540_be.restdto.response.EndUserResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restservice.ProfileRestService;
import apap.ti._5.tour_package_2306165540_be.security.jwt.JwtTokenFilter;
import apap.ti._5.tour_package_2306165540_be.security.jwt.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private ProfileRestService profileRestService;

    @MockBean
    private JwtTokenFilter jwtTokenFilter;

    @Test
    void login_returnsTokenOnSuccess() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken("alice", null, List.of());
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(jwtUtils.generateToken(authentication)).thenReturn("token-value");
        when(jwtUtils.getExpirationInstant("token-value")).thenReturn(Instant.now().plusSeconds(3600));
        EndUserResponseDTO dto = new EndUserResponseDTO();
        dto.setUsername("alice");
        when(profileRestService.getEndUserByUsername("alice")).thenReturn(dto);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginPayload("alice", "Secret1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("token-value"))
                .andExpect(jsonPath("$.data.user.username").value("alice"));
    }

    @Test
    void login_returnsUnauthorizedOnBadCredentials() throws Exception {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("bad"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginPayload("alice", "Secret1"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Username atau password tidak valid."));
    }

    @Test
    void register_defaultsToCustomerRole() throws Exception {
        EndUserResponseDTO responseDTO = new EndUserResponseDTO();
        responseDTO.setUsername("newuser");
        when(profileRestService.upsertEndUser(any())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"username\":\"newuser\"," +
                        "\"email\":\"newuser@example.com\"," +
                        "\"fullName\":\"New User\"," +
                        "\"password\":\"Secret1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username").value("newuser"));

        ArgumentCaptor<apap.ti._5.tour_package_2306165540_be.restdto.request.UpsertEndUserRequestDTO> captor = ArgumentCaptor
                .forClass(apap.ti._5.tour_package_2306165540_be.restdto.request.UpsertEndUserRequestDTO.class);
        verify(profileRestService).upsertEndUser(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo("CUSTOMER");
    }

    private record LoginPayload(String username, String password) {
    }
}
