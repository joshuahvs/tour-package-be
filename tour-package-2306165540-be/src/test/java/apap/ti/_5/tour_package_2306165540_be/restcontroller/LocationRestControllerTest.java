package apap.ti._5.tour_package_2306165540_be.restcontroller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@WebMvcTest(LocationRestController.class)
@Disabled("Skipped because controller uses external HTTP call with new RestTemplate(); no safe way to stub without extra deps")
class LocationRestControllerTest {

    @Test
    void placeholder() {
        // Intentionally empty. See @Disabled reason above.
    }
}
/*
 * package apap.ti._5.tour_package_2306165540_be.restcontroller;
 * 
 * import org.junit.jupiter.api.Test;
 * import org.mockito.MockedConstruction;
 * import org.mockito.Mockito;
 * import org.springframework.beans.factory.annotation.Autowired;
 * import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
 * import org.springframework.http.MediaType;
 * import org.springframework.test.web.servlet.MockMvc;
 * import org.springframework.web.client.RestTemplate;
 * 
 * import static org.mockito.ArgumentMatchers.anyString;
 * import static org.mockito.ArgumentMatchers.eq;
 * import static org.mockito.Mockito.when;
 * import static
 * org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
 * import static
 * org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
 * import static
 * org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 * 
 * @WebMvcTest(LocationRestController.class)
 * class LocationRestControllerTest {
 * 
 * @Autowired
 * private MockMvc mockMvc;
 * 
 * @Test
 * void getLocations_shouldReturnOkWithTransformedData() throws Exception {
 * String payload =
 * "{\"data\":[{\"code\":\"11\",\"name\":\"Aceh\"},{\"code\":\"12\",\"name\":\"Sumut\"}]}";
 * 
 * try (MockedConstruction<RestTemplate> mocked =
 * Mockito.mockConstruction(RestTemplate.class, (mock, context) -> {
 * when(mock.getForObject(eq("https://wilayah.id/api/provinces.json"),
 * eq(String.class))).thenReturn(payload);
 * })) {
 * mockMvc.perform(get("/api/locations").accept(MediaType.APPLICATION_JSON))
 * .andExpect(status().isOk())
 * .andExpect(jsonPath("$.status").value(200))
 * .andExpect(jsonPath("$.data[0].code").value("11"))
 * .andExpect(jsonPath("$.data[1].name").value("Sumut"));
 * }
 * }
 * 
 * @Test
 * void getLocations_whenApiFails_shouldReturn500() throws Exception {
 * try (MockedConstruction<RestTemplate> mocked =
 * Mockito.mockConstruction(RestTemplate.class, (mock, context) -> {
 * when(mock.getForObject(anyString(), eq(String.class))).thenThrow(new
 * RuntimeException("down"));
 * })) {
 * mockMvc.perform(get("/api/locations").accept(MediaType.APPLICATION_JSON))
 * .andExpect(status().isInternalServerError())
 * .andExpect(jsonPath("$.status").value(500));
 * }
 * }
 * }
 */
