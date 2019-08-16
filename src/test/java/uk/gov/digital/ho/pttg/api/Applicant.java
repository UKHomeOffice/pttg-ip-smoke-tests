package uk.gov.digital.ho.pttg.api;

import java.time.LocalDate;

class Applicant {

    Applicant(String forename, String surname, LocalDate dateOfBirth, String nino) {
        this.forename = forename;
        this.surname = surname;
        this.dateOfBirth = dateOfBirth;
        this.nino = nino;
    }

    private String forename;
    private String surname;
    private LocalDate dateOfBirth;
    private String nino;
}
