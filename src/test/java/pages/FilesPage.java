package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

public class FilesPage extends BasePage {

    private final By fileListItems = By.cssSelector("ul.divide-y li");
    private final By downloadLinks = By.linkText("Download");
    private final By backToDashboard = By.linkText("Back to Dashboard");

    public FilesPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/files");
    }

    public List<String> listedFileNames() {
        return driver.findElements(fileListItems).stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    public String firstDownloadHref() {
        List<WebElement> links = driver.findElements(downloadLinks);
        return links.isEmpty() ? null : links.get(0).getAttribute("href");
    }

    public DashboardPage backToDashboard() {
        waitClickable(backToDashboard).click();
        return new DashboardPage(driver);
    }
}
