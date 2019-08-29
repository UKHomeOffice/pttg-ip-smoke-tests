package uk.gov.digital.ho.pttg.testrunner;

import lombok.AllArgsConstructor;
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
public class SmokeTestsService {

    private static final String TEST_NINO = "QQ123456C";
    private final IpsClient ipsClient;
    private final Clock clock;

    public SmokeTestsResult runSmokeTests() {
        try {
            ipsClient.sendFinancialStatusRequest(someRequest());
            return new SmokeTestsResult(false, "Did not expect 200 OK response from IPS");
        } catch (HttpStatusCodeException e) {
            if (e.getResponseHeaders() == null) {
                return new SmokeTestsResult(false, "No x-component-trace header");
            }

            List<String> components = e.getResponseHeaders().get("x-component-trace");
            if (components == null) {
                return new SmokeTestsResult(false, "Null x-component-trace header");
            }
            if (components.isEmpty()) {
                return new SmokeTestsResult(false, "Empty x-component-trace header");
            }
            for (String component : components) {
                if (component.contains("pttg-ip-audit")) {
                    return SmokeTestsResult.SUCCESS;
                }
            }
            return new SmokeTestsResult(false, "Did not find all expected components in x-component-trace header");
        }
    }

    private FinancialStatusRequest someRequest() {
        Applicant someApplicant = new Applicant("smoke", "tests", LocalDate.now(clock), TEST_NINO);
        return new FinancialStatusRequest(Collections.singletonList(someApplicant), LocalDate.now(clock), 0);
    }
}
