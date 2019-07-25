package uk.gov.digital.ho.proving.income.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
@ToString
public class Applicant {
    @JsonProperty
    private String forename;
    @JsonProperty
    private String surname;
    @JsonProperty
    private LocalDate dateOfBirth;
    @JsonProperty
    private String nino;
}
