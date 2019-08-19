package uk.gov.digital.ho.pttg.api;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import uk.gov.digital.ho.pttg.application.TestFailureException;
import uk.gov.digital.ho.pttg.testrunner.SmokeTestsService;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class SmokeTestsResourceTest {

    private static final SmokeTestsResult ANY_RESULT = new SmokeTestsResult(true);

    @Mock
    private Appender<ILoggingEvent> mockAppender;
    @Mock
    private SmokeTestsService mockService;

    private SmokeTestsResource resource;
    private ArgumentCaptor<ILoggingEvent> logCaptor;

    @Before
    public void setUp() {
        Logger logger = (Logger) LoggerFactory.getLogger(SmokeTestsResource.class);
        logger.addAppender(mockAppender);

        resource = new SmokeTestsResource(mockService);
        logCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
    }

    @Test
    public void runSmokeTest_always_callService() {
        given(mockService.runSmokeTests()).willReturn(ANY_RESULT);
        resource.runSmokeTests();
        then(mockService).should().runSmokeTests();
    }

    @Test
    public void runSmokeTest_successFromService_noException() {
        SmokeTestsResult expectedResult = SmokeTestsResult.SUCCESS;
        given(mockService.runSmokeTests()).willReturn(expectedResult);

        resource.runSmokeTests();
    }

    @Test
    public void runSmokeTest_failureFromService_throwException() {
        SmokeTestsResult failure = new SmokeTestsResult(false);
        given(mockService.runSmokeTests()).willReturn(failure);

        assertThatThrownBy(() -> resource.runSmokeTests())
                .isInstanceOf(TestFailureException.class);
    }

    @Test
    public void runSmokeTest_failureFromServiceWithReason_throwExceptionWithReason() {
        SmokeTestsResult failure = new SmokeTestsResult(false, "some reason");
        given(mockService.runSmokeTests()).willReturn(failure);

        assertThatThrownBy(() -> resource.runSmokeTests())
                .hasMessage("some reason");
    }

    @Test
    public void runSmokeTests_always_logEntry() {
        given(mockService.runSmokeTests()).willReturn(ANY_RESULT);
        resource.runSmokeTests();

        then(mockAppender).should(atLeastOnce()).doAppend(logCaptor.capture());
        assertLogWithMessageContaining(logCaptor.getAllValues(), "Smoke Tests", "triggered");
    }

    @Test
    public void runSmokeTest_success_logSuccess() {
        given(mockService.runSmokeTests()).willReturn(SmokeTestsResult.SUCCESS);

        resource.runSmokeTests();
        then(mockAppender).should(times(2)).doAppend(logCaptor.capture());

        assertLogWithMessageContaining(logCaptor.getAllValues(), "successful");
    }

    @Test
    public void runSmokeTest_failure_logFailure() {
        SmokeTestsResult someFailure = new SmokeTestsResult(false);
        given(mockService.runSmokeTests()).willReturn(someFailure);

        try {
            resource.runSmokeTests();
        } catch (TestFailureException ignored) {
            // Not of interest to this test.
        }
        then(mockAppender).should(times(2)).doAppend(logCaptor.capture());

        assertLogWithMessageContaining(logCaptor.getAllValues(), "failed");
    }

    @Test
    public void runSmokeTest_failureWithReason_logReason() {
        String someReason = "some reason for failure";

        SmokeTestsResult someFailureWithReason = new SmokeTestsResult(false, someReason);
        given(mockService.runSmokeTests()).willReturn(someFailureWithReason);

        try {
            resource.runSmokeTests();
        } catch (TestFailureException ignored) {
            // Not of interest to this test.
        }
        then(mockAppender).should(times(2)).doAppend(logCaptor.capture());

        assertLogWithMessageContaining(logCaptor.getAllValues(), "failed", someReason);
    }

    public ILoggingEvent assertLogWithMessageContaining(List<ILoggingEvent> loggingEvents, String... expectedMessageContents) {
        return loggingEvents.stream()
                            .filter(loggingEvent -> containsAllMessageParts(loggingEvent, expectedMessageContents))
                            .findFirst()
                            .orElseThrow(AssertionError::new);
    }

    private boolean containsAllMessageParts(ILoggingEvent loggingEvent, String[] expectedMessageContents) {
        return Arrays.stream(expectedMessageContents)
                     .allMatch(expectedMessagePart -> loggingEvent.getFormattedMessage().contains(expectedMessagePart));
    }
}