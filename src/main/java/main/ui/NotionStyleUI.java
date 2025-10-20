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
        learnCardsPanel.add(createLearnCard("Quick<br>Business<br>Breakthroughs", "src/main/resources/assets/learn_1.png"));
        learnCardsPanel.add(createLearnCard("Create<br>guide to", "src/main/resources/assets/learn_2.png"));
        learnCardsPanel.add(createLearnCard("Customize &<br>style your", "src/main/resources/assets/learn_3.png"));
        learnCardsPanel.add(createLearnCard("Getting<br>started", "src/main/resources/assets/learn_4.png"));
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

    private static JPanel createLearnCard(String title, String imagePath) {
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

        // make quick-start card clickable: embed the inbuilt template into the main content area
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        // create an embeddable panel and show it inside the mainContent area
                        JPanel embedded = InbuiltJavaTemplate1.createEmbeddedPanel(-1);
                        mainContent.removeAll();
                        mainContent.setLayout(new BorderLayout());
                        mainContent.add(embedded, BorderLayout.CENTER);
                        mainContent.revalidate();
                        mainContent.repaint();
                    } catch (Exception ex) {
                        // fallback: open in a separate window
                        new InbuiltJavaTemplate1().show();
                    }
                });
            }
        });

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
}
