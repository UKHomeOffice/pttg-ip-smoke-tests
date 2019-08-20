package uk.gov.digital.ho.pttg.testrunner.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class Applicant {
    @JsonProperty("forename")
    private final String forename;

    @JsonProperty("surname")
    private final String surname;

    @JsonProperty("dateOfBirth")
    private final LocalDate dateOfBirth;

    @JsonProperty("nino")
    private final String nino;

    public Applicant(String forename, String surname, LocalDate dateOfBirth, String nino) {
        this.forename = forename;
        this.surname = surname;
        this.dateOfBirth = dateOfBirth;
        this.nino = nino;
    }
}
