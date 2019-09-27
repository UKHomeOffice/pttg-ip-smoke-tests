package uk.gov.digital.ho.pttg.testrunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.digital.ho.pttg.api.SmokeTestsResult;
import uk.gov.digital.ho.pttg.application.ServiceConfiguration;
import uk.gov.digital.ho.pttg.testrunner.domain.Applicant;
import uk.gov.digital.ho.pttg.testrunner.domain.FinancialStatusRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RunWith(MockitoJUnitRunner.class)
public class SmokeTestsServiceTest {

    @Mock
    private IpsClient mockIpsClient;
    @Mock

    private SmokeTestsService service;
    private ArgumentCaptor<FinancialStatusRequest> requestCaptor;
    private Clock fixedClock;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        LocalDate anyDate = LocalDate.of(2019, 8, 23);;
        Instant instant = Instant.from(anyDate.atStartOfDay().atZone(ZoneId.systemDefault()));
        fixedClock = Clock.fixed(instant, ZoneId.systemDefault());
        objectMapper = new ServiceConfiguration().createObjectMapper();

        service = new SmokeTestsService(mockIpsClient, fixedClock, objectMapper);

        requestCaptor = ArgumentCaptor.forClass(FinancialStatusRequest.class);
    }

    @Test
    public void runSmokeTests_always_callIpsWithTestRequest() {
        service.runSmokeTests();

        then(mockIpsClient).should().sendFinancialStatusRequest(requestCaptor.capture());
        FinancialStatusRequest expectedRequest = new FinancialStatusRequest(
                singletonList(new Applicant("smoke", "tests", LocalDate.now(fixedClock), "QQ123456C")),
                LocalDate.now(fixedClock),
                0);
        assertThat(requestCaptor.getValue()).isEqualTo(expectedRequest);
    }

    @Test
    public void runSmokeTests_financialStatusRequestSuccess_returnFailure() {
        given(mockIpsClient.sendFinancialStatusRequest(any())).willReturn(new ResponseEntity<>(HttpStatus.OK));

        assertThat(service.runSmokeTests()).isEqualTo(new SmokeTestsResult(false, "Did not expect 200 OK response from IPS"));
    }

    @Test
    public void runSmokeTests_financialStatusRequestServerError_returnFailure() {
        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(new HttpServerErrorException(INTERNAL_SERVER_ERROR));

        SmokeTestsResult result = service.runSmokeTests();
        assertThat(result.success()).isFalse();
    }

    @Test
    public void runSmokeTests_notFoundResponseNoBody_returnFailure() {
        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(new HttpClientErrorException(NOT_FOUND));

        SmokeTestsResult result = service.runSmokeTests();
        assertThat(result.success()).isFalse();
    }

    @Test
    public void runSmokeTests_notFoundExpectedResponse_returnSuccess() {
        String expectedResponseBody = "{\"status\": {\"code\": \"0009\", \"message\": \"Resource not found: QQ123****\"}}";
        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(new HttpClientErrorException(NOT_FOUND, "any status text", expectedResponseBody.getBytes(), null));

        assertThat(service.runSmokeTests()).isEqualTo(SmokeTestsResult.SUCCESS);
    }

    @Test
    public void runSmokeTests_notFoundWrongCode_returnFailure() {
        String expectedResponseBody = "{\"status\": {\"code\": \"0001\", \"message\": \"Resource not found: QQ123****\"}}";
        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(new HttpClientErrorException(NOT_FOUND, "any status text", expectedResponseBody.getBytes(), null));

        assertThat(service.runSmokeTests().success()).isFalse();
    }

    @Test
    public void runSmokeTests_notFoundWrongMessage_returnFailure() {
        String expectedResponseBody = "{\"status\": {\"code\": \"0009\", \"message\": \"Some wrong message: QQ123****\"}}";
        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(new HttpClientErrorException(NOT_FOUND, "any status text", expectedResponseBody.getBytes(), null));

        assertThat(service.runSmokeTests().success()).isFalse();
    }

    @Test
    public void runSmokeTests_notFoundWrongNino_returnFailure() {
        String expectedResponseBody = "{\"status\": \"{\"code\": \"0009\", \"message\": \"Resource not found: AA000****\"}\"}";
        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(new HttpClientErrorException(NOT_FOUND, "any status text", expectedResponseBody.getBytes(), null));

        assertThat(service.runSmokeTests().success()).isFalse();
    }
}