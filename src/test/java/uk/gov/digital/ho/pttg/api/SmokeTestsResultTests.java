package uk.gov.digital.ho.pttg.api;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class SmokeTestsResultTests {

    private static final boolean ANY_BOOLEAN = false;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void serialization_success_serializes() throws JsonProcessingException {
        SmokeTestsResult someResult = new SmokeTestsResult(true);
        String serializedResult = objectMapper.writeValueAsString(someResult);
        boolean success = JsonPath.read(serializedResult, "$.success");
        assertThat(success).isTrue();
    }

    @Test
    public void serialization_failure_serializes() throws JsonProcessingException {
        SmokeTestsResult someResult = new SmokeTestsResult(false);
        String serializedResult = objectMapper.writeValueAsString(someResult);
        boolean success = JsonPath.read(serializedResult, "$.success");
        assertThat(success).isFalse();
    }

    @Test
    public void serialization_noReason_doNotIncludeInSerialization() throws JsonProcessingException {
        SmokeTestsResult noReason = new SmokeTestsResult(ANY_BOOLEAN);
        String serializedResult = objectMapper.writeValueAsString(noReason);

        expectedException.expect(PathNotFoundException.class);
        JsonPath.read(serializedResult, "$.reason");
    }

    @Test
    public void serialization_emptyReason_doNotIncludeInSerialization() throws JsonProcessingException {
        SmokeTestsResult emptyReason = new SmokeTestsResult(ANY_BOOLEAN, "");
        String serializedResult = objectMapper.writeValueAsString(emptyReason);

        expectedException.expect(PathNotFoundException.class);
        JsonPath.read(serializedResult, "$.reason");
    }

    @Test
    public void serialization_someReason_serializes() throws JsonProcessingException {
        SmokeTestsResult someResultWithReason = new SmokeTestsResult(ANY_BOOLEAN, "expected reason");
        String serializedResult = objectMapper.writeValueAsString(someResultWithReason);

        String actualReason = JsonPath.read(serializedResult, "$.reason");
        assertThat(actualReason).isEqualTo("expected reason");
    }
}