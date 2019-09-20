package uk.gov.digital.ho.pttg.testrunner;

import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class ComponentHeaderCheckerTest {

    private final ComponentHeaderChecker componentHeaderChecker = new ComponentHeaderChecker();

    @Test
    public void checkAllComponentsPresent_null_returnFalse() {
        assertThat(componentHeaderChecker.checkAllComponentsPresent(null)).isFalse();
    }

    @Test
    public void checkAllComponentsPresent_emptyList_returnFalse() {
        assertThat(componentHeaderChecker.checkAllComponentsPresent(emptyList())).isFalse();
    }

    @Test
    public void checkAllComponentsPresent_singleHeaderAllComponents_returnTrue() {
        List<String> singleHeaderAllComponents = singletonList("pttg-ip-api,pttg-ip-hmrc,pttg-ip-audit,HMRC");
        assertThat(componentHeaderChecker.checkAllComponentsPresent(singleHeaderAllComponents)).isTrue();
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
}