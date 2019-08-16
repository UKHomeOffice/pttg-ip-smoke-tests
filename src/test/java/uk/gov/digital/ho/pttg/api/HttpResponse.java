package uk.gov.digital.ho.pttg.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public class HttpResponse {
    private final int statusCode;
    private final String body;
}
