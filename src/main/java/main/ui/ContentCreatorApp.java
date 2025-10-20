package main.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ContentCreatorApp.java
 *
 * Modern-ish, draggable, resizable "cards" UI.
 *
 * Place this file in:
 * C:\Users\joyal\OneDrive\Documents\SolFlow\src\main\java\main\ContentCreatorApp.java
 *
 */
public class ContentCreatorApp extends JPanel {

    private final JLayeredPane canvas;
    private final JScrollPane canvasScroll;
    private final AtomicInteger cardCounter = new AtomicInteger(0);
    private final int CANVAS_WIDTH = 1200;
    private final int CANVAS_HEIGHT = 900;

    // make these fields so header actions can reference them
    private JButton addBtn;
    private JButton googleBtn;

    // Theme colors used by the header / background
    private static final Color APP_BG = UITheme.BG;
    private static final Color HEADER_BG = UITheme.ACCENT.darker();
    private static final Color HEADER_GRADIENT = UITheme.ACCENT;

    // Path you gave (escape backslashes if used elsewhere)
    private static final String GOOGLE_ICON_PATH =
            "C:\\Users\\joyal\\OneDrive\\Documents\\SolFlow\\src\\main\\resources\\assets\\google.png";

    public ContentCreatorApp() {
    setLayout(new BorderLayout());
    setBackground(APP_BG);

        // Build page content so we can show the shared NavigationBar above it
        JPanel pageContent = new JPanel(new BorderLayout());
        pageContent.setOpaque(false);

        // Header
        JPanel header = new JPanel(new BorderLayout(12, 12)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, HEADER_BG, getWidth(), 0, HEADER_GRADIENT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0, 64));
        header.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel title = new JLabel("Customize the Contents", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setBorder(new EmptyBorder(6, 12, 6, 12));
        header.add(title, BorderLayout.CENTER);
        // --- small header action buttons (Add block + Google) ---
        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        headerActions.setOpaque(false);
        JButton headerAddSmall = createHeaderSmallBtn("+", e -> addBtn.doClick());
        JButton headerGoogleSmall = createHeaderSmallBtn("", e -> googleBtn.doClick());
        // try to set a small google icon if available
        try {
            File gf = new File(GOOGLE_ICON_PATH);
            if (gf.exists()) {
                Image gi = ImageIO.read(gf).getScaledInstance(18, 18, Image.SCALE_SMOOTH);
                headerGoogleSmall.setIcon(new ImageIcon(gi));
            } else {
                headerGoogleSmall.setText("G");
            }
        } catch (Exception ignored) { headerGoogleSmall.setText("G"); }
        headerActions.add(headerAddSmall);
        headerActions.add(headerGoogleSmall);
        header.add(headerActions, BorderLayout.EAST);
        // --- end header actions ---
        // make header visible by adding to the page content
        pageContent.add(header, BorderLayout.NORTH);

        // Left sidebar with add button + google icon
        JPanel leftBar = new JPanel();
        leftBar.setOpaque(false);
        leftBar.setLayout(new BoxLayout(leftBar, BoxLayout.Y_AXIS));
        leftBar.setBorder(new EmptyBorder(20, 12, 20, 24));
        leftBar.setPreferredSize(new Dimension(120, 0));

        // Add new card button (rounded)
    addBtn = new RoundedIconButton("+", 64, 64);
        addBtn.setToolTipText("Add new content block");
        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addBtn.setFont(new Font("SansSerif", Font.BOLD, 32));
        leftBar.add(Box.createVerticalGlue());
        leftBar.add(addBtn);
        leftBar.add(Box.createRigidArea(new Dimension(0, 18)));

        // Google image button
    googleBtn = createImageButton(GOOGLE_ICON_PATH, 62, 62);
    // style primary-like controls
    UITheme.stylePrimaryButton(addBtn);
        googleBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        googleBtn.setToolTipText("Google icon (from local path)");
        leftBar.add(googleBtn);
        leftBar.add(Box.createVerticalGlue());

        // add the sidebar to the page content so buttons show
        pageContent.add(leftBar, BorderLayout.EAST);

        // Canvas (layered pane) inside scrollpane
        canvas = new JLayeredPane();
        canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        canvas.setBackground(new Color(0, 0, 0));
        canvas.setOpaque(true);

        canvasScroll = new JScrollPane(canvas,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        canvasScroll.getViewport().setBackground(new Color(10, 10, 10));
        canvasScroll.setBorder(BorderFactory.createEmptyBorder());
        pageContent.add(canvasScroll, BorderLayout.CENTER);

        // Add initial cards positioned to resemble your screenshot
        addCard("Block A", 80, 120, 360, 200);
        addCard("Block B", 480, 120, 560, 200);
        addCard("Block C", 80, 360, 520, 260);
        addCard("Block D", 640, 360, 520, 260);

        // Add button behaviour
        addBtn.addActionListener(e -> {
            int centerX = Math.max(40, (canvas.getWidth() - 360) / 2);
            int centerY = Math.max(120, (canvas.getHeight() - 220) / 2);
            addCard("Block " + (cardCounter.incrementAndGet()), centerX, centerY, 360, 220);
            canvas.revalidate();
            canvas.repaint();
        });

        // google btn action (for demonstration) - simply shows a small dialog
        googleBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Google icon loaded from:\n" + GOOGLE_ICON_PATH,
                "Icon info", JOptionPane.INFORMATION_MESSAGE));

        // wrap the page content with the shared navigation bar
        add(NavigationBar.wrap(pageContent), BorderLayout.CENTER);
    }

    private JButton createImageButton(String path, int w, int h) {
        JButton b = new RoundedIconButton("", w, h);
        try {
            File f = new File(path);
            if (!f.exists()) throw new IOException("file not found");
            Image img = ImageIO.read(f);
            Image scaled = img.getScaledInstance(w - 12, h - 12, Image.SCALE_SMOOTH);
            b.setIcon(new ImageIcon(scaled));
        } catch (IOException ex) {
            // fallback text
            b.setText("G");
            b.setFont(new Font("SansSerif", Font.BOLD, 24));
        }
        return b;
    }

    // single helper for small round header buttons (used by headerAddSmall/headerGoogleSmall)
    private JButton createHeaderSmallBtn(String text, ActionListener al) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,255,255,24));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255,255,255,40));
                g2.drawOval(0, 0, getWidth()-1, getHeight()-1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setPreferredSize(new Dimension(36, 36));
        btn.setForeground(Color.WHITE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.addActionListener(al);
        btn.setContentAreaFilled(false);
        return btn;
    }

    /**
     * Create and add a draggable/resizable card to the canvas.
     */
    private void addCard(String title, int x, int y, int w, int h) {
        DraggableCard card = new DraggableCard(title, x, y, w, h);
        canvas.add(card, JLayeredPane.DEFAULT_LAYER);
        canvas.revalidate();
        canvas.repaint();
    }

    // ---------- Inner helper classes ----------

    /**
     * Simple rounded button that paints a rounded background and optionally an icon.
     */
    private static class RoundedIconButton extends JButton {
        private final int btnW;
        private final int btnH;

        public RoundedIconButton(String text, int w, int h) {
            super(text);
            btnW = w;
            btnH = h;
            setPreferredSize(new Dimension(btnW, btnH));
            setMinimumSize(new Dimension(btnW, btnH));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setForeground(Color.BLACK);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // subtle 3D/gradient background
            GradientPaint gp = new GradientPaint(0, 0, new Color(245, 245, 245),
                    0, getHeight(), new Color(210, 210, 210));
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);

            // stroke
            g2.setColor(new Color(140, 140, 140, 180));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);

            super.paintComponent(g);
            g2.dispose();
        }
    }

    /**
     * A draggable, resizable content card.
     */
    private class DraggableCard extends JPanel {
        private final int RESIZE_MARGIN = 10;
        private Point pressPointParent;
        private Rectangle origBounds;
        private boolean dragging = false;
        private boolean resizing = false;
        private boolean resizeLeft, resizeRight, resizeTop, resizeBottom;
        private boolean selected = false;

        private final JLabel titleLabel;
        private final JButton removeButton;
        private final JTextArea contentArea;

        public DraggableCard(String title, int x, int y, int w, int h) {
            setLayout(new BorderLayout());
            setOpaque(false);
            setBounds(x, y, w, h);

            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);
            top.setBorder(new EmptyBorder(8, 12, 8, 8));

            titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
            titleLabel.setForeground(Color.WHITE);

            removeButton = new JButton("✖");
            removeButton.setFocusable(false);
            removeButton.setBorderPainted(false);
            removeButton.setContentAreaFilled(false);
            removeButton.setForeground(Color.WHITE);
            removeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            top.add(titleLabel, BorderLayout.WEST);
            top.add(removeButton, BorderLayout.EAST);

            contentArea = new PlaceholderTextArea("Type details here...");
            contentArea.setLineWrap(true);
            contentArea.setWrapStyleWord(true);
            contentArea.setOpaque(false);
            contentArea.setForeground(Color.WHITE);
            contentArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
            JScrollPane contentScroll = new JScrollPane(contentArea);
            contentScroll.setOpaque(false);
            contentScroll.getViewport().setOpaque(false);
            contentScroll.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));

            add(top, BorderLayout.NORTH);
            add(contentScroll, BorderLayout.CENTER);

            removeButton.addActionListener(e -> {
                canvas.remove(this);
                canvas.revalidate();
                canvas.repaint();
            });
            
            CardMouseAdapter ma = new CardMouseAdapter();
            addMouseListener(ma);
            addMouseMotionListener(ma);
            top.addMouseListener(ma);
            top.addMouseMotionListener(ma);
            titleLabel.addMouseListener(ma);
            titleLabel.addMouseMotionListener(ma);
            removeButton.addMouseListener(ma);
            removeButton.addMouseMotionListener(ma);

            setSelected(false);
        }

        private void setSelected(boolean sel) {
            this.selected = sel;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            int w = getWidth();
            int h = getHeight();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRoundRect(4, 6, w - 2, h - 2, 24, 24);

            GradientPaint gp = new GradientPaint(0, 0, new Color(230, 230, 230, 35),
                    w, h, new Color(255, 255, 255, 18));
            g2.setPaint(gp);
            RoundRectangle2D rect = new RoundRectangle2D.Double(0, 0, w - 8, h - 8, 22, 22);
            g2.fill(rect);

            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRoundRect(0, 0, w - 8, h - 8, 22, 22);

            if (selected) {
                g2.setColor(new Color(180, 90, 240));
                g2.setStroke(new BasicStroke(3f));
            } else {
                g2.setColor(new Color(120, 120, 120, 140));
                g2.setStroke(new BasicStroke(2f));
            }
            g2.drawRoundRect(0, 0, w - 9, h - 9, 22, 22);

            g2.dispose();
            super.paintComponent(g);
        }

        private boolean isInResizeZone(Point p) {
            int w = getWidth(), h = getHeight();
            return p.x <= RESIZE_MARGIN || p.x >= w - RESIZE_MARGIN || p.y <= RESIZE_MARGIN || p.y >= h - RESIZE_MARGIN;
        }

        private class CardMouseAdapter extends MouseAdapter {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) return;
                pressPointParent = SwingUtilities.convertPoint(DraggableCard.this, e.getPoint(), canvas);
                origBounds = getBounds();

                Point local = e.getPoint();
                resizeLeft = local.x <= RESIZE_MARGIN;
                resizeRight = local.x >= getWidth() - RESIZE_MARGIN;
                resizeTop = local.y <= RESIZE_MARGIN;
                resizeBottom = local.y >= getHeight() - RESIZE_MARGIN;
                resizing = resizeLeft || resizeRight || resizeTop || resizeBottom;
                dragging = !resizing;

                for (Component c : canvas.getComponents()) {
                    if (c instanceof DraggableCard) ((DraggableCard) c).setSelected(c == DraggableCard.this);
                }

                canvas.moveToFront(DraggableCard.this);
                DraggableCard.this.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = false;
                resizing = false;
                resizeLeft = resizeRight = resizeTop = resizeBottom = false;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) return;
                Point currentParent = SwingUtilities.convertPoint(DraggableCard.this, e.getPoint(), canvas);
                int dx = currentParent.x - pressPointParent.x;
                int dy = currentParent.y - pressPointParent.y;

                if (dragging) {
                    int newX = origBounds.x + dx;
                    int newY = origBounds.y + dy;
                    newX = Math.max(8, Math.min(newX, canvas.getWidth() - origBounds.width - 8));
                    newY = Math.max(8, Math.min(newY, canvas.getHeight() - origBounds.height - 8));
                    setBounds(newX, newY, origBounds.width, origBounds.height);
                } else if (resizing) {
                    int newX = origBounds.x;
                    int newY = origBounds.y;
                    int newW = origBounds.width;
                    int newH = origBounds.height;

                    if (resizeLeft) {
                        newX = origBounds.x + dx;
                        newW = origBounds.width - dx;
                    }
                    if (resizeRight) {
                        newW = origBounds.width + dx;
                    }
                    if (resizeTop) {
                        newY = origBounds.y + dy;
                        newH = origBounds.height - dy;
                    }
                    if (resizeBottom) {
                        newH = origBounds.height + dy;
                    }

                    newW = Math.max(140, newW);
                    newH = Math.max(100, newH);

                    // if left resize hit min width, adjust x
                    if (resizeLeft && newW == 140) newX = origBounds.x + (origBounds.width - 140);

                    // constrain to canvas
                    newX = Math.max(8, Math.min(newX, canvas.getWidth() - newW - 8));
                    newY = Math.max(8, Math.min(newY, canvas.getHeight() - newH - 8));

                    setBounds(newX, newY, newW, newH);
                }
                revalidate();
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                int w = getWidth(), h = getHeight();
                boolean left = p.x <= RESIZE_MARGIN;
                boolean right = p.x >= w - RESIZE_MARGIN;
                boolean top = p.y <= RESIZE_MARGIN;
                boolean bottom = p.y >= h - RESIZE_MARGIN;

                Cursor c = Cursor.getDefaultCursor();
                if (left && top) c = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
                else if (right && top) c = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
                else if (left && bottom) c = Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
                else if (right && bottom) c = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
                else if (top) c = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
                else if (bottom) c = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
                else if (left) c = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
                else if (right) c = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
                else c = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);

                setCursor(c);
            }

            @Override
            public void mouseEntered(MouseEvent e) { setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)); }

            @Override
            public void mouseExited(MouseEvent e) { setCursor(Cursor.getDefaultCursor()); }
        }
    }

    // simple JTextArea with placeholder text (drawn when empty & unfocused)
    private static class PlaceholderTextArea extends JTextArea {
        private final String placeholder;
        private final Color placeholderColor = new Color(200, 200, 200);
        PlaceholderTextArea(String placeholder) {
            super();
            this.placeholder = placeholder;
            setOpaque(false);
            // repaint on focus changes so placeholder appears/disappears
            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { repaint(); }
                @Override public void focusLost(FocusEvent e) { repaint(); }
            });
            getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { repaint(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { repaint(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().length() == 0 && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(placeholderColor);
                Insets ins = getInsets();
                FontMetrics fm = g2.getFontMetrics(getFont());
                int x = ins.left + 2;
                int y = ins.top + fm.getAscent();
                g2.drawString(placeholder, x, y);
                g2.dispose();
            }
        }
    }

    // ---------- main for quick testing ----------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Customize the Contents (Preview)");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(1200, 800);
            f.setLayout(new BorderLayout());
            f.add(new ContentCreatorApp(), BorderLayout.CENTER);
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
