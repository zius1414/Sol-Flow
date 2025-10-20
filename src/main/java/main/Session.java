package main;

/**
 * Simple session holder - stores current user id and username for the running app.
 * This is intentionally minimal; it's stored in memory only.
 */
public final class Session {
    private static int currentUserId = 0;
    private static String currentUsername = null;

    public static void set(int userId, String username) { currentUserId = userId; currentUsername = username; }
    public static void clear() { currentUserId = 0; currentUsername = null; }
    public static int getUserId() { return currentUserId; }
    public static String getUsername() { return currentUsername; }
}
