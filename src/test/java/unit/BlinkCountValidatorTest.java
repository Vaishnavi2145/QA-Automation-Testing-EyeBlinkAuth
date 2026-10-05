package unit;

import com.vaishnavi.qa.util.BlinkCountValidator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * TC-UNIT-01..08 : boundary and negative-value coverage for the
 * blink_count.isdigit() rule from app.py.
 */
public class BlinkCountValidatorTest {

    private BlinkCountValidator validator;

    @Before
    public void setUp() {
        validator = new BlinkCountValidator();
    }

    @Test
    public void rejectsNullInput() {
        assertFalse(validator.isValidDigitString(null));
    }

    @Test
    public void rejectsEmptyString() {
        assertFalse(validator.isValidDigitString(""));
    }

    @Test
    public void rejectsNonNumeric() {
        assertFalse(validator.isValidDigitString("abc"));
    }

    @Test
    public void rejectsNegativeSign_boundaryCase() {
        // Python's "-1".isdigit() is False - the sign character isn't a digit.
        assertFalse(validator.isValidDigitString("-1"));
    }

    @Test
    public void rejectsDecimalPoint() {
        assertFalse(validator.isValidDigitString("3.5"));
    }

    @Test
    public void acceptsZero_boundaryCase() {
        assertTrue(validator.isValidDigitString("0"));
    }

    @Test
    public void acceptsSingleDigit() {
        assertTrue(validator.isValidDigitString("5"));
    }

    @Test
    public void acceptsMultiDigit() {
        assertTrue(validator.isValidDigitString("42"));
    }

    @Test
    public void blinkCountsMatch_equalValues() {
        assertTrue(validator.blinkCountsMatch(5, 5));
    }

    @Test
    public void blinkCountsMatch_differentValues() {
        assertFalse(validator.blinkCountsMatch(5, 3));
    }
}
