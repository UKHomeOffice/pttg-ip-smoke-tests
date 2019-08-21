package uk.gov.digital.ho.pttg.testrunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.testrunner.domain.FinancialStatusRequest;

import java.time.LocalDate;
import java.util.Base64;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@RunWith(MockitoJUnitRunner.class)
public class IpsClientTest {

    private static final String SOME_IPS_ENDPOINT = "http://someurl/incomeproving/v3/individual/financialstatus";
    private static final String SOME_BASIC_AUTH = "someuser:somepass";
    private static final FinancialStatusRequest ANY_REQUEST = new FinancialStatusRequest(Collections.emptyList(), LocalDate.now(), 0);
    @Mock
    private RestTemplate mockRestTemplate;

    private IpsClient ipsClient;

    private ArgumentCaptor<HttpEntity> httpEntityCaptor;

    @Before
    public void setUp() {
        ipsClient = new IpsClient(SOME_IPS_ENDPOINT, SOME_BASIC_AUTH, mockRestTemplate);
        httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
    }

    @Test
    public void sendFinancialStatusRequest_givenRequest_passedToRestTemplate() {
        FinancialStatusRequest someRequest = new FinancialStatusRequest(Collections.emptyList(), LocalDate.now(), 0);
        ipsClient.sendFinancialStatusRequest(someRequest);

        then(mockRestTemplate).should()
                              .exchange(eq(SOME_IPS_ENDPOINT), eq(POST), httpEntityCaptor.capture(), eq(Void.class));
        assertThat((FinancialStatusRequest) httpEntityCaptor.getValue().getBody()).isEqualTo(someRequest);
    }

    @Test
    public void sendFinancialStatusRequest_anyRequest_contentTypeJson() {
        ipsClient.sendFinancialStatusRequest(ANY_REQUEST);

        then(mockRestTemplate).should()
                              .exchange(eq(SOME_IPS_ENDPOINT), eq(POST), httpEntityCaptor.capture(), eq(Void.class));
        HttpHeaders headers = httpEntityCaptor.getValue().getHeaders();
        assertThat(headers.getContentType()).isEqualTo(APPLICATION_JSON);
    }

    @Test
    public void sendFinancialStatusRequest_anyRequest_includeBasicAuthHeader() {
        ipsClient.sendFinancialStatusRequest(ANY_REQUEST);

        then(mockRestTemplate).should()
                              .exchange(eq(SOME_IPS_ENDPOINT), eq(POST), httpEntityCaptor.capture(), eq(Void.class));
        HttpHeaders headers = httpEntityCaptor.getValue().getHeaders();
        assertThat(headers.get("Authorization").get(0)).isEqualTo("Basic " + Base64.getEncoder().encodeToString(SOME_BASIC_AUTH.getBytes()));
    }

    @Test
    public void sendFinancialStatusRequest_anyRequest_includeCorrelationId() {
        ipsClient.sendFinancialStatusRequest(ANY_REQUEST);

        then(mockRestTemplate).should()
                              .exchange(eq(SOME_IPS_ENDPOINT), eq(POST), httpEntityCaptor.capture(), eq(Void.class));
        HttpHeaders headers = httpEntityCaptor.getValue().getHeaders();
        String correlationId = headers.get("x-correlation-id").get(0);
        assertThatCode(() -> UUID.fromString(correlationId)).doesNotThrowAnyException();
    }

    @Test
    public void sendFinancialStatusRequest_anyRequest_includeUserId() {
        ipsClient.sendFinancialStatusRequest(ANY_REQUEST);

        then(mockRestTemplate).should()
                              .exchange(eq(SOME_IPS_ENDPOINT), eq(POST), httpEntityCaptor.capture(), eq(Void.class));
        HttpHeaders headers = httpEntityCaptor.getValue().getHeaders();
        String userId = headers.get("x-auth-userid").get(0);
        assertThat(userId).isEqualTo("smoke-tests");
    }

    @Test
    public void sendFinancialStatusRequest_responseFromIps_returned() {
        ResponseEntity<Void> expectedResponse = new ResponseEntity<>(HttpStatus.OK);
        given(mockRestTemplate.exchange(eq(SOME_IPS_ENDPOINT), eq(POST), any(HttpEntity.class), eq(Void.class)))
                .willReturn(expectedResponse);

        ResponseEntity<Void> actualResponse = ipsClient.sendFinancialStatusRequest(ANY_REQUEST);
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }
}