package uk.gov.digital.ho.pttg.testrunner.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.digital.ho.pttg.application.ServiceConfiguration;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class BaseResponseTest {

    private ObjectMapper objectMapper = new ServiceConfiguration().createObjectMapper();

    @Test
    public void shouldDeserialise() throws IOException {
        String serialised = "{\"status\": {\"code\": \"0009\", \"message\": \"Resource not found: QQ123****\"}}";
        BaseResponse deserialised = objectMapper.readValue(serialised, BaseResponse.class);
        assertThat(deserialised).isEqualTo(new BaseResponse(new ResponseStatus("0009", "Resource not found: QQ123****")));
    }

}