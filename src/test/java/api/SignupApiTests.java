package api;

import config.TestConfig;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

/**
 * TC-SU-API-* : server-side validation on POST /signup.
 *
 * *** DO NOT add a "happy path" test here that submits a fully valid
 * signup (non-blank name/email + numeric blink_count). Passing all three
 * checks makes app.py call capture_face_samples(), which opens a
 * blocking OpenCV camera window on the SERVER process — it will hang the
 * Flask dev server, not just this test. See docs/DEFECT_LOG.xlsx BUG-01. ***
 *
 * Only the early-return / invalid-input branch of the handler is safe to
 * exercise over HTTP, so that's all this class covers.
 */
public class SignupApiTests {

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = TestConfig.BASE_URL;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test(description = "TC-SU-API-01: Missing name redirects back to /signup with an error")
    public void missingNameRejected() {
        Response resp = given()
                .redirects().follow(true)
                .formParam("name", "")
                .formParam("email", "someone@example.com")
                .formParam("phone", "9999999999")
                .formParam("gender", "Other")
                .formParam("address", "Test address")
                .formParam("blink_count", "3")
                .when().post("/signup");

        Assert.assertEquals(resp.statusCode(), 200);
        Assert.assertTrue(resp.getBody().asString().contains("complete the form"),
                "Expected the 'please complete the form' flash message");
    }

    @Test(description = "TC-SU-API-02: Missing email redirects back to /signup with an error")
    public void missingEmailRejected() {
        Response resp = given()
                .redirects().follow(true)
                .formParam("name", "Test User")
                .formParam("email", "")
                .formParam("phone", "9999999999")
                .formParam("gender", "Other")
                .formParam("address", "Test address")
                .formParam("blink_count", "3")
                .when().post("/signup");

        Assert.assertEquals(resp.statusCode(), 200);
        Assert.assertTrue(resp.getBody().asString().contains("complete the form"));
    }

    @Test(description = "TC-SU-API-03: Non-numeric blink_count is rejected (boundary/type check)")
    public void nonNumericBlinkCountRejected() {
        Response resp = given()
                .redirects().follow(true)
                .formParam("name", "Test User")
                .formParam("email", "someone@example.com")
                .formParam("phone", "9999999999")
                .formParam("gender", "Other")
                .formParam("address", "Test address")
                .formParam("blink_count", "abc")
                .when().post("/signup");

        Assert.assertEquals(resp.statusCode(), 200);
        Assert.assertTrue(resp.getBody().asString().contains("complete the form"),
                "blink_count.isdigit() should reject non-numeric values");
    }

    @Test(description = "TC-SU-API-04: Empty blink_count is rejected")
    public void emptyBlinkCountRejected() {
        Response resp = given()
                .redirects().follow(true)
                .formParam("name", "Test User")
                .formParam("email", "someone@example.com")
                .formParam("phone", "9999999999")
                .formParam("gender", "Other")
                .formParam("address", "Test address")
                .formParam("blink_count", "")
                .when().post("/signup");

        Assert.assertEquals(resp.statusCode(), 200);
        Assert.assertTrue(resp.getBody().asString().contains("complete the form"));
    }

    @Test(description = "TC-SU-API-05: Negative blink_count string is rejected by isdigit() " +
            "(boundary case: '-1'.isdigit() is False in Python)")
    public void negativeBlinkCountRejected() {
        Response resp = given()
                .redirects().follow(true)
                .formParam("name", "Test User")
                .formParam("email", "someone@example.com")
                .formParam("phone", "9999999999")
                .formParam("gender", "Other")
                .formParam("address", "Test address")
                .formParam("blink_count", "-1")
                .when().post("/signup");

        Assert.assertEquals(resp.statusCode(), 200);
        Assert.assertTrue(resp.getBody().asString().contains("complete the form"));
    }

    @Test(description = "TC-NAV-API-01: GET /signup renders the signup form")
    public void getSignupRendersForm() {
        Response resp = given().when().get("/signup");
        Assert.assertEquals(resp.statusCode(), 200);
        Assert.assertTrue(resp.getBody().asString().contains("Create Account"));
    }
}
