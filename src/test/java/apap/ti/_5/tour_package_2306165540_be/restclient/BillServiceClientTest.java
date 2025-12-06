package apap.ti._5.tour_package_2306165540_be.restclient;

import apap.ti._5.tour_package_2306165540_be.model.Package;
import apap.ti._5.tour_package_2306165540_be.restclient.dto.BillCreateRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BillServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    private BillServiceClient client;

    @BeforeEach
    void setUp() {
        client = new BillServiceClient(restTemplate, "https://billing.example", "api-key", "/api/create");
    }

    @Test
    void createBillForPackage_callsRemoteServiceWithHeaders() {
        Package pkg = buildPackage(UUID.randomUUID().toString(), 10_000L);
        ResponseEntity<String> okResponse = new ResponseEntity<>("OK", HttpStatus.OK);
        ArgumentCaptor<HttpEntity<BillCreateRequestDTO>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        when(restTemplate.exchange(eq("https://billing.example/api/create"), eq(HttpMethod.POST), any(),
                eq(String.class)))
                .thenReturn(okResponse);

        client.createBillForPackage(pkg);

        verify(restTemplate).exchange(eq("https://billing.example/api/create"), eq(HttpMethod.POST), captor.capture(),
                eq(String.class));
        HttpHeaders headers = captor.getValue().getHeaders();
        assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(headers.getFirst("X-API-KEY")).isEqualTo("api-key");
        BillCreateRequestDTO body = captor.getValue().getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCustomerId().toString()).isEqualTo(pkg.getUserId());
        assertThat(body.getAmount()).isEqualTo(pkg.getPrice().doubleValue());
        assertThat(body.getServiceName()).isEqualTo("Tour Package");
    }

    @Test
    void createBillForPackage_throwsWhenConfigurationMissing() {
        BillServiceClient misconfigured = new BillServiceClient(restTemplate, "", "api-key", "/api/create");
        Package pkg = buildPackage(UUID.randomUUID().toString(), 5_000L);

        assertThatThrownBy(() -> misconfigured.createBillForPackage(pkg))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("base URL");
    }

    @Test
    void createBillForPackage_wrapsRestClientException() {
        Package pkg = buildPackage(UUID.randomUUID().toString(), 20_000L);
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenThrow(new RestClientException("boom"));

        assertThatThrownBy(() -> client.createBillForPackage(pkg))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to create bill");
    }

    @Test
    void createBillForPackage_rejectsInvalidOwnerId() {
        Package pkg = buildPackage("not-a-uuid", 10_000L);

        assertThatThrownBy(() -> client.createBillForPackage(pkg))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("valid UUID");
    }

    private Package buildPackage(String userId, Long price) {
        Package pkg = new Package();
        pkg.setId(UUID.randomUUID().toString());
        pkg.setUserId(userId);
        pkg.setPackageName("Holiday");
        pkg.setQuota(10);
        pkg.setPrice(price);
        pkg.setStatus("Pending");
        pkg.setStartDate(LocalDateTime.now());
        pkg.setEndDate(LocalDateTime.now().plusDays(2));
        return pkg;
    }
}
