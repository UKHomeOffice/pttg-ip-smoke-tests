package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.pttg.testrunner.SmokeTestsService;

@RestController
@Slf4j
public class SmokeTestsResource {

    private SmokeTestsService smokeTestsService;

    public SmokeTestsResource(SmokeTestsService smokeTestsService) {
        this.smokeTestsService = smokeTestsService;
    }

    @PostMapping
    public void runSmokeTests() {
        log.info("Smoke Tests triggered");
        SmokeTestsResult smokeTestsResult = smokeTestsService.runSmokeTests();

        logOutcome(smokeTestsResult);

        if (!smokeTestsResult.success()) {
            throw new TestFailureException(smokeTestsResult.reason());
        }
    }

    private void logOutcome(SmokeTestsResult result) {
        if (result.success()) {
            log.info("Smoke Tests successful");
        } else {
            String failureMessage = "Smoke Tests failed";
            failureMessage = addReason(result.reason(), failureMessage);
            log.info(failureMessage);
        }
    }

    private String addReason(String reason, String failureMessage) {
        if (reason != null) {
            failureMessage += ": " + reason;
        }
        return failureMessage;
    }
}
