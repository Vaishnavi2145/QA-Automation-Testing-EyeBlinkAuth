package api;

import config.TestConfig;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

/**
 * TC-SEC-* : server-side (no browser, no cookies at all) checks of every
 * route that is SUPPOSED to require a logged-in session.
 *
 * The headline finding: /download/<email>/<filename> has NO session check
 * at all in app.py, unlike /dashboard, /files and /upload which all guard
 * with `if "user_email" not in session`. That means any file any user has
 * ever uploaded can be downloaded by anyone who knows (or guesses) the
 * owner's email and the filename - no login required. See
 * docs/DEFECT_LOG.xlsx BUG-03 (Critical) for full details and remediation.
 *
 * downloadWithoutSessionSucceeds() below targets a file that ships in the
 * sample project fixtures (static/uploads/shri@gmail.com/segmentation_results.png).
 * If you've cleared that fixture data, update KNOWN_EMAIL / KNOWN_FILENAME
 * to any file that exists under static/uploads/ on your machine.
 */
public class AccessControlApiTests {

    private static final String KNOWN_EMAIL = "shri@gmail.com";
    private static final String KNOWN_FILENAME = "segmentation_results.png";

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = TestConfig.BASE_URL;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test(description = "TC-SEC-06: Unauthenticated GET /dashboard redirects to /login (protected correctly)")
    public void dashboardRequiresSession() {
        Response resp = given().redirects().follow(false).when().get("/dashboard");
        Assert.assertEquals(resp.statusCode(), 302);
        Assert.assertTrue(resp.getHeader("Location").contains("/login"));
    }

    @Test(description = "TC-SEC-07: Unauthenticated GET /files redirects to /login (protected correctly)")
    public void filesRequiresSession() {
        Response resp = given().redirects().follow(false).when().get("/files");
        Assert.assertEquals(resp.statusCode(), 302);
        Assert.assertTrue(resp.getHeader("Location").contains("/login"));
    }

    @Test(description = "TC-SEC-08: Unauthenticated POST /upload redirects to /login (protected correctly)")
    public void uploadRequiresSession() {
        Response resp = given()
                .redirects().follow(false)
                .multiPart("file", "poc.txt", "not a real upload".getBytes())
                .when().post("/upload");
        Assert.assertEquals(resp.statusCode(), 302);
        Assert.assertTrue(resp.getHeader("Location").contains("/login"));
    }

    @Test(description = "TC-SEC-09 / BUG-03 (Critical): Unauthenticated GET /download/{email}/{filename} " +
            "SUCCEEDS with no session cookie at all - Insecure Direct Object Reference. " +
            "This test currently PASSES because the vulnerability is present; if BUG-03 is fixed " +
            "(a session check is added), this assertion should be changed to expect a redirect, " +
            "matching dashboardRequiresSession() / filesRequiresSession() above.")
    public void downloadWithoutSessionSucceeds() {
        Response resp = given()
                .redirects().follow(false)
                .when().get("/download/" + KNOWN_EMAIL + "/" + KNOWN_FILENAME);

        Assert.assertEquals(resp.statusCode(), 200,
                "No auth required to download another user's file - unlike /dashboard, /files " +
                "and /upload, this route has no `if \"user_email\" not in session` check in app.py.");
    }

    @Test(description = "TC-SEC-10: GET /download for a non-existent user folder returns 404 " +
            "(sanity check - the route works normally, it's just unauthenticated)")
    public void downloadForUnknownUserReturns404() {
        Response resp = given()
                .redirects().follow(false)
                .when().get("/download/definitely-not-a-real-user@example.com/whatever.txt");
        Assert.assertEquals(resp.statusCode(), 404);
    }
}
