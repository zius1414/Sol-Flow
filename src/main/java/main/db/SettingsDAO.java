package main.db;

import java.sql.*;

/**
 * Tiny settings helper stored in the `settings` table (key/value TEXT).
 */
public final class SettingsDAO {
    private SettingsDAO() {}

    public static String getString(String key, String defaultValue) {
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT value FROM settings WHERE key = ?")) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (Exception ignored) {}
        return defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        String v = getString(key, null);
        if (v == null) return defaultValue;
        try { return Integer.parseInt(v); } catch (Exception ignored) { return defaultValue; }
    }

    public static void setString(String key, String value) {
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(
                "INSERT INTO settings(key,value) VALUES(?,?) ON CONFLICT(key) DO UPDATE SET value=excluded.value")) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    public static void setInt(String key, int value) { setString(key, String.valueOf(value)); }
}
