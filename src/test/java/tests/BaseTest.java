package tests;

import config.TestConfig;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class BaseTest {

    protected WebDriver driver;
    protected final String baseUrl = TestConfig.BASE_URL;

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();

        // Run headless by default; pass -Dheadless=false to watch it run.
        if (!"false".equalsIgnoreCase(System.getProperty("headless", "true"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1400,1000");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");

        // OPTIONAL, for anyone extending this suite to automate the login
        // page's browser-side face capture: Chrome can feed a fake webcam
        // from a video file instead of prompting for real camera access.
        // Uncomment and point at a short face video to enable:
        //
        // options.addArguments("--use-fake-ui-for-media-stream");
        // options.addArguments("--use-fake-device-for-media-stream");
        // options.addArguments("--use-file-for-fake-video-capture=/absolute/path/to/face.y4m");
        //
        // Even with this, login will only succeed if that video's face
        // matches an embedding already stored in ref_embed.pkl for the
        // seeded test user - which requires having run signup for real at
        // least once. See docs/README.md "Camera-dependent flows".

        driver = new ChromeDriver(options);
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
