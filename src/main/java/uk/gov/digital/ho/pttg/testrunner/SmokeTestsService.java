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

    private static final String TEST_NINO = "AA000000A";
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
        if (!isHttpNotFound(e.getStatusCode())) {
            return false;
        }

        String errorResponse = e.getResponseBodyAsString();

        try {
            boolean hasNotMatchedCode = readJsonPath(errorResponse, "$.status.code").equals("0009");
            boolean containsNino = readJsonPath(errorResponse, "$.status.message").contains(getUnredactedNinoPart(TEST_NINO));
            return hasNotMatchedCode && containsNino;
        } catch (PathNotFoundException ignored) {
            return false;
        }
    }

    private String getUnredactedNinoPart(String nino) {
        return nino.substring(0, 5);
    }

    private String readJsonPath(String json, String path) {
        return JsonPath.read(json, path);
    }

    private boolean isHttpNotFound(HttpStatus httpStatus) {
        return httpStatus.equals(HttpStatus.NOT_FOUND);
    }

    private FinancialStatusRequest someRequest() {
        Applicant someApplicant = new Applicant("smoke", "tests", LocalDate.now(clock), TEST_NINO);
        return new FinancialStatusRequest(Collections.singletonList(someApplicant), LocalDate.now(clock), 0);
    }
}
