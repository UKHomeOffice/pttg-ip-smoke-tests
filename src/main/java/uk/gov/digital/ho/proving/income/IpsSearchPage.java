package uk.gov.digital.ho.proving.income;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.digital.ho.proving.income.domain.Applicant;

public class IpsSearchPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpsSearchPage.class);


    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(id = "forename")
    private WebElement forename;

    @FindBy(id = "surname")
    private WebElement surname;

    @FindBy(id = "dateOfBirthDay")
    private WebElement dateOfBirthDay;

    @FindBy(id = "dateOfBirthMonth")
    private WebElement dateOfBirthMonth;

    @FindBy(id = "dateOfBirthYear")
    private WebElement dateOfBirthYear;

    @FindBy(id = "nino")
    private WebElement nino;

    @FindBy(id = "dependants")
    private WebElement dependants;

    @FindBy(id = "applicationRaisedDateDay")
    private WebElement applicationRaisedDateDay;

    @FindBy(id = "applicationRaisedDateMonth")
    private WebElement applicationRaisedDateMonth;

    @FindBy(id = "applicationRaisedDateYear")
    private WebElement applicationRaisedDateYear;

    @FindBy(id = "submitBtn")
    private WebElement searchButton;

    @FindBy(id = "validation-error-summary-heading")
    private WebElement errorSummaryHeader;

    @FindBy(id = "pageDynamicHeading")
    private WebElement pageHeading;

    public IpsSearchPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        this.wait = new WebDriverWait(driver, 30);
    }

    public void start() {
        this.driver.get("http://ui:8000/#!/familymigration");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("submitBtn")));
    }

    public void search() {
        this.searchButton.click();
    }

    public void search(Applicant applicant) {
        forename.sendKeys(applicant.forename());
        surname.sendKeys(applicant.surname());
        dateOfBirthDay.sendKeys(Integer.valueOf(applicant.dateOfBirth().getDayOfMonth()).toString());
        dateOfBirthMonth.sendKeys(Integer.valueOf(applicant.dateOfBirth().getMonthValue()).toString());
        dateOfBirthYear.sendKeys(Integer.valueOf(applicant.dateOfBirth().getYear()).toString());
        nino.sendKeys(applicant.nino());

        dependants.sendKeys("0");
        applicationRaisedDateDay.sendKeys("3");
        applicationRaisedDateMonth.sendKeys("7");
        applicationRaisedDateYear.sendKeys("2018");

        search();
    }

    public WebElement getErrorSummaryHeader() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("validation-error-summary-heading")));
        return errorSummaryHeader;
    }

    public WebElement getPageHeading() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("pageDynamicHeading")));
        return pageHeading;
    }
}
