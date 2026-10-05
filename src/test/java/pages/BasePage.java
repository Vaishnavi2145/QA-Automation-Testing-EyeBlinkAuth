package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    protected WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    public String pageSource() {
        return driver.getPageSource();
    }

    /** Reads the flash-message banner text rendered in base.html, if present. */
    public String flashMessage() {
        try {
            WebElement el = driver.findElement(By.cssSelector("div.max-w-3xl.mx-auto.mt-4"));
            return el.getText();
        } catch (Exception e) {
            return "";
        }
    }
}
