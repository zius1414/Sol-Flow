package main.ui;

import main.db.WorkflowDAO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List; // <-- added to fix List<WorkflowDAO.Workflow> usage
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;

public class NotionStyleUI {
    // static show() so Main can call NotionStyleUI.show()
    public static void show() {
        // Delegate to the real GUI builder. If we're not on the EDT, schedule it there.
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(NotionStyleUI::createAndShowGUI);
        } else {
            createAndShowGUI();
        }
    }

    private static JFrame frame;
    private static JPanel mainContent;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(NotionStyleUI::createAndShowGUI);
    }

    public static void createAndShowGUI() {
        // Apply UI theme early
        UITheme.applyGlobalTheme();
        frame = new JFrame("SolFlow - Workspace");
        frame.setSize(1200, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(new Color(24, 24, 24));
        frame.setLayout(new BorderLayout());

        // Left Sidebar Panel
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(38, 38, 38));
        sidebar.setPreferredSize(new Dimension(250, frame.getHeight()));
        sidebar.setLayout(new BorderLayout());

        // Sidebar Top Section
        JPanel sidebarTop = new JPanel();
        sidebarTop.setBackground(new Color(38, 38, 38));
        sidebarTop.setLayout(new BoxLayout(sidebarTop, BoxLayout.Y_AXIS));
        sidebarTop.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JLabel userLabel = new JLabel("  SolFlow - WorkSpace");
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarTop.add(userLabel);

        sidebarTop.add(Box.createVerticalStrut(20));

    JButton searchButton = createSidebarButton("  Search"); UITheme.styleSidebarButton(searchButton);
    JButton homeButton = createSidebarButton("  Home"); UITheme.styleSidebarButton(homeButton);
    JButton inboxButton = createSidebarButton("  Inbox"); UITheme.styleSidebarButton(inboxButton);
    JButton newButton = createSidebarButton("  Add new"); UITheme.styleSidebarButton(newButton);

        sidebarTop.add(searchButton);
        sidebarTop.add(homeButton);
        sidebarTop.add(inboxButton);
        sidebarTop.add(Box.createVerticalStrut(10));
        sidebarTop.add(newButton);

        sidebar.add(sidebarTop, BorderLayout.NORTH);

        // Sidebar Middle Section
        JPanel sidebarPrivate = new JPanel();
        sidebarPrivate.setBackground(new Color(38, 38, 38));
        sidebarPrivate.setLayout(new BoxLayout(sidebarPrivate, BoxLayout.Y_AXIS));
        sidebarPrivate.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel privateLabel = new JLabel("Private");
        privateLabel.setForeground(new Color(150, 150, 150));
        privateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebarPrivate.add(privateLabel);
        sidebarPrivate.add(Box.createVerticalStrut(5));

        JButton trafficControl = createSidebarButton("  Traffic Control");
        JButton lunarLander = createSidebarButton("  LunarLander-v2");
        sidebarPrivate.add(trafficControl);
        sidebarPrivate.add(lunarLander);

        sidebar.add(sidebarPrivate, BorderLayout.CENTER);

        // Sidebar Bottom Section
        JPanel sidebarBottom = new JPanel();
        sidebarBottom.setBackground(new Color(38, 38, 38));
        sidebarBottom.setLayout(new BoxLayout(sidebarBottom, BoxLayout.Y_AXIS));
        sidebarBottom.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JButton settingsButton = createSidebarButton("  Settings");
        JButton marketplaceButton = createSidebarButton("  Marketplace");
        JButton trashButton = createSidebarButton("  Trash");
        sidebarBottom.add(settingsButton);
        sidebarBottom.add(marketplaceButton);
        sidebarBottom.add(trashButton);
        sidebar.add(sidebarBottom, BorderLayout.SOUTH);

        frame.add(sidebar, BorderLayout.WEST);

        // Main Content Panel
        mainContent = new JPanel();
        mainContent.setBackground(new Color(24, 24, 24));
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(24, 24, 24));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        frame.add(scrollPane, BorderLayout.CENTER);

        // If not logged in, show login/register first
        if (main.Session.getUserId() <= 0) {
            showLoginPanel();
        } else {
            showHomePage();
        }

        frame.setVisible(true);

        // start background reminder timer to notify about tasks not checked within 24 hours
        try {
            java.util.Timer reminderTimer = new java.util.Timer("TaskReminderTimer", true);
            // Poll frequently (every 10s) so short reminder windows (e.g. 1 minute)
            // are detected promptly while the app is running. If you prefer lower
            // CPU usage, increase this value (e.g. 60*1000 for 1 minute, or 3600*1000 for 1 hour).
            long periodMs = 10_000L; // check every 10 seconds
            reminderTimer.schedule(new java.util.TimerTask() {
                @Override public void run() {
                    try {
                        // read configurable window (minutes) from settings (default 1440 = 24h)
                        int windowMinutes = main.db.SettingsDAO.getInt("reminder_window_minutes", 1);
                        java.util.List<main.db.TaskDAO.TaskRecord> due = main.db.TaskDAO.listTasksNeedingReminderMinutes(windowMinutes);
                        if (due == null || due.isEmpty()) return;
                        long now = System.currentTimeMillis() / 1000L;
                        for (main.db.TaskDAO.TaskRecord tr : due) {
                            // mark reminder sent immediately to avoid duplicates
                            main.db.TaskDAO.setLastReminderSent(tr.id, now);
                            SwingUtilities.invokeLater(() -> {
                                try {
                                    String msg = "Reminder: task still open for " + windowMinutes + "+ minutes:\n" + tr.text + "\n\nMark as done?";
                                    int res = showReminderDialog("Task Reminder", msg);
                                    if (res == JOptionPane.YES_OPTION) {
                                        // mark task as checked
                                        main.db.TaskDAO.update(tr.id, tr.text == null ? "" : tr.text, true, tr.ord);
                                        // refresh home if currently showing workflow page
                                        navigateToHome();
                                    }
                                } catch (Throwable t) { t.printStackTrace(); }
                            });
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }, 10_000L, periodMs); // initial delay 10s then repeat
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Navigate the already-open main window back to the Home page.
     * This is used by NavigationBar so the Back action doesn't open a new window.
     */
    public static void navigateToHome() {
        if (mainContent == null) return;
        SwingUtilities.invokeLater(() -> {
            showHomePage();
        });
    }

    // Custom themed reminder dialog to ensure good contrast/readability on dark UI
    private static int showReminderDialog(String title, String message) {
        final int[] result = new int[]{JOptionPane.CLOSED_OPTION};
        // build dialog on EDT and block until disposed
        final JDialog dialog = new JDialog(frame, title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout(12,12));
        Color bg = new Color(34, 34, 34);
        root.setBackground(bg);
        root.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        // message label (use HTML for wrapping and color)
        String html = "<html><div style='color:#FFFFFF;font-family:Arial;font-size:12px;'>" + message.replace("\n", "<br/>") + "</div></html>";
        JLabel lbl = new JLabel(html);
        lbl.setBackground(bg);
        root.add(lbl, BorderLayout.CENTER);

        // buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(bg);
        JButton yes = new JButton("Yes");
        JButton no = new JButton("No");
        // style buttons for contrast
        yes.setBackground(new Color(88, 101, 242)); yes.setForeground(Color.WHITE); yes.setFocusPainted(false); yes.setBorderPainted(false);
        no.setBackground(new Color(80, 80, 80)); no.setForeground(Color.WHITE); no.setFocusPainted(false); no.setBorderPainted(false);
        yes.addActionListener(a -> { result[0] = JOptionPane.YES_OPTION; dialog.dispose(); });
        no.addActionListener(a -> { result[0] = JOptionPane.NO_OPTION; dialog.dispose(); });
        btns.add(no);
        btns.add(yes);
        root.add(btns, BorderLayout.SOUTH);

        dialog.getContentPane().add(root);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
        return result[0];
    }

    private static void showHomePage() {
        mainContent.removeAll();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS)); // Reset layout

        JLabel welcomeLabel = new JLabel("Let's Spice the Business");
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 36));
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(welcomeLabel);
        mainContent.add(Box.createVerticalStrut(30));

        // AI Panel
        JPanel aiPanel = createRoundedPanel(new Color(240, 240, 240), 10, true, false);
        aiPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        aiPanel.setLayout(new BorderLayout(10, 10));
        JLabel aiTitle = new JLabel("Create your own Workflow?");
        aiTitle.setForeground(Color.BLACK);
        aiTitle.setFont(new Font("Arial", Font.BOLD, 18));
        aiPanel.add(aiTitle, BorderLayout.NORTH);
        JLabel aiDesc = new JLabel("Create a system for your Business!");
        aiDesc.setForeground(Color.BLACK);
        aiPanel.add(aiDesc, BorderLayout.CENTER);
        JButton tryButton = new JButton("Create New Workflow");
        tryButton.setBackground(new Color(240, 240, 240));
        tryButton.setForeground(Color.BLACK);
        tryButton.setFocusPainted(false);
        tryButton.setBorderPainted(false);
        tryButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { tryButton.setBackground(new Color(220, 220, 220)); }
            public void mouseExited(MouseEvent e) { tryButton.setBackground(new Color(240, 240, 240)); }
        });

    UITheme.stylePrimaryButton(tryButton);
    tryButton.addActionListener(e -> {
            String defaultName = "Workflow " + (System.currentTimeMillis() % 100000);
            int uid = main.Session.getUserId();
            int id = WorkflowDAO.insert(defaultName, uid); // create workflow row for current user
            addWorkflowQuickStart(defaultName, "new", id);
            // open a blank editor bound to the new workflow id
            showNewPage(id, true);
        });

        aiPanel.add(tryButton, BorderLayout.SOUTH);
        aiPanel.setMaximumSize(new Dimension(600, 150));
        aiPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(aiPanel);
        mainContent.add(Box.createVerticalStrut(30));

        // Recently Visited Section
        JLabel recentLabel = new JLabel("Recently visited");
        recentLabel.setForeground(Color.WHITE);
        recentLabel.setFont(new Font("Arial", Font.BOLD, 18));
        recentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(recentLabel);
        mainContent.add(Box.createVerticalStrut(10));

        JPanel recentCardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        recentCardsPanel.setBackground(new Color(24, 24, 24));
        // load workflows from DB and show as quick starts
        try {
            int uid = main.Session.getUserId();
            List<WorkflowDAO.Workflow> workflows = WorkflowDAO.listForUser(uid);
            if (workflows.isEmpty()) {
                recentCardsPanel.add(createCard("Traffic Control", "Sep 17"));
                recentCardsPanel.add(createCard("LunarLander-v2", "2s ago"));
            } else {
                for (WorkflowDAO.Workflow wf : workflows) {
                    recentCardsPanel.add(createCard(wf.name, "created", wf.id));
                }
            }
        } catch (Exception ex) {
            recentCardsPanel.add(createCard("Traffic Control", "Sep 17"));
            recentCardsPanel.add(createCard("LunarLander-v2", "2s ago"));
        }
        recentCardsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(recentCardsPanel);
        mainContent.add(Box.createVerticalStrut(30));

        // Quick Starts Section
        JLabel learnLabel = new JLabel("Quick Starts");
        learnLabel.setForeground(Color.WHITE);
        learnLabel.setFont(new Font("Arial", Font.BOLD, 18));
        learnLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(learnLabel);
        mainContent.add(Box.createVerticalStrut(10));

        JPanel learnCardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        learnCardsPanel.setBackground(new Color(24, 24, 24));

        // Link quick-start cards to other app modules / screens.
        learnCardsPanel.add(createLearnCard("Quick<br>Business<br>Breakthroughs", "src/main/resources/assets/learn_1.png",
            () -> {
                // embed InbuiltJavaTemplate1 panel into mainContent (existing behavior)
                SwingUtilities.invokeLater(() -> {
                    try {
                        JPanel embedded = InbuiltJavaTemplate1.createEmbeddedPanel(-1);
                        mainContent.removeAll();
                        mainContent.setLayout(new BorderLayout());
                        mainContent.add(embedded, BorderLayout.CENTER);
                        mainContent.revalidate();
                        mainContent.repaint();
                    } catch (Throwable ex) {
                        // fallback to opening as a separate window
                        try { new InbuiltJavaTemplate1().show(); } catch (Throwable ex2) { ex2.printStackTrace(); }
                    }
                });
            }));

        learnCardsPanel.add(createLearnCard("Create<br>Content<br>Creator", "src/main/resources/assets/learn_2.png",
            () -> launchClass("main.ui.ContentCreatorApp")));

        learnCardsPanel.add(createLearnCard("Docs<br>Upload", "src/main/resources/assets/learn_3.png",
            () -> launchClass("main.ui.Docupload")));

        learnCardsPanel.add(createLearnCard("Tasks<br>Dashboard", "src/main/resources/assets/learn_4.png",
            () -> launchClass("main.ui.NotionDashboard")));

        // Additional templates: DashboardUI and NewTemplate (added as quick-starts)
        learnCardsPanel.add(createLearnCard("Dashboard<br>UI", "src/main/resources/assets/learn_5.png",
            () -> embedClassAsPanel("main.ui.DashboardUI")));

        learnCardsPanel.add(createLearnCard("New<br>Template", "src/main/resources/assets/learn_6.png",
            () -> embedClassAsPanel("NewTemplate")));

        learnCardsPanel.add(createLearnCard("Sales<br>Panel", "src/main/resources/assets/learn_7.png",
            () -> embedClassAsPanel("main.ui.SalesPanel")));

        learnCardsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(learnCardsPanel);

        mainContent.revalidate();
        mainContent.repaint();
    }

    // change: keep the no-arg helper but delegate to int overload
    private static void showNewPage() {
        showNewPage(-1);
    }

    // convenience overload to accept only id (delegates to (id,false))
    private static void showNewPage(int workflowId) {
        showNewPage(workflowId, false);
    }

    // new overload: open workflow editor for a specific workflow id (or new if id <= 0)
    private static void showNewPage(int workflowId, boolean createBlank) {
        mainContent.removeAll();
        mainContent.setLayout(new BorderLayout());

        WorkflowPage workflowPage = new WorkflowPage(workflowId, createBlank);
        mainContent.add(workflowPage, BorderLayout.CENTER);

        JButton backButton = new JButton("Go Back");
        backButton.setBackground(new Color(60, 60, 60));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.setFont(new Font("Arial", Font.PLAIN, 12));
        backButton.addActionListener(e -> showHomePage());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(backButton);

        mainContent.add(buttonPanel, BorderLayout.NORTH);
        mainContent.revalidate();
        mainContent.repaint();
    }

    private static JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(38, 38, 38));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setPreferredSize(new Dimension(230, 30));

        button.addMouseListener(new MouseAdapter() {
            private Color originalColor = new Color(38, 38, 38);
            private Color hoverColor = new Color(50, 50, 50);

            public void mouseEntered(MouseEvent e) { button.setBackground(hoverColor); }
            public void mouseExited(MouseEvent e) { button.setBackground(originalColor); }
        });

        return button;
    }

    // --- simple login/register UI ---
    private static void showLoginPanel() {
        mainContent.removeAll();
        mainContent.setLayout(new BorderLayout());

        // Center container
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

        JLabel subtitle = new JLabel("Sign in to continue or create a new account");
        subtitle.setForeground(new Color(180, 180, 180));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridy = 1; card.add(subtitle, gbc);

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

        // message label
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        JLabel msg = new JLabel(" "); msg.setForeground(new Color(220,100,100)); msg.setFont(new Font("SansSerif", Font.PLAIN, 12));
        card.add(msg, gbc);

        // buttons
        gbc.gridy = 5; gbc.gridwidth = 1; gbc.gridx = 0;
        JButton loginBtn = new JButton("Sign in");
        loginBtn.setBackground(new Color(88,101,242)); loginBtn.setForeground(Color.WHITE); loginBtn.setFocusPainted(false);
        loginBtn.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
        card.add(loginBtn, gbc);

        gbc.gridx = 1;
        JButton registerBtn = new JButton("Create account");
        registerBtn.setBackground(new Color(58,58,58)); registerBtn.setForeground(Color.WHITE); registerBtn.setFocusPainted(false);
        registerBtn.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
        card.add(registerBtn, gbc);

        // small footer
        gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JLabel footer = new JLabel("© SolFlow"); footer.setForeground(new Color(130,130,130)); footer.setFont(new Font("SansSerif", Font.PLAIN, 11));
        card.add(footer, gbc);

        center.add(card);
        mainContent.add(center, BorderLayout.CENTER);

        // wiring
        loginBtn.addActionListener(e -> {
            String u = userField.getText().trim();
            String p = new String(passField.getPassword());
            if (u.isEmpty() || p.isEmpty()) { msg.setText("Please enter username and password"); return; }
            try {
                main.db.UserDAO.AuthRecord ar = main.db.UserDAO.getAuthByUsername(u);
                if (ar == null) { msg.setText("User not found. Please register."); return; }
                String hashed = HashUtil.hashWithSalt(p, ar.salt);
                if (hashed.equals(ar.passwordHash)) {
                    main.Session.set(ar.id, ar.username);
                    showHomePage();
                } else { msg.setText("Invalid credentials"); }
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

    // existing simple card (keeps compatibility)
    private static JPanel createCard(String title, String date) {
        JPanel card = createRoundedPanel(new Color(38, 38, 38), 10, false, true);
        card.setPreferredSize(new Dimension(170, 110));
        card.setLayout(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        top.add(new JLabel("📁"));
        card.add(top, BorderLayout.NORTH);

        JLabel titleLabel = new JLabel("<html><b>" + title + "</b></html>");
        titleLabel.setForeground(Color.WHITE);
        card.add(titleLabel, BorderLayout.CENTER);

        JLabel dateLabel = new JLabel(date);
        dateLabel.setForeground(new Color(150, 150, 150));
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        card.add(dateLabel, BorderLayout.SOUTH);

        return card;
    }

    // overloaded card for a workflow — clicking will open the workflow (by id)
    private static JPanel createCard(String title, String date, int workflowId) {
        JPanel card = createCard(title, date);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                // open the workflow editor (created with "Create New") for this workflow id
                SwingUtilities.invokeLater(() -> {
                    showNewPage(workflowId);
                });
            }
        });
        return card;
    }

    // helper to add a workflow card to quick starts (called after creation)
    private static void addWorkflowQuickStart(String name, String when, int id) {
        // find the recent cards panel inside mainContent and add a card at top
        for (Component c : mainContent.getComponents()) {
            if (c instanceof JPanel) {
                // quick heuristic: the first FlowLayout panel we created earlier is the recentCardsPanel
                JPanel p = (JPanel) c;
                if (p.getLayout() instanceof FlowLayout) {
                    p.add(createCard(name, when, id), 0);
                    mainContent.revalidate();
                    mainContent.repaint();
                    return;
                }
            }
        }
    }

    // helper: try to launch a UI class by name (show() static/instance, main(String[]), or instantiate JFrame/JPanel)
    private static void launchClass(String className) {
        SwingUtilities.invokeLater(() -> {
            // Try the requested class name, but be resilient: several templates in this
            // workspace are in the default package (no package statement) even though
            // source files live under src/main/java/main/ui. To reduce "Component not
            // found" errors, try a few sensible fallbacks before giving up.
            String[] candidates = new String[] {
                className,
                // if given fqcn contains package segments, try the simple class name too
                className != null && className.contains(".") ? className.substring(className.lastIndexOf('.') + 1) : null,
                // try adding the common package if caller used simple name
                className != null && !className.startsWith("main.") ? "main.ui." + className : null,
                // try removing the leading package if present (e.g. main.ui.NotionDashboard -> NotionDashboard)
                className != null && className.startsWith("main.ui.") ? className.substring("main.ui.".length()) : null
            };

            // iterate candidates; keep trying until one succeeds
            for (String cand : candidates) {
                if (cand == null || cand.trim().isEmpty()) continue;
                try {
                    Class<?> cls = Class.forName(cand);

                    // 1) try show() — if static invoke with null, if instance invoke on new instance
                    try {
                        Method showM = cls.getMethod("show");
                        if (Modifier.isStatic(showM.getModifiers())) {
                            showM.invoke(null);
                            return;
                        } else {
                            Object inst = cls.getDeclaredConstructor().newInstance();
                            showM.invoke(inst);
                            return;
                        }
                    } catch (NoSuchMethodException ignored) {}

                    // 2) try static main(String[])
                    try {
                        Method mainM = cls.getMethod("main", String[].class);
                        if (Modifier.isStatic(mainM.getModifiers())) {
                            mainM.invoke(null, (Object) new String[0]);
                            return;
                        }
                    } catch (NoSuchMethodException ignored) {}

                    // 3) try no-arg constructor
                    Object inst = null;
                    try {
                        inst = cls.getDeclaredConstructor().newInstance();
                    } catch (NoSuchMethodException nsme) {
                        // no default ctor — fallthrough to next candidate
                    }

                    if (inst != null) {
                        if (inst instanceof JFrame) {
                            ((JFrame) inst).setVisible(true);
                            return;
                        }
                        if (inst instanceof JPanel) {
                            JFrame f = new JFrame(cls.getSimpleName());
                            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            f.getContentPane().add((JPanel) inst);
                            f.pack();
                            f.setLocationRelativeTo(null);
                            f.setVisible(true);
                            return;
                        }
                    }

                    // last resort: notify user that we found the class but didn't know how to open it
                    JOptionPane.showMessageDialog(null, "Found class: " + cand + " — launched (no known UI entrypoint).\nTry opening it directly.");
                    return;

                } catch (ClassNotFoundException e) {
                    // try next candidate
                    // try next candidate
                } catch (InvocationTargetException ite) {
                    Throwable cause = ite.getCause() != null ? ite.getCause() : ite;
                    cause.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error launching " + cand + ":\n" + cause.getMessage());
                    return;
                } catch (Throwable t) {
                    t.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to launch " + cand + ":\n" + t.getMessage());
                    return;
                }
            }

            // If we reach here no candidate succeeded
            String tried = String.join(", ", java.util.Arrays.stream(candidates).filter(s -> s != null).toArray(String[]::new));
            JOptionPane.showMessageDialog(null,
                "Component not found: " + className + "\nTried: " + tried + "\n\n" +
                "Possible causes: class not compiled or not on runtime classpath.\n" +
                "Build the project (mvn -DskipTests package) and run with target/classes on the classpath.");
        });
    }

    // change signature: accept Runnable action to run when clicked
    private static JPanel createLearnCard(String title, String imagePath, Runnable action) {
        JPanel card = createRoundedPanel(new Color(38, 38, 38), 10, false, true);
        card.setPreferredSize(new Dimension(250, 80));
        card.setLayout(new BorderLayout(10, 0));
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("<html>" + title + "</html>", SwingConstants.LEFT);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        card.add(titleLabel, BorderLayout.CENTER);

        try {
            File f = new File(imagePath);
            if (f.exists()) {
                Image img = ImageIO.read(f).getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(img);
                JLabel imageLabel = new JLabel(icon);
                card.add(imageLabel, BorderLayout.EAST);
            } else {
                JLabel placeholder = new JLabel("[Image]");
                placeholder.setForeground(Color.RED);
                placeholder.setPreferredSize(new Dimension(40, 40));
                card.add(placeholder, BorderLayout.EAST);
            }
        } catch (IOException e) {
            JLabel placeholder = new JLabel("[Image]");
            placeholder.setForeground(Color.RED);
            placeholder.setPreferredSize(new Dimension(40, 40));
            card.add(placeholder, BorderLayout.EAST);
        }

        // make quick-start card clickable: use provided action
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (action != null) {
            card.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { action.run(); }
            });
        }

        return card;
    }

    private static JPanel createRoundedPanel(Color backgroundColor, int cornerRadius, boolean withShadow, boolean withHoverEffect) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (withShadow) {
                    g2d.setColor(new Color(0, 0, 0, 50));
                    g2d.fill(new RoundRectangle2D.Double(2, 2, getWidth() - 4, getHeight() - 4, cornerRadius, cornerRadius));
                }

                g2d.setColor(getBackground());
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius));
                g2d.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBackground(backgroundColor);

        if (withHoverEffect) {
            panel.addMouseListener(new MouseAdapter() {
                private Color originalColor = backgroundColor;
                private Color hoverColor = new Color(50, 50, 50);

                @Override
                public void mouseEntered(MouseEvent e) { panel.setBackground(hoverColor); }
                @Override
                public void mouseExited(MouseEvent e) { panel.setBackground(originalColor); }
            });
        }

        return panel;
    }

    // Try to embed a UI class as a panel inside the mainContent area.
    private static void embedClassAsPanel(String className) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Try a few candidate names (same approach as launchClass)
                String[] candidates = new String[] {
                    className,
                    className != null && className.contains(".") ? className.substring(className.lastIndexOf('.') + 1) : null,
                    className != null && !className.startsWith("main.") ? "main.ui." + className : null,
                    className != null && className.startsWith("main.ui.") ? className.substring("main.ui.".length()) : null
                };

                for (String cand : candidates) {
                    if (cand == null || cand.trim().isEmpty()) continue;
                    try {
                        Class<?> cls = Class.forName(cand);

                        // 1) If class has a static factory createEmbeddedPanel() that returns JPanel, use it
                        try {
                            Method m = cls.getMethod("createEmbeddedPanel");
                            Object res = m.invoke(null);
                            if (res instanceof JPanel) {
                                showPanelInMain((JPanel) res, cls.getSimpleName());
                                return;
                            }
                        } catch (NoSuchMethodException ignored) {}

                        // 2) If class is a JFrame subclass, instantiate and take its content pane
                        if (JFrame.class.isAssignableFrom(cls)) {
                            Object inst = cls.getDeclaredConstructor().newInstance();
                            if (inst instanceof JFrame) {
                                Container content = ((JFrame) inst).getContentPane();
                                // Detach content into a panel wrapper
                                JPanel wrapper = new JPanel(new BorderLayout());
                                wrapper.add(content, BorderLayout.CENTER);
                                showPanelInMain(wrapper, cls.getSimpleName());
                                return;
                            }
                        }

                        // 3) If class itself is a JPanel, instantiate and embed
                        if (JPanel.class.isAssignableFrom(cls)) {
                            Object inst = cls.getDeclaredConstructor().newInstance();
                            if (inst instanceof JPanel) {
                                showPanelInMain((JPanel) inst, cls.getSimpleName());
                                return;
                            }
                        }

                        // 4) Try inner classes: look for a declared class that extends JPanel
                        for (Class<?> inner : cls.getDeclaredClasses()) {
                            if (JPanel.class.isAssignableFrom(inner)) {
                                Object inst = inner.getDeclaredConstructor().newInstance();
                                if (inst instanceof JPanel) {
                                    showPanelInMain((JPanel) inst, inner.getSimpleName());
                                    return;
                                }
                            }
                        }

                    } catch (ClassNotFoundException cnf) {
                        // try next candidate
                    }
                }

                // If nothing worked, show an informative message
                JOptionPane.showMessageDialog(frame,
                    "Could not embed component: " + className + "\n" +
                    "Tried common names and patterns. Ensure the class is compiled and exposes a JPanel or a createEmbeddedPanel() static method.");

            } catch (InvocationTargetException ite) {
                Throwable cause = ite.getCause() != null ? ite.getCause() : ite;
                cause.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error embedding " + className + ":\n" + cause.getMessage());
            } catch (Throwable t) {
                t.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Failed to embed " + className + ":\n" + t.getMessage());
            }
        });
    }

    private static void showPanelInMain(JPanel panel, String title) {
        // replace mainContent contents with the provided panel
        mainContent.removeAll();
        mainContent.setLayout(new BorderLayout());
        mainContent.add(panel, BorderLayout.CENTER);
        // add a small back button to return to home
        JButton back = new JButton("Back");
        back.addActionListener(e -> showHomePage());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT)); top.setOpaque(false);
        top.add(back);
        mainContent.add(top, BorderLayout.NORTH);
        mainContent.revalidate();
        mainContent.repaint();
    }
}
