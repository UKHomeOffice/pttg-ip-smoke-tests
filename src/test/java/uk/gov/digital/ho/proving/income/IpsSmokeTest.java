package uk.gov.digital.ho.proving.income;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import org.apache.commons.io.IOUtils;
import org.junit.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import uk.gov.digital.ho.proving.income.domain.AccessCode;
import uk.gov.digital.ho.proving.income.domain.Applicant;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class IpsSmokeTest {

    private WebDriver driver;
    private IpsSearchPage ipsSearchPage;

    private ObjectMapper objectMapper = new ObjectMapper();

    static final String MATCH_ID = "MATCH-ID";
    static final String ACCESS_ID = "ACCESS-ID";

    @BeforeClass
    public static void setUpClass() {
        ChromeDriverManager.getInstance().setup();
    }

    @Before
    public void setUpTest() throws MalformedURLException {
        ChromeOptions options = new ChromeOptions();
        options.setAcceptInsecureCerts(true);
        options.addArguments("--disable-extensions");
        options.addArguments("--no-sandbox");

        driver = new RemoteWebDriver(new URL("http://selenium:4444/wd/hub"), options);
        ipsSearchPage = new IpsSearchPage(driver);
        ipsSearchPage.start();
    }

    @After
    public void tearDown() {
        driver.quit();
    }

    @Test
    public void shouldReturnNotFoundWithValidNino() {


        Applicant applicant = new Applicant("Val", "Lee", LocalDate.of(1953, 12, 6), "YS255610C");
        ipsSearchPage.search(applicant);

        assertThat(ipsSearchPage.getPageHeading()).withFailMessage("The page heading should exist").isNotNull();
        assertThat(ipsSearchPage.getPageHeading()
                .getText())
                .withFailMessage("The page heading should indicate failure")
                .contains("There is no record");

    }


//    @Test
//    public void thatInvalidSearchesShowErrors() {
//        ipsSearchPage.search();
//        assertThat(ipsSearchPage.getErrorSummaryHeader()).isNotNull().withFailMessage("The error summary should be displayed");
//    }

//    @Test
//    public void thatUnknownIndividualReturnsNoRecord() throws IOException {
//        createFailedMatchStubs();
//        Applicant applicant = new Applicant("Val", "Lee", LocalDate.of(1953, 12, 6), "YS255610C");
//        ipsSearchPage.search(applicant);
//        assertThat(ipsSearchPage.getPageHeading()).isNotNull().withFailMessage("The page heading should exist");
//        assertThat(ipsSearchPage.getPageHeading().getText()).contains("There is no record").withFailMessage("The page heading should indicate failure");
//    }

//    @Test
//    public void thatPassingIndividualReturnsSuccess() throws IOException {
//        createStubs();
//        Applicant applicant = new Applicant("Laurie", "Halford", LocalDate.of(1992, 3, 1), "GH576240A");
//        ipsSearchPage.search(applicant);
//        assertThat(ipsSearchPage.getPageHeading()).isNotNull().withFailMessage("The page heading should exist");
//        assertThat(ipsSearchPage.getPageHeading().getText()).contains("Passed").withFailMessage("The page heading should indicate success");
//    }


}
