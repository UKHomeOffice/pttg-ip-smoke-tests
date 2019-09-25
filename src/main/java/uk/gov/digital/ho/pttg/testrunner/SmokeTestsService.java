package uk.gov.digital.ho.pttg.testrunner;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import uk.gov.digital.ho.pttg.api.SmokeTestsResult;
import uk.gov.digital.ho.pttg.testrunner.domain.Applicant;
import uk.gov.digital.ho.pttg.testrunner.domain.FinancialStatusRequest;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class SmokeTestsService {

    private static final String TEST_NINO = "QQ123456C";
    private final IpsClient ipsClient;
    private final Clock clock;
    private final ComponentHeaderChecker componentHeaderChecker;

    public SmokeTestsResult runSmokeTests() {
        try {
            ipsClient.sendFinancialStatusRequest(someRequest());
            return failure("Did not expect 200 OK response from IPS");
        } catch (HttpStatusCodeException e) {
            log.info("HttpStatusCodeException: {}", e.toString(), e);
            log.info("Headers: {}", e.getResponseHeaders());
            return isExpectedComponentTrace(e.getResponseHeaders());
        }
    }

    private SmokeTestsResult isExpectedComponentTrace(HttpHeaders headers) {
        if (headers == null) {
            return failure("No headers");
        }

        List<String> componentTraceHeaders = headers.get("x-component-trace");
        if (componentHeaderChecker.checkAllComponentsPresent(componentTraceHeaders)) {
            return SmokeTestsResult.SUCCESS;
        }
        return failure("Components missing from trace");
    }

    private FinancialStatusRequest someRequest() {
        Applicant someApplicant = new Applicant("smoke", "tests", LocalDate.now(clock), TEST_NINO);
        return new FinancialStatusRequest(Collections.singletonList(someApplicant), LocalDate.now(clock), 0);
    }

    private SmokeTestsResult failure(String reason) {
        return new SmokeTestsResult(false, reason);
    }
}
