package tests;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC-SEC-03/04 : an unauthenticated browser session must never reach
 * /dashboard or /files - both should bounce to /login.
 */
public class AccessControlUiTests extends BaseTest {

    @Test(description = "TC-SEC-03: Unauthenticated GET /dashboard redirects to /login")
    public void dashboardRequiresLogin() {
        driver.get(baseUrl + "/dashboard");
        Assert.assertTrue(driver.getCurrentUrl().endsWith("/login"),
                "Unauthenticated user should be redirected away from /dashboard");
    }

    @Test(description = "TC-SEC-04: Unauthenticated GET /files redirects to /login")
    public void filesRequiresLogin() {
        driver.get(baseUrl + "/files");
        Assert.assertTrue(driver.getCurrentUrl().endsWith("/login"),
                "Unauthenticated user should be redirected away from /files");
    }
}
