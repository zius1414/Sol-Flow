package main.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Simple consistent navigation bar shown on all pages.
 * - Back button returns to the app home (NotionStyleUI.show()).
 * - Forward button is kept for future wiring (disabled by default).
 * Use: add NavigationBar.create() above your page content or call NavigationBar.wrap(panel).
 */
public class NavigationBar extends JPanel {
    private final JButton backBtn;
    private final JButton forwardBtn;

    public NavigationBar() {
        setLayout(new BorderLayout());
        setBackground(new Color(24, 26, 28)); // dark neutral
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        // Try to load icons from resources (place your icons on the classpath, e.g. src/main/resources/icons/back.png)
        ImageIcon backIcon = null;
        ImageIcon forwardIcon = null;
        java.net.URL backUrl = getClass().getResource("/assets/back.png");
        java.net.URL forwardUrl = getClass().getResource("/assets/forward.png");
        if (backUrl != null) {
            backIcon = new ImageIcon(new ImageIcon(backUrl).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
        }
        if (forwardUrl != null) {
            forwardIcon = new ImageIcon(new ImageIcon(forwardUrl).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
        }

    backBtn = (backIcon != null) ? new JButton(backIcon) : new JButton("←");
        backBtn.setToolTipText("Back");
        backBtn.setFocusPainted(false);
        backBtn.setForeground(Color.WHITE);
        backBtn.setBackground(new Color(48, 51, 57));
        backBtn.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
        backBtn.addActionListener(e -> {
            // navigate back to the existing main/home UI (don't create a new window)
            try { NotionStyleUI.navigateToHome(); } catch (Exception ignored) {}
        });

    forwardBtn = (forwardIcon != null) ? new JButton(forwardIcon) : new JButton("→");
        forwardBtn.setToolTipText("Forward");
        forwardBtn.setFocusPainted(false);
        forwardBtn.setForeground(Color.WHITE);
        forwardBtn.setBackground(new Color(48, 51, 57));
        forwardBtn.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
        forwardBtn.setEnabled(false); // placeholder for future navigation

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

    public static NavigationBar create() {
        return new NavigationBar();
    }

    /**
     * Wrap an existing page panel in a container that shows the nav bar above it.
     * Returns the wrapper panel (BorderLayout) — add that to your frame instead of the raw page.
     */
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