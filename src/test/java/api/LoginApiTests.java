package api;

import config.SeedUtil;
import config.TestConfig;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

/**
 * TC-LG-API-* : server-side validation on POST /login that does NOT
 * require a genuine matching face (which this suite has no way to supply -
 * see LoginPage javadoc). A full, real face+blink login is documented as a
 * MANUAL test case (docs/TEST_CASES.xlsx TC-LG-08/09/10).
 *
 * Tests in this class that reference a specific pickle-file state
 * (ref_embed.pkl present/absent) were derived from reading app.py, not
 * from a live run in this environment. Run this class once against your
 * local Flask+MySQL instance and adjust any assertion that doesn't match -
 * see docs/README.md "First run checklist".
 */
public class LoginApiTests {

    // A syntactically-plausible but meaningless base64 payload, WITH a comma,
    // so get_encoding_from_base64() reaches base64.b64decode() and cv2.imdecode()
    // returns None (graceful path) rather than failing the initial .split(",").
    private static final String DUMMY_FACE_IMAGE_WITH_COMMA =
            "data:image/jpeg;base64,AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    // No comma at all -> get_encoding_from_base64()'s `header, encoded = image_data.split(",", 1)`
    // raises ValueError, which login() does not catch. See docs/DEFECT_LOG.xlsx BUG-08.
    private static final String MALFORMED_FACE_IMAGE_NO_COMMA = "not-a-data-url";

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = TestConfig.BASE_URL;
        RestAssured.useRelaxedHTTPSValidation();
        SeedUtil.ensureSeedUser();
    }

    @AfterClass
    public void cleanup() {
        SeedUtil.removeSeedUser();
    }

    @Test(description = "TC-LG-API-01: Missing email is rejected before any DB/face lookup")
    public void missingEmailRejected() {
        Response resp = given()
                .redirects().follow(true)
                .formParam("email", "")
                .formParam("blink_count", "5")
                .formParam("face_image", DUMMY_FACE_IMAGE_WITH_COMMA)
                .when().post("/login");

        Assert.assertEquals(resp.statusCode(), 200);
        Assert.assertTrue(resp.getBody().asString().contains("Incomplete login data"));
    }

    @Test(description = "TC-LG-API-02: Missing blink_count is rejected")
    public void missingBlinkCountRejected() {
        Response resp = given()
                .redirects().follow(true)
                .formParam("email", TestConfig.SEED_EMAIL)
                .formParam("blink_count", "")
                .formParam("face_image", DUMMY_FACE_IMAGE_WITH_COMMA)
                .when().post("/login");

        Assert.assertEquals(resp.statusCode(), 200);
        Assert.assertTrue(resp.getBody().asString().contains("Incomplete login data"));
    }

    @Test(description = "TC-LG-API-03: Non-numeric blink_count is rejected")
    public void nonNumericBlinkCountRejected() {
        Response resp = given()
                .redirects().follow(true)
                .formParam("email", TestConfig.SEED_EMAIL)
                .formParam("blink_count", "five")
                .formParam("face_image", DUMMY_FACE_IMAGE_WITH_COMMA)
                .when().post("/login");

        Assert.assertEquals(resp.statusCode(), 200);
        Assert.assertTrue(resp.getBody().asString().contains("Incomplete login data"));
    }

    @Test(description = "TC-LG-API-04: Missing face_image is rejected")
    public void missingFaceImageRejected() {
        Response resp = given()
                .redirects().follow(true)
                .formParam("email", TestConfig.SEED_EMAIL)
                .formParam("blink_count", "5")
                .when().post("/login");

        Assert.assertEquals(resp.statusCode(), 200);
        Assert.assertTrue(resp.getBody().asString().contains("Incomplete login data"));
    }

    @Test(description = "TC-LG-API-05: Unregistered email is rejected with 'Email not registered', " +
            "checked BEFORE any face/blink comparison")
    public void unregisteredEmailRejected() {
        Response resp = given()
                .redirects().follow(true)
                .formParam("email", TestConfig.NON_EXISTENT_EMAIL)
                .formParam("blink_count", "5")
                .formParam("face_image", DUMMY_FACE_IMAGE_WITH_COMMA)
                .when().post("/login");

        Assert.assertEquals(resp.statusCode(), 200);
        Assert.assertTrue(resp.getBody().asString().contains("Email not registered"));
    }

    @Test(description = "TC-SEC-05 / BUG-08: face_image with no comma crashes the request " +
            "(unhandled ValueError from String.split unpacking) instead of failing gracefully. " +
            "If app.py is fixed to catch this, update the expected status to 200.")
    public void malformedFaceImageCausesServerError() {
        Response resp = given()
                .redirects().follow(false)
                .formParam("email", TestConfig.SEED_EMAIL)
                .formParam("blink_count", "5")
                .formParam("face_image", MALFORMED_FACE_IMAGE_NO_COMMA)
                .when().post("/login");

        // Current (buggy) expected behaviour per code review: HTTP 500.
        Assert.assertEquals(resp.statusCode(), 500,
                "Expected an unhandled server error for a face_image with no comma - " +
                "see docs/DEFECT_LOG.xlsx BUG-08. If this now returns 200 with a graceful " +
                "flash message, the bug has been fixed - update this assertion.");
    }

    @Test(description = "TC-NAV-API-02: GET /login renders the login form")
    public void getLoginRendersForm() {
        Response resp = given().when().get("/login");
        Assert.assertEquals(resp.statusCode(), 200);
        Assert.assertTrue(resp.getBody().asString().contains("Login"));
    }
}
