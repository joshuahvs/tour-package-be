package apap.ti._5.tour_package_2306165540_be.restcontroller;

import apap.ti._5.tour_package_2306165540_be.restdto.request.UpsertEndUserRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.BaseResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.EndUserResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.RoleResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restservice.ProfileRestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileRestController {

    private final ProfileRestService profileRestService;

    @PostMapping("/users")
    public ResponseEntity<BaseResponseDTO<EndUserResponseDTO>> upsertUser(
            @RequestBody UpsertEndUserRequestDTO requestDTO) {
        BaseResponseDTO<EndUserResponseDTO> response = new BaseResponseDTO<>();
        try {
            EndUserResponseDTO dto = profileRestService.upsertEndUser(requestDTO);
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("User berhasil diproses (upsert).");
            response.setData(dto);
            response.setTimestamp(new Date());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage(ex.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception ex) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Gagal memproses user: " + ex.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<BaseResponseDTO<List<EndUserResponseDTO>>> getUsers(
            @RequestParam(name = "includeInactive", defaultValue = "false") boolean includeInactive) {
        BaseResponseDTO<List<EndUserResponseDTO>> response = new BaseResponseDTO<>();
        try {
            List<EndUserResponseDTO> data = profileRestService.getEndUsers(includeInactive);
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Berhasil mengambil data user.");
            response.setData(data);
            response.setTimestamp(new Date());
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Gagal mengambil data user: " + ex.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<BaseResponseDTO<EndUserResponseDTO>> getUserByUsername(@PathVariable String username) {
        BaseResponseDTO<EndUserResponseDTO> response = new BaseResponseDTO<>();
        try {
            EndUserResponseDTO data = profileRestService.getEndUserByUsername(username);
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Berhasil mengambil detail user.");
            response.setData(data);
            response.setTimestamp(new Date());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setMessage(ex.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception ex) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Gagal mengambil detail user: " + ex.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/users/{username}")
    public ResponseEntity<BaseResponseDTO<Void>> deactivateUser(@PathVariable String username) {
        BaseResponseDTO<Void> response = new BaseResponseDTO<>();
        try {
            profileRestService.deactivateEndUser(username);
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("User berhasil dinonaktifkan.");
            response.setTimestamp(new Date());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setMessage(ex.getMessage());
            response.setTimestamp(new Date());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception ex) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Gagal menonaktifkan user: " + ex.getMessage());
            response.setTimestamp(new Date());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/roles")
    public ResponseEntity<BaseResponseDTO<List<RoleResponseDTO>>> getRoles() {
        BaseResponseDTO<List<RoleResponseDTO>> response = new BaseResponseDTO<>();
        try {
            List<RoleResponseDTO> roles = profileRestService.getRoleDefinitions();
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Berhasil mengambil daftar role.");
            response.setData(roles);
            response.setTimestamp(new Date());
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Gagal mengambil daftar role: " + ex.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
