package uk.gov.digital.ho.pttg.testrunner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.testrunner.domain.FinancialStatusRequest;

import java.util.Base64;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class IpsClient {

    private final String ipsEndpoint;
    private final String basicAuth;
    private RestTemplate restTemplate;

    public IpsClient(@Value("${ips.endpoint}") String ipsEndpoint, @Value("${ips.basicauth}") String basicAuth, RestTemplate restTemplate) {
        this.ipsEndpoint = ipsEndpoint;
        this.basicAuth = basicAuth;
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<Void> sendFinancialStatusRequest(FinancialStatusRequest financialStatusRequest) {
        return restTemplate.exchange(ipsEndpoint, POST, new HttpEntity<>(financialStatusRequest, getHeaders()), Void.class);
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.add(AUTHORIZATION, String.format("Basic %s", Base64.getEncoder().encodeToString(basicAuth.getBytes())));
        return headers;
    }
}

