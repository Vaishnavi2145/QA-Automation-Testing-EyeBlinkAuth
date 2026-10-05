package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.IndexPage;
import pages.LoginPage;
import pages.SignupPage;

/**
 * TC-NAV-* : basic navigation / smoke coverage across the public pages.
 */
public class NavigationTests extends BaseTest {

    @Test(description = "TC-NAV-01: Home page loads and shows the app heading")
    public void homePageLoads() {
        IndexPage index = new IndexPage(driver);
        index.open(baseUrl);
        Assert.assertTrue(index.headingText().toLowerCase().contains("password authentication"),
                "Home page heading should mention password authentication");
    }

    @Test(description = "TC-NAV-02: 'Get Started' on home page navigates to Signup")
    public void getStartedGoesToSignup() {
        IndexPage index = new IndexPage(driver);
        index.open(baseUrl);
        SignupPage signup = index.clickGetStarted();
        Assert.assertTrue(signup.currentUrl().endsWith("/signup"));
    }

    @Test(description = "TC-NAV-03: Login nav link navigates to Login")
    public void loginNavGoesToLogin() {
        IndexPage index = new IndexPage(driver);
        index.open(baseUrl);
        LoginPage login = index.clickLoginNav();
        Assert.assertTrue(login.currentUrl().endsWith("/login"));
    }

    @Test(description = "TC-NAV-04: Direct navigation to /signup renders the signup form")
    public void directSignupNavigation() {
        SignupPage signup = new SignupPage(driver);
        signup.open(baseUrl);
        Assert.assertTrue(signup.isSubmitButtonPresent(), "Signup form should render a submit button");
    }

    @Test(description = "TC-NAV-05: Direct navigation to /login renders the login form")
    public void directLoginNavigation() {
        LoginPage login = new LoginPage(driver);
        login.open(baseUrl);
        Assert.assertTrue(login.isStartCameraButtonPresent(), "Login form should render a Start Camera button");
    }
}
