package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class IndexPage extends BasePage {

    private final By getStartedLink = By.linkText("Get Started");
    private final By loginNavLink = By.cssSelector("nav a[href*='login']");
    private final By heading = By.tagName("h1");

    public IndexPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/");
    }

    public String headingText() {
        return waitVisible(heading).getText();
    }

    public SignupPage clickGetStarted() {
        waitClickable(getStartedLink).click();
        return new SignupPage(driver);
    }

    public LoginPage clickLoginNav() {
        waitClickable(loginNavLink).click();
        return new LoginPage(driver);
    }
}
