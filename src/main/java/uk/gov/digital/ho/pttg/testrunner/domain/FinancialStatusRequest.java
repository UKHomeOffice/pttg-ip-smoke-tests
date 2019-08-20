package uk.gov.digital.ho.pttg.testrunner.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class FinancialStatusRequest {

    @JsonProperty("individuals")
    private final List<Applicant> applicants;

    @JsonProperty("applicationRaisedDate")
    private final LocalDate applicationRaisedDate;

    @JsonProperty("dependants")
    @JsonInclude(NON_NULL)
    private final Integer dependants;


    public FinancialStatusRequest(List<Applicant> applicants, LocalDate applicationRaisedDate, Integer dependants) {
        this.applicants = applicants;
        this.applicationRaisedDate = applicationRaisedDate;
        this.dependants = dependants;
    }
}
