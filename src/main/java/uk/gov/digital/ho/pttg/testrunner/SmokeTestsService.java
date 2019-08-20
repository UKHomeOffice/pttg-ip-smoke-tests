package uk.gov.digital.ho.pttg.testrunner;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.digital.ho.pttg.api.SmokeTestsResult;
import uk.gov.digital.ho.pttg.testrunner.domain.Applicant;
import uk.gov.digital.ho.pttg.testrunner.domain.FinancialStatusRequest;

import java.time.LocalDate;
import java.util.Collections;

@Component
public class SmokeTestsService {

    private final IpsClient ipsClient;

    public SmokeTestsService(IpsClient ipsClient) {
        this.ipsClient = ipsClient;
    }

    public SmokeTestsResult runSmokeTests() {
        try {
            ipsClient.sendFinancialStatusRequest(someRequest());
            return SmokeTestsResult.SUCCESS;
        } catch (RestClientResponseException e) {
            return new SmokeTestsResult(false, e.getResponseBodyAsString());
        } catch (RestClientException e) {
            return new SmokeTestsResult(false, e.getMessage());
        }
    }

    private FinancialStatusRequest someRequest() {
        Applicant someApplicant = new Applicant("smoke", "tests", LocalDate.now(), "AA000000A");
        return new FinancialStatusRequest(Collections.singletonList(someApplicant), LocalDate.now(), 0);
    }
}
