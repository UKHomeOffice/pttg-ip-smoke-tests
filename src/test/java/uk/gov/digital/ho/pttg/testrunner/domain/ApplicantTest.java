package uk.gov.digital.ho.pttg.testrunner.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import uk.gov.digital.ho.pttg.application.ServiceConfiguration;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicantTest {

    private final ObjectMapper objectMapper = new ServiceConfiguration().createObjectMapper();

    private static final String SOME_DATE_OF_BIRTH = "2019-08-20";
    private static final Applicant SOME_APPLICANT = new Applicant("some forename", "some surname", LocalDate.parse(SOME_DATE_OF_BIRTH), "some nino");

    @Test
    public void serialization_someApplicant_serializeForename() throws JsonProcessingException {
        String serialized = objectMapper.writeValueAsString(SOME_APPLICANT);
        String forename = JsonPath.read(serialized, "$.forename");

        assertThat(forename).isEqualTo("some forename");
    }

    @Test
    public void serialization_someApplicant_serializeSurname() throws JsonProcessingException {
        String serialized = objectMapper.writeValueAsString(SOME_APPLICANT);
        String surname = JsonPath.read(serialized, "$.surname");

        assertThat(surname).isEqualTo("some surname");
    }

    @Test
    public void serialization_someApplicant_serializeDateOfBirth() throws JsonProcessingException {
        String serialized = objectMapper.writeValueAsString(SOME_APPLICANT);
        String dateOfBirth = JsonPath.read(serialized, "$.dateOfBirth");

        assertThat(dateOfBirth).isEqualTo(SOME_DATE_OF_BIRTH);
    }

    @Test
    public void serialization_someApplicant_serializeNino() throws JsonProcessingException {
        String serialized = objectMapper.writeValueAsString(SOME_APPLICANT);
        String nino = JsonPath.read(serialized, "$.nino");

        assertThat(nino).isEqualTo("some nino");
    }
}