package main.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class WorkflowDAO {
    public static class Workflow {
        public final int id;
        public final String name;
        public final long createdAt;
        public Workflow(int id, String name, long createdAt) { this.id = id; this.name = name; this.createdAt = createdAt; }
    }

    public static int insert(String name) {
        long now = System.currentTimeMillis();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO workflows(name,created_at,updated_at) VALUES (?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setLong(2, now);
            ps.setLong(3, now);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    // insert a workflow for a specific user (userId==0 -> global)
    public static int insert(String name, int userId) {
        long now = System.currentTimeMillis();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO workflows(name,created_at,updated_at,user_id) VALUES (?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setLong(2, now);
            ps.setLong(3, now);
            ps.setInt(4, userId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    // legacy: list all workflows (global and user-agnostic)
    public static List<Workflow> listAll() {
        return listForUser(0);
    }

    // list workflows for a specific user (userId==0 -> global)
    public static List<Workflow> listForUser(int userId) {
        List<Workflow> out = new ArrayList<>();
        String q = "SELECT id,name,created_at FROM workflows WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(q)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(new Workflow(rs.getInt("id"), rs.getString("name"), rs.getLong("created_at")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return out;
    }

    public static Workflow getById(int id) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id,name,created_at FROM workflows WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new Workflow(rs.getInt("id"), rs.getString("name"), rs.getLong("created_at"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
}