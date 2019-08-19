package uk.gov.digital.ho.pttg.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public class SmokeTestsResult {

    public static final SmokeTestsResult SUCCESS = new SmokeTestsResult(true);

    @JsonProperty("success")
    private final boolean success;

    @JsonProperty("reason")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final String reason;

    public SmokeTestsResult(boolean success) {
        this.success = success;
        reason = null;
    }
}
