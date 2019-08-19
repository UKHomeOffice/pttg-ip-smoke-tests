package uk.gov.digital.ho.pttg.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.digital.ho.pttg.testrunner.SmokeTestsService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@WebMvcTest(SmokeTestsResource.class)
public class SmokeTestsResourceWebTest {

    private static final String SMOKE_TESTS_PATH = "/smoketests";

    @MockBean
    private SmokeTestsService mockService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void runSmokeTests_get_methodNotAllowed() throws Exception {
        mockMvc.perform(get(SMOKE_TESTS_PATH))
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void runSmokeTests_testSuccess_returnSuccess() throws Exception {
        when(mockService.runSmokeTests()).thenReturn(SmokeTestsResult.SUCCESS);

        mockMvc.perform(post(SMOKE_TESTS_PATH))
               .andExpect(status().isOk());
    }

    @Test
    public void runSmokeTests_testSuccess_emptyBody() throws Exception {
        when(mockService.runSmokeTests()).thenReturn(SmokeTestsResult.SUCCESS);

        mockMvc.perform(post(SMOKE_TESTS_PATH))
               .andExpect(content().string(""));
    }

    @Test
    public void runSmokeTests_testFailure_returnInternalServerError() throws Exception {
        when(mockService.runSmokeTests()).thenReturn(new SmokeTestsResult(false));

        mockMvc.perform(post(SMOKE_TESTS_PATH))
               .andExpect(status().isInternalServerError());
    }

    @Test
    public void runSmokeTests_testFailure_jsonHeader() throws Exception {
        when(mockService.runSmokeTests()).thenReturn(new SmokeTestsResult(false));

        mockMvc.perform(post(SMOKE_TESTS_PATH))
               .andExpect(header().string("Content-Type", "application/json"));
    }

    @Test
    public void runSmokeTests_testFailureWithoutMessage_emptyResponse() throws Exception {
        when(mockService.runSmokeTests()).thenReturn(new SmokeTestsResult(false));

        mockMvc.perform(post(SMOKE_TESTS_PATH))
               .andExpect(content().string(""));
    }

    @Test
    public void runSmokeTests_testFailureWithMessage_messageInResponse() throws Exception {
        String someFailureReason = "some failure reason";
        when(mockService.runSmokeTests()).thenReturn(new SmokeTestsResult(false, someFailureReason));

        mockMvc.perform(post(SMOKE_TESTS_PATH))
               .andExpect(content().string(someFailureReason));
    }
}