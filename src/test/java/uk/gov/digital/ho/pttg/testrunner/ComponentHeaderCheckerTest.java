package uk.gov.digital.ho.pttg.testrunner;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

@RunWith(MockitoJUnitRunner.class)
public class ComponentHeaderCheckerTest {

    @Mock
    public Appender<ILoggingEvent> mockLogAppender;

    private final ComponentHeaderChecker componentHeaderChecker = new ComponentHeaderChecker();
    private ArgumentCaptor<LoggingEvent> logCaptor;

    @Before
    public void setUp() {
        Logger logger = (Logger) LoggerFactory.getLogger(ComponentHeaderChecker.class);
        logger.addAppender(mockLogAppender);
        logger.setLevel(Level.INFO);

        logCaptor = ArgumentCaptor.forClass(LoggingEvent.class);
    }

    @Test
    public void checkAllComponentsPresent_null_returnFalse() {
        assertThat(componentHeaderChecker.checkAllComponentsPresent(null)).isFalse();
    }

    @Test
    public void checkAllComponentsPresent_null_log() {
        componentHeaderChecker.checkAllComponentsPresent(null);
        then(mockLogAppender).should().doAppend(logCaptor.capture());

        assertThat(logCaptor.getValue().getFormattedMessage()).contains("null");
    }

    @Test
    public void checkAllComponentsPresent_emptyList_returnFalse() {
        assertThat(componentHeaderChecker.checkAllComponentsPresent(emptyList())).isFalse();
    }

    @Test
    public void checkAllComponentsPresent_singleHeaderAllComponents_returnTrue() {
        assertThat(componentHeaderChecker.checkAllComponentsPresent(singleHeaderAllComponents())).isTrue();
    }

    @Test
    public void checkAllComponentsPresent_allComponentsOneHeaderSomeRepeated_returnTrue() {
        List<String> allComponentsWithRepeats = singletonList("pttg-ip-api,pttg-ip-audit,pttg-ip-hmrc,pttg-ip-audit,HMRC,HMRC");
        assertThat(componentHeaderChecker.checkAllComponentsPresent(allComponentsWithRepeats)).isTrue();
    }

    @Test
    public void checkAllComponentsPresent_allComponentsOneHeader_withSpaces_returnTrue() {
        List<String> allComponentsWithSpaces = singletonList("pttg-ip-audit, pttg-ip-api ,pttg-ip-hmrc  ,  HMRC");
        assertThat(componentHeaderChecker.checkAllComponentsPresent(allComponentsWithSpaces)).isTrue();
    }

    @Test
    public void checkAllComponentsPresent_componentMissingSingleHeader_returnFalse() {
        List<String> singleHeaderWithMissing = singletonList("pttg-ip-hmrc,HMRC,pttg-ip-audit");
        assertThat(componentHeaderChecker.checkAllComponentsPresent(singleHeaderWithMissing)).isFalse();
    }

    @Test
    public void checkAllComponentsPresent_allComponentsSeparateHeaders_returnTrue() {
        List<String> allComponentsSeparateHeaders = asList("pttg-ip-audit", "pttg-ip-hmrc", "HMRC", "pttg-ip-api");
        assertThat(componentHeaderChecker.checkAllComponentsPresent(allComponentsSeparateHeaders)).isTrue();
    }

    @Test
    public void checkAllComponentsPresent_allComponentsSeparateHeadersSomeRepeated_returnTrue() {
        List<String> allComponentsWithRepeats = asList("pttg-ip-hmrc", "pttg-ip-hmrc", "pttg-ip-audit", "HMRC", "pttg-ip-hmrc", "HMRC", "pttg-ip-api");
        assertThat(componentHeaderChecker.checkAllComponentsPresent(allComponentsWithRepeats)).isTrue();
    }

    @Test
    public void checkAllComponentsPresent_separateHeadersComponentsMissing_returnFalse() {
        List<String> multipleHeadersWithMissing = asList("pttg-ip-api", "pttg-ip-audit", "pttg-ip-audit");
        assertThat(componentHeaderChecker.checkAllComponentsPresent(multipleHeadersWithMissing)).isFalse();
    }

    @Test
    public void checkAllComponentsPresent_allPresent_someInSameHeader_returnTrue() {
        List<String> allComponentsSomeSameHeader = asList("pttg-ip-hmrc,pttg-ip-hmrc", "pttg-ip-audit", "HMRC,pttg-ip-hmrc", "pttg-ip-api");
        assertThat(componentHeaderChecker.checkAllComponentsPresent(allComponentsSomeSameHeader)).isTrue();
    }

    @Test
    public void checkAllComponentsPresent_allPresentMultipleHeaders_someSpaces_returnTrue() {
        List<String> allComponentsWithSpaces = asList("pttg-ip-audit ", " pttg-ip-hmrc", " HMRC ", "   pttg-ip-api");
        assertThat(componentHeaderChecker.checkAllComponentsPresent(allComponentsWithSpaces)).isTrue();
    }

    @Test
    public void checkAllComponentsPresent_allPresent_log() {
        componentHeaderChecker.checkAllComponentsPresent(singleHeaderAllComponents());

        then(mockLogAppender).should().doAppend(logCaptor.capture());
        assertThat(logCaptor.getValue().getFormattedMessage()).isEqualTo("All components present");
    }

    @Test
    public void checkAllComponentsPresent_componentMissing_logged() {
        List<String> hmrcServiceMissing = singletonList("pttg-ip-api,pttg-ip-audit,HMRC");
        componentHeaderChecker.checkAllComponentsPresent(hmrcServiceMissing);

        then(mockLogAppender).should().doAppend(logCaptor.capture());
        String logMessage = logCaptor.getValue().getFormattedMessage();
        assertThat(logMessage).startsWith("Component(s) missing")
                              .contains("pttg-ip-hmrc")
                              .doesNotContain("pttg-ip-api", "pttg-ip-audit", "HMRC");
    }

    @Test
    public void checkAllComponentsPresent_multipleMissing_logAll() {
        List<String> componentsMissing = Arrays.asList("pttg-ip-audit", "pttg-ip-hmrc");
        componentHeaderChecker.checkAllComponentsPresent(componentsMissing);

        then(mockLogAppender).should().doAppend(logCaptor.capture());
        String logMessage = logCaptor.getValue().getFormattedMessage();
        assertThat(logMessage).startsWith("Component(s) missing")
                              .contains("HMRC", "pttg-ip-api")
                              .doesNotContain("pttg-ip-audit", "pttg-ip-hmrc");
    }

    private List<String> singleHeaderAllComponents() {
        return singletonList("pttg-ip-api,pttg-ip-hmrc,pttg-ip-audit,HMRC");
    }
}