package uk.gov.digital.ho.pttg.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
class Applicant {
    @JsonProperty("forename")
    private String forename;
    @JsonProperty("surname")
    private String surname;
    @JsonProperty("dateOfBirth")
    private LocalDate dateOfBirth;
    @JsonProperty("nino")
    private String nino;
}
