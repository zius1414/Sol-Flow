package main.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class SheetDAO {
    public static int saveSheet(String name, String csv) throws SQLException {
        long now = System.currentTimeMillis();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO sheets (name,csv,updated_at) VALUES (?,?,?) " +
                     "ON CONFLICT(name) DO UPDATE SET csv=excluded.csv, updated_at=excluded.updated_at")) {
            ps.setString(1, name); ps.setString(2, csv); ps.setLong(3, now);
            ps.executeUpdate();
        }
        // return the id
        try (Connection c = Database.getConnection();
             PreparedStatement ps2 = c.prepareStatement("SELECT id FROM sheets WHERE name = ?")) {
            ps2.setString(1, name);
            try (ResultSet rs = ps2.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    setLastSheetId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public static void updateSheet(int id, String csv) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE sheets SET csv = ?, updated_at = ? WHERE id = ?")) {
            ps.setString(1, csv); ps.setLong(2, System.currentTimeMillis()); ps.setInt(3, id); ps.executeUpdate();
            setLastSheetId(id);
        }
    }

    public static List<SheetRecord> listSheets() {
        List<SheetRecord> out = new ArrayList<>();
        try (Connection c = Database.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT id,name,csv,updated_at FROM sheets ORDER BY updated_at DESC")) {
            while (rs.next()) out.add(new SheetRecord(rs.getInt("id"), rs.getString("name"), rs.getString("csv")));
        } catch (Exception e) { e.printStackTrace(); }
        return out;
    }

    public static SheetRecord getSheetById(int id) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id,name,csv FROM sheets WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new SheetRecord(rs.getInt("id"), rs.getString("name"), rs.getString("csv"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public static SheetRecord getLastSavedSheet() {
        Integer id = getLastSheetId();
        if (id != null) {
            SheetRecord r = getSheetById(id);
            if (r != null) return r;
        }
        // fallback to latest updated
        List<SheetRecord> all = listSheets();
        return all.isEmpty() ? null : all.get(0);
    }

    private static void setLastSheetId(int id) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO settings(key,value) VALUES('last_sheet_id',?) " +
                             "ON CONFLICT(key) DO UPDATE SET value=excluded.value")) {
            ps.setString(1, String.valueOf(id));
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    private static Integer getLastSheetId() {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT value FROM settings WHERE key = 'last_sheet_id'")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Integer.valueOf(rs.getString(1));
            }
        } catch (Exception ignored) {}
        return null;
    }

    public static class SheetRecord {
        public final int id; public final String name; public final String csv;
        public SheetRecord(int id, String name, String csv) { this.id = id; this.name = name; this.csv = csv; }
    }
}