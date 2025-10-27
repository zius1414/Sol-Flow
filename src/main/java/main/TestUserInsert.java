package main;

import main.db.Database;
import main.db.UserDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class TestUserInsert {
    public static void main(String[] args) {
        Database.init();
        String u = "test_dup_user";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE username = ?")) {
            ps.setString(1, u); ps.executeUpdate();
        } catch (Exception ignored) {}

        int id1 = UserDAO.insert(u, "h", "s");
        System.out.println("first-insert-id=" + id1);
        int id2 = UserDAO.insert(u, "h2", "s2");
        System.out.println("second-insert-result=" + id2);
        if (id2 == -2) System.out.println("duplicate-detected: PASS"); else System.out.println("duplicate-detected: FAIL");
    }
}
