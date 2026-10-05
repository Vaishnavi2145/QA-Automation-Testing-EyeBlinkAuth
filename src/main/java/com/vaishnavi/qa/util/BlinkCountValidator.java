package com.vaishnavi.qa.util;

/**
 * Pure-Java mirror of the validation app.py performs inline as
 * `blink_count.isdigit()` before a signup/login request is accepted.
 *
 * This class exists so the suite can demonstrate JUnit unit testing
 * against equivalent logic - Mockito/JUnit cannot directly unit-test the
 * Python Flask handler itself, so this is a clearly-scoped Java
 * re-implementation of just that one rule, not a test of app.py.
 */
public class BlinkCountValidator {

    /** Mirrors Python's str.isdigit(): only non-empty strings of digits 0-9. */
    public boolean isValidDigitString(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /** Mirrors the exact comparison in app.py's login(): int(blink_count) != int(user.blink_count) */
    public boolean blinkCountsMatch(int submittedCount, int storedCount) {
        return submittedCount == storedCount;
    }
}
