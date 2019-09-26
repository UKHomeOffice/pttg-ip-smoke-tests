package uk.gov.digital.ho.pttg.testrunner;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.api.SmokeTestsResult;
import uk.gov.digital.ho.pttg.testrunner.domain.Applicant;
import uk.gov.digital.ho.pttg.testrunner.domain.FinancialStatusRequest;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;

@Component
@AllArgsConstructor
public class SmokeTestsService {

    private static final String TEST_NINO = "QQ123456C";
    private final IpsClient ipsClient;
    private final Clock clock;

    public SmokeTestsResult runSmokeTests() {
        ipsClient.sendFinancialStatusRequest(someRequest());
        return failure("Did not expect 200 OK response from IPS");
    }

    private FinancialStatusRequest someRequest() {
        Applicant someApplicant = new Applicant("smoke", "tests", LocalDate.now(clock), TEST_NINO);
        return new FinancialStatusRequest(Collections.singletonList(someApplicant), LocalDate.now(clock), 0);
    }

    private SmokeTestsResult failure(String reason) {
        return new SmokeTestsResult(false, reason);
    }
}
