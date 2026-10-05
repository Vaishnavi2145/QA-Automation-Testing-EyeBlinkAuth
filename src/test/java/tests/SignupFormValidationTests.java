package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.SignupPage;

/**
 * TC-SU-AUTO-* : field-level / client-side checks on the Signup form only.
 * The full signup submission (name+email+phone+gender+address+blink_count
 * -> server-side face capture -> DB insert) is NOT automated here.
 *
 * Why: app.py's /signup handler calls capture_face_samples(), which opens
 * a blocking OpenCV window (cv2.VideoCapture + cv2.imshow/waitKey loop) on
 * whatever machine is running the Flask server. There is no browser-side
 * hook to fake this the way /login's client-side capture can be faked
 * with Chrome's --use-fake-device-for-media-stream. Submitting signup
 * through Selenium (or a raw HTTP client) would hang the server process
 * waiting for a physical keypress in a native window.
 *
 * See docs/DEFECT_LOG.xlsx BUG-01 and docs/TEST_CASES.xlsx TC-SU-01..08
 * for the full manual signup test cases (positive, negative, boundary).
 */
public class SignupFormValidationTests extends BaseTest {

    @Test(description = "TC-SU-AUTO-01: Name field is marked required")
    public void nameFieldIsRequired() {
        SignupPage signup = new SignupPage(driver);
        signup.open(baseUrl);
        Assert.assertTrue(signup.isNameRequired(), "Name field should be marked required");
    }

    @Test(description = "TC-SU-AUTO-02: Email field is marked required and typed as email")
    public void emailFieldIsRequiredAndTyped() {
        SignupPage signup = new SignupPage(driver);
        signup.open(baseUrl);
        Assert.assertTrue(signup.isEmailRequired(), "Email field should be marked required");
        Assert.assertEquals(signup.emailFieldType(), "email",
                "Email field should use type='email' for basic client-side format checking");
    }

    @Test(description = "TC-SU-AUTO-03: Blink counter starts at 0 before camera is used")
    public void blinkCounterStartsAtZero() {
        SignupPage signup = new SignupPage(driver);
        signup.open(baseUrl);
        Assert.assertEquals(signup.blinkCountDisplayed(), "0");
        Assert.assertEquals(signup.blinkHiddenValue(), "0");
    }

    @Test(description = "TC-SU-AUTO-04: Start Camera and Reset controls are present")
    public void cameraControlsPresent() {
        SignupPage signup = new SignupPage(driver);
        signup.open(baseUrl);
        Assert.assertTrue(signup.isStartCameraButtonPresent());
        Assert.assertTrue(signup.isResetButtonPresent());
    }

    @Test(description = "TC-SEC-01 (UI evidence): Signup form has no CSRF token field")
    public void signupFormHasNoCsrfToken() {
        SignupPage signup = new SignupPage(driver);
        signup.open(baseUrl);
        Assert.assertFalse(signup.formHasCsrfToken(),
                "Documenting current (insecure) behaviour: no CSRF token is rendered. " +
                "See docs/DEFECT_LOG.xlsx BUG-06.");
    }
}
