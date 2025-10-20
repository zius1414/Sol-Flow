package main.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class CardDAO {
    public static int insert(String title, int x, int y, int w, int h, String content) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO cards (title,x,y,w,h,content,updated_at) VALUES(?,?,?,?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setInt(2, x); ps.setInt(3, y); ps.setInt(4, w); ps.setInt(5, h);
            ps.setString(6, content); ps.setLong(7, System.currentTimeMillis());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); }
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    public static List<CardRecord> listAll() {
        List<CardRecord> out = new ArrayList<>();
        try (Connection c = Database.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT id,title,x,y,w,h,content FROM cards ORDER BY updated_at DESC")) {
            while (rs.next()) out.add(new CardRecord(
                    rs.getInt("id"), rs.getString("title"),
                    rs.getInt("x"), rs.getInt("y"), rs.getInt("w"), rs.getInt("h"), rs.getString("content")));
        } catch (Exception e) { e.printStackTrace(); }
        return out;
    }

    public static class CardRecord {
        public final int id; public final String title; public final int x,y,w,h; public final String content;
        public CardRecord(int id, String title,int x,int y,int w,int h,String content) {
            this.id=id;this.title=title;this.x=x;this.y=y;this.w=w;this.h=h;this.content=content;
        }
    }
}