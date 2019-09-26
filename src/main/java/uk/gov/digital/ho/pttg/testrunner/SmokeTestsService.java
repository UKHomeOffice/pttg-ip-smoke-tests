package uk.gov.digital.ho.pttg.testrunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import uk.gov.digital.ho.pttg.api.SmokeTestsResult;
import uk.gov.digital.ho.pttg.testrunner.domain.Applicant;
import uk.gov.digital.ho.pttg.testrunner.domain.BaseResponse;
import uk.gov.digital.ho.pttg.testrunner.domain.FinancialStatusRequest;
import uk.gov.digital.ho.pttg.testrunner.domain.ResponseStatus;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;

@Component
@AllArgsConstructor
public class SmokeTestsService {

    private static final String TEST_NINO = "QQ123456C";
    private static final String EXPECTED_RESPONSE_MESSAGE = String.format("Resource not found: %s****", TEST_NINO.substring(0, 5));

    private final IpsClient ipsClient;
    private final Clock clock;
    private final ObjectMapper objectMapper;

    public SmokeTestsResult runSmokeTests() {
        try {
            ipsClient.sendFinancialStatusRequest(someRequest());
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                String responseBody = e.getResponseBodyAsString();
                if (isExpectedResponse(responseBody)) {
                    return SmokeTestsResult.SUCCESS;
                }
            }
            return failure("pttg-ip-api error");
        }
        return failure("Did not expect 200 OK response from IPS");
    }

    private FinancialStatusRequest someRequest() {
        Applicant someApplicant = new Applicant("smoke", "tests", LocalDate.now(clock), TEST_NINO);
        return new FinancialStatusRequest(Collections.singletonList(someApplicant), LocalDate.now(clock), 0);
    }

    private SmokeTestsResult failure(String reason) {
        return new SmokeTestsResult(false, reason);
    }

    private boolean isExpectedResponse(String responseBody) {
        try {
            ResponseStatus responseStatus = objectMapper.readValue(responseBody, BaseResponse.class).status();
            return responseStatus.code().equals("0009") && responseStatus.message().equals(EXPECTED_RESPONSE_MESSAGE);
        } catch (IOException e) {
            return false;
        }
    }
}
