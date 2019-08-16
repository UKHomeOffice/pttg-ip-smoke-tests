package uk.gov.digital.ho.pttg.api;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class FinancialStatusRequest {
    private final List<Applicant> individuals;
    private final LocalDate applicationRaisedDate;
    private final Integer dependants;

    private FinancialStatusRequest(List<Applicant> applicants, LocalDate applicationRaisedDate, Integer dependants) {
        this.individuals = applicants;
        this.applicationRaisedDate = applicationRaisedDate;
        this.dependants = dependants;
    }

    public static FinancialStatusRequest anyRequest() {
        return new FinancialStatusRequest(Collections.singletonList(new Applicant("Smoke", "Test", LocalDate.now(), "AA000000A")),
                                          LocalDate.now(),
                                          0);
    }
}