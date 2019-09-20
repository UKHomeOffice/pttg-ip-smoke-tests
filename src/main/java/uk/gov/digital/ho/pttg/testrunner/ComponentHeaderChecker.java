package uk.gov.digital.ho.pttg.testrunner;

import com.google.common.collect.ImmutableSet;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ComponentHeaderChecker {
    private static final Set<String> ALL_EXPECTED_COMPONENTS = ImmutableSet.of("pttg-ip-api", "pttg-ip-hmrc", "pttg-ip-audit", "HMRC");

    public boolean checkAllComponentsPresent(List<String> componentTraceHeaders) {
        if (componentTraceHeaders == null) {
            return false;
        }

        Set<String> componentsPresent = componentTraceHeaders.stream()
                                                             .flatMap(ComponentHeaderChecker::splitComponents)
                                                             .collect(Collectors.toSet());
        return componentsPresent.equals(ALL_EXPECTED_COMPONENTS);

    }

    private static Stream<? extends String> splitComponents(String header) {
        return Arrays.stream(header.split(","));
    }
}
