package apap.ti._5.tour_package_2306165540_be.restcontroller;

import apap.ti._5.tour_package_2306165540_be.restdto.response.PlanDetailResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.PlanResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restservice.PlanRestService;
import apap.ti._5.tour_package_2306165540_be.security.jwt.JwtTokenFilter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlanRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlanRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlanRestService planRestService;

    @MockBean
    private JwtTokenFilter jwtTokenFilter;

    @Test
    void getAllPlansByPackage_shouldReturnOk() throws Exception {
        PlanResponseDTO dto = new PlanResponseDTO();
        dto.setId(UUID.randomUUID());
        Mockito.when(planRestService.getAllPlansByPackage("PKG-1"))
                .thenReturn(java.util.List.of(dto));

        mockMvc.perform(get("/api/packages/PKG-1/plans").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(dto.getId().toString()));
    }

    @Test
    void getAllPlansByPackage_notFound() throws Exception {
        Mockito.when(planRestService.getAllPlansByPackage("PKG-404"))
                .thenThrow(new RuntimeException("Package not found"));

        mockMvc.perform(get("/api/packages/PKG-404/plans").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Package not found"));
    }

    @Test
    void getAllPlansByPackage_forbidden() throws Exception {
        Mockito.when(planRestService.getAllPlansByPackage("PKG-1"))
                .thenThrow(new RuntimeException("Access denied"));

        mockMvc.perform(get("/api/packages/PKG-1/plans").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void getPlanDetail_shouldReturnOk() throws Exception {
        PlanDetailResponseDTO dto = new PlanDetailResponseDTO();
        dto.setId(UUID.randomUUID());
        Mockito.when(planRestService.getPlanDetail(any())).thenReturn(dto);

        mockMvc.perform(get("/api/packages/plans/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void deletePlan_shouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/packages/plans/" + id + "/delete").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
        Mockito.verify(planRestService).deletePlan(id);
    }

    @Test
    void getPlanDetail_notFound() throws Exception {
        Mockito.when(planRestService.getPlanDetail(any())).thenThrow(new RuntimeException("Plan not found"));

        mockMvc.perform(get("/api/packages/plans/" + UUID.randomUUID()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePlan_badRequest() throws Exception {
        Mockito.doThrow(new RuntimeException("Cannot delete")).when(planRestService).deletePlan(any());

        mockMvc.perform(
                delete("/api/packages/plans/" + UUID.randomUUID() + "/delete").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPlanDetail_serverError() throws Exception {
        Mockito.when(planRestService.getPlanDetail(any())).thenAnswer(inv -> {
            throw new Exception("boom");
        });

        mockMvc.perform(get("/api/packages/plans/" + UUID.randomUUID()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }
}
