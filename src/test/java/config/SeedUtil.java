package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Seeds / removes the known test user directly in MySQL so tests that need
 * an "existing registered user" (e.g. login negative-path tests, access
 * control tests) don't depend on driving the real /signup form — which
 * cannot be automated. See TestConfig and docs/DEFECT_LOG.xlsx (BUG-01).
 *
 * Requires a MySQL instance matching db/blinkauth.sql already running and
 * reachable with the credentials in TestConfig. If your local DB uses
 * different credentials, pass them as system properties, e.g.:
 *   mvn test -Ddb.user=root -Ddb.password=yourpassword
 */
public class SeedUtil {

    private static String jdbcUrl() {
        return "jdbc:mysql://" + TestConfig.DB_HOST + ":3306/" + TestConfig.DB_NAME
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(jdbcUrl(), TestConfig.DB_USER, TestConfig.DB_PASSWORD);
    }

    /** Inserts the seed user if it doesn't already exist. Safe to call repeatedly. */
    public static void ensureSeedUser() {
        String checkSql = "SELECT id FROM users WHERE email = ?";
        String insertSql = "INSERT INTO users (name, email, phone, gender, address, blink_count) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = connect()) {
            try (PreparedStatement check = con.prepareStatement(checkSql)) {
                check.setString(1, TestConfig.SEED_EMAIL);
                if (check.executeQuery().next()) {
                    return; // already present
                }
            }
            try (PreparedStatement insert = con.prepareStatement(insertSql)) {
                insert.setString(1, TestConfig.SEED_NAME);
                insert.setString(2, TestConfig.SEED_EMAIL);
                insert.setString(3, "9999999999");
                insert.setString(4, "Other");
                insert.setString(5, "QA automation seed row - safe to delete");
                insert.setInt(6, TestConfig.SEED_BLINK_COUNT);
                insert.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Could not seed test user. Is MySQL running with the schema from " +
                    "db/blinkauth.sql, and are the db.* system properties correct? " +
                    "Underlying error: " + e.getMessage(), e);
        }
    }

    /** Removes the seed user. Call from an @AfterSuite to leave the DB clean. */
    public static void removeSeedUser() {
        String deleteSql = "DELETE FROM users WHERE email = ?";
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(deleteSql)) {
            stmt.setString(1, TestConfig.SEED_EMAIL);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Warning: could not clean up seed user: " + e.getMessage());
        }
    }

    /** Checks whether a given email currently exists in the users table. */
    public static boolean emailExists(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection con = connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, email);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            throw new RuntimeException("DB check failed: " + e.getMessage(), e);
        }
    }
}
