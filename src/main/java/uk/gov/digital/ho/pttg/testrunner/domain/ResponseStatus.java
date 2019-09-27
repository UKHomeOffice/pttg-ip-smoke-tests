package uk.gov.digital.ho.pttg.testrunner.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
@ToString
@EqualsAndHashCode
public class ResponseStatus {

    @JsonProperty(value = "code")
    private String code;
    @JsonProperty(value = "message")
    private String message;

    // Provided to facilitate com.fasterxml.jackson.databind.ObjectMapper
    private ResponseStatus() {
        code = null;
        message = null;
    }
}
