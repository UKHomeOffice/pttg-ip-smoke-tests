package uk.gov.digital.ho.pttg.testrunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
                Collections.singletonList(new Applicant("smoke", "tests", LocalDate.now(fixedClock), "QQ123456C")),
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
    public void runSmokeTests_errorWithoutComponentTrace_returnFailure() {
        HttpServerErrorException errorWithoutComponentTrace = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "any text", new HttpHeaders(), null, null);
        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(errorWithoutComponentTrace);

        SmokeTestsResult testsResult = service.runSmokeTests();
        assertThat(testsResult.success()).isFalse();
    }

    @Test
    public void runSmokeTests_errorWithoutComponentTrace_giveReason() {
        HttpServerErrorException errorWithoutComponentTrace = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "any text", new HttpHeaders(), null, null);
        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(errorWithoutComponentTrace);

        SmokeTestsResult testsResult = service.runSmokeTests();
        assertThat(testsResult.reason()).contains("x-component-trace");
    }

    @Test
    public void runSmokeTests_errorWithInvalidComponentTrace_returnFailure() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-component-trace", "pttg-ip-api");
        HttpServerErrorException errorWithoutComponentTrace = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "any text", headers, null, null);
        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(errorWithoutComponentTrace);

        SmokeTestsResult testsResult = service.runSmokeTests();
        assertThat(testsResult.success()).isFalse();
    }

    @Test
    public void runSmokeTests_errorWithInvalidComponentTrace_giveReason() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-component-trace", "pttg-ip-api");
        HttpServerErrorException errorWithoutComponentTrace = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "any text", headers, null, null);
        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(errorWithoutComponentTrace);

        SmokeTestsResult testsResult = service.runSmokeTests();
        assertThat(testsResult.reason()).contains("x-component-trace");
    }

    @Test
    public void runSmokeTests_errorWithExpectedComponentTrace_returnSuccess() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-component-trace", "pttg-ip-api,pttg-ip-audit,pttg-ip-hmrc,pttg-ip-hmrc-access-code,HMRC");
        HttpServerErrorException errorWithoutComponentTrace = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "any text", headers, null, null);
        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(errorWithoutComponentTrace);

        assertThat(service.runSmokeTests()).isEqualTo(SmokeTestsResult.SUCCESS);
    }
}