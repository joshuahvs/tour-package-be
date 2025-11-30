package apap.ti._5.tour_package_2306165540_be.restcontroller;

import apap.ti._5.tour_package_2306165540_be.restdto.request.CreatePackageRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PackageDetailResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PackageResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restservice.PackageRestService;
import apap.ti._5.tour_package_2306165540_be.security.jwt.JwtTokenFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PackageRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class PackageRestControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private PackageRestController packageRestController;

        @MockBean
        private PackageRestService packageRestService;

        @MockBean
        private JwtTokenFilter jwtTokenFilter;

        private final ObjectMapper objectMapper = new ObjectMapper();

        private PackageResponseDTO samplePackage(String id) {
                return new PackageResponseDTO(
                                id,
                                "user-1",
                                "Honeymoon Bali",
                                2,
                                5_000_000L,
                                "PENDING",
                                LocalDateTime.now(),
                                LocalDateTime.now().plusDays(3));
        }

        @Test
        @DisplayName("GET /api/package should return all packages")
        void getAllPackages_shouldReturnList() throws Exception {
                when(packageRestService.getAllPackages()).thenReturn(List.of(samplePackage("PKG-1")));

                mockMvc.perform(get("/api/package").accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.data[0].id").value("PKG-1"));
        }

        @Test
        @DisplayName("GET /api/package?name=... should search by name")
        void getAllPackages_withName_shouldSearch() throws Exception {
                when(packageRestService.searchPackagesByName("Honey")).thenReturn(List.of(samplePackage("PKG-2")));

                mockMvc.perform(get("/api/package").param("name", "Honey").accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data[0].id").value("PKG-2"));
        }

        @Test
        @DisplayName("GET /api/package/{id} should return package")
        void getPackageById_shouldReturnOk() throws Exception {
                when(packageRestService.getPackageById("PKG-1")).thenReturn(samplePackage("PKG-1"));

                mockMvc.perform(get("/api/package/PKG-1").accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.id").value("PKG-1"));
        }

        @Test
        @DisplayName("GET /api/package/{id} not found")
        void getPackageById_notFound() throws Exception {
                when(packageRestService.getPackageById("PKG-404")).thenThrow(new RuntimeException("Package not found"));

                mockMvc.perform(get("/api/package/PKG-404").accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("GET /api/package/{id}/detail should return detail")
        void getPackageDetailById_shouldReturnOk() throws Exception {
                PackageDetailResponseDTO detail = new PackageDetailResponseDTO();
                detail.setId("PKG-9");
                detail.setPackageName("Romantic Escape");
                when(packageRestService.getPackageDetailById("PKG-9")).thenReturn(detail);

                mockMvc.perform(get("/api/package/PKG-9/detail").accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.id").value("PKG-9"));
        }

        @Test
        @DisplayName("GET /api/package/{id}/detail not found")
        void getPackageDetailById_notFound() throws Exception {
                when(packageRestService.getPackageDetailById("PKG-404"))
                                .thenThrow(new RuntimeException("Package not found"));

                mockMvc.perform(get("/api/package/PKG-404/detail").accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("POST /api/package should create package")
        void createPackage_shouldCreate() throws Exception {
                CreatePackageRequestDTO req = new CreatePackageRequestDTO(null, "user-1",
                                "Honeymoon Bali", 2, 5_000_000L,
                                "PENDING", null, null);
                PackageResponseDTO created = samplePackage("PKG-NEW");
                when(packageRestService.createPackage(any(CreatePackageRequestDTO.class))).thenReturn(created);

                mockMvc.perform(post("/api/package")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.data.id").value("PKG-NEW"));
        }

        @Test
        @DisplayName("GET /api/package server error -> 500")
        void getAllPackages_serverError() throws Exception {
                when(packageRestService.getAllPackages()).thenThrow(new RuntimeException("down"));

                mockMvc.perform(get("/api/package").accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.status").value(500));
        }

        @Test
        @DisplayName("POST /api/package server error -> 500")
        void createPackage_serverError() throws Exception {
                CreatePackageRequestDTO req = new CreatePackageRequestDTO(null, "user-1",
                                "A", 1, 0L, null, null, null);
                when(packageRestService.createPackage(any(CreatePackageRequestDTO.class)))
                                .thenThrow(new RuntimeException("boom"));

                mockMvc.perform(post("/api/package")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.status").value(500));
        }

        @Test
        @DisplayName("PUT /api/package/{id} should update package")
        void updatePackage_shouldUpdate() throws Exception {
                CreatePackageRequestDTO req = new CreatePackageRequestDTO(null, "user-1",
                                "Trip A", 3, 4_000_000L, "PENDING",
                                null, null);
                when(packageRestService.updatePackage(eq("PKG-1"),
                                any(CreatePackageRequestDTO.class)))
                                .thenReturn(samplePackage("PKG-1"));

                mockMvc.perform(put("/api/package/PKG-1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.id").value("PKG-1"));
        }

        @Test
        @DisplayName("PUT /api/package/{id} not found -> 404")
        void updatePackage_notFound() throws Exception {
                CreatePackageRequestDTO req = new CreatePackageRequestDTO();
                when(packageRestService.updatePackage(eq("PKG-404"), any(CreatePackageRequestDTO.class)))
                                .thenThrow(new RuntimeException("Package not found"));

                mockMvc.perform(put("/api/package/PKG-404")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT /api/package/{id} validation error -> 400")
        void updatePackage_badRequest() throws Exception {
                CreatePackageRequestDTO req = new CreatePackageRequestDTO();
                when(packageRestService.updatePackage(eq("PKG-1"), any(CreatePackageRequestDTO.class)))
                                .thenThrow(new RuntimeException("Validation failed"));

                mockMvc.perform(put("/api/package/PKG-1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("DELETE /api/package/{id} should delete -> 200")
        void deletePackage_shouldDelete() throws Exception {
                doNothing().when(packageRestService).deletePackage("PKG-1");

                mockMvc.perform(delete("/api/package/PKG-1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200));
        }

        @Test
        @DisplayName("DELETE /api/package/{id} not pending -> 400")
        void deletePackage_notPending_badRequest() throws Exception {
                doThrow(new RuntimeException("Only packages with status 'PENDING' can be deleted"))
                                .when(packageRestService).deletePackage("PKG-1");

                mockMvc.perform(delete("/api/package/PKG-1"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("DELETE /api/package/{id} not found -> 404")
        void deletePackage_notFound() throws Exception {
                doThrow(new RuntimeException("Package not found"))
                                .when(packageRestService).deletePackage("PKG-404");

                mockMvc.perform(delete("/api/package/PKG-404"))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT /api/package/{id}/process should process -> 200")
        void processPackage_shouldProcess() throws Exception {
                when(packageRestService.processPackage("PKG-2")).thenReturn(samplePackage("PKG-2"));

                mockMvc.perform(put("/api/package/PKG-2/process"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.id").value("PKG-2"));
        }

        @Test
        @DisplayName("PUT /api/package/{id}/process invalid state -> 400")
        void processPackage_badRequest() throws Exception {
                when(packageRestService.processPackage("PKG-2"))
                                .thenThrow(new RuntimeException("Cannot process package from current state"));

                mockMvc.perform(put("/api/package/PKG-2/process"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("PUT /api/package/{id}/process not found -> 404")
        void processPackage_notFound() throws Exception {
                when(packageRestService.processPackage("PKG-404"))
                                .thenThrow(new RuntimeException("Package not found"));

                mockMvc.perform(put("/api/package/PKG-404/process"))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT /api/package/{id}/payment/confirm missing config -> 500")
        void confirmPayment_missingConfig() throws Exception {
                setBillApiKey("");

                mockMvc.perform(put("/api/package/PKG-1/payment/confirm"))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.message").value("BILL_API_KEY is not configured on the server"));
        }

        @Test
        @DisplayName("PUT /api/package/{id}/payment/confirm forbidden -> 403")
        void confirmPayment_forbidden() throws Exception {
                setBillApiKey("secret");

                mockMvc.perform(put("/api/package/PKG-1/payment/confirm"))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.message").value("Forbidden: Invalid billing API key"));
        }

        @Test
        @DisplayName("PUT /api/package/{id}/payment/confirm success -> 200")
        void confirmPayment_success() throws Exception {
                setBillApiKey("secret");
                when(packageRestService.confirmPackagePayment("PKG-1"))
                                .thenReturn(samplePackage("PKG-1"));

                mockMvc.perform(put("/api/package/PKG-1/payment/confirm")
                                .header("X-API-KEY", "secret"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.id").value("PKG-1"));
        }

        @Test
        @DisplayName("PUT /api/package/{id}/payment/confirm not found -> 404")
        void confirmPayment_notFound() throws Exception {
                setBillApiKey("secret");
                when(packageRestService.confirmPackagePayment("PKG-99"))
                                .thenThrow(new RuntimeException("Package not found"));

                mockMvc.perform(put("/api/package/PKG-99/payment/confirm")
                                .header("X-API-KEY", "secret"))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT /api/package/{id}/payment/confirm bad request -> 400")
        void confirmPayment_badRequest() throws Exception {
                setBillApiKey("secret");
                when(packageRestService.confirmPackagePayment("PKG-1"))
                                .thenThrow(new RuntimeException("Already paid"));

                mockMvc.perform(put("/api/package/PKG-1/payment/confirm")
                                .header("X-API-KEY", "secret"))
                                .andExpect(status().isBadRequest());
        }

        private void setBillApiKey(String value) {
                ReflectionTestUtils.setField(packageRestController, "billApiKey", value);
        }
}
