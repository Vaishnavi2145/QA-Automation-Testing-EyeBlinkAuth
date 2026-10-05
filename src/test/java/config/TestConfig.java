package config;

/**
 * Central place for environment values the suite needs.
 * Override base.url with: mvn test -Dbase.url=http://127.0.0.1:5000
 *
 * NOTE ON TEST DATA:
 * The app's /signup route triggers a server-side OpenCV webcam capture
 * (cv2.VideoCapture(0) + a blocking cv2.imshow/waitKey loop) — see
 * docs/DEFECT_LOG.xlsx, BUG-01. That makes /signup impossible to drive
 * through Selenium or a plain HTTP client. So this suite seeds a known
 * test user directly into the `users` table (SeedUtil) instead of
 * automating signup, and documents signup only as manual test cases.
 */
public class TestConfig {

    public static final String BASE_URL =
            System.getProperty("base.url", "http://127.0.0.1:5000");

    // MySQL connection - matches app.py's dbconnection()
    public static final String DB_HOST = System.getProperty("db.host", "127.0.0.1");
    public static final String DB_NAME = System.getProperty("db.name", "blinkcountauth");
    public static final String DB_USER = System.getProperty("db.user", "root");
    public static final String DB_PASSWORD = System.getProperty("db.password", "");

    // Known seeded user (see SeedUtil / db/blinkauth.sql sample row)
    public static final String SEED_EMAIL = "qa.automation@example.com";
    public static final String SEED_NAME = "QA Automation";
    public static final int SEED_BLINK_COUNT = 5;

    // An email guaranteed NOT to exist in the users table
    public static final String NON_EXISTENT_EMAIL = "no.such.user@example.com";
}
