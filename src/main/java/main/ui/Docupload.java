package main.ui;

import main.db.FileDAO;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.awt.Desktop;

public class Docupload extends JPanel {

    private enum ViewMode { MINIMAL, MEDIUM }
    private ViewMode viewMode = ViewMode.MEDIUM;
    private final JPanel listPanel = new JPanel();
    private final List<File> uploadedFiles = new ArrayList<>();
    private final int workflowId;

    // legacy ctor: global files
    public Docupload() { this(0); }

    // new ctor: scope this uploader to a workflow id (0 = global)
    public Docupload(int workflowId) {
        this.workflowId = workflowId;
    Color primary = UITheme.ACCENT;
    Color surface = UITheme.SURFACE;
    Color bg = UITheme.BG;
    Color card = UITheme.SURFACE;

    setBackground(bg);
        // Keep outer container simple and insert a navigation bar above page content.
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // Build page content in a panel so we can wrap it with the shared NavigationBar
        JPanel pageContent = new JPanel(new BorderLayout(12, 12));
        pageContent.setOpaque(false);
        pageContent.setBorder(new EmptyBorder(0,0,0,0));

        JPanel top = new RoundedPanel(bg, 12);
        top.setLayout(new BorderLayout(8, 8));
        top.setOpaque(false);

    JButton uploadBtn = createUploadButton("Upload", primary, surface);
    UITheme.stylePrimaryButton(uploadBtn);
        top.add(uploadBtn, BorderLayout.WEST);

        JLabel title = new JLabel("Document Organiser", SwingConstants.CENTER);
        title.setFont(new Font("Roboto", Font.BOLD, 16));
        title.setForeground(new Color(28, 30, 33));
        top.add(title, BorderLayout.CENTER);

        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        rightTop.setOpaque(false);
        Icon minimalIcon = loadIcon("/assets/grid.png", 20, 20);
        Icon mediumIcon = loadIcon("/assets/list1.png", 20, 20);
        JToggleButton minimalBtn = new JToggleButton();
        JToggleButton mediumBtn = new JToggleButton();
        if (minimalIcon != null) minimalBtn.setIcon(minimalIcon); else minimalBtn.setText("Min");
        if (mediumIcon != null) mediumBtn.setIcon(mediumIcon); else mediumBtn.setText("Med");
        minimalBtn.setFocusPainted(false);
        mediumBtn.setFocusPainted(false);
        minimalBtn.setPreferredSize(new Dimension(40, 30));
        mediumBtn.setPreferredSize(new Dimension(40, 30));
        minimalBtn.setToolTipText("Minimal view");
        mediumBtn.setToolTipText("Medium view");
        ButtonGroup g = new ButtonGroup();
        g.add(minimalBtn);
        g.add(mediumBtn);
        mediumBtn.setSelected(true);
        minimalBtn.addActionListener(e -> { viewMode = ViewMode.MINIMAL; rebuildList(); });
        mediumBtn.addActionListener(e -> { viewMode = ViewMode.MEDIUM; rebuildList(); });
        rightTop.add(minimalBtn);
        rightTop.add(mediumBtn);

    JButton done = createFlatButton("Done", primary);
    UITheme.stylePrimaryButton(done);
        // close parent dialog on Done if shown as a dialog; if embedded, do nothing (user can navigate back)
        done.addActionListener(a -> {
            // try to find a JDialog ancestor and dispose it only
            Component c = Docupload.this;
            while (c != null && !(c instanceof Window)) c = c.getParent();
            if (c instanceof JDialog) {
                ((JDialog) c).dispose();
            } else if (c instanceof Window) {
                // if it's a standalone frame (legacy), dispose that
                ((Window) c).dispose();
            } else {
                // embedded: optionally show a toast or do nothing
            }
        });
        rightTop.add(done);
        top.add(rightTop, BorderLayout.EAST);

        // assemble page content
        pageContent.add(top, BorderLayout.NORTH);

        listPanel.setOpaque(false);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(new EmptyBorder(10, 6, 10, 6));

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(bg);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        // add(scroll, BorderLayout.CENTER);
        pageContent.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new RoundedPanel(bg, 12);
        bottom.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottom.setOpaque(false);
    JButton finishBtn = createFlatButton("Finish", new Color(56, 142, 60));
    UITheme.stylePrimaryButton(finishBtn);
        // ensure all uploaded files are persisted and show summary
        finishBtn.addActionListener(a -> {
            int synced = 0;
            synchronized (uploadedFiles) {
                for (File f : uploadedFiles) {
                    try { FileDAO.insertOrUpdate(f, this.workflowId); synced++; } catch (Exception ignored) {}
                }
            }
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Docupload.this),
                    "Synced " + synced + " uploaded files to database.", "Done", JOptionPane.INFORMATION_MESSAGE);
        });
        bottom.add(finishBtn);
        // add(bottom, BorderLayout.SOUTH);
        pageContent.add(bottom, BorderLayout.SOUTH);

        // finally wrap the pageContent with shared navigation bar and add to this panel
        add(NavigationBar.wrap(pageContent), BorderLayout.CENTER);

        // load persisted uploaded files from DB for this workflow
        try {
            List<File> persisted = FileDAO.listForWorkflow(this.workflowId);
            synchronized (uploadedFiles) {
                uploadedFiles.clear();
                uploadedFiles.addAll(persisted);
            }
        } catch (Exception ignored) {}

        rebuildList();
    }

    private void rebuildList() {
        listPanel.removeAll();

        List<File> source;
        synchronized (uploadedFiles) {
            source = new ArrayList<>(uploadedFiles);
        }

        if (source.isEmpty()) {
            listPanel.setLayout(new BorderLayout());
            listPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
            JLabel empty = new JLabel("No uploads", SwingConstants.CENTER);
            empty.setFont(new Font("Roboto", Font.PLAIN, 14));
            empty.setForeground(new Color(100, 110, 120));
            listPanel.add(empty, BorderLayout.CENTER);
        } else {
            if (viewMode == ViewMode.MINIMAL) {
                listPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 12, 12));
                listPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
                for (File f : source) listPanel.add(createFileCardMinimal(f.getName(), extOf(f.getName()), f));
            } else {
                listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
                listPanel.setBorder(new EmptyBorder(10, 6, 10, 6));
                boolean first = true;
                for (File f : source) {
                    if (!first) listPanel.add(Box.createVerticalStrut(10));
                    listPanel.add(createFileCardMedium(f.getName(), extOf(f.getName()), humanSize(f.length()), mDate(f.lastModified()), f));
                    first = false;
                }
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    private JComponent makeFileItem(String title, String type, String size, String date) {
        if (viewMode == ViewMode.MINIMAL) return createFileCardMinimal(title, type, null);
        return createFileCardMedium(title, type, size, date, null);
    }

    private JPanel createFileCardMinimal(String title, String type, File file) {
        RoundedPanel p = new RoundedPanel(Color.WHITE, 8);
        p.setLayout(new BorderLayout());
        p.setPreferredSize(new Dimension(160, 120));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Icon thumb = null;
        if (file != null && file.exists()) {
            thumb = thumbFromFile(file, 72, 72);
        }
        if (thumb == null) {
            if ("txt".equalsIgnoreCase(type) || "text".equalsIgnoreCase(type)) thumb = loadIcon("/assets/learn_1.png", 72, 72);
            if ("xlsx".equalsIgnoreCase(type) || "xls".equalsIgnoreCase(type)) thumb = loadIcon("/assets/learn_2.png", 72, 72);
            if ("csv".equalsIgnoreCase(type)) thumb = loadIcon("/assets/learn_3.png", 72, 72);
            if (thumb == null) thumb = loadIcon("/assets/list.png", 72, 72);
        }

        JLabel thumbLabel = new JLabel();
        if (thumb != null) {
            thumbLabel.setIcon(thumb);
            thumbLabel.setHorizontalAlignment(SwingConstants.CENTER);
        } else {
            thumbLabel.setText(type);
            thumbLabel.setHorizontalAlignment(SwingConstants.CENTER);
            thumbLabel.setForeground(new Color(80, 80, 80));
            thumbLabel.setFont(new Font("Roboto", Font.BOLD, 14));
        }
        thumbLabel.setBorder(new EmptyBorder(6, 6, 6, 6));
        p.add(thumbLabel, BorderLayout.CENTER);

        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(new Font("Roboto", Font.PLAIN, 13));
        t.setForeground(new Color(20, 22, 24));
        t.setBorder(new EmptyBorder(6, 4, 2, 4));
        p.add(t, BorderLayout.SOUTH);

        // interactions: double-click open, right-click menu (remove)
        if (file != null) {
            p.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                        try { Desktop.getDesktop().open(file); } catch (Exception ex) { /* ignore */ }
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        showFilePopupMenu(p, file, e.getX(), e.getY());
                    }
                }
            });
        }

        return p;
    }

    private JPanel createFileCardMedium(String title, String type, String size, String date, File file) {
        RoundedPanel p = new RoundedPanel(Color.WHITE, 10);
        p.setLayout(new BorderLayout(8, 8));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        p.setBorder(new EmptyBorder(10, 12, 10, 12));

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        JLabel icon = new JLabel(type, SwingConstants.CENTER);
        icon.setFont(new Font("Roboto", Font.BOLD, 14));
        icon.setPreferredSize(new Dimension(56, 56));
        icon.setOpaque(true);
        icon.setBackground(new Color(244, 246, 248));
        icon.setBorder(new RoundedBorder(8, new Color(0,0,0,10)));
        if (file != null && file.exists()) {
            Icon thumb = thumbFromFile(file, 40, 40);
            if (thumb != null) icon.setIcon(thumb);
        }
        left.add(icon, BorderLayout.CENTER);
        p.add(left, BorderLayout.WEST);

        JPanel center = new JPanel(new BorderLayout(4, 4));
        center.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(new Font("Roboto", Font.BOLD, 14));
        t.setForeground(new Color(20, 22, 24));
        center.add(t, BorderLayout.NORTH);

        JLabel meta = new JLabel(type + " · " + size + " · " + date);
        meta.setFont(new Font("Roboto", Font.PLAIN, 12));
        meta.setForeground(new Color(100, 110, 120));
        center.add(meta, BorderLayout.CENTER);

        p.add(center, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        right.setOpaque(false);
        JButton open = new JButton("Open");
        open.setFont(new Font("Roboto", Font.PLAIN, 12));
        open.setPreferredSize(new Dimension(72, 30));
        open.setFocusPainted(false);
    UITheme.stylePrimaryButton(open);
    open.setBorder(new RoundedBorder(8, new Color(0,0,0,18)));

        JButton more = new JButton("⋯");
        more.setPreferredSize(new Dimension(40, 30));
        more.setFocusPainted(false);
        more.setBorder(new RoundedBorder(8, new Color(0,0,0,14)));
        right.add(open);
        right.add(more);
        p.add(right, BorderLayout.EAST);

        if (file != null) {
            open.addActionListener(a -> {
                try { Desktop.getDesktop().open(file); } catch (Exception ex) { /* ignore */ }
                try { FileDAO.insertOrUpdate(file, Docupload.this.workflowId); } catch (Exception ignored) {}
            });
            more.addActionListener(a -> {
                showFilePopupMenu(more, file, 0, more.getHeight());
            });
        }

        return p;
    }

    private JButton createUploadButton(String text, Color accent, Color surface) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBackground(surface);
        btn.setForeground(new Color(20, 22, 24));
        btn.setFont(new Font("Roboto", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        btn.setOpaque(true);

        Icon icon = loadIcon("/assets/upload.png", 18, 18);
        if (icon != null) btn.setIcon(icon);
        btn.setIconTextGap(10);
        btn.setPreferredSize(new Dimension(120, 40));
        btn.setBorder(new RoundedBorder(10, new Color(0,0,0,18)));

        // open file chooser and add files
        btn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setMultiSelectionEnabled(true);
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int res = fc.showOpenDialog(SwingUtilities.getWindowAncestor(this));
            if (res == JFileChooser.APPROVE_OPTION) {
                File[] files = fc.getSelectedFiles();
                int added = 0;
                synchronized (uploadedFiles) {
                    for (File f : files) {
                        if (f != null && f.exists() && !containsFile(uploadedFiles, f)) {
                            // persist metadata immediately scoped to workflow
                            try { FileDAO.insertOrUpdate(f, Docupload.this.workflowId); } catch (Exception ignored) {}
                            uploadedFiles.add(f);
                            added++;
                        }
                    }
                }
                if (added > 0) rebuildList();
                else JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "No new files selected.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        return btn;
    }

    private boolean containsFile(List<File> list, File f) {
        for (File e : list) if (e.getAbsolutePath().equals(f.getAbsolutePath())) return true;
        return false;
    }

    private JButton createFlatButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Roboto", Font.PLAIN, 13));
        b.setBorder(new RoundedBorder(10, new Color(0,0,0,30)));
        b.setPreferredSize(new Dimension(88, 36));
        return b;
    }

    private Icon loadIcon(String resourcePath, int w, int h) {
        try {
            URL url = MailOrganize.class.getResource(resourcePath);

            // try classpath
            if (url != null) {
                try (InputStream is = url.openStream()) {
                    BufferedImage img = ImageIO.read(is);
                    if (img != null) {
                        Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaled);
                    }
                } catch (Exception ignored) {}

                ImageIcon raw = new ImageIcon(url);
                if (raw.getIconWidth() > 0) {
                    Image scaled = raw.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);
                }
            }

            // fallback to local src (IDE)
            File f = new File(System.getProperty("user.dir") + "/src/main/resources" + resourcePath);
            if (f.exists()) {
                try {
                    BufferedImage img = ImageIO.read(f);
                    if (img != null) {
                        Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaled);
                    }
                } catch (Exception ignored) {}
                ImageIcon rawFile = new ImageIcon(f.toURI().toURL());
                if (rawFile.getIconWidth() > 0) return new ImageIcon(rawFile.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
            }
        } catch (Exception ignored) {}
        return null;
    }

    private Icon thumbFromFile(File f, int w, int h) {
        try {
            String ext = extOf(f.getName()).toLowerCase();
            if (ext.matches("png|jpg|jpeg|gif|bmp")) {
                BufferedImage img = ImageIO.read(f);
                if (img != null) {
                    Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);
                }
            }
        } catch (IOException ignored) {}
        return null;
    }

    private static String extOf(String name) {
        int i = name.lastIndexOf('.');
        return (i > 0) ? name.substring(i+1) : "";
    }

    private static String humanSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    private static String mDate(long ms) {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(ms));
    }

    // WrapLayout for horizontal wrapping
    private static class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        @Override
        public Dimension preferredLayoutSize(Container target) {
            synchronized (target.getTreeLock()) {
                int hgap = getHgap(), vgap = getVgap();
                int width = target.getWidth();
                if (width <= 0) width = Integer.MAX_VALUE / 2;
                Insets insets = target.getInsets();
                int maxwidth = width - (insets.left + insets.right + hgap*2);
                int x = 0, y = insets.top + vgap;
                int rowh = 0;
                for (Component c : target.getComponents()) {
                    if (!c.isVisible()) continue;
                    Dimension d = c.getPreferredSize();
                    if (x == 0 || x + d.width <= maxwidth) {
                        if (x > 0) x += hgap;
                        x += d.width;
                        rowh = Math.max(rowh, d.height);
                    } else {
                        x = d.width;
                        y += vgap + rowh;
                        rowh = d.height;
                    }
                }
                y += rowh + insets.bottom;
                return new Dimension(width, y);
            }
        }
    }

    private static class RoundedPanel extends JPanel {
        private final Color bg;
        private final int radius;
        RoundedPanel(Color bg, int r) { this.bg = bg; this.radius = r; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedBorder extends AbstractBorder {
        private final int r;
        private final Color col;
        RoundedBorder(int r, Color col) { this.r = r; this.col = col; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(col);
            g2.drawRoundRect(x, y, width - 1, height - 1, r, r);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Mail Organizer");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(900, 640);
        f.setLocationRelativeTo(null);
        f.setContentPane(new MailOrganize());
        f.setVisible(true);
    }

    // popup menu for a file card
    private void showFilePopupMenu(Component invoker, File file, int x, int y) {
        JPopupMenu pm = new JPopupMenu();
        JMenuItem open = new JMenuItem("Open");
        JMenuItem remove = new JMenuItem("Remove");
        open.addActionListener(e -> {
            try {
                if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Cannot open file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        remove.addActionListener(e -> {
            synchronized (uploadedFiles) {
                uploadedFiles.removeIf(f -> f.getAbsolutePath().equals(file.getAbsolutePath()));
                try { FileDAO.deleteByPath(file.getAbsolutePath()); } catch (Exception ignored) {}
            }
            rebuildList();
        });
        pm.add(open);
        pm.addSeparator();
        pm.add(remove);
        pm.show(invoker, x, y);
    }
}
