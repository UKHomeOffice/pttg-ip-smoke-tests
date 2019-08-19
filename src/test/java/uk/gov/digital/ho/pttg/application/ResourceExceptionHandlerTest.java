package uk.gov.digital.ho.pttg.application;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class ResourceExceptionHandlerTest {

    private static final TestFailureException ANY_TEST_FAILURE_EXCEPTION = new TestFailureException("any message");

    private ResourceExceptionHandler exceptionHandler = new ResourceExceptionHandler();

    @Test
    public void handle_TestFailureException_internalServerError() {
        ResponseEntity responseEntity = exceptionHandler.handle(ANY_TEST_FAILURE_EXCEPTION);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void handle_TestFailureException_includeExceptionMessage() {
        String expectedMessage = "some error";
        ResponseEntity responseEntity = exceptionHandler.handle(new TestFailureException(expectedMessage));

        assertThat(responseEntity.getBody()).isEqualTo(expectedMessage);
    }

    @Test
    public void handle_TestFailureException_expectedHeaders() {
        ResponseEntity responseEntity = exceptionHandler.handle(ANY_TEST_FAILURE_EXCEPTION);

        assertThat(responseEntity.getHeaders()).containsEntry(CONTENT_TYPE, Collections.singletonList(APPLICATION_JSON_VALUE));
    }
}