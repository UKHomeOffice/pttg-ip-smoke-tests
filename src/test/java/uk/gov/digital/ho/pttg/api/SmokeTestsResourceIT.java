package uk.gov.digital.ho.pttg.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(SpringRunner.class)
@SpringBootTest(
        properties = {
                "ips.endpoint=http://ips.endpoint/incomeproving/v3/individual/financialstatus",
                "ips.basicauth=someuser:somepass",
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SmokeTestsResourceIT {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private TestRestTemplate testRestTemplate;

    private MockRestServiceServer mockIpsService;

    @Before
    public void setUp() {
        mockIpsService = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    public void runSmokeTests_testSuccess_returnSuccess() {
        String testSuccessBody = "{\"status\": {\"code\": \"0009\", \"message\": \"Resource not found: QQ123****\"}}";
        mockIpsService.expect(requestTo(containsString("/incomeproving/v3/individual/financialstatus")))
                      .andExpect(method(POST))
                      .andExpect(jsonPath("$.individuals[0].forename", equalTo("smoke")))
                      .andRespond(withStatus(HttpStatus.NOT_FOUND).body(testSuccessBody));

        ResponseEntity<Void> response = testRestTemplate.exchange("/smoketests", POST, new HttpEntity<>(""), Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void runSmokeTests_testFailure_returnFailure() {
        mockIpsService.expect(requestTo(containsString("/incomeproving/v3/individual/financialstatus")))
                      .andExpect(method(POST))
                      .andExpect(jsonPath("$.individuals[0].forename", equalTo("smoke")))
                      .andRespond(withServerError());

        ResponseEntity<String> response = testRestTemplate.exchange("/smoketests", POST, new HttpEntity<>(""), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).contains("pttg-ip-api error");
    }
}