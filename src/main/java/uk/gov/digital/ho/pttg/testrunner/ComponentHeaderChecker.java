package uk.gov.digital.ho.pttg.testrunner;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class ComponentHeaderChecker {
    private static final Set<String> ALL_EXPECTED_COMPONENTS = ImmutableSet.of("pttg-ip-api", "pttg-ip-hmrc", "pttg-ip-audit", "HMRC");

    public boolean checkAllComponentsPresent(List<String> componentTraceHeaders) {
        if (componentTraceHeaders == null) {
            log.info("x-component-trace header is null");
            return false;
        }

        Set<String> componentsPresent = componentTraceHeaders.stream()
                                                             .flatMap(ComponentHeaderChecker::splitComponents)
                                                             .map(String::trim)
                                                             .collect(Collectors.toSet());

        boolean allPresent = componentsPresent.equals(ALL_EXPECTED_COMPONENTS);
        logMissingComponents(componentsPresent, allPresent);
        return allPresent;
    }

    private static Stream<? extends String> splitComponents(String header) {
        return Arrays.stream(header.split(","));
    }

    private void logMissingComponents(Set<String> componentsPresent, boolean allPresent) {
        if (allPresent) {
            log.info("All components present");
        } else {
            log.info("Component(s) missing: {}", getMissingComponents(componentsPresent));
        }
    }

    private String getMissingComponents(Set<String> componentsPresent) {
        return ALL_EXPECTED_COMPONENTS.stream()
                                      .filter(component -> !componentsPresent.contains(component))
                                      .collect(Collectors.joining(","));
    }
}
