package uk.gov.digital.ho.pttg.application;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ControllerAdvice
public class ResourceExceptionHandler {

    @ExceptionHandler
    public ResponseEntity handle(TestFailureException testFailureException) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(CONTENT_TYPE, APPLICATION_JSON_VALUE);

        return new ResponseEntity<>(testFailureException.getMessage(), headers, INTERNAL_SERVER_ERROR);
    }
}
