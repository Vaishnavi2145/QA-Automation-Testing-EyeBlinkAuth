package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DashboardPage extends BasePage {

    private final By welcomeHeading = By.tagName("h1");
    private final By uploadFileBtn = By.xpath("//button[contains(text(),'Upload File')]");
    private final By viewFilesLink = By.linkText("View Files");
    private final By logoutLink = By.linkText("Logout");
    private final By uploadModal = By.id("uploadModal");
    private final By fileInput = By.cssSelector("#uploadModal input[type=file]");
    private final By uploadSubmitBtn = By.cssSelector("#uploadModal button[type=submit]");

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/dashboard");
    }

    public String welcomeText() {
        return waitVisible(welcomeHeading).getText();
    }

    public void openUploadModal() {
        waitClickable(uploadFileBtn).click();
        waitVisible(uploadModal);
    }

    public void uploadFile(String absoluteFilePath) {
        driver.findElement(fileInput).sendKeys(absoluteFilePath);
        driver.findElement(uploadSubmitBtn).click();
    }

    public FilesPage goToFiles() {
        waitClickable(viewFilesLink).click();
        return new FilesPage(driver);
    }

    public void logout() {
        waitClickable(logoutLink).click();
    }
}
