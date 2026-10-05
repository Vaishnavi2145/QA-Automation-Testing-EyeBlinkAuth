package com.vaishnavi.qa.service;

/**
 * Mirrors app.py's get_user_by_email(email) at an interface level, so
 * LoginValidator's branching logic can be unit-tested with Mockito
 * without needing a real MySQL connection.
 */
public interface UserLookupService {
    StoredUser findByEmail(String email);

    class StoredUser {
        public final String email;
        public final int blinkCount;

        public StoredUser(String email, int blinkCount) {
            this.email = email;
            this.blinkCount = blinkCount;
        }
    }
}
