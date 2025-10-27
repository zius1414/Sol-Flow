package main.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class ClientDAO {
    private ClientDAO() {}

    public static class Client {
        public final int id;
        public String name, company, email, phone;
        public long createdAt, updatedAt;
        public Client(int id, String name, String company, String email, String phone, long createdAt, long updatedAt) {
            this.id = id; this.name = name; this.company = company; this.email = email; this.phone = phone; this.createdAt = createdAt; this.updatedAt = updatedAt;
        }
    }

    public static List<Client> listAll() {
        List<Client> out = new ArrayList<>();
        String q = "SELECT id, name, company, email, phone, created_at, updated_at FROM clients ORDER BY name";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(q); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new Client(
                        rs.getInt("id"), rs.getString("name"), rs.getString("company"), rs.getString("email"), rs.getString("phone"), rs.getLong("created_at"), rs.getLong("updated_at")
                ));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return out;
    }

    public static int insert(String name, String company, String email, String phone) {
        String sql = "INSERT INTO clients(name,company,email,phone,created_at) VALUES(?,?,?,?,?)";
        long now = System.currentTimeMillis();
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, company);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setLong(5, now);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); }
        } catch (Exception ex) { ex.printStackTrace(); }
        return -1;
    }
}
