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
        HttpServerErrorException exceptionWithoutHeaders = exceptionWithHeaders(null);
        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(exceptionWithoutHeaders);

        assertThat(service.runSmokeTests()).isEqualTo(new SmokeTestsResult(false, "No headers"));
    }

    @Test
    public void runSmokeTests_noTraceHeader_passNullToChecker() {
        HttpServerErrorException exceptionWithoutTraceHeader = exceptionWithHeaders(new HttpHeaders());
        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(exceptionWithoutTraceHeader);

        service.runSmokeTests();
        then(mockComponentHeaderChecker).should().checkAllComponentsPresent(null);
    }

    @Test
    public void runSmokeTests_traceHeader_passHeaderToChecker() {
        String someComponentTrace = "pttg-ip-api,pttg-ip-audit,pttg-ip-hmrc,pttg-ip-audit,HMRC";

        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(exceptionWithTrace(someComponentTrace));

        service.runSmokeTests();

        then(mockComponentHeaderChecker).should().checkAllComponentsPresent(singletonList(someComponentTrace));
    }

    @Test
    public void runSmokeTests_failureFromChecker_returnFailure() {
        String anyComponentTrace = "pttg-ip-api,pttg-ip-audit,pttg-ip-hmrc,pttg-ip-audit,HMRC";

        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(exceptionWithTrace(anyComponentTrace));
        given(mockComponentHeaderChecker.checkAllComponentsPresent(anyList())).willReturn(false);

        assertThat(service.runSmokeTests()).isEqualTo(new SmokeTestsResult(false, "Components missing from trace"));
    }

    @Test
    public void runSmokeTests_successFromChecker_returnSuccess() {
        String anyComponentTrace = "pttg-ip-api,pttg-ip-audit,pttg-ip-hmrc,pttg-ip-audit,HMRC";

        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(exceptionWithTrace(anyComponentTrace));
        given(mockComponentHeaderChecker.checkAllComponentsPresent(anyList())).willReturn(true);

        assertThat(service.runSmokeTests()).isEqualTo(SmokeTestsResult.SUCCESS);
    }

    private HttpServerErrorException exceptionWithTrace(String componentTrace) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-component-trace", componentTrace);
        return exceptionWithHeaders(headers);
    }

    private HttpServerErrorException exceptionWithHeaders(HttpHeaders headers) {
        return new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "any status text", headers, null, null);
    }
}