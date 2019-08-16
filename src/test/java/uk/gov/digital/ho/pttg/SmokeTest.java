package uk.gov.digital.ho.pttg;

import com.google.gson.*;
import org.junit.Before;
import org.junit.Test;
import uk.gov.digital.ho.pttg.api.FinancialStatusRequest;
import uk.gov.digital.ho.pttg.api.HttpResponse;

import java.lang.reflect.Type;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class SmokeTest {

    private final String IP_API_PATH = "/incomeproving/v3/individual/financialstatus";

    private String ipApiRootUrl;
    private SimpleHttpClient simpleHttpClient;
    private Gson gson;

    @Before
    public void setUp() {
        ipApiRootUrl = getMandatoryEnvVar("IP_API_ROOT_URL");
        simpleHttpClient = new SimpleHttpClient();
        gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
                                .create();
    }

    @Test
    public void testIpApi() {
        String body = buildRequest();
        System.out.println(body);
        HttpResponse response = simpleHttpClient.post(ipApiRootUrl + IP_API_PATH, body);
        System.out.println(response.body());

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.body()).contains("Resource not found");
    }

    private String buildRequest() {
        return gson.toJson(FinancialStatusRequest.anyRequest());
    }

    private String getMandatoryEnvVar(String envVarName) {
        String envVarValue = System.getenv(envVarName);
        if (envVarValue == null) {
            fail(String.format("No environment variable for %s found", envVarName));
        }
        return envVarValue;
    }

    private class LocalDateSerializer implements JsonSerializer<LocalDate> {

        @Override
        public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }
}
