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

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@RunWith(MockitoJUnitRunner.class)
public class SmokeTestsServiceTest {

    @Mock
    private IpsClient mockIpsClient;
    @Mock
    private ComponentHeaderChecker mockComponentHeaderChecker;

    private SmokeTestsService service;
    private ArgumentCaptor<FinancialStatusRequest> requestCaptor;
    private Clock fixedClock;

    @Before
    public void setUp() {
        LocalDate anyDate = LocalDate.of(2019, 8, 23);;
        Instant instant = Instant.from(anyDate.atStartOfDay().atZone(ZoneId.systemDefault()));
        fixedClock = Clock.fixed(instant, ZoneId.systemDefault());

        service = new SmokeTestsService(mockIpsClient, fixedClock, mockComponentHeaderChecker);

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
    public void runSmokeTests_errorNoHeaders_returnFailure() {
        HttpServerErrorException exceptionWithoutHeaders = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "any status text", null, null, null);
        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(exceptionWithoutHeaders);

        assertThat(service.runSmokeTests()).isEqualTo(new SmokeTestsResult(false, "No headers"));
    }

    @Test
    public void runSmokeTests_noTraceHeader_passNullToChecker() {
        HttpServerErrorException exceptionWithoutTraceHeader = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "any status text", new HttpHeaders(), null, null);
        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(exceptionWithoutTraceHeader);

        service.runSmokeTests();
        then(mockComponentHeaderChecker).should().checkAllComponentsPresent(null);
    }

    @Test
    public void runSmokeTests_traceHeader_passHeaderToChecker() {
        HttpHeaders componentTraceHeader = new HttpHeaders();
        String someComponentTrace = "pttg-ip-api,pttg-ip-audit,pttg-ip-hmrc,pttg-ip-audit,HMRC";
        componentTraceHeader.add("x-component-trace", someComponentTrace);

        HttpServerErrorException exceptionWithTraceHeader = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "any status text", componentTraceHeader, null, null);
        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(exceptionWithTraceHeader);

        service.runSmokeTests();

        then(mockComponentHeaderChecker).should().checkAllComponentsPresent(singletonList(someComponentTrace));
    }

    @Test
    public void runSmokeTests_failureFromChecker_returnFailure() {
        HttpHeaders anyComponentHeaders = new HttpHeaders();
        String anyComponentTrace = "pttg-ip-api,pttg-ip-audit,pttg-ip-hmrc,pttg-ip-audit,HMRC";
        anyComponentHeaders.add("x-component-trace", anyComponentTrace);

        HttpServerErrorException anyExceptionWithTraceHeader = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "any status text", anyComponentHeaders, null, null);
        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(anyExceptionWithTraceHeader);
        given(mockComponentHeaderChecker.checkAllComponentsPresent(anyList())).willReturn(false);

        assertThat(service.runSmokeTests()).isEqualTo(new SmokeTestsResult(false, "Components missing from trace"));
    }

    @Test
    public void runSmokeTests_successFromChecker_returnSuccess() {
        HttpHeaders anyComponentHeaders = new HttpHeaders();
        String anyComponentTrace = "pttg-ip-api,pttg-ip-audit,pttg-ip-hmrc,pttg-ip-audit,HMRC";
        anyComponentHeaders.add("x-component-trace", anyComponentTrace);

        HttpServerErrorException anyExceptionWithTraceHeader = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "any status text", anyComponentHeaders, null, null);
        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(anyExceptionWithTraceHeader);
        given(mockComponentHeaderChecker.checkAllComponentsPresent(anyList())).willReturn(true);

        assertThat(service.runSmokeTests()).isEqualTo(SmokeTestsResult.SUCCESS);
    }
}