package uk.gov.digital.ho.pttg.testrunner;

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
import uk.gov.digital.ho.pttg.testrunner.domain.Applicant;
import uk.gov.digital.ho.pttg.testrunner.domain.FinancialStatusRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.HttpStatus.*;

@RunWith(MockitoJUnitRunner.class)
public class SmokeTestsServiceTest {

    @Mock
    private IpsClient mockIpsClient;

    private SmokeTestsService service;
    private ArgumentCaptor<FinancialStatusRequest> requestCaptor;
    private Clock fixedClock;

    @Before
    public void setUp() {
        LocalDate anyDate = LocalDate.of(2019, 8, 23);;
        Instant instant = Instant.from(anyDate.atStartOfDay().atZone(ZoneId.systemDefault()));
        fixedClock = Clock.fixed(instant, ZoneId.systemDefault());

        service = new SmokeTestsService(mockIpsClient, fixedClock);

        requestCaptor = ArgumentCaptor.forClass(FinancialStatusRequest.class);
    }

    @Test
    public void runSmokeTests_always_callIpsWithTestRequest() {
        service.runSmokeTests();

        then(mockIpsClient).should().sendFinancialStatusRequest(requestCaptor.capture());
        FinancialStatusRequest expectedRequest = new FinancialStatusRequest(
                Collections.singletonList(new Applicant("smoke", "tests", LocalDate.now(fixedClock), "AA000000A")),
                LocalDate.now(fixedClock),
                0);
        assertThat(requestCaptor.getValue()).isEqualTo(expectedRequest);
    }

    @Test
    public void runSmokeTests_financialStatusRequestSuccess_returnSuccess() {
        given(mockIpsClient.sendFinancialStatusRequest(any())).willReturn(new ResponseEntity<>(HttpStatus.OK));

        SmokeTestsResult testsResult = service.runSmokeTests();

        assertThat(testsResult).isEqualTo(SmokeTestsResult.SUCCESS);
    }

    @Test
    public void runSmokeTests_financialStatusRequestNoMatch_returnSuccess() {
        String notFoundMessage = "{\"status\":{\"code\":\"0009\",\"message\":\"Resource not found: AA000****\"}}";
        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(getHttpClientErrorException(NOT_FOUND, notFoundMessage));

        SmokeTestsResult testsResult = service.runSmokeTests();

        assertThat(testsResult).isEqualTo(SmokeTestsResult.SUCCESS);
    }

    @Test
    public void runSmokeTests_noHandlerFound_returnFailure() {
        String noHandlerFoundMessage = "{\"status\":{\"code\":\"0009\",\"message\":\"Resource not found: /smoketests\"}}";
        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(getHttpClientErrorException(NOT_FOUND, noHandlerFoundMessage));

        SmokeTestsResult testsResult = service.runSmokeTests();

        assertThat(testsResult).isEqualTo(new SmokeTestsResult(false, noHandlerFoundMessage));
    }

    @Test
    public void runSmokeTests_notFoundButWrongMessage_returnFailure() {
        String failureMessage = "some failure message";
        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(getHttpClientErrorException(NOT_FOUND, failureMessage));

        SmokeTestsResult testsResult = service.runSmokeTests();

        assertThat(testsResult).isEqualTo(new SmokeTestsResult(false, failureMessage));
    }

    @Test
    public void runSmokeTests_otherClientError_returnFailure() {
        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(getHttpClientErrorException(BAD_REQUEST, "some failure message"));

        SmokeTestsResult testsResult = service.runSmokeTests();

        assertThat(testsResult).isEqualTo(new SmokeTestsResult(false, "some failure message"));
    }

    @Test
    public void runSmokeTests_serverError_returnFailure() {
        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(getHttpServerErrorException(INTERNAL_SERVER_ERROR, "some failure message"));

        SmokeTestsResult testsResult = service.runSmokeTests();

        assertThat(testsResult).isEqualTo(new SmokeTestsResult(false, "some failure message"));
    }

    private HttpClientErrorException getHttpClientErrorException(HttpStatus httpStatus, String failureMessage) {
        return new HttpClientErrorException(httpStatus, "", failureMessage.getBytes(), null);
    }

    private HttpServerErrorException getHttpServerErrorException(HttpStatus httpStatus, String failureMessage) {
        return new HttpServerErrorException(httpStatus, "", failureMessage.getBytes(), null);
    }
}