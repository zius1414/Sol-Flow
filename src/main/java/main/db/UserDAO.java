package main.db;

import java.sql.*;

public final class UserDAO {
    public static class User {
        public final int id;
        public final String username;
        public User(int id, String username) { this.id = id; this.username = username; }
    }

    public static int insert(String username, String passwordHash, String salt) {
        long now = System.currentTimeMillis();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO users(username,password_hash,salt,created_at) VALUES (?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, salt);
            ps.setLong(4, now);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static User getByUsername(String username) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, username FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new User(rs.getInt("id"), rs.getString("username"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public static class AuthRecord {
        public final int id;
        public final String username;
        public final String passwordHash;
        public final String salt;
        public AuthRecord(int id, String username, String passwordHash, String salt) {
            this.id = id; this.username = username; this.passwordHash = passwordHash; this.salt = salt;
        }
    }

    public static AuthRecord getAuthByUsername(String username) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, username, password_hash, salt FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new AuthRecord(rs.getInt("id"), rs.getString("username"), rs.getString("password_hash"), rs.getString("salt"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
}
