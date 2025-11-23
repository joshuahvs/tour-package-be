package apap.ti._5.tour_package_2306165540_be.restcontroller;

import apap.ti._5.tour_package_2306165540_be.restdto.request.LoginRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.request.RegisterRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.request.UpsertEndUserRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.BaseResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.EndUserResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.LoginResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restservice.ProfileRestService;
import apap.ti._5.tour_package_2306165540_be.security.jwt.JwtUtils;
import apap.ti._5.tour_package_2306165540_be.model.profile.RoleType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final ProfileRestService profileRestService;

    public AuthRestController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, ProfileRestService profileRestService){
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.profileRestService = profileRestService;
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponseDTO<LoginResponseDTO>> login(@RequestBody LoginRequestDTO requestDTO) {
        BaseResponseDTO<LoginResponseDTO> response = new BaseResponseDTO<>();
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestDTO.getUsername(), requestDTO.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateToken(authentication);

            EndUserResponseDTO user = profileRestService.getEndUserByUsername(authentication.getName());
            LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
            loginResponseDTO.setToken(jwt);
            loginResponseDTO.setExpiresAt(jwtUtils.getExpirationInstant(jwt));
            loginResponseDTO.setUser(user);

            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Login berhasil.");
            response.setData(loginResponseDTO);
            response.setTimestamp(new Date());
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException ex) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setMessage("Username atau password tidak valid.");
            response.setTimestamp(new Date());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception ex) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Gagal melakukan login: " + ex.getMessage());
            response.setTimestamp(new Date());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<BaseResponseDTO<EndUserResponseDTO>> register(@RequestBody RegisterRequestDTO requestDTO) {
        BaseResponseDTO<EndUserResponseDTO> response = new BaseResponseDTO<>();
        try {
            UpsertEndUserRequestDTO upsert = new UpsertEndUserRequestDTO();
            upsert.setUsername(requestDTO.getUsername());
            upsert.setEmail(requestDTO.getEmail());
            upsert.setFullName(requestDTO.getFullName());
            upsert.setPhoneNumber(requestDTO.getPhoneNumber());
            upsert.setPassword(requestDTO.getPassword());
            upsert.setOrganizationName(requestDTO.getOrganizationName());
            upsert.setNotes(requestDTO.getNotes());
            String role = requestDTO.getRole();
            if (role == null || role.isBlank()) {
                role = RoleType.CUSTOMER.name();
            }
            upsert.setRole(role);
            upsert.setActive(true);

            EndUserResponseDTO user = profileRestService.upsertEndUser(upsert);
            response.setStatus(HttpStatus.CREATED.value());
            response.setMessage("Registrasi berhasil.");
            response.setData(user);
            response.setTimestamp(new Date());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage(ex.getMessage());
            response.setTimestamp(new Date());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception ex) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Gagal melakukan registrasi: " + ex.getMessage());
            response.setTimestamp(new Date());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
