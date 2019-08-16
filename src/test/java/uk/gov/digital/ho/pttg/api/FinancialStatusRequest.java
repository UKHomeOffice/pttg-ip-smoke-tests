package uk.gov.digital.ho.pttg.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class FinancialStatusRequest {
    @JsonProperty("individuals")
    private final List<Applicant> individuals;
    @JsonProperty("applicationRaisedDate")
    private final LocalDate applicationRaisedDate;
    @JsonProperty("dependants")
    private final Integer dependants;

    public static FinancialStatusRequest anyRequest() {
        return new FinancialStatusRequest(Collections.singletonList(new Applicant("Smoke", "Test", LocalDate.now(), "AA000000A")),
                                          LocalDate.now(),
                                          0);
    }
}