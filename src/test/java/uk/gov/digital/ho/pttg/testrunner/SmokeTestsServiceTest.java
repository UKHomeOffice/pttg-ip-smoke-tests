package uk.gov.digital.ho.pttg.testrunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.digital.ho.pttg.api.SmokeTestsResult;
import uk.gov.digital.ho.pttg.testrunner.domain.Applicant;
import uk.gov.digital.ho.pttg.testrunner.domain.FinancialStatusRequest;

import java.time.LocalDate;
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

    @Before
    public void setUp() {
        service = new SmokeTestsService(mockIpsClient);
        requestCaptor = ArgumentCaptor.forClass(FinancialStatusRequest.class);
    }

    @Test
    public void runSmokeTests_always_callIpsWithTestRequest() {
        service.runSmokeTests();

        then(mockIpsClient).should().sendFinancialStatusRequest(requestCaptor.capture());
        FinancialStatusRequest expectedRequest = new FinancialStatusRequest(
                Collections.singletonList(new Applicant("smoke", "tests", LocalDate.now(), "AA000000A")),
                LocalDate.now(),
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
    public void runSmokeTests_RestClientResponseException_returnFailure() {
        String failureMessage = "some failure message";
        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(getRestClientResponseException(failureMessage));

        SmokeTestsResult testsResult = service.runSmokeTests();

        assertThat(testsResult).isEqualTo(new SmokeTestsResult(false, failureMessage));
    }

    @Test
    public void runSmokeTests_RestClientException_returnFailure() {
        given(mockIpsClient.sendFinancialStatusRequest(any())).willThrow(new RestClientException("some failure message"));

        SmokeTestsResult testsResult = service.runSmokeTests();

        assertThat(testsResult).isEqualTo(new SmokeTestsResult(false, "some failure message"));
    }

    private RestClientResponseException getRestClientResponseException(String failureMessage) {
        return new RestClientResponseException("Internal Server Error", 500, "Internal Server Error", null, failureMessage.getBytes(), null);
    }
}