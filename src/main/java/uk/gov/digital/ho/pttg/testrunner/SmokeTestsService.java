package uk.gov.digital.ho.pttg.testrunner;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import uk.gov.digital.ho.pttg.api.SmokeTestsResult;
import uk.gov.digital.ho.pttg.testrunner.domain.Applicant;
import uk.gov.digital.ho.pttg.testrunner.domain.FinancialStatusRequest;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;

@Component
@AllArgsConstructor
public class SmokeTestsService {

    private final IpsClient ipsClient;
    private final Clock clock;

    public SmokeTestsResult runSmokeTests() {
        try {
            ipsClient.sendFinancialStatusRequest(someRequest());
            return SmokeTestsResult.SUCCESS;
        } catch (HttpStatusCodeException e) {
            if (isIdentityUnmatched(e)) {
                return SmokeTestsResult.SUCCESS;
            }
            return new SmokeTestsResult(false, e.getResponseBodyAsString());
        } catch (RestClientException e) {
            return new SmokeTestsResult(false, e.getMessage());
        }
    }

    private boolean isIdentityUnmatched(HttpStatusCodeException e) {
        try {
            boolean hasNotMatchedCode = JsonPath.read(e.getResponseBodyAsString(), "$.status.code").equals("0009");
            return e.getStatusCode().equals(HttpStatus.NOT_FOUND) && hasNotMatchedCode;
        } catch (PathNotFoundException ignored) {
            return false;
        }
    }

    private FinancialStatusRequest someRequest() {
        Applicant someApplicant = new Applicant("smoke", "tests", LocalDate.now(clock), "AA000000A");
        return new FinancialStatusRequest(Collections.singletonList(someApplicant), LocalDate.now(clock), 0);
    }
}
