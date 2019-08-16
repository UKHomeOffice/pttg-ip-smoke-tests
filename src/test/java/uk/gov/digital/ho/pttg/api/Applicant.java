package uk.gov.digital.ho.pttg.api;

import lombok.AllArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
class Applicant {
    private String forename;
    private String surname;
    private LocalDate dateOfBirth;
    private String nino;
}
