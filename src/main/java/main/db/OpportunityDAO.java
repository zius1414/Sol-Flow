package main.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class OpportunityDAO {
    private OpportunityDAO() {}

    public static class Opp {
        public final int id;
        public final int clientId;
        public String title;
        public double value;
        public String status;
        public String stage;
        public long createdAt, updatedAt;

        public Opp(int id, int clientId, String title, double value, String status, String stage, long createdAt, long updatedAt) {
            this.id = id; this.clientId = clientId; this.title = title; this.value = value; this.status = status; this.stage = stage; this.createdAt = createdAt; this.updatedAt = updatedAt;
        }
    }

    public static List<Opp> listAll() {
        List<Opp> out = new ArrayList<>();
        String q = "SELECT id, client_id, title, value, status, stage, created_at, updated_at FROM opportunities ORDER BY updated_at DESC";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(q); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new Opp(rs.getInt("id"), rs.getInt("client_id"), rs.getString("title"), rs.getDouble("value"), rs.getString("status"), rs.getString("stage"), rs.getLong("created_at"), rs.getLong("updated_at")));
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return out;
    }

    public static int insert(int clientId, String title, double value, String status, String stage) {
        String sql = "INSERT INTO opportunities(client_id,title,value,status,stage,created_at) VALUES(?,?,?,?,?,?)";
        long now = System.currentTimeMillis();
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, clientId);
            ps.setString(2, title);
            ps.setDouble(3, value);
            ps.setString(4, status);
            ps.setString(5, stage);
            ps.setLong(6, now);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); }
        } catch (Exception ex) { ex.printStackTrace(); }
        return -1;
    }
}
