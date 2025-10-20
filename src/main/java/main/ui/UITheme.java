package main.ui;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class UITheme {
    public static final Color BG = new Color(24, 24, 24);
    public static final Color SURFACE = new Color(34, 36, 40);
    public static final Color MUTED = new Color(140, 140, 150);
    public static final Color ACCENT = new Color(88, 101, 242);
    public static final Color ACCENT_HOVER = new Color(102, 115, 250);
    public static final Font UI_FONT = new Font("Inter", Font.PLAIN, 13);
    public static final Font UI_FONT_BOLD = new Font("Inter", Font.BOLD, 13);

    private UITheme() {}

    // Apply some global UI defaults for fonts/colors that modernize look
    public static void applyGlobalTheme() {
        UIManager.put("Label.foreground", Color.WHITE);
        UIManager.put("Panel.background", BG);
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("Button.font", UI_FONT);
        UIManager.put("Label.font", UI_FONT);
        UIManager.put("TextField.font", UI_FONT);
        UIManager.put("PasswordField.font", UI_FONT);
        UIManager.put("TextField.background", new Color(40,40,40));
        UIManager.put("TextField.foreground", Color.WHITE);
    }

    public static void styleSidebarButton(JButton b) {
        b.setBackground(new Color(44,44,44));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        b.setFont(UI_FONT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            Color orig = b.getBackground();
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(orig.brighter()); }
            @Override public void mouseExited(MouseEvent e) { b.setBackground(orig); }
        });
    }

    public static void styleNavButton(JButton b) {
        b.setBackground(new Color(48,51,57));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(new Color(60,63,70), 1, true));
        b.setFont(UI_FONT_BOLD);
    }

    public static void stylePrimaryButton(JButton b) {
        b.setBackground(ACCENT);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        b.setFont(UI_FONT_BOLD);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(ACCENT_HOVER); }
            @Override public void mouseExited(MouseEvent e) { b.setBackground(ACCENT); }
        });
    }
}
