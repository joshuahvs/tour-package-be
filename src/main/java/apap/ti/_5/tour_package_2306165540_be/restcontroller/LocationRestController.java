package apap.ti._5.tour_package_2306165540_be.restcontroller;

import apap.ti._5.tour_package_2306165540_be.restdto.response.BaseResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.LocationResponseDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationRestController {

    private static final String WILAYAH_API_URL = "https://wilayah.id/api/provinces.json";

    @GetMapping
    public ResponseEntity<BaseResponseDTO<List<LocationResponseDTO>>> getLocations() {
        try {
            // Call external API
            RestTemplate restTemplate = new RestTemplate();
            String jsonResponse = restTemplate.getForObject(WILAYAH_API_URL, String.class);

            // Parse JSON response
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode dataNode = root.get("data");

            List<LocationResponseDTO> locations = new ArrayList<>();

            // Convert to LocationResponseDTO
            if (dataNode != null && dataNode.isArray()) {
                for (JsonNode provinceNode : dataNode) {
                    String code = provinceNode.get("code").asText();
                    String name = provinceNode.get("name").asText();
                    locations.add(new LocationResponseDTO(code, name));
                }
            }

            BaseResponseDTO<List<LocationResponseDTO>> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Successfully retrieved locations");
            response.setData(locations);
            response.setTimestamp(new Date());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            BaseResponseDTO<List<LocationResponseDTO>> response = new BaseResponseDTO<>();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Failed to retrieve locations: " + e.getMessage());
            response.setData(null);
            response.setTimestamp(new Date());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
