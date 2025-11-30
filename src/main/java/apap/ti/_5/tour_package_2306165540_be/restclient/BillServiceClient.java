package apap.ti._5.tour_package_2306165540_be.restclient;

import apap.ti._5.tour_package_2306165540_be.model.Package;
import apap.ti._5.tour_package_2306165540_be.restclient.dto.BillCreateRequestDTO;
import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class BillServiceClient {

    private static final Logger log = LoggerFactory.getLogger(BillServiceClient.class);
    private static final String DEFAULT_SERVICE_NAME = "Tour Package";

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;
    private final String createPath;

    public BillServiceClient(
            RestTemplate restTemplate,
            @Value("${bill.service.base-url:}") String baseUrl,
            @Value("${bill.service.api-key:}") String apiKey,
            @Value("${bill.service.create-path:/api/bill/create}") String createPath) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.createPath = createPath;
    }

    public void createBillForPackage(Package packageEntity) {
        validateConfiguration();
        BillCreateRequestDTO payload = buildRequest(packageEntity);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-KEY", apiKey);

        HttpEntity<BillCreateRequestDTO> entity = new HttpEntity<>(payload, headers);
        String targetUrl = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path(createPath)
                .toUriString();

        try {
            ResponseEntity<String> response = restTemplate.exchange(targetUrl, HttpMethod.POST, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Bill service returned status " + response.getStatusCode());
            }
            log.info("Successfully created bill for package {} via bill service", packageEntity.getId());
        } catch (RestClientException ex) {
            throw new RuntimeException("Failed to create bill via bill service: " + ex.getMessage(), ex);
        }
    }

    private BillCreateRequestDTO buildRequest(Package packageEntity) {
        UUID customerId;
        try {
            customerId = UUID.fromString(packageEntity.getUserId());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Package owner id is not a valid UUID; cannot create bill", ex);
        }

        if (packageEntity.getPrice() == null || packageEntity.getPrice() <= 0) {
            throw new RuntimeException("Package price must be greater than zero before creating bill");
        }

        LocalDateTime dueDate = packageEntity.getEndDate();
        LocalDateTime now = LocalDateTime.now();
        if (dueDate == null || dueDate.isBefore(now)) {
            dueDate = now.plusDays(1);
        }

        return BillCreateRequestDTO.builder()
                .customerId(customerId)
                .serviceName(DEFAULT_SERVICE_NAME)
                .serviceReferenceId(packageEntity.getId())
                .description("Payment for package " + packageEntity.getPackageName())
                .amount(Double.valueOf(packageEntity.getPrice()))
                .dueDate(dueDate)
                .build();
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(baseUrl)) {
            throw new RuntimeException("Bill service base URL is not configured");
        }
        if (!StringUtils.hasText(apiKey)) {
            throw new RuntimeException("Bill service API key is not configured");
        }
    }
}
