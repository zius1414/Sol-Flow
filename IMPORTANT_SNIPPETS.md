# Important code snippets — Sol-Flow

Generated: October 27, 2025

This file collects concise, focused snippets and short notes for the most important modules in the Sol-Flow project so you can quickly find core behavior.

## Overview
- Purpose: quick reference of entry points, database bootstrap, representative DAOs, and principal UI components.
- Files scanned when generating these snippets: `Main.java`, `Session.java`, `db/Database.java`, `db/UserDAO.java`, `db/ClientDAO.java`, `db/TaskDAO.java`, `ui/NavigationBar.java`, `ui/ContentCreatorApp.java`, `ui/DashboardUI.java`, `ui/WorkflowPage.java`, plus `pom.xml`, `build.gradle`, and `guide.md`.

---

## Core / App entry

`src/main/java/main/Main.java`

```java
package main;

import javax.swing.SwingUtilities;
import main.ui.NotionStyleUI;
import main.db.Database;

public class Main {
    public static void main(String[] args) {
        Database.init();
        SwingUtilities.invokeLater(() -> NotionStyleUI.show());
    }
}
```

`src/main/java/main/Session.java` (in-memory session holder)

```java
package main;

public final class Session {
    private static int currentUserId = 0;
    private static String currentUsername = null;

    public static void set(int userId, String username) { currentUserId = userId; currentUsername = username; }
    public static void clear() { currentUserId = 0; currentUsername = null; }
    public static int getUserId() { return currentUserId; }
    public static String getUsername() { return currentUsername; }
}
```

---

## Database bootstrap (SQLite)

`src/main/java/main/db/Database.java`

Key responsibilities: open SQLite connection and create/upgrade tables. Many `ALTER TABLE` attempts are performed inside try/catch blocks to maintain backward compatibility.

```java
package main.db;

import java.sql.*;
import java.io.File;

public final class Database {
    private static final String DB_NAME = "SolFlow.db";
    private static final String URL = "jdbc:sqlite:" + System.getProperty("user.dir") + File.separator + DB_NAME;

    public static Connection getConnection() throws SQLException {
        try { Class.forName("org.sqlite.JDBC"); } catch (ClassNotFoundException ignored) {}
        return DriverManager.getConnection(URL);
    }

    public static void init() {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON;");
            s.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL UNIQUE, password_hash TEXT NOT NULL, salt TEXT NOT NULL, created_at INTEGER)");
            
        } catch (SQLException ex) { ex.printStackTrace(); }
    }
}
```

---

## Representative DAOs

`src/main/java/main/db/UserDAO.java` — user insert and auth retrieval.

```java
package main.db;

import java.sql.*;

public final class UserDAO {
    public static class User { public final int id; public final String username; public User(int id, String username) { this.id = id; this.username = username; } }

    public static int insert(String username, String passwordHash, String salt) {
        long now = System.currentTimeMillis();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO users(username,password_hash,salt,created_at) VALUES (?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, salt);
            ps.setLong(4, now);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); }
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    
}
```

`src/main/java/main/db/ClientDAO.java` — simple list/insert for clients.

```java
package main.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class ClientDAO {
    public static class Client { public final int id; public String name, company, email, phone; public long createdAt, updatedAt; public Client(int id, String name, String company, String email, String phone, long createdAt, long updatedAt) { this.id = id; this.name = name; this.company = company; this.email = email; this.phone = phone; this.createdAt = createdAt; this.updatedAt = updatedAt; } }

    public static List<Client> listAll() { return new ArrayList<>(); }
    public static int insert(String name, String company, String email, String phone) { return -1; }
}
```

`src/main/java/main/db/TaskDAO.java` — task persistence, list, insert, update, delete, reminders, workflow scoping.

Highlights:
- Ensures `tasks` table on static init.
- Methods return safe defaults on error (empty lists, -1) so UI callers don't need to catch SQLExceptions.
- Supports `workflow_id` scoping and reminder metadata (created_at, last_reminder_sent, reminder_window_minutes).

---

## UI — representative components

`src/main/java/main/ui/NavigationBar.java` — shared top nav; use `NavigationBar.wrap(panel)` to embed.

`src/main/java/main/ui/ContentCreatorApp.java` — draggable/resizable card canvas. Contains inner classes `RoundedIconButton`, `DraggableCard`, and `PlaceholderTextArea`. Includes a preview `main()`.

`src/main/java/main/ui/DashboardUI.java` — Finance dashboard with `TransactionTablePanel`, `SpendingAreasPanel`, `CategoryCardPanel`, `CategoryDetailPanel`. Saves/loads CSV files `income.csv`, `expenses.csv`, `budget.csv`.

`src/main/java/main/ui/WorkflowPage.java` — workflow/task editor that loads tasks via `TaskDAO` (supports createBlank mode and per-workflow scoping). Right-hand steps panel can embed `ContentCreatorApp`, `MailOrganize`, or `Docupload`.

---

## Build & run (notes)
- `pom.xml` targets Java 17 and includes `org.xerial:sqlite-jdbc` and Apache POI.
- `build.gradle` snippet includes the SQLite JDBC dependency.

- `guide.md` contains a quick manual build/run flow using `javac` and a local `libs/sqlite-jdbc.jar` and running `java -cp "out/classes:libs/sqlite-jdbc.jar" main.Main`.

---

## Files I read (quick list)
- `src/main/java/main/Main.java` — app entry
- `src/main/java/main/Session.java`
- `src/main/java/main/db/Database.java`
- `src/main/java/main/db/UserDAO.java`
- `src/main/java/main/db/ClientDAO.java`
- `src/main/java/main/db/TaskDAO.java`
- `src/main/java/main/ui/NavigationBar.java`
- `src/main/java/main/ui/ContentCreatorApp.java`
- `src/main/java/main/ui/DashboardUI.java`
- `src/main/java/main/ui/WorkflowPage.java`
- `pom.xml`, `build.gradle`, `guide.md`

---

## More UI snippets requested

`src/main/java/main/ui/NavigationBar.java`

```java
package main.ui;

import javax.swing.*;
import java.awt.*;

public class NavigationBar extends JPanel {
    private final JButton backBtn;
    private final JButton forwardBtn;

    public NavigationBar() {
        setLayout(new BorderLayout());
        setBackground(new Color(24, 26, 28));
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        ImageIcon backIcon = null;
        ImageIcon forwardIcon = null;
        java.net.URL backUrl = getClass().getResource("/assets/back.png");
        java.net.URL forwardUrl = getClass().getResource("/assets/forward.png");
        if (backUrl != null) backIcon = new ImageIcon(new ImageIcon(backUrl).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
        if (forwardUrl != null) forwardIcon = new ImageIcon(new ImageIcon(forwardUrl).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));

        backBtn = (backIcon != null) ? new JButton(backIcon) : new JButton("←");
        backBtn.setToolTipText("Back");
        backBtn.setFocusPainted(false);
        backBtn.setForeground(Color.WHITE);
        backBtn.setBackground(new Color(48, 51, 57));
        backBtn.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
        backBtn.addActionListener(e -> { try { NotionStyleUI.navigateToHome(); } catch (Exception ignored) {} });

        forwardBtn = (forwardIcon != null) ? new JButton(forwardIcon) : new JButton("→");
        forwardBtn.setToolTipText("Forward");
        forwardBtn.setFocusPainted(false);
        forwardBtn.setForeground(Color.WHITE);
        forwardBtn.setBackground(new Color(48, 51, 57));
        forwardBtn.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
        forwardBtn.setEnabled(false);

        UITheme.styleNavButton(backBtn);
        UITheme.styleNavButton(forwardBtn);
        left.add(backBtn);
        left.add(forwardBtn);

        JLabel title = new JLabel("SolFlow");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 14));

        add(left, BorderLayout.WEST);
        add(title, BorderLayout.CENTER);
    }

    public static NavigationBar create() { return new NavigationBar(); }
    public static JPanel wrap(JComponent page) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(create(), BorderLayout.NORTH);
        wrapper.add(page, BorderLayout.CENTER);
        return wrapper;
    }

    public JButton getBackButton() { return backBtn; }
    public JButton getForwardButton() { return forwardBtn; }
}
```

`src/main/java/main/ui/NotionStyleUI.java` (login/register wiring)

```java
private static void showLoginPanel() {
    mainContent.removeAll();
    mainContent.setLayout(new BorderLayout());

    JPanel center = new JPanel(new GridBagLayout());
    center.setOpaque(false);

    JPanel card = new JPanel() {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(30, 32, 36));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    };
    card.setOpaque(false);
    card.setLayout(new GridBagLayout());
    card.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(8, 8, 8, 8);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    JLabel title = new JLabel("Welcome to SolFlow");
    title.setForeground(Color.WHITE);
    title.setFont(new Font("SansSerif", Font.BOLD, 22));
    gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; card.add(title, gbc);

    gbc.gridwidth = 1;
    gbc.gridy = 2; gbc.gridx = 0;
    JLabel uLabel = new JLabel("Username"); uLabel.setForeground(new Color(200,200,200)); uLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
    card.add(uLabel, gbc);
    gbc.gridx = 1;
    JTextField userField = new JTextField(18);
    userField.setBackground(new Color(40,40,40)); userField.setForeground(Color.WHITE); userField.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
    card.add(userField, gbc);

    gbc.gridy = 3; gbc.gridx = 0;
    JLabel pLabel = new JLabel("Password"); pLabel.setForeground(new Color(200,200,200)); pLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
    card.add(pLabel, gbc);
    gbc.gridx = 1;
    JPasswordField passField = new JPasswordField(18);
    passField.setBackground(new Color(40,40,40)); passField.setForeground(Color.WHITE); passField.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
    card.add(passField, gbc);

    gbc.gridy = 5; gbc.gridwidth = 1; gbc.gridx = 0;
    JButton loginBtn = new JButton("Sign in");
    loginBtn.setBackground(new Color(88,101,242)); loginBtn.setForeground(Color.WHITE); loginBtn.setFocusPainted(false);
    card.add(loginBtn, gbc);

    gbc.gridx = 1;
    JButton registerBtn = new JButton("Create account");
    registerBtn.setBackground(new Color(58,58,58)); registerBtn.setForeground(Color.WHITE); registerBtn.setFocusPainted(false);
    card.add(registerBtn, gbc);

    JLabel msg = new JLabel(" "); msg.setForeground(new Color(220,100,100));
    gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2; card.add(msg, gbc);

    center.add(card);
    mainContent.add(center, BorderLayout.CENTER);

    loginBtn.addActionListener(e -> {
        String u = userField.getText().trim();
        String p = new String(passField.getPassword());
        if (u.isEmpty() || p.isEmpty()) { msg.setText("Please enter username and password"); return; }
        try {
            main.db.UserDAO.AuthRecord ar = main.db.UserDAO.getAuthByUsername(u);
            if (ar == null) { msg.setText("User not found. Please register."); return; }
            String hashed = HashUtil.hashWithSalt(p, ar.salt);
            if (hashed.equals(ar.passwordHash)) { main.Session.set(ar.id, ar.username); showHomePage(); }
            else { msg.setText("Invalid credentials"); }
        } catch (Exception ex) { ex.printStackTrace(); msg.setText("Error during login"); }
    });

    registerBtn.addActionListener(e -> {
        String u = userField.getText().trim();
        String p = new String(passField.getPassword());
        if (u.isEmpty() || p.isEmpty()) { msg.setText("Please enter username and password"); return; }
        try {
            String salt = HashUtil.generateSalt();
            String hash = HashUtil.hashWithSalt(p, salt);
            int id = main.db.UserDAO.insert(u, hash, salt);
            if (id > 0) { main.Session.set(id, u); showHomePage(); } else { msg.setText("Registration failed (username may exist)"); }
        } catch (Exception ex) { ex.printStackTrace(); msg.setText("Error during registration"); }
    });

    mainContent.revalidate(); mainContent.repaint();
}
```

`src/main/java/main/ui/MailOrganize.java` (constructor and CSV load)

```java
public MailOrganize() {
    setLayout(new BorderLayout());
    setBackground(APP_BG);
    setBorder(new EmptyBorder(12,12,12,12));

    JPanel pageContent = new JPanel(new BorderLayout());
    pageContent.setOpaque(false);

    JPanel top = new JPanel(new BorderLayout(12, 12));
    top.setBackground(APP_BG);
    top.setBorder(new EmptyBorder(6, 6, 6, 6));

    JLabel title = new JLabel("Sheets");
    title.setFont(HEAD_FONT);
    title.setForeground(new Color(26,26,26));
    top.add(title, BorderLayout.WEST);

    JButton importBtn = createToolbarBtn("Import CSV", "/assets/csv-file-format-extension.png", new Color(64,160,255));
    pageContent.add(top, BorderLayout.NORTH);

    File projectRoot = new File(System.getProperty("user.dir"));
    rootNode = new DefaultMutableTreeNode(new FileNode(projectRoot));
    tree = new JTree(rootNode);
    populateChildren(rootNode);

    JScrollPane leftScroll = new JScrollPane(tree);
    leftScroll.setPreferredSize(new Dimension(320, 640));

    JPanel right = new JPanel(new BorderLayout(10,10));
    right.setBackground(APP_BG);

    JPanel card = new JPanel(new BorderLayout(8,8));
    card.setBackground(SURFACE);

    table.setFillsViewportHeight(true);
    table.setFont(TABLE_FONT);
    table.setRowHeight(34);

    JScrollPane tableScroll = new JScrollPane(table);
    tableScroll.setBorder(BorderFactory.createEmptyBorder());
    card.add(tableScroll, BorderLayout.CENTER);

    right.add(card, BorderLayout.CENTER);
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, right);
    split.setResizeWeight(0.28);
    pageContent.add(split, BorderLayout.CENTER);

    add(NavigationBar.wrap(pageContent), BorderLayout.CENTER);
}

private void loadCsvFile(File f) {
    if (f == null || !f.exists()) return;
    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
        String line;
        List<String[]> rows = new ArrayList<>();
        while ((line = br.readLine()) != null) rows.add(parseCsvLine(line));
        if (rows.isEmpty()) return;
        int maxCols = 0;
        for (String[] r : rows) if (r.length > maxCols) maxCols = r.length;

        tableModel.clear();
        for (int c = 0; c < maxCols; c++) tableModel.addColumn("C" + (c+1));
        for (String[] r : rows) {
            String[] row = new String[maxCols];
            for (int i = 0; i < maxCols; i++) row[i] = (i < r.length) ? r[i] : "";
            tableModel.addRow(row);
        }

        try { FileDAO.insertOrUpdate(f); } catch (Exception ignored) {}
    } catch (IOException ex) {
        JOptionPane.showMessageDialog(this, "Failed to load CSV: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
```

`src/main/java/main/ui/InbuiltJavaTemplate1.java` (embed + planner task helpers)

```java
public static JPanel createEmbeddedPanel(int workflowId) {
    InbuiltJavaTemplate1 t = new InbuiltJavaTemplate1(workflowId);
    return NavigationBar.wrap(t.getPanel());
}

private void loadTasksForWorkflow() {
    tableModel.setRowCount(0);
    List<TaskDAO.TaskRecord> tasks = TaskDAO.listForWorkflow(this.workflowId <= 0 ? 0 : this.workflowId);
    for (TaskDAO.TaskRecord t : tasks) {
        tableModel.addRow(new Object[] { t.text, t.checked });
    }
}

private void createNewTask(String text) {
    if (text == null || text.trim().isEmpty()) return;
    int ord = tableModel.getRowCount();
    int id = TaskDAO.insert(text, false, ord, this.workflowId <= 0 ? 0 : this.workflowId);
    if (id != -1) loadTasksForWorkflow();
    else tableModel.addRow(new Object[] { text, false });
}
```

`src/main/java/main/ui/NewTemplate.java` (embed + breadcrumb)

```java
public static JPanel createEmbeddedPanel() {
    return new NewTemplatePanel();
}

private static class BreadcrumbPanel extends JPanel {
    private final JLabel activeModuleLabel = new JLabel("Sales");
    private final JPopupMenu navMenu = new JPopupMenu();
    private final java.util.function.Consumer<String> onNavigate;

    BreadcrumbPanel(java.util.function.Consumer<String> onNavigate) {
        super(new BorderLayout(10, 10));
        this.onNavigate = onNavigate;
        setBackground(Theme.BACKGROUND);
        setBorder(new EmptyBorder(15, 20, 10, 20));

        navMenu.add(createNavItem("Opportunities", "JOB_SEARCH"));
        navMenu.add(createNavItem("Clients", "CAREER_GOALS"));
        navMenu.add(createNavItem("Activity", "ACTION_ITEMS"));

        JPanel breadcrumbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        breadcrumbPanel.setOpaque(false);
        breadcrumbPanel.add(new IconOnlyButton(IconType.BRIEFCASE));

        JButton workButton = new FlatButton("Sales & Clients");
        workButton.setFont(Theme.MUTED_FONT);
        workButton.addActionListener(e -> navMenu.show(workButton, 0, workButton.getHeight()));
        breadcrumbPanel.add(workButton);

        JLabel separatorLabel = new JLabel("/");
        separatorLabel.setFont(Theme.MUTED_FONT);
        separatorLabel.setForeground(Theme.TEXT_MUTED);
        breadcrumbPanel.add(separatorLabel);

        activeModuleLabel.setFont(Theme.MUTED_FONT.deriveFont(Font.BOLD));
        activeModuleLabel.setForeground(Theme.TEXT);
        breadcrumbPanel.add(activeModuleLabel);

        add(breadcrumbPanel, BorderLayout.WEST);
    }

    private JMenuItem createNavItem(String text, String moduleName) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(Theme.BODY_FONT);
        item.addActionListener(e -> onNavigate.accept(moduleName));
        return item;
    }

    void setActiveModule(String moduleName) {
        switch(moduleName) {
            case "JOB_SEARCH": activeModuleLabel.setText("Opportunities"); break;
            case "CAREER_GOALS": activeModuleLabel.setText("Clients"); break;
            case "ACTION_ITEMS": activeModuleLabel.setText("Activity"); break;
        }
    }
}
```

## Next steps (suggestions)
- I can paste any full file's contents into the repo (as more docs) if you want complete sources in markdown.
- Create a `README.md` with build instructions for Maven and Gradle.
- Generate unit tests for DAOs using an in-memory SQLite database.

Tell me which next step you prefer and I will continue.
