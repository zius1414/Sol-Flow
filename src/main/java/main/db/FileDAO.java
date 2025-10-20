package main.db;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class FileDAO {
    // legacy: insert/update without workflow scope (workflow_id = 0)
    public static void insertOrUpdate(File f) {
        insertOrUpdate(f, 0);
    }

    // insert/update with workflow scope
    public static void insertOrUpdate(File f, int workflowId) {
        long now = System.currentTimeMillis();
        String sql = "INSERT INTO files (path,name,size,mtime,added_at,workflow_id) VALUES(?,?,?,?,?,?) " +
                "ON CONFLICT(path) DO UPDATE SET name=excluded.name,size=excluded.size,mtime=excluded.mtime, workflow_id=excluded.workflow_id";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, f.getAbsolutePath());
            ps.setString(2, f.getName());
            ps.setLong(3, f.length());
            ps.setLong(4, f.lastModified());
            ps.setLong(5, now);
            ps.setInt(6, workflowId);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // legacy: list all files regardless of workflow
    public static List<File> listAll() {
        return listForWorkflow(0);
    }

    // list files for a given workflow (workflowId==0 -> global files)
    public static List<File> listForWorkflow(int workflowId) {
        List<File> out = new ArrayList<>();
        String q = "SELECT path FROM files WHERE workflow_id = ? ORDER BY added_at DESC";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(q)) {
            ps.setInt(1, workflowId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(new File(rs.getString("path")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return out;
    }

    public static void deleteByPath(String path) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM files WHERE path = ?")) {
            ps.setString(1, path); ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
}