package uk.gov.digital.ho.pttg;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.YearMonthDeserializer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.digital.ho.pttg.api.FinancialStatusRequest;
import uk.gov.digital.ho.pttg.api.HttpResponse;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@Ignore("TODO EE-6825 Moving away from a Kubernetes Job that runs these tests towards a RESTful service.")
public class SmokeTest {

    private final String IP_API_PATH = "/incomeproving/v3/individual/financialstatus";

    private String ipApiRootUrl;

    private SimpleHttpClient simpleHttpClient = getClient();
    private ObjectMapper objectMapper = initialiseObjectMapper(new ObjectMapper());

    @Before
    public void setUp() {
        ipApiRootUrl = getMandatoryEnvVar("IP_API_ROOT_URL");
    }

    @Test
    public void testIpApi() throws JsonProcessingException {
        HttpResponse response = simpleHttpClient.post(ipApiRootUrl + IP_API_PATH, buildRequest());
        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.body()).contains("Resource not found");
    }

    private String buildRequest() throws JsonProcessingException {
        return objectMapper.writeValueAsString(FinancialStatusRequest.anyRequest());
    }

    private static String getMandatoryEnvVar(String envVarName) {
        String envVarValue = System.getenv(envVarName);
        if (envVarValue == null) {
            fail(String.format("No environment variable for %s found", envVarName));
        }
        return envVarValue;
    }

    private static SimpleHttpClient getClient() {
        String ipApiRootUrl = getMandatoryEnvVar("IP_API_ROOT_URL");
        String ipApiAuth = getMandatoryEnvVar("IP_API_AUTH");
        return new SimpleHttpClient(Collections.singletonMap(ipApiRootUrl, ipApiAuth));
    }

    private static ObjectMapper initialiseObjectMapper(final ObjectMapper objectMapper) {
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-M-d")));
        javaTimeModule.addDeserializer(YearMonth.class, new YearMonthDeserializer(DateTimeFormatter.ofPattern("yyyy-M")));
        objectMapper.registerModule(javaTimeModule);

        return objectMapper;
    }
}
