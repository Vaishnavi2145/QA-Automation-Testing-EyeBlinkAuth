# Eye-Blink Authentication System — QA & Test Automation Suite

A QA test suite built against your actual `app.py` (Flask + MySQL + OpenCV/face_recognition),
covering Manual Testing, Selenium WebDriver UI automation, REST Assured API testing, and
JUnit + Mockito unit tests — matching the stack on your resume.

This isn't a generic template: the test cases and defects below came from actually reading
your `app.py` and templates line by line. Several are real bugs, not hypothetical edge cases.

## What's inside

```
eyeblink-qa-suite/
├── pom.xml
├── docs/
│   ├── TEST_CASES.xlsx      <- 36 manual + automated test cases (STLC-style)
│   ├── DEFECT_LOG.xlsx      <- 11 defects found via code review, with repro steps
│   ├── build_test_cases.py  <- regenerates TEST_CASES.xlsx
│   └── build_defect_log.py  <- regenerates DEFECT_LOG.xlsx
└── src/
    ├── main/java/com/vaishnavi/qa/
    │   ├── util/BlinkCountValidator.java     <- mirrors app.py's isdigit() check
    │   └── service/{UserLookupService,LoginValidator}.java
    └── test/java/
        ├── config/   <- TestConfig, SeedUtil (JDBC test-data seeding)
        ├── pages/    <- Page Object Model: Index, Signup, Login, Dashboard, Files
        ├── tests/    <- Selenium + TestNG UI tests
        ├── api/      <- REST Assured API tests
        └── unit/     <- JUnit + Mockito unit tests
```

## Why some things are "Manual" instead of automated

Two things in this app make full end-to-end automation impossible without hardware/real
biometric data, and I've been upfront about it everywhere it matters rather than faking
coverage:

1. **Signup captures the face server-side.** `capture_face_samples()` opens a blocking OpenCV
   window (`cv2.VideoCapture` + `cv2.imshow`/`waitKey`) directly in the Flask request handler.
   There's no way to drive that from Selenium or a plain HTTP call — doing so would hang the
   server waiting for a real keypress on its own machine. This is itself **BUG-01** in the
   defect log (a server that blocks on local hardware input isn't deployable). Only the
   early-return validation branch of `/signup` is safe to test over HTTP, so that's all
   `api.SignupApiTests` covers.

2. **A genuine login needs a real, previously-registered face.** `/login`'s face capture is
   client-side (browser `getUserMedia` → canvas → base64), which unlike signup *could* be
   automated with Chrome's fake-camera flags (see the commented-out block in `tests/BaseTest.java`)
   — but only if fed a video of a face that's already been registered via a real signup. I don't
   have that data, so the full positive login flow (`TC-LG-01`), and everything downstream of it
   (dashboard, upload, download, logout as a genuinely logged-in user), are documented as manual
   test cases in `TEST_CASES.xlsx` instead of automated.

Everything server-side that doesn't require a real face — missing fields, unregistered emails,
access control on every protected route, and the IDOR on `/download` — **is** automated.

## The most important finding

**BUG-03 (Critical):** `/download/<email>/<filename>` has no session check at all, unlike
`/dashboard`, `/files`, and `/upload`, which all check `if "user_email" not in session`. Any
file any user has ever uploaded can be downloaded by anyone who knows or guesses their email
and a filename — no login required. `api.AccessControlApiTests#downloadWithoutSessionSucceeds`
proves this automatically against your existing `static/uploads/` fixture data.

This is the kind of finding worth walking an interviewer through directly — it shows you found
a real, exploitable defect through analysis, not just filled in a happy-path checklist.

## First run checklist

1. **Start your Flask app** locally (`python app.py`), with MySQL running and
   `db/blinkauth.sql` imported.
2. **Seed data**: `config.SeedUtil` inserts/removes a test user (`qa.automation@example.com`)
   directly via JDBC for tests that need "a registered user" without going through `/signup`.
   If your local DB uses different credentials than `root` / empty password, pass them:
   ```
   mvn test -Ddb.user=root -Ddb.password=yourpassword
   ```
3. **Run the UI + API suite** (TestNG):
   ```
   mvn test
   ```
   Override the target URL if it's not `http://127.0.0.1:5000`:
   ```
   mvn test -Dbase.url=http://127.0.0.1:5000
   ```
   Watch it run in a real browser instead of headless:
   ```
   mvn test -Dheadless=false
   ```
4. **Run the JUnit + Mockito unit tests separately** (these mirror app logic in Java, so they
   don't need the server running at all):
   ```
   mvn test -Dtest=BlinkCountValidatorTest,LoginValidatorTest
   ```
5. A few assertions (marked in code comments) were derived from reading `app.py`, not from a
   live run against your machine, since this environment couldn't run your Flask+MySQL+OpenCV
   stack directly. Run the suite once, and if any of those specific assertions don't match what
   you actually see (they're commented clearly, e.g. `LoginApiTests#malformedFaceImageCausesServerError`),
   tell me the actual result and I'll correct the test.

## Requirements

- Java 11+, Maven, Google Chrome (for Selenium — WebDriverManager downloads the matching driver)
- Your Flask app + MySQL running locally
- First `mvn test` needs internet access to Maven Central to pull dependencies

## For your resume / interviews

This suite backs up the exact bullet points already on your QA resume:
- Manual test case design with STLC (positive/negative/boundary) → `TEST_CASES.xlsx`
- Defect reporting with severity/priority → `DEFECT_LOG.xlsx`
- UI automation with Selenium + TestNG + POM → `src/test/java/pages`, `tests`
- API testing with REST Assured → `src/test/java/api`
- Unit testing with JUnit + Mockito → `src/test/java/unit`
- Build management with Maven → `pom.xml`
