package unit;

import com.vaishnavi.qa.service.LoginValidator;
import com.vaishnavi.qa.service.UserLookupService;
import com.vaishnavi.qa.util.BlinkCountValidator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

/**
 * TC-UNIT-09..13 : mocks the DB-facing UserLookupService with Mockito so
 * LoginValidator's branching (mirroring app.py's login()) can be verified
 * without a real MySQL connection.
 */
public class LoginValidatorTest {

    @Mock
    private UserLookupService userLookupService;

    private LoginValidator loginValidator;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        loginValidator = new LoginValidator(userLookupService, new BlinkCountValidator());
    }

    @Test
    public void missingEmail_returnsMissingFields_andNeverHitsDb() {
        LoginValidator.Result result = loginValidator.validate("", "5");
        assertEquals(LoginValidator.Result.MISSING_FIELDS, result);
        verify(userLookupService, never()).findByEmail(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    public void nonNumericBlinkCount_returnsMissingFields() {
        LoginValidator.Result result = loginValidator.validate("user@example.com", "abc");
        assertEquals(LoginValidator.Result.MISSING_FIELDS, result);
    }

    @Test
    public void unregisteredEmail_returnsEmailNotRegistered() {
        when(userLookupService.findByEmail("nobody@example.com")).thenReturn(null);

        LoginValidator.Result result = loginValidator.validate("nobody@example.com", "5");

        assertEquals(LoginValidator.Result.EMAIL_NOT_REGISTERED, result);
        verify(userLookupService).findByEmail("nobody@example.com");
    }

    @Test
    public void blinkCountMismatch_returnsBlinkMismatch() {
        when(userLookupService.findByEmail("user@example.com"))
                .thenReturn(new UserLookupService.StoredUser("user@example.com", 7));

        LoginValidator.Result result = loginValidator.validate("user@example.com", "3");

        assertEquals(LoginValidator.Result.BLINK_MISMATCH, result);
    }

    @Test
    public void matchingEmailAndBlinkCount_returnsOk() {
        when(userLookupService.findByEmail("user@example.com"))
                .thenReturn(new UserLookupService.StoredUser("user@example.com", 7));

        LoginValidator.Result result = loginValidator.validate("user@example.com", "7");

        assertEquals(LoginValidator.Result.OK, result);
    }
}
