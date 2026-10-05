package com.vaishnavi.qa.service;

import com.vaishnavi.qa.util.BlinkCountValidator;

/**
 * Mirrors the ORDER of checks in app.py's login():
 *   1. required fields present
 *   2. user exists for that email
 *   3. (face match - out of scope here, handled by the QA suite's manual
 *      test cases and LoginApiTests)
 *   4. blink count matches
 *
 * This lets LoginValidatorTest demonstrate Mockito mocking of the DB-facing
 * dependency (UserLookupService) while asserting the branching outcome,
 * which is a common real-world pattern for unit-testing controller logic
 * without hitting a real database.
 */
public class LoginValidator {

    public enum Result {
        MISSING_FIELDS,
        EMAIL_NOT_REGISTERED,
        BLINK_MISMATCH,
        OK
    }

    private final UserLookupService userLookupService;
    private final BlinkCountValidator blinkCountValidator;

    public LoginValidator(UserLookupService userLookupService, BlinkCountValidator blinkCountValidator) {
        this.userLookupService = userLookupService;
        this.blinkCountValidator = blinkCountValidator;
    }

    public Result validate(String email, String blinkCountInput) {
        if (email == null || email.isEmpty()
                || !blinkCountValidator.isValidDigitString(blinkCountInput)) {
            return Result.MISSING_FIELDS;
        }

        UserLookupService.StoredUser user = userLookupService.findByEmail(email);
        if (user == null) {
            return Result.EMAIL_NOT_REGISTERED;
        }

        int submitted = Integer.parseInt(blinkCountInput);
        if (!blinkCountValidator.blinkCountsMatch(submitted, user.blinkCount)) {
            return Result.BLINK_MISMATCH;
        }

        return Result.OK;
    }
}
