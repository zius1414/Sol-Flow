package main.db;

import java.sql.*;
import java.io.File;

public final class Database {
    private static final String DB_NAME = "SolFlow.db";
    private static final String URL = "jdbc:sqlite:" + System.getProperty("user.dir") + File.separator + DB_NAME;

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ignored) {}
        return DriverManager.getConnection(URL);
    }

    public static void init() {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
        s.execute("PRAGMA foreign_keys = ON;");
    // create users table
    s.execute("CREATE TABLE IF NOT EXISTS users (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "username TEXT NOT NULL UNIQUE, " +
        "password_hash TEXT NOT NULL, " +
        "salt TEXT NOT NULL, " +
        "created_at INTEGER)");
        s.execute("CREATE TABLE IF NOT EXISTS files (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "path TEXT NOT NULL UNIQUE, " +
            "name TEXT, size INTEGER, mtime INTEGER, added_at INTEGER)");
            s.execute("CREATE TABLE IF NOT EXISTS sheets (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT UNIQUE, csv TEXT, updated_at INTEGER)");
            s.execute("CREATE TABLE IF NOT EXISTS settings (" +
                    "key TEXT PRIMARY KEY, value TEXT)");
            s.execute("CREATE TABLE IF NOT EXISTS workflows (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT NOT NULL, " +
            "created_at INTEGER, " +
            "updated_at INTEGER, " +
            "template INTEGER DEFAULT 0, " +
            "user_id INTEGER DEFAULT 0)");
            s.execute("CREATE TABLE IF NOT EXISTS tasks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "text TEXT NOT NULL, " +
                    "checked INTEGER DEFAULT 0, " +
                    "ord INTEGER DEFAULT 0, " +
                    "updated_at INTEGER)");
            // Ensure schema has workflow scoping columns for backwards compatibility
            try {
                // add workflow_id to tasks if missing
                s.execute("ALTER TABLE tasks ADD COLUMN workflow_id INTEGER DEFAULT 0");
            } catch (SQLException ignored) {}
            try {
                // add created_at to tasks if missing (epoch seconds)
                s.execute("ALTER TABLE tasks ADD COLUMN created_at INTEGER DEFAULT 0");
            } catch (SQLException ignored) {}
            try {
                // add last_reminder_sent to tasks if missing (epoch seconds)
                s.execute("ALTER TABLE tasks ADD COLUMN last_reminder_sent INTEGER DEFAULT 0");
            } catch (SQLException ignored) {}
            try {
                // add per-task reminder window (hours). 0 == use global setting
                s.execute("ALTER TABLE tasks ADD COLUMN reminder_window_hours INTEGER DEFAULT 0");
            } catch (SQLException ignored) {}
            try {
                // add per-task reminder window (minutes). 0 == use global setting
                s.execute("ALTER TABLE tasks ADD COLUMN reminder_window_minutes INTEGER DEFAULT 0");
            } catch (SQLException ignored) {}
            try {
                // add workflow_id to files if missing
                s.execute("ALTER TABLE files ADD COLUMN workflow_id INTEGER DEFAULT 0");
            } catch (SQLException ignored) {}
            try {
                // add user_id to workflows if missing
                s.execute("ALTER TABLE workflows ADD COLUMN user_id INTEGER DEFAULT 0");
            } catch (SQLException ignored) {}
            try {
                // add user_id to files if missing
                s.execute("ALTER TABLE files ADD COLUMN user_id INTEGER DEFAULT 0");
            } catch (SQLException ignored) {}
            s.execute("CREATE TABLE IF NOT EXISTS cards (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title TEXT, x INTEGER, y INTEGER, w INTEGER, h INTEGER, content TEXT, updated_at INTEGER)");
        // Sales / CRM tables
        s.execute("CREATE TABLE IF NOT EXISTS clients (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT NOT NULL, " +
            "company TEXT, " +
            "email TEXT, " +
            "phone TEXT, " +
            "created_at INTEGER, " +
            "updated_at INTEGER)");

        s.execute("CREATE TABLE IF NOT EXISTS opportunities (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "client_id INTEGER, " +
            "title TEXT NOT NULL, " +
            "value REAL DEFAULT 0, " +
            "status TEXT DEFAULT 'prospect', " +
            "stage TEXT DEFAULT 'in_progress', " +
            "owner_id INTEGER DEFAULT 0, " +
            "created_at INTEGER, " +
            "updated_at INTEGER, " +
            "workflow_id INTEGER DEFAULT 0)");

        s.execute("CREATE TABLE IF NOT EXISTS interactions (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "opportunity_id INTEGER, " +
            "kind TEXT, " +
            "note TEXT, " +
            "when_ts INTEGER, " +
            "created_at INTEGER)");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}