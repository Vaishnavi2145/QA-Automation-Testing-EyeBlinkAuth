package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.LoginPage;

/**
 * TC-LG-AUTO-* : field-level checks and the client-side "camera not ready"
 * guard on the Login form. A genuine face+blink login cannot be reproduced
 * with synthetic data (see LoginPage javadoc) - that full flow is covered
 * as manual test cases (docs/TEST_CASES.xlsx TC-LG-01..10) and as
 * server-side negative-path checks in api.LoginApiTests.
 */
public class LoginUiTests extends BaseTest {

    @Test(description = "TC-LG-AUTO-01: Email field is required and typed as email")
    public void emailFieldRequiredAndTyped() {
        LoginPage login = new LoginPage(driver);
        login.open(baseUrl);
        Assert.assertTrue(login.isEmailRequired());
        Assert.assertEquals(login.emailFieldType(), "email");
    }

    @Test(description = "TC-LG-AUTO-02: Start Camera and Reset controls are present")
    public void cameraControlsPresent() {
        LoginPage login = new LoginPage(driver);
        login.open(baseUrl);
        Assert.assertTrue(login.isStartCameraButtonPresent());
        Assert.assertTrue(login.isResetButtonPresent());
    }

    @Test(description = "TC-LG-AUTO-03: Clicking Login without starting the camera does not " +
            "navigate away from /login (client-side 'camera not ready' guard in blink.js)")
    public void loginBlockedWithoutCamera() {
        LoginPage login = new LoginPage(driver);
        login.open(baseUrl);
        login.enterEmail("someone@example.com");
        login.clickLoginWithoutStartingCamera();
        // The page's own JS shows a alert() and returns early; no form submit should occur.
        Assert.assertTrue(login.currentUrlAfterClick().endsWith("/login"),
                "Should remain on /login when no face frame has been captured yet");
    }

    @Test(description = "TC-SEC-02 (UI evidence): Login form has no CSRF token field")
    public void loginFormHasNoCsrfToken() {
        LoginPage login = new LoginPage(driver);
        login.open(baseUrl);
        Assert.assertFalse(login.formHasCsrfToken(),
                "Documenting current (insecure) behaviour. See docs/DEFECT_LOG.xlsx BUG-06.");
    }
}
