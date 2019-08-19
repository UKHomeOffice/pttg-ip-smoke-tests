package uk.gov.digital.ho.pttg.application;

public class TestFailureException extends RuntimeException {
    public TestFailureException(String message) {
        super(message);
    }
}
