package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Page object for /login.
 *
 * The face capture on this page happens client-side (browser getUserMedia
 * + canvas -> base64 -> hidden "face_image" field), unlike /signup's
 * server-side capture. A full, genuinely-matching login therefore needs a
 * real registered face and cannot be reproduced with synthetic test data
 * in this suite - see docs/DEFECT_LOG.xlsx, BUG-02 for the inconsistency,
 * and docs/TEST_CASES.xlsx, TC-LG-* for the manual login test cases.
 *
 * What IS automated here: field-level validation, the "camera not ready"
 * client guard, and (via LoginApiTests) every server-side negative path
 * that doesn't require a genuine face match.
 */
public class LoginPage extends BasePage {

    private final By emailInput = By.name("email");
    private final By blinkCountDisplay = By.id("blinkCount");
    private final By startCameraBtn = By.id("startCam");
    private final By resetBtn = By.id("resetBtn");
    private final By loginBtn = By.id("loginBtn");
    private final By faceImageHidden = By.id("faceImage");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/login");
    }

    public void enterEmail(String email) {
        waitVisible(emailInput).sendKeys(email);
    }

    public boolean isEmailRequired() {
        WebElement el = driver.findElement(emailInput);
        return el.getAttribute("required") != null;
    }

    public String emailFieldType() {
        return driver.findElement(emailInput).getAttribute("type");
    }

    public boolean isStartCameraButtonPresent() {
        return !driver.findElements(startCameraBtn).isEmpty();
    }

    public boolean isResetButtonPresent() {
        return !driver.findElements(resetBtn).isEmpty();
    }

    /**
     * Clicking Login before the camera has produced a frame triggers the
     * page's own JS `alert("Camera not ready...")` and returns without
     * submitting. We accept that native alert so WebDriver doesn't choke
     * on subsequent commands.
     */
    public void clickLoginWithoutStartingCamera() {
        waitClickable(loginBtn).click();
        try {
            wait.until(org.openqa.selenium.support.ui.ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (org.openqa.selenium.TimeoutException ignored) {
            // no alert appeared - fine, we only care that we're still on /login
        }
    }

    public String currentUrlAfterClick() {
        return driver.getCurrentUrl();
    }

    public boolean formHasCsrfToken() {
        java.util.List<WebElement> hiddenInputs =
                driver.findElements(By.cssSelector("form input[type=hidden]"));
        return hiddenInputs.stream().anyMatch(i -> {
            String n = i.getAttribute("name");
            return n != null && n.toLowerCase().contains("csrf");
        });
    }
}
