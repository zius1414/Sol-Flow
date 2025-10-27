package main.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TaskDAO: handles SQLExceptions internally so UI callers don't need to catch them.
 * - Creates tasks table with workflow_id (default 0)
 * - Public methods return safe defaults on error.
 */
public final class TaskDAO {
    private TaskDAO() {}

    public static class TaskRecord {
        public final int id;
        public String text;
        public boolean checked;
        public int ord;
        public final int workflowId;
        public long createdAt; // epoch seconds
        public long lastReminderSent; // epoch seconds (0 == never)
        public int reminderWindowMinutes; // 0 == use global

        public TaskRecord(int id, String text, boolean checked, int ord, int workflowId, long createdAt, long lastReminderSent, int reminderWindowMinutes) {
            this.id = id;
            this.text = text;
            this.checked = checked;
            this.ord = ord;
            this.workflowId = workflowId;
            this.createdAt = createdAt;
            this.lastReminderSent = lastReminderSent;
            this.reminderWindowMinutes = reminderWindowMinutes;
        }
    }

    static {
        ensureTable();
    }

    private static Connection conn() throws SQLException {
        return Database.getConnection();
    }

    private static void ensureTable() {
        String sql = "CREATE TABLE IF NOT EXISTS tasks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "text TEXT DEFAULT ''," +
                "checked INTEGER DEFAULT 0," +
                "ord INTEGER DEFAULT 0," +
                "workflow_id INTEGER DEFAULT 0," +
                "created_at INTEGER DEFAULT 0," +
                "last_reminder_sent INTEGER DEFAULT 0" +
                ")";
        try (Connection c = conn(); Statement s = c.createStatement()) {
            s.execute(sql);
        } catch (Exception ignored) { /* ignore initialization errors */ }
    }

    // Legacy: returns all tasks (on error returns empty list)
    public static List<TaskRecord> listAll() {
        String q = "SELECT id, text, checked, ord, workflow_id, created_at, last_reminder_sent, reminder_window_minutes FROM tasks ORDER BY ord ASC, id ASC";
        List<TaskRecord> out = new ArrayList<>();
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(q);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new TaskRecord(
                        rs.getInt("id"),
                        rs.getString("text"),
                        rs.getInt("checked") != 0,
                        rs.getInt("ord"),
                        rs.getInt("workflow_id"),
                        rs.getLong("created_at"),
                        rs.getLong("last_reminder_sent"),
                        rs.getInt("reminder_window_minutes")
                ));
            }
        } catch (Exception ex) {
            // return empty list on error
        }
        return out;
    }

    // List tasks for a workflow (workflowId==0 -> tasks with workflow_id = 0)
    public static List<TaskRecord> listForWorkflow(int workflowId) {
        String q = "SELECT id, text, checked, ord, workflow_id, created_at, last_reminder_sent, reminder_window_minutes FROM tasks WHERE workflow_id = ? ORDER BY ord ASC, id ASC";
        List<TaskRecord> out = new ArrayList<>();
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(q)) {
            ps.setInt(1, workflowId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new TaskRecord(
                            rs.getInt("id"),
                            rs.getString("text"),
                            rs.getInt("checked") != 0,
                            rs.getInt("ord"),
                            rs.getInt("workflow_id"),
                            rs.getLong("created_at"),
                            rs.getLong("last_reminder_sent"),
                            rs.getInt("reminder_window_minutes")
                    ));
                }
            }
        } catch (Exception ex) {
            // return empty on error
        }
        return out;
    }

    // Legacy insert -> inserts with workflow_id = 0
    public static int insert(String text, boolean checked, int ord) {
        return insert(text, checked, ord, 0);
    }

    // Insert with workflow scope; returns generated id or -1 on error
    public static int insert(String text, boolean checked, int ord, int workflowId) {
        String sql = "INSERT INTO tasks(text, checked, ord, workflow_id, created_at, last_reminder_sent) VALUES (?, ?, ?, ?, ?, ?)";
        long now = System.currentTimeMillis() / 1000L;
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, text == null ? "" : text);
            ps.setInt(2, checked ? 1 : 0);
            ps.setInt(3, ord);
            ps.setInt(4, workflowId);
            ps.setLong(5, now);
            ps.setLong(6, 0L);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (Exception ex) {
            // ignore and fall through
        }
        return -1;
    }

    // Update (silent on error)
    public static void update(int id, String text, boolean checked, int ord) {
        String sql = "UPDATE tasks SET text = ?, checked = ?, ord = ? WHERE id = ?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, text == null ? "" : text);
            ps.setInt(2, checked ? 1 : 0);
            ps.setInt(3, ord);
            ps.setInt(4, id);
            ps.executeUpdate();
        } catch (Exception ex) {
            // ignore
        }
    }

    // Return tasks that need a reminder: wrapper uses minutes (default 1440 = 24h)
    public static List<TaskRecord> listTasksNeedingReminder() {
        return listTasksNeedingReminderMinutes(1440);
    }

    // Return tasks that need a reminder based on a configurable window (minutes)
    public static List<TaskRecord> listTasksNeedingReminderMinutes(int minutes) {
        String q = "SELECT id, text, checked, ord, workflow_id, created_at, last_reminder_sent, reminder_window_minutes FROM tasks WHERE checked = 0 AND created_at > 0 AND created_at <= ? AND (last_reminder_sent IS NULL OR last_reminder_sent = 0)";
        List<TaskRecord> out = new ArrayList<>();
        long cutoff = (System.currentTimeMillis() / 1000L) - (long) minutes * 60L;
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(q)) {
            ps.setLong(1, cutoff);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new TaskRecord(
                            rs.getInt("id"),
                            rs.getString("text"),
                            rs.getInt("checked") != 0,
                            rs.getInt("ord"),
                            rs.getInt("workflow_id"),
                            rs.getLong("created_at"),
                            rs.getLong("last_reminder_sent"),
                            rs.getInt("reminder_window_minutes")
                    ));
                }
            }
        } catch (Exception ex) {
            // ignore
        }
        return out;
    }

    // set per-task reminder window (minutes)
    public static void setReminderWindowMinutes(int id, int minutes) {
        String sql = "UPDATE tasks SET reminder_window_minutes = ? WHERE id = ?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, minutes);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (Exception ex) {
            // ignore
        }
    }

    // mark reminder as sent for a task
    public static void setLastReminderSent(int id, long epochSeconds) {
        String sql = "UPDATE tasks SET last_reminder_sent = ? WHERE id = ?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, epochSeconds);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (Exception ex) {
            // ignore
        }
    }

    // Delete (silent on error)
    public static void delete(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception ex) {
            // ignore
        }
    }
}