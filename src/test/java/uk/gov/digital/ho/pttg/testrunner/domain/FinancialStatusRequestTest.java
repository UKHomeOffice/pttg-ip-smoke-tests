package uk.gov.digital.ho.pttg.testrunner.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.junit.Test;
import uk.gov.digital.ho.pttg.application.ServiceConfiguration;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FinancialStatusRequestTest {

    private static final String SOME_DATE_OF_BIRTH = "1990-01-01";
    private static final String SOME_OTHER_DATE_OF_BIRTH = "1991-02-02";
    private static final List<Applicant> SOME_APPLICANTS = Arrays.asList(new Applicant("some forename", "some surname", LocalDate.parse(SOME_DATE_OF_BIRTH), "some nino"),
                                                                         new Applicant("some other forename", "some other surname", LocalDate.parse(SOME_OTHER_DATE_OF_BIRTH), "some other nino"));
    private static final String SOME_APPLICATION_RAISED_DATE = "2019-08-20";
    private static final Integer SOME_DEPENDANTS = 4;
    private final FinancialStatusRequest SOME_REQUEST = new FinancialStatusRequest(SOME_APPLICANTS, LocalDate.parse(SOME_APPLICATION_RAISED_DATE), SOME_DEPENDANTS);

    private ObjectMapper objectMapper = new ServiceConfiguration().createObjectMapper();

    @Test
    public void serialization_someRequest_serializeApplicants() throws JsonProcessingException {
        String serialized = objectMapper.writeValueAsString(SOME_REQUEST);
        String applicant1Forename = JsonPath.read(serialized, "$.individuals[0].forename");
        String applicant2DateOfBirth = JsonPath.read(serialized, "$.individuals[1].dateOfBirth");
        assertThat(applicant1Forename).isEqualTo("some forename");
        assertThat(applicant2DateOfBirth).isEqualTo(SOME_OTHER_DATE_OF_BIRTH);
    }

    @Test
    public void serialization_someRequest_serializeApplicationRaisedDate() throws JsonProcessingException {
        String serialized = objectMapper.writeValueAsString(SOME_REQUEST);
        String applicationRaisedDate = JsonPath.read(serialized, "$.applicationRaisedDate");

        assertThat(applicationRaisedDate).isEqualTo(SOME_APPLICATION_RAISED_DATE);
    }

    @Test
    public void serialization_someRequest_serializeDependants() throws JsonProcessingException {
        String serialized = objectMapper.writeValueAsString(SOME_REQUEST);
        int dependants = JsonPath.read(serialized, "$.dependants");

        assertThat(dependants).isEqualTo(SOME_DEPENDANTS);
    }

    @Test
    public void serialization_requestWithoutDependants_notInRequest() throws JsonProcessingException {
        FinancialStatusRequest requestWithoutDependants = new FinancialStatusRequest(SOME_APPLICANTS, LocalDate.parse(SOME_APPLICATION_RAISED_DATE), null);
        String serialized = objectMapper.writeValueAsString(requestWithoutDependants);

        assertThatThrownBy(() -> JsonPath.read(serialized, "$.dependants"))
                .isInstanceOf(PathNotFoundException.class);
    }
}
