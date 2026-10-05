package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * Page object for /signup.
 *
 * IMPORTANT: This page object deliberately does NOT expose a submit()
 * method. Submitting a real signup triggers app.py's capture_face_samples(),
 * which opens a blocking OpenCV window on the SERVER machine (cv2.VideoCapture
 * + cv2.imshow/waitKey). That means:
 *   - it cannot run headless / in CI
 *   - it will hang the Flask dev server on the first submit
 * See docs/DEFECT_LOG.xlsx, BUG-01. Only field-level / client-side
 * validation is automated here; the full signup flow is a MANUAL test
 * case (see docs/TEST_CASES.xlsx, TC-SU-*).
 */
public class SignupPage extends BasePage {

    private final By nameInput = By.name("name");
    private final By emailInput = By.name("email");
    private final By phoneInput = By.name("phone");
    private final By genderSelect = By.name("gender");
    private final By addressTextarea = By.name("address");
    private final By blinkCountDisplay = By.id("blinkCount");
    private final By blinkHiddenInput = By.id("blinkInput");
    private final By startCameraBtn = By.id("startCam");
    private final By resetBtn = By.id("resetBtn");
    private final By submitBtn = By.id("submitBtn");
    private final By formTag = By.cssSelector("form");

    public SignupPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/signup");
    }

    public void fillBasicDetails(String name, String email, String phone, String gender, String address) {
        waitVisible(nameInput).sendKeys(name);
        driver.findElement(emailInput).sendKeys(email);
        driver.findElement(phoneInput).sendKeys(phone);
        if (gender != null && !gender.isEmpty()) {
            new Select(driver.findElement(genderSelect)).selectByVisibleText(gender);
        }
        driver.findElement(addressTextarea).sendKeys(address);
    }

    public boolean isNameRequired() {
        return isRequired(nameInput);
    }

    public boolean isEmailRequired() {
        return isRequired(emailInput);
    }

    public String emailFieldType() {
        return driver.findElement(emailInput).getAttribute("type");
    }

    private boolean isRequired(By locator) {
        WebElement el = driver.findElement(locator);
        String required = el.getAttribute("required");
        return required != null;
    }

    public String blinkCountDisplayed() {
        return waitVisible(blinkCountDisplay).getText();
    }

    public String blinkHiddenValue() {
        return driver.findElement(blinkHiddenInput).getAttribute("value");
    }

    public boolean isStartCameraButtonPresent() {
        return !driver.findElements(startCameraBtn).isEmpty();
    }

    public boolean isResetButtonPresent() {
        return !driver.findElements(resetBtn).isEmpty();
    }

    public boolean isSubmitButtonPresent() {
        return !driver.findElements(submitBtn).isEmpty();
    }

    /** True if the form has no visible CSRF token field - used by SecurityTests. */
    public boolean formHasCsrfToken() {
        java.util.List<WebElement> hiddenInputs =
                driver.findElements(By.cssSelector("form input[type=hidden]"));
        return hiddenInputs.stream().anyMatch(i -> {
            String n = i.getAttribute("name");
            return n != null && n.toLowerCase().contains("csrf");
        });
    }
}
