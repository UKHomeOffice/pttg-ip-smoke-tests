package uk.gov.digital.ho.pttg.testrunner.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
@ToString
@EqualsAndHashCode
public class BaseResponse {

    @JsonProperty(value = "status")
    private final ResponseStatus status;

    // Provided to facilitate com.fasterxml.jackson.databind.ObjectMapper
    private BaseResponse() {
        status = null;
    }
}
