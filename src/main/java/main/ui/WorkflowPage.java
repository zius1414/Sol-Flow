package main.ui;

import main.db.TaskDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Workflow editor page.
 * - Keeps compatibility with existing TaskDAO API (insert(text,checked,ord) and TaskRecord(id,text,checked,ord))
 * - Supports opening as "blank new" (createBlank=true) so new workflows don't show old/global tasks.
 * - Stores workflowId for future per-workflow migration (DB changes required to fully scope tasks).
 */
public class WorkflowPage extends JPanel {
    private final int workflowId; // current workflow id (0 = none)
    private JPanel listContentPanel;
    private JPanel stepsPanel; // Right panel
    private JSplitPane splitPane;

    private static final Color BG_COLOR = new Color(40, 44, 52);
    private static final Color PANEL_COLOR = new Color(220, 220, 220);
    private static final Color TEXT_COLOR = new Color(24, 24, 24);
    private static final Color ACCENT_COLOR = new Color(132, 94, 194);
    private static final Color ACCENT_HOVER_COLOR = new Color(150, 110, 210);
    private static final Color DELETE_COLOR = new Color(200, 60, 60);
    private static final Color DELETE_HOVER_COLOR = new Color(220, 80, 80);
    private static final int CORNER_RADIUS = 15;

    // no-arg constructor (legacy/testing) — behaves like workflowId = 0, load tasks
    public WorkflowPage() {
        this(0, false);
    }

    // open existing workflow, load tasks (uses global TaskDAO for now)
    public WorkflowPage(int workflowId) {
        this(workflowId, false);
    }

    // main constructor
    // createBlank=true => do NOT load tasks from DB (show empty)
    public WorkflowPage(int workflowId, boolean createBlank) {
        this.workflowId = workflowId;
        initUI(!createBlank); // loadTasks = !createBlank
        if (workflowId > 0) {
            String title = createBlank ? ("New Workflow " + workflowId) : ("Workflow " + workflowId);
            setBorder(BorderFactory.createTitledBorder(title));
        }
    }

    // initialize UI, optionally loading tasks from DB (legacy global behavior)
    private void initUI(boolean loadTasks) {
    // use a split pane so user can drag separator between the planner and steps
    setLayout(new BorderLayout());
    setBackground(UITheme.BG);

        // Left panel (todo list)
        JPanel listPanel = createRoundedPanel(PANEL_COLOR, CORNER_RADIUS);
    listPanel.setLayout(new BorderLayout());
        listPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Plan your Day");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_COLOR);

    JButton addButton = createStyledButton("+", ACCENT_COLOR, ACCENT_HOVER_COLOR);
        addButton.setFont(new Font("Arial", Font.BOLD, 28));
        addButton.setPreferredSize(new Dimension(50, 40));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(addButton, BorderLayout.EAST);
        listPanel.add(headerPanel, BorderLayout.NORTH);

        // List content panel (scrollable)
        listContentPanel = new JPanel();
        listContentPanel.setLayout(new BoxLayout(listContentPanel, BoxLayout.Y_AXIS));
        listContentPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(listContentPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(PANEL_COLOR);

        listPanel.add(scrollPane, BorderLayout.CENTER);

        // Load tasks from DB (or show placeholder if none) ONLY when requested
        if (loadTasks) {
            try {
                // legacy TaskDAO.listAll() -> global tasks
                List<TaskDAO.TaskRecord> tasks = (workflowId > 0)
                        ? TaskDAO.listForWorkflow(workflowId)
                        : TaskDAO.listAll();
                if (tasks.isEmpty()) {
                    addListItemNew("Enter a new task...");
                } else {
                    for (TaskDAO.TaskRecord tr : tasks) addListItem(tr);
                }
            } catch (Exception ex) {
                addListItemNew("Enter a new task...");
            }
        } else {
            // new/blank workflow -> show a single empty placeholder
            addListItemNew("");
        }

        // Add button functionality
        addButton.addActionListener(e -> addListItemNew(""));

        // Steps Panel (right side)
        stepsPanel = createRoundedPanel(Color.WHITE, CORNER_RADIUS);
        stepsPanel.setPreferredSize(new Dimension(400, 0));
        stepsPanel.setVisible(false);

    JButton nextButton = createStyledButton("Next", ACCENT_COLOR, ACCENT_HOVER_COLOR);
    UITheme.stylePrimaryButton(nextButton);
        nextButton.setFont(new Font("Arial", Font.BOLD, 16));
        nextButton.setPreferredSize(new Dimension(100, 40));
        nextButton.addActionListener(e -> showStepsPanel());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.add(nextButton);
        listPanel.add(bottomPanel, BorderLayout.SOUTH);

        // split pane: left = listPanel, right = stepsPanel
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, stepsPanel);
        splitPane.setResizeWeight(0.6); // left side gets 60% by default when divider is shown
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);
        // start with stepsPanel effectively collapsed (divider at far right)
        // use proportional location 1.0 to place divider at 100% so right pane is hidden initially
        splitPane.setDividerLocation(1.0);

        add(splitPane, BorderLayout.CENTER);
    }

    private void showStepsPanel() {
        stepsPanel.removeAll();
        stepsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));

        // Gradient buttons
        JButton studentsBtn = createGradientButton("CUSTOMIZE CONTENTS",
                new Color(135, 206, 250), new Color(25, 25, 112));
        JButton teachersBtn = createGradientButton("MAIL ORGANISING",
                new Color(144, 238, 144), new Color(34, 139, 34));
        JButton peonsBtn = createGradientButton("DOC UPLOAD",
                new Color(255, 182, 193), new Color(178, 34, 34));

    studentsBtn.addActionListener(e -> showPanel(new ContentCreatorApp()));
    teachersBtn.addActionListener(e -> showPanel(new MailOrganize()));
    // pass current workflowId so Docupload stores/loads files scoped to this workflow
    peonsBtn.addActionListener(e -> showPanel(new Docupload(this.workflowId)));

        stepsPanel.add(studentsBtn);
        stepsPanel.add(teachersBtn);
        stepsPanel.add(peonsBtn);

        // reveal the right-side pane by moving the divider to a reasonable default
        try {
            splitPane.setDividerLocation(0.6); // reveal right panel at 40% width
        } catch (Exception ignored) {
            // ignore if divider can't be moved yet
        }
        stepsPanel.setVisible(true);
        revalidate();
        repaint();
    }

    private void showPanel(JPanel panelToShow) {
        // Instead of replacing the entire application window, embed the panel into the
        // right-side stepsPanel so the main app chrome (sidebar/nav) remains visible.
        try {
            stepsPanel.removeAll();
            // ensure stepsPanel uses BorderLayout so the embedded panel fills it
            stepsPanel.setLayout(new BorderLayout());
            // embed the panel directly (don't wrap with NavigationBar again) to avoid duplicate nav bars
            stepsPanel.add(panelToShow, BorderLayout.CENTER);
            stepsPanel.setVisible(true);
            stepsPanel.revalidate();
            stepsPanel.repaint();
        } catch (Exception ex) {
            // fallback to replacing frame content if embedding fails
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame != null) {
                frame.getContentPane().removeAll();
                frame.getContentPane().add(panelToShow, BorderLayout.CENTER);
                frame.revalidate();
                frame.repaint();
            }
        }
    }

    // load tasks (legacy behavior) or show blank
    private void initTasks(boolean loadTasks) {
        listContentPanel.removeAll();
        if (loadTasks) {
            try {
                List<TaskDAO.TaskRecord> tasks = (workflowId > 0)
                        ? TaskDAO.listForWorkflow(workflowId)
                        : TaskDAO.listAll();
                if (tasks.isEmpty()) {
                    addListItemNew("Enter a new task...");
                } else {
                    for (TaskDAO.TaskRecord tr : tasks) addListItem(tr);
                }
            } catch (Exception ex) {
                addListItemNew("Enter a new task...");
            }
        } else {
            addListItemNew("");
        }
        listContentPanel.revalidate();
        listContentPanel.repaint();
    }

    // legacy insert; behavior unchanged
    private void addListItemNew(String text) {
        int ord = Math.max(0, listContentPanel.getComponentCount()/2); // nicer ordering heuristic
        // insert into the current workflow scope (0 = global)
        int id;
        try {
            id = TaskDAO.insert(text == null ? "" : text, false, ord, workflowId);
        } catch (Exception ex) {
            id = -1;
        }
        TaskDAO.TaskRecord tr = new TaskDAO.TaskRecord(id, text == null ? "" : text, false, ord, workflowId);
        addListItem(tr);
    }

    // add UI for an existing TaskRecord (loaded from DB or just created)
    private void addListItem(TaskDAO.TaskRecord tr) {
        JPanel itemPanel = new JPanel(new BorderLayout(10, 0));
        itemPanel.setOpaque(false);
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JCheckBox checkBox = new JCheckBox();
        checkBox.setOpaque(false);
        checkBox.setPreferredSize(new Dimension(20, 20));
        checkBox.setSelected(tr.checked);

        RoundedTextField textField = new RoundedTextField(CORNER_RADIUS);
        textField.setText(tr.text == null || tr.text.isEmpty() ? "Enter a new task..." : tr.text);
        textField.setFont(new Font("Arial", Font.PLAIN, 18));
        textField.setForeground((tr.text == null || tr.text.isEmpty()) ? Color.GRAY : TEXT_COLOR);
        textField.setBackground(new Color(240, 240, 240));
        textField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // persist changes on focus lost
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals("Enter a new task...")) {
                    textField.setText("");
                    textField.setForeground(TEXT_COLOR);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                String newText = textField.getText().trim();
                if (newText.isEmpty()) {
                    textField.setForeground(Color.GRAY);
                    textField.setText("Enter a new task...");
                    newText = "";
                } else {
                    textField.setForeground(TEXT_COLOR);
                }
                // update DB
                if (tr.id > 0) {
                    TaskDAO.update(tr.id, newText, checkBox.isSelected(), tr.ord);
                    tr.text = newText;
                }
            }
        });

        // Enter key creates new list item
        textField.addActionListener(e -> addListItemNew(""));

        JButton deleteBtn = createStyledButton("x", DELETE_COLOR, DELETE_HOVER_COLOR);
        deleteBtn.setFont(new Font("Arial", Font.BOLD, 16));
        deleteBtn.setPreferredSize(new Dimension(40, 30));
        deleteBtn.addActionListener(e -> {
            listContentPanel.remove(itemPanel);
            listContentPanel.revalidate();
            listContentPanel.repaint();
            if (tr.id > 0) {
                try { TaskDAO.delete(tr.id); } catch (Exception ignored) {}
            }
        });

        checkBox.addActionListener(e -> {
            if (tr.id > 0) {
                try { TaskDAO.update(tr.id, tr.text == null ? "" : tr.text, checkBox.isSelected(), tr.ord); } catch (Exception ignored) {}
            }
            tr.checked = checkBox.isSelected();
        });

        itemPanel.add(checkBox, BorderLayout.WEST);
        itemPanel.add(textField, BorderLayout.CENTER);
        itemPanel.add(deleteBtn, BorderLayout.EAST);

        listContentPanel.add(Box.createVerticalStrut(10));
        listContentPanel.add(itemPanel);
        listContentPanel.revalidate();
        listContentPanel.repaint();
    }

    private JPanel createRoundedPanel(Color backgroundColor, int cornerRadius) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
                g2d.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBackground(backgroundColor);
        return panel;
    }

    private JButton createStyledButton(String text, Color normalColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(normalColor);
        button.setFocusPainted(false);
        button.setBorder(new LineBorder(normalColor, 2, true));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { button.setBackground(hoverColor); }
            @Override
            public void mouseExited(MouseEvent e) { button.setBackground(normalColor); }
        });
        return button;
    }

    private JButton createGradientButton(String text, Color startColor, Color endColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, startColor, getWidth(), getHeight(), endColor);
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS);
                g2d.dispose();

                super.paintComponent(g); // paint text only
            }
        };
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(250, 50));
        return button;
    }

    private static class RoundedTextField extends JTextField {
        private final int cornerRadius;
        public RoundedTextField(int radius) {
            this.cornerRadius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(getBackground());
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            super.paintComponent(g);
            g2d.dispose();
        }

        @Override protected void paintBorder(Graphics g) {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Workflow Notion UI");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.setLayout(new BorderLayout());
            frame.add(new WorkflowPage(), BorderLayout.CENTER);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
