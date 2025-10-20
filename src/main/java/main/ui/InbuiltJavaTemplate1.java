package main.ui;

import main.db.TaskDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Calendar;
import java.util.Date;

/**
 * InbuiltJavaTemplate1: The Main Application Frame & All Page 1 Functionality.
 * This file contains the main entry point, navigation logic, the main dashboard
 * for page 1, and the detailed views for all its functions, managed by a CardLayout.
 *
 * @author 100x Java Software Engineer
 * @version FINAL (Habit Tracker Implemented & Verified)
 */
public class InbuiltJavaTemplate1 {

    // --- CardLayout Constants for Navigation ---
    private static final String PAGE1_DASHBOARD = "PAGE1_DASHBOARD";
    private static final String PLANNER_TASKS_PAGE = "PLANNER_TASKS_PAGE";
    private static final String DAILY_ROUTINES_PAGE = "DAILY_ROUTINES_PAGE";
    private static final String JOURNAL_MOODS_PAGE = "JOURNAL_MOODS_PAGE";
    private static final String GOALS_MILESTONES_PAGE = "GOALS_MILESTONES_PAGE";

    // --- UI Theme Constants ---
    private static final Color BACKGROUND_COLOR = Color.decode("#2F3437");
    private static final Color PANEL_COLOR = Color.decode("#3C4043");
    private static final Color TEXT_COLOR = Color.decode("#E8EAED");
    private static final Color ACCENT_COLOR_HOVER = Color.decode("#5F6368");
    private static final Color ACCENT_COLOR_BLUE = Color.decode("#8AB4F8");
    private static final Font NAV_FONT = new Font("Segoe UI", Font.PLAIN, 16);

    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel mainContentPanel;

    // changed: workflowId is no longer final so we can set it from multiple ctors
    private int workflowId;

    // default ctor delegates to int ctor
    public InbuiltJavaTemplate1() {
        this(-1);
    }

    // new ctor that accepts a workflow id so PlannerTasksPage can load/save todos for that workflow
    public InbuiltJavaTemplate1(int workflowId) {
        this.workflowId = workflowId;
        // initialize UI (shared code moved into initUI to keep both ctors consistent)
        initUI();
    }

    // shared initialization originally in the ctor
    private void initUI() {

    frame = new JFrame("Productivity Hub");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        cardLayout = new CardLayout();
    mainContentPanel = new JPanel(cardLayout);
    mainContentPanel.setBackground(UITheme.BG);

        frame.add(createSidebar(), BorderLayout.WEST);

        addPagePanels();

        frame.add(mainContentPanel, BorderLayout.CENTER);
        cardLayout.show(mainContentPanel, PAGE1_DASHBOARD);
    }
    
    private void addPagePanels() {
        Page1DashboardPanel page1Dashboard = new Page1DashboardPanel(
            () -> cardLayout.show(mainContentPanel, PLANNER_TASKS_PAGE),
            () -> cardLayout.show(mainContentPanel, DAILY_ROUTINES_PAGE),
            () -> cardLayout.show(mainContentPanel, JOURNAL_MOODS_PAGE),
            () -> cardLayout.show(mainContentPanel, GOALS_MILESTONES_PAGE)
        );

        // pass workflowId into PlannerTasksPage so it can load/save tasks for the selected workflow
        PlannerTasksPage plannerTasksPage = new PlannerTasksPage(() -> cardLayout.show(mainContentPanel, PAGE1_DASHBOARD), this.workflowId);
        DailyRoutinesPage dailyRoutinesPage = new DailyRoutinesPage(() -> cardLayout.show(mainContentPanel, PAGE1_DASHBOARD));
        JournalMoodsPage journalMoodsPage = new JournalMoodsPage(() -> cardLayout.show(mainContentPanel, PAGE1_DASHBOARD));
        GoalsMilestonesPage goalsMilestonesPage = new GoalsMilestonesPage(() -> cardLayout.show(mainContentPanel, PAGE1_DASHBOARD));

        mainContentPanel.add(page1Dashboard, PAGE1_DASHBOARD);
        mainContentPanel.add(plannerTasksPage, PLANNER_TASKS_PAGE);
        mainContentPanel.add(dailyRoutinesPage, DAILY_ROUTINES_PAGE);
        mainContentPanel.add(journalMoodsPage, JOURNAL_MOODS_PAGE);
        mainContentPanel.add(goalsMilestonesPage, GOALS_MILESTONES_PAGE);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(PANEL_COLOR);
        sidebar.setPreferredSize(new Dimension(180, 0));
        sidebar.setBorder(new EmptyBorder(20, 0, 20, 0));

        sidebar.add(createNavButton("Page 1", () -> cardLayout.show(mainContentPanel, PAGE1_DASHBOARD) ));
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    private JButton createNavButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(NAV_FONT);
        button.setForeground(TEXT_COLOR);
        button.setBackground(PANEL_COLOR);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) { button.setBackground(ACCENT_COLOR_HOVER); }
            @Override
            public void mouseExited(MouseEvent evt) { button.setBackground(PANEL_COLOR); }
        });
        
        button.addActionListener(e -> action.run());
        return button;
    }

    public void show() {
        if (frame != null) frame.setVisible(true);
    }

    /**
     * Return a panel that can be embedded into another container (wrapped with NavigationBar by caller).
     * This does not create a separate JFrame.
     */
    public JPanel getPanel() {
        // Ensure UI is initialized
        if (mainContentPanel == null) {
            initUI();
        }
        return mainContentPanel;
    }

    /**
     * Helper to create an embeddable panel for a given workflow id.
     */
    public static JPanel createEmbeddedPanel(int workflowId) {
        InbuiltJavaTemplate1 t = new InbuiltJavaTemplate1(workflowId);
        // callers expect a panel they can put inside their frame; return the wrapper with nav bar
        return NavigationBar.wrap(t.getPanel());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
            new InbuiltJavaTemplate1().show();
        });
    }

    // --- NESTED STATIC CLASSES FOR UI PANELS ---

    static class Page1DashboardPanel extends JPanel {
    private static final Font PRIMARY_FONT = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font CARD_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font CARD_SUB_FONT = new Font("Segoe UI", Font.PLAIN, 12);
        
        public Page1DashboardPanel(Runnable onPlannerClick, Runnable onRoutinesClick, Runnable onJournalClick, Runnable onGoalsClick) {
            setBackground(BACKGROUND_COLOR); setLayout(new BorderLayout()); setBorder(new EmptyBorder(20, 30, 20, 30));
            
            JPanel gridPanel = new JPanel(new GridLayout(0, 4, 15, 15)); 
            gridPanel.setBackground(BACKGROUND_COLOR); 
            gridPanel.setBorder(new EmptyBorder(10,10,10,10));
            gridPanel.add(createDashboardCard("⚡ Planner & Tasks", onPlannerClick));
            gridPanel.add(createDashboardCard("🔄 Daily Routines & Habits", onRoutinesClick));
            gridPanel.add(createDashboardCard("📖 Journal & Moods", onJournalClick));
            gridPanel.add(createDashboardCard("🎯 Goals & Milestones", onGoalsClick));
            
            JScrollPane scrollPane = new JScrollPane(gridPanel); 
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setBorder(BorderFactory.createEmptyBorder()); 
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);

            add(scrollPane, BorderLayout.CENTER);
        }

        private JPanel createDashboardCard(String title, Runnable action) {
            RoundedPanel cardPanel = new RoundedPanel(14, PANEL_COLOR);
            cardPanel.setPreferredSize(new Dimension(260, 120));
            cardPanel.setMaximumSize(new Dimension(280, 140));
            cardPanel.setLayout(new BorderLayout());
            cardPanel.setBorder(new EmptyBorder(18, 18, 18, 18));
            cardPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JPanel textPanel = new JPanel(); textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS)); textPanel.setOpaque(false);
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(CARD_TITLE_FONT);
            titleLabel.setForeground(TEXT_COLOR);
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel subtitle = new JLabel("Open and manage your workflows");
            subtitle.setFont(CARD_SUB_FONT);
            subtitle.setForeground(new Color(180, 180, 180));
            subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

            textPanel.add(titleLabel);
            textPanel.add(Box.createVerticalStrut(6));
            textPanel.add(subtitle);

            cardPanel.add(textPanel, BorderLayout.CENTER);

            cardPanel.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent evt) { cardPanel.setBackgroundColor(ACCENT_COLOR_HOVER); cardPanel.repaint(); }
                @Override public void mouseExited(MouseEvent evt) { cardPanel.setBackgroundColor(PANEL_COLOR); cardPanel.repaint(); }
                @Override public void mouseClicked(MouseEvent evt) { if (action != null) action.run(); else JOptionPane.showMessageDialog(cardPanel, "This feature is not yet implemented.", "Info", JOptionPane.INFORMATION_MESSAGE); }
            });

            return cardPanel;
        }
    }

    static class PlannerTasksPage extends JPanel {
        private static final Color INFO_BOX_COLOR = new Color(0x3A, 0x4B, 0x6D);
        private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 32);
        private static final Font PRIMARY_FONT = new Font("Segoe UI", Font.PLAIN, 15);
        private static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 15);
        private final DefaultTableModel tableModel;
        private final Calendar calendar;
        private final JLabel monthYearLabel;
        private final JPanel calendarGridPanel;

        // added field to store workflowId for this planner instance
        private final int workflowId;

        // update constructor signature to accept workflowId
        public PlannerTasksPage(Runnable backAction, int workflowId) {
            this.workflowId = workflowId;

            setLayout(new BorderLayout(0, 20)); setBackground(BACKGROUND_COLOR); setBorder(new EmptyBorder(20, 30, 20, 30));
            
            calendar = Calendar.getInstance();
            calendar.set(2025, Calendar.OCTOBER, 20);

            monthYearLabel = new JLabel("", SwingConstants.CENTER);
            calendarGridPanel = new JPanel(new GridLayout(0, 7, 5, 5));
            
            tableModel = new DefaultTableModel(new Object[][]{
                {"Design new dashboard UI", "High", "Work", "2025-10-22"},
                {"Schedule team meeting", "Medium", "Work", "2025-10-21"},
                {"Buy groceries", "Low", "Personal", "2025-10-20"},
            }, new String[]{"Aa Task", "Priority", "Type", "Date"});

            add(createHeaderPanel(backAction), BorderLayout.NORTH);
            
            JPanel contentPanel = new JPanel(); 
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS)); 
            contentPanel.setOpaque(false);
            contentPanel.add(createInfoBox()); 
            contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            contentPanel.add(createTaskPanel()); 
            contentPanel.add(Box.createRigidArea(new Dimension(0, 20))); 
            contentPanel.add(createMonthlyPlannerPanel());
            
            JScrollPane scrollPane = new JScrollPane(contentPanel); 
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setBorder(BorderFactory.createEmptyBorder()); 
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            add(scrollPane, BorderLayout.CENTER);
            
            updateCalendar();

            // AFTER tableModel is created (ensure this location matches your file's constructor)
            // load tasks for this workflow into the table model
            loadTasksForWorkflow();
        }

        // implement load/create/save using TaskDAO.listForWorkflow / TaskDAO.insert / TaskDAO.update / TaskDAO.delete
        // Example helper to load tasks (adjust to your UI model):
        private void loadTasksForWorkflow() {
            tableModel.setRowCount(0);
            List<TaskDAO.TaskRecord> tasks = TaskDAO.listForWorkflow(this.workflowId <= 0 ? 0 : this.workflowId);
            for (TaskDAO.TaskRecord t : tasks) {
                tableModel.addRow(new Object[] { t.text, t.checked });
            }
        }

        // create task and persist
        private void createNewTask(String text) {
            if (text == null || text.trim().isEmpty()) return;
            int ord = tableModel.getRowCount();
            int id = TaskDAO.insert(text, false, ord, this.workflowId <= 0 ? 0 : this.workflowId);
            if (id != -1) loadTasksForWorkflow();
            else tableModel.addRow(new Object[] { text, false });
        }

        private JPanel createHeaderPanel(Runnable backAction) {
            JPanel headerPanel = new JPanel(new BorderLayout()); headerPanel.setBackground(BACKGROUND_COLOR);
            JButton backButton = new JButton("⬅️ Back"); backButton.setFont(BOLD_FONT); backButton.setForeground(ACCENT_COLOR_BLUE); backButton.setContentAreaFilled(false); backButton.setBorder(new EmptyBorder(5, 0, 5, 15)); backButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); backButton.addActionListener(e -> backAction.run()); headerPanel.add(backButton, BorderLayout.WEST);
            JLabel titleLabel = new JLabel("⚡ Planner & Tasks"); titleLabel.setFont(HEADER_FONT); titleLabel.setForeground(TEXT_COLOR); headerPanel.add(titleLabel, BorderLayout.CENTER);
            return headerPanel;
        }

        private JPanel createInfoBox() {
            RoundedPanel infoPanel = new RoundedPanel(10, INFO_BOX_COLOR); infoPanel.setLayout(new BorderLayout(10, 0)); infoPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
            JLabel iconLabel = new JLabel("💡"); iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20)); infoPanel.add(iconLabel, BorderLayout.WEST);
            JTextPane infoText = new JTextPane(); infoText.setOpaque(false); infoText.setEditable(false); infoText.setContentType("text/html"); infoText.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE); infoText.setFont(PRIMARY_FONT);
            infoText.setText("<html><body style='color: #E8EAED;'><b>Welcome to Planner & Tasks</b><br>Add your to-dos to the database below. For more help, contact us.</body></html>");
            infoPanel.add(infoText, BorderLayout.CENTER);
            return infoPanel;
        }

        private JPanel createTaskPanel() {
            JPanel taskPanel = new JPanel(new BorderLayout(0, 10)); taskPanel.setOpaque(false);
            taskPanel.add(createTaskToolbar(), BorderLayout.NORTH);

            JTable table = new JTable(tableModel);
            table.setBackground(PANEL_COLOR); table.setForeground(TEXT_COLOR); table.setGridColor(BACKGROUND_COLOR); table.setFont(PRIMARY_FONT); table.setRowHeight(30); table.setSelectionBackground(ACCENT_COLOR_BLUE); table.setSelectionForeground(BACKGROUND_COLOR);
            JTableHeader header = table.getTableHeader(); header.setBackground(BACKGROUND_COLOR); header.setForeground(TEXT_COLOR); header.setFont(BOLD_FONT); header.setPreferredSize(new Dimension(0, 40));
            ((DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
            
            JScrollPane scrollPane = new JScrollPane(table); 
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setBorder(BorderFactory.createLineBorder(PANEL_COLOR));
            taskPanel.add(scrollPane, BorderLayout.CENTER);
            return taskPanel;
        }

        private JPanel createTaskToolbar() {
            JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            toolbar.setOpaque(false);

            toolbar.add(createToolbarButton("Today"));
            toolbar.add(createToolbarButton("To Do This Week"));
            toolbar.add(createToolbarButton("5 more..."));
            
            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
            rightPanel.setOpaque(false);
            
            JButton newButton = new JButton("New ▾");
            newButton.setFont(BOLD_FONT);
            UITheme.stylePrimaryButton(newButton);
            newButton.addActionListener(e -> openNewTaskDialog());
            
            rightPanel.add(newButton);

            JPanel mainToolbar = new JPanel(new BorderLayout());
            mainToolbar.setOpaque(false);
            mainToolbar.add(toolbar, BorderLayout.WEST);
            mainToolbar.add(rightPanel, BorderLayout.EAST);
            
            return mainToolbar;
        }
        
        private JButton createToolbarButton(String text) {
            JButton button = new JButton(text);
            button.setFont(PRIMARY_FONT);
            button.setForeground(TEXT_COLOR);
            button.setBackground(PANEL_COLOR);
            button.setFocusPainted(false);
            button.setBorder(new EmptyBorder(5, 10, 5, 10));
            return button;
        }

        private void openNewTaskDialog() {
            JTextField taskField = new JTextField(); JTextField priorityField = new JTextField(); JTextField typeField = new JTextField(); JTextField dateField = new JTextField();
            for(Component c : new Component[]{taskField, priorityField, typeField, dateField}) c.setFont(PRIMARY_FONT);
            JPanel dialogPanel = new JPanel(new GridLayout(0, 1, 0, 5));
            dialogPanel.add(new JLabel("Task:")); dialogPanel.add(taskField); dialogPanel.add(new JLabel("Priority:")); dialogPanel.add(priorityField); dialogPanel.add(new JLabel("Type:")); dialogPanel.add(typeField); dialogPanel.add(new JLabel("Date:")); dialogPanel.add(dateField);
            int result = JOptionPane.showConfirmDialog(this, dialogPanel, "Create New Task", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION && !taskField.getText().trim().isEmpty()) {
                tableModel.addRow(new Object[]{taskField.getText(), priorityField.getText(), typeField.getText(), dateField.getText()});
            }
        }

        private JPanel createMonthlyPlannerPanel() {
            JPanel plannerPanel = new JPanel(new BorderLayout()); plannerPanel.setOpaque(false);
            
            JLabel title = new JLabel("🗓️ Monthly Planner"); title.setFont(new Font("Segoe UI", Font.BOLD, 22)); title.setForeground(TEXT_COLOR);
            plannerPanel.add(title, BorderLayout.NORTH);

            JPanel toolbar = new JPanel(new BorderLayout()); toolbar.setOpaque(false); toolbar.setBorder(new EmptyBorder(10,0,10,0));
            monthYearLabel.setFont(BOLD_FONT); monthYearLabel.setForeground(TEXT_COLOR);
            
            JPanel leftToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
            leftToolbar.setOpaque(false);
            leftToolbar.add(monthYearLabel);
            toolbar.add(leftToolbar, BorderLayout.WEST);

            JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0)); navButtons.setOpaque(false);
            navButtons.add(createToolbarButton("Manage in Calendar"));
            JButton prev = new JButton("<"); prev.addActionListener(e -> { calendar.add(Calendar.MONTH, -1); updateCalendar(); });
            JButton today = new JButton("Today"); today.addActionListener(e -> { calendar.set(2025, Calendar.OCTOBER, 20); updateCalendar(); });
            JButton next = new JButton(">"); next.addActionListener(e -> { calendar.add(Calendar.MONTH, 1); updateCalendar(); });
            for(JButton b : new JButton[]{prev, today, next}) {
                b.setFont(BOLD_FONT); b.setForeground(TEXT_COLOR); b.setBackground(PANEL_COLOR); b.setFocusPainted(false); navButtons.add(b);
            }
            toolbar.add(navButtons, BorderLayout.EAST);
            
            JPanel calendarContainer = new JPanel(new BorderLayout());
            calendarContainer.setOpaque(false);
            calendarContainer.add(toolbar, BorderLayout.NORTH);

            calendarGridPanel.setOpaque(false);
            calendarContainer.add(calendarGridPanel, BorderLayout.CENTER);
            
            plannerPanel.add(calendarContainer, BorderLayout.CENTER);
            return plannerPanel;
        }

        private void updateCalendar() {
            calendarGridPanel.removeAll();
            
            monthYearLabel.setText(new SimpleDateFormat("MMMM yyyy").format(calendar.getTime()));
            
            String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            for(String day : days) {
                JLabel dayLabel = new JLabel(day, SwingConstants.CENTER); dayLabel.setFont(BOLD_FONT); dayLabel.setForeground(TEXT_COLOR); calendarGridPanel.add(dayLabel);
            }

            Calendar tempCal = (Calendar) calendar.clone();
            tempCal.set(Calendar.DAY_OF_MONTH, 1);
            int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK);
            int offset = (firstDayOfWeek == Calendar.SUNDAY) ? 6 : firstDayOfWeek - 2;

            for(int i=0; i < offset; i++) calendarGridPanel.add(new JLabel(""));

            int daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);
            Calendar today = Calendar.getInstance();
            today.set(2025, Calendar.OCTOBER, 20);

            for(int i=1; i <= daysInMonth; i++) {
                JLabel dayLabel = new JLabel(String.valueOf(i), SwingConstants.CENTER); dayLabel.setFont(PRIMARY_FONT); dayLabel.setForeground(TEXT_COLOR);
                if(i == today.get(Calendar.DAY_OF_MONTH) && calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) && calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
                    dayLabel.setOpaque(true); dayLabel.setBackground(Color.decode("#F28B82")); dayLabel.setForeground(BACKGROUND_COLOR);
                }
                calendarGridPanel.add(dayLabel);
            }
            
            int totalCells = offset + daysInMonth;
            int remainingCells = (42 - totalCells);
             for(int i=0; i < remainingCells; i++) {
                calendarGridPanel.add(new JLabel(""));
            }

            calendarGridPanel.revalidate();
            calendarGridPanel.repaint();
        }
    }

    static class DailyRoutinesPage extends JPanel {
        private static final Color INFO_BOX_COLOR = new Color(0x3A, 0x4B, 0x6D);
        private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 32);
        private static final Font PRIMARY_FONT = new Font("Segoe UI", Font.PLAIN, 15);
        private static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 15);
        private final JPanel routineListPanel;

        public DailyRoutinesPage(Runnable backAction) {
             setLayout(new BorderLayout(0, 20)); setBackground(BACKGROUND_COLOR); setBorder(new EmptyBorder(20, 30, 20, 30));
             add(createHeaderPanel(backAction), BorderLayout.NORTH);
             JPanel contentPanel = new JPanel(new BorderLayout(0, 20)); contentPanel.setOpaque(false);
             contentPanel.add(createInfoBox(), BorderLayout.NORTH);
             
             routineListPanel = new JPanel();
             routineListPanel.setLayout(new BoxLayout(routineListPanel, BoxLayout.Y_AXIS));
             routineListPanel.setBackground(BACKGROUND_COLOR);
             createRoutineList();

             JScrollPane scrollPane = new JScrollPane(routineListPanel); 
             scrollPane.setOpaque(false);
             scrollPane.getViewport().setOpaque(false);
             scrollPane.setBorder(BorderFactory.createEmptyBorder()); 
             scrollPane.getVerticalScrollBar().setUnitIncrement(16);
             contentPanel.add(scrollPane, BorderLayout.CENTER);
             add(contentPanel, BorderLayout.CENTER);
        }

        private JPanel createHeaderPanel(Runnable backAction) {
            JPanel headerPanel = new JPanel(new BorderLayout()); headerPanel.setBackground(BACKGROUND_COLOR);
            JButton backButton = new JButton("⬅️ Back"); backButton.setFont(BOLD_FONT); backButton.setForeground(ACCENT_COLOR_BLUE); backButton.setContentAreaFilled(false); backButton.setBorder(new EmptyBorder(5, 0, 5, 15)); backButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); backButton.addActionListener(e -> backAction.run()); headerPanel.add(backButton, BorderLayout.WEST);
            JLabel titleLabel = new JLabel("🔄 Daily Routines & Habits"); titleLabel.setFont(HEADER_FONT); titleLabel.setForeground(TEXT_COLOR); headerPanel.add(titleLabel, BorderLayout.CENTER);
            return headerPanel;
        }

        private JPanel createInfoBox() {
            RoundedPanel infoPanel = new RoundedPanel(10, INFO_BOX_COLOR); infoPanel.setLayout(new BorderLayout(10, 0)); infoPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
            JLabel iconLabel = new JLabel("💡"); iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20)); infoPanel.add(iconLabel, BorderLayout.WEST);
            JTextPane infoText = new JTextPane(); infoText.setOpaque(false); infoText.setEditable(false); infoText.setContentType("text/html"); infoText.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE); infoText.setFont(PRIMARY_FONT);
            infoText.setText("<html><body style='color: #E8EAED;'><b>Welcome to Routines & Habits!</b><br>Routines below are automatically created daily. To create a new routine that recurs every day, or edit what's already inputted, click the arrow to the right of the New button.</body></html>");
            infoPanel.add(infoText, BorderLayout.CENTER);
            return infoPanel;
        }

        private void createRoutineList() {
            addRoutine(new RoutineEntry("🧘 Meditate", "AM"));
            addRoutine(new RoutineEntry("💪 Exercise", "AM"));
            addRoutine(new RoutineEntry("🧴 Morning Skincare", "AM"));
            addRoutine(new RoutineEntry("📖 Morning Journal", "AM"));
            addRoutine(new RoutineEntry("💤 Got 8h of Sleep Last Night", "AM"));
            addRoutine(new RoutineEntry("💊 Vitamins", "AM"));
            addRoutine(new RoutineEntry("📖 Evening Journal", "PM"));
            addRoutine(new RoutineEntry("🧴 Evening Skincare", "PM"));
            addRoutine(new RoutineEntry("📱 Screens off Before Bed", "PM"));
            addRoutine(new RoutineEntry("💧 8 Glasses of Water", "All Day"));
            addRoutine(new RoutineEntry("🧹 Keep Home Tidy", "All Day"));
        }

        private void addRoutine(RoutineEntry entry) {
            routineListPanel.add(new RoutinePanel(entry));
            routineListPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        private static class RoutineEntry {
            String task; String time;
            RoutineEntry(String task, String time) { this.task = task; this.time = time; }
        }

        private class RoutinePanel extends JPanel {
            public RoutinePanel(RoutineEntry entry) {
                setLayout(new BorderLayout(10, 0)); setOpaque(false);
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, PANEL_COLOR));
                
                JCheckBox checkBox = new JCheckBox(); checkBox.setOpaque(false);
                add(checkBox, BorderLayout.WEST);

                JLabel taskLabel = new JLabel(entry.task); taskLabel.setFont(PRIMARY_FONT); taskLabel.setForeground(TEXT_COLOR);
                add(taskLabel, BorderLayout.CENTER);
                
                checkBox.addActionListener(e -> {
                    if (checkBox.isSelected()) {
                        taskLabel.setText("<html><strike>" + entry.task + "</strike></html>");
                        taskLabel.setForeground(Color.GRAY);
                    } else {
                        taskLabel.setText(entry.task);
                        taskLabel.setForeground(TEXT_COLOR);
                    }
                });
                
                JLabel timeTag = new JLabel(entry.time); timeTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
                timeTag.setBorder(new EmptyBorder(4, 8, 4, 8)); timeTag.setOpaque(true);
                
                switch (entry.time) {
                    case "AM" -> {
                        timeTag.setBackground(new Color(0xE9, 0xC4, 0x6A));
                        timeTag.setForeground(Color.BLACK);
                    }
                    case "PM" -> {
                        timeTag.setBackground(new Color(0xBE, 0x95, 0xC4));
                        timeTag.setForeground(Color.BLACK);
                    }
                    default -> {
                        timeTag.setBackground(new Color(0xA7, 0xC9, 0xA7));
                        timeTag.setForeground(Color.BLACK);
                    }
                }
                
                JPanel tagWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT)); tagWrapper.setOpaque(false);
                tagWrapper.add(timeTag);
                add(tagWrapper, BorderLayout.EAST);
            }
        }
    }

    static class JournalMoodsPage extends JPanel {
        private static final Color INFO_BOX_COLOR = new Color(0x3A, 0x4B, 0x6D);
        private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 32);
        private static final Font PRIMARY_FONT = new Font("Segoe UI", Font.PLAIN, 15);
        private static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 15);
        private final JPanel entryListPanel = new JPanel();
        
        public JournalMoodsPage(Runnable backAction) {
             setLayout(new BorderLayout(0, 20)); setBackground(BACKGROUND_COLOR); setBorder(new EmptyBorder(20, 30, 20, 30));
             add(createHeaderPanel(backAction), BorderLayout.NORTH);
             

             JPanel contentWrapper = new JPanel(new BorderLayout()); contentWrapper.setOpaque(false);
             JPanel contentPanel = new JPanel(); contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS)); contentPanel.setOpaque(false);
             contentPanel.add(createInfoBox()); contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
             contentPanel.add(createJournalPanel());
             contentWrapper.add(contentPanel, BorderLayout.NORTH);
             

             JScrollPane scrollPane = new JScrollPane(contentWrapper); 
             scrollPane.setOpaque(false);
             scrollPane.getViewport().setOpaque(false);
             scrollPane.setBorder(BorderFactory.createEmptyBorder()); 
             scrollPane.getVerticalScrollBar().setUnitIncrement(16);
             add(scrollPane, BorderLayout.CENTER);
        }

        private JPanel createHeaderPanel(Runnable backAction) {
            JPanel headerPanel = new JPanel(new BorderLayout()); headerPanel.setBackground(BACKGROUND_COLOR);
            JButton backButton = new JButton("⬅️ Back"); backButton.setFont(BOLD_FONT); backButton.setForeground(ACCENT_COLOR_BLUE); backButton.setContentAreaFilled(false); backButton.setBorder(new EmptyBorder(5, 0, 5, 15)); backButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); backButton.addActionListener(e -> backAction.run()); headerPanel.add(backButton, BorderLayout.WEST);
            JLabel titleLabel = new JLabel("📖 Journal & Moods"); titleLabel.setFont(HEADER_FONT); titleLabel.setForeground(TEXT_COLOR); headerPanel.add(titleLabel, BorderLayout.CENTER);
            return headerPanel;
        }

        private JPanel createInfoBox() {
            RoundedPanel infoPanel = new RoundedPanel(10, INFO_BOX_COLOR); infoPanel.setLayout(new BorderLayout(10, 0)); infoPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
            JLabel iconLabel = new JLabel("💡"); iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20)); infoPanel.add(iconLabel, BorderLayout.WEST);
            JTextPane infoText = new JTextPane(); infoText.setOpaque(false); infoText.setEditable(false); infoText.setContentType("text/html"); infoText.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE); infoText.setFont(PRIMARY_FONT);
            infoText.setText("<html><body style='color: #E8EAED;'><b>Welcome to Journal & Mood Tracking!</b><br>Track all of your daily happenings, thoughts, and feelings in daily entries here!</body></html>");
            infoPanel.add(infoText, BorderLayout.CENTER);
            return infoPanel;
        }
        
        private JPanel createJournalPanel() {
            JPanel mainPanel = new JPanel(new BorderLayout(0, 15)); mainPanel.setOpaque(false);
            
            JButton newButton = new JButton("✨ New"); newButton.setFont(BOLD_FONT); UITheme.stylePrimaryButton(newButton);
            newButton.addActionListener(e -> openNewEntryDialog());
            JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT)); toolbar.setOpaque(false); toolbar.add(newButton);
            mainPanel.add(toolbar, BorderLayout.NORTH);

            entryListPanel.setLayout(new BoxLayout(entryListPanel, BoxLayout.Y_AXIS)); entryListPanel.setOpaque(false);
            addJournalEntry(new JournalEntry("🎉 A New Chapter", List.of("Focused", "Excited"), "December 22, 2022"));
            addJournalEntry(new JournalEntry("💻 New Computer", List.of("Excited"), "December 21, 2022"));
            
            mainPanel.add(entryListPanel, BorderLayout.CENTER);
            return mainPanel;
        }
        
        private void openNewEntryDialog() {
            JTextField titleField = new JTextField(); JTextField moodsField = new JTextField(); JTextField dateField = new JTextField(new SimpleDateFormat("MMMM dd, yyyy").format(new Date()));
            for(Component c : new Component[]{titleField, moodsField, dateField}) c.setFont(PRIMARY_FONT);
            JPanel dialogPanel = new JPanel(new GridLayout(0, 1, 0, 5));
            dialogPanel.add(new JLabel("Entry Title:")); dialogPanel.add(titleField); dialogPanel.add(new JLabel("Moods (comma separated):")); dialogPanel.add(moodsField); dialogPanel.add(new JLabel("Date:")); dialogPanel.add(dateField);
            int result = JOptionPane.showConfirmDialog(this, dialogPanel, "Create New Journal Entry", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION && !titleField.getText().trim().isEmpty()) {
                List<String> moodList = List.of(moodsField.getText().split("\\s*,\\s*"));
                addJournalEntry(new JournalEntry(titleField.getText(), moodList, dateField.getText()));
                revalidate(); repaint();
            }
        }

        private void addJournalEntry(JournalEntry entry) {
            entryListPanel.add(new JournalEntryPanel(entry));
        }

        private static class JournalEntry {
            String title; List<String> moods; String date;
            JournalEntry(String t, List<String> m, String d) { title=t; moods=m; date=d; }
        }

        private static class JournalEntryPanel extends JPanel {
            public JournalEntryPanel(JournalEntry entry) {
                setLayout(new BorderLayout(10, 0)); setOpaque(false); setBorder(new MatteBorder(0, 0, 1, 0, PANEL_COLOR)); setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
                JLabel titleLabel = new JLabel(entry.title); titleLabel.setFont(PRIMARY_FONT); titleLabel.setForeground(TEXT_COLOR);
                add(titleLabel, BorderLayout.WEST);

                JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)); centerPanel.setOpaque(false);
                for (String mood : entry.moods) { if(!mood.isBlank()) centerPanel.add(new MoodTag(mood)); }
                add(centerPanel, BorderLayout.CENTER);
                
                JLabel dateLabel = new JLabel(entry.date); dateLabel.setFont(PRIMARY_FONT); dateLabel.setForeground(Color.GRAY);
                add(dateLabel, BorderLayout.EAST);
            }
        }
        
        private static class MoodTag extends JLabel {
            public MoodTag(String mood) {
                super("● " + mood); setFont(new Font("Segoe UI", Font.BOLD, 12)); setBorder(new EmptyBorder(5, 10, 5, 10)); setOpaque(true);
                setBackground(PANEL_COLOR); setForeground(TEXT_COLOR);
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create(); g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground()); g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15); g2d.dispose();
                super.paintComponent(g);
            }
        }
    }

    static class GoalsMilestonesPage extends JPanel {
        private static final Color INFO_BOX_COLOR = new Color(0x3A, 0x4B, 0x6D);
        private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 32);
        private static final Font PRIMARY_FONT = new Font("Segoe UI", Font.PLAIN, 15);
        private static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 15);
        private final DefaultTableModel areasOfLifeTableModel;

        public GoalsMilestonesPage(Runnable backAction) {
            setLayout(new BorderLayout(0, 20)); setBackground(BACKGROUND_COLOR); setBorder(new EmptyBorder(20, 30, 20, 30));
            
            areasOfLifeTableModel = new DefaultTableModel(new Object[][]{
                {"💛 Relationships", "Get Promotion", 0}, {"💼 Career", "Get Promotion\nGrow Personal Brand", 45},{"💰 Finances", "", 0}, {"❤️ Physical Health", "Run a Marathon\nCycle Yosemite", 40},
                {"💖 Mental Health", "Limit Pre-Bed Screen Time", 86}, {"🧠 Intellect", "", 0}, {"🙌 Community", "Donate $1,000 to Charity", 100}, {"🛠️ Skills", "Learn Javascript\nLearn Basic SEO", 48}, {"🎨 Creativity", "Learn Guitar", 50}
            }, new String[]{"Aa Area", "▲ Goals", "🏆 Avg Goals Complete"}) { @Override public boolean isCellEditable(int r, int c){ return false; } };

            add(createHeaderPanel(backAction), BorderLayout.NORTH);
            
            JScrollPane scrollPane = new JScrollPane(createMainContent());
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);

            add(scrollPane, BorderLayout.CENTER);
        }

        private JPanel createMainContent() {
            JPanel mainContentContainer = new JPanel(new BorderLayout(20, 0)); 
            mainContentContainer.setOpaque(false);
            
            JPanel centerPanel = new JPanel();
            centerPanel.setOpaque(false);
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            centerPanel.add(createInfoBox()); 
            centerPanel.add(Box.createRigidArea(new Dimension(0, 25)));
            centerPanel.add(createMyGoalsPanel());
            centerPanel.add(Box.createRigidArea(new Dimension(0, 25)));
            centerPanel.add(createActionHubPanel()); 
            centerPanel.add(Box.createRigidArea(new Dimension(0, 25)));
            centerPanel.add(createAreasOfLifeTablePanel());
            
            mainContentContainer.add(centerPanel, BorderLayout.CENTER);
            mainContentContainer.add(createRightSidebar(), BorderLayout.EAST);
            return mainContentContainer;
        }

        private JPanel createHeaderPanel(Runnable backAction) {
             JPanel headerPanel = new JPanel(new BorderLayout()); headerPanel.setBackground(BACKGROUND_COLOR);
            JButton backButton = new JButton("⬅️ Back"); backButton.setFont(BOLD_FONT); backButton.setForeground(ACCENT_COLOR_BLUE); backButton.setContentAreaFilled(false); backButton.setBorder(new EmptyBorder(5, 0, 5, 15)); backButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); backButton.addActionListener(e -> backAction.run()); headerPanel.add(backButton, BorderLayout.WEST);
            JLabel titleLabel = new JLabel("🎯 Goals & Milestones"); titleLabel.setFont(HEADER_FONT); titleLabel.setForeground(TEXT_COLOR); headerPanel.add(titleLabel, BorderLayout.CENTER);
            return headerPanel;
        }
        
        private JPanel createInfoBox() {
            RoundedPanel infoPanel = new RoundedPanel(10, INFO_BOX_COLOR); infoPanel.setLayout(new BorderLayout(10, 0)); infoPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
            JLabel iconLabel = new JLabel("💡"); iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20)); infoPanel.add(iconLabel, BorderLayout.WEST);
            JTextPane infoText = new JTextPane(); infoText.setOpaque(false); infoText.setEditable(false); infoText.setContentType("text/html"); infoText.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE); infoText.setFont(PRIMARY_FONT);
            infoText.setText("<html><body style='color: #E8EAED;'><b>Welcome to Goals & Milestones!</b><br>List and follow through on all your goals, both big and small, and celebrate recently achieved milestones.</body></html>");
            infoPanel.add(infoText, BorderLayout.CENTER);
            return infoPanel;
        }
        
        private JPanel createMyGoalsPanel() {
            JPanel myGoalsPanel = new JPanel(new BorderLayout(0, 10));
            myGoalsPanel.setOpaque(false);

            JLabel title = new JLabel("My Goals");
            title.setFont(new Font("Segoe UI", Font.BOLD, 22));
            title.setForeground(TEXT_COLOR);
            myGoalsPanel.add(title, BorderLayout.NORTH);

            DefaultTableModel myGoalsModel = new DefaultTableModel(new Object[][]{
                {true, "Run a Marathon", "❤️ Physical Health"},
                {true, "Learn Guitar", "🎨 Creativity"},
                {false, "Learn Javascript", "🛠️ Skills"},
            }, new String[]{"", "Goal", "Area of Life"}) {
                @Override public Class<?> getColumnClass(int c) { return c == 0 ? Boolean.class : String.class; }
            };
            JTable myGoalsTable = new JTable(myGoalsModel);
            myGoalsTable.setOpaque(false);
            ((JComponent)myGoalsTable.getDefaultRenderer(Boolean.class)).setOpaque(false);
            myGoalsTable.setBackground(BACKGROUND_COLOR);
            myGoalsTable.setForeground(TEXT_COLOR);
            myGoalsTable.setGridColor(PANEL_COLOR);
            myGoalsTable.setFont(PRIMARY_FONT);
            myGoalsTable.setRowHeight(30);
            myGoalsTable.setShowGrid(false);
            myGoalsTable.getTableHeader().setUI(null); // Hide header
            
            myGoalsPanel.add(new JScrollPane(myGoalsTable), BorderLayout.CENTER);
            return myGoalsPanel;
        }

        private JPanel createActionHubPanel() {
            JPanel hubPanel = new JPanel(); hubPanel.setOpaque(false); hubPanel.setLayout(new GridLayout(0, 1, 15, 15));
            hubPanel.add(createGoalCard("Attend 10 Networking Events", "● Grow Personal Brand", 8, 10));
            hubPanel.add(createGoalCard("Meditate Every Day", "🧘 Become More Mindful", 5, 7));
            hubPanel.add(createGoalCard("Read 52 Books", "📚 Read Weekly", 34, 52));
            return hubPanel;
        }

        private JPanel createGoalCard(String title, String subtitle, int current, int objective) {
            RoundedPanel card = new RoundedPanel(15, PANEL_COLOR); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); card.setBorder(new EmptyBorder(15, 15, 15, 15));
            JLabel titleLabel = new JLabel(title); titleLabel.setFont(BOLD_FONT); titleLabel.setForeground(TEXT_COLOR); card.add(titleLabel);
            card.add(Box.createRigidArea(new Dimension(0, 5)));
            JLabel subtitleLabel = new JLabel(subtitle); subtitleLabel.setFont(PRIMARY_FONT); subtitleLabel.setForeground(Color.GRAY); card.add(subtitleLabel);
            card.add(Box.createVerticalStrut(20));
            card.add(new JLabel("Current: " + current)); card.add(new JLabel("Objective: " + objective)); card.add(Box.createVerticalStrut(10));
            JProgressBar progressBar = new JProgressBar(0, objective); progressBar.setValue(current); progressBar.setStringPainted(true); progressBar.setString(((current*100)/objective) + "%"); progressBar.setUI(new StyledProgressBarUI()); progressBar.setFont(new Font("Segoe UI", Font.BOLD, 12)); progressBar.setForeground(ACCENT_COLOR_BLUE); progressBar.setBackground(BACKGROUND_COLOR); progressBar.setBorder(BorderFactory.createEmptyBorder());
            card.add(progressBar);
            for(Component c : card.getComponents()){ if(c instanceof JLabel label){ label.setForeground(TEXT_COLOR); if(label.getFont().isPlain()) label.setFont(PRIMARY_FONT); } }
            titleLabel.setFont(BOLD_FONT); subtitleLabel.setForeground(Color.GRAY);
            return card;
        }

        private JPanel createRightSidebar() {
            JPanel sidebar = new JPanel(); sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS)); sidebar.setOpaque(false); sidebar.setPreferredSize(new Dimension(280, 0));
            sidebar.add(createRecentlyCompletedPanel());
            sidebar.add(Box.createRigidArea(new Dimension(0, 25)));
            sidebar.add(createAreasOfLifeSidebar());
            return sidebar;
        }

        private JPanel createRecentlyCompletedPanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setOpaque(false);

            JLabel title = new JLabel("Recently Completed");
            title.setFont(new Font("Segoe UI", Font.BOLD, 22));
            title.setForeground(TEXT_COLOR);
            panel.add(title);
            panel.add(Box.createRigidArea(new Dimension(0, 15)));

            panel.add(createCompletedCard("💰 Donate $1,000 to Charity", "🙌 Community", "January 13, 2023"));
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
            panel.add(createCompletedCard("📵 Limit Pre-Bed Screen Time", "💖 Mental Health", "January 11, 2023"));
            
            return panel;
        }
        
        private JPanel createCompletedCard(String title, String category, String date) {
            RoundedPanel card = new RoundedPanel(15, PANEL_COLOR);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(new EmptyBorder(15, 15, 15, 15));
            
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(BOLD_FONT);
            titleLabel.setForeground(TEXT_COLOR);
            card.add(titleLabel);

            JLabel categoryLabel = new JLabel(category);
            categoryLabel.setFont(PRIMARY_FONT);
            categoryLabel.setForeground(Color.GRAY);
            card.add(categoryLabel);

            JLabel dateLabel = new JLabel("Completed: " + date);
            dateLabel.setFont(PRIMARY_FONT);
            dateLabel.setForeground(Color.GRAY);
            card.add(dateLabel);

            return card;
        }
        
        private JPanel createAreasOfLifeSidebar() {
            JPanel sidebar = new JPanel(); sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS)); sidebar.setOpaque(false);
            sidebar.add(createAreaCard("💖 Mental Health", 86)); sidebar.add(Box.createRigidArea(new Dimension(0, 15)));
            sidebar.add(createAreaCard("🎨 Creativity", 50)); sidebar.add(Box.createRigidArea(new Dimension(0, 15)));
            sidebar.add(createAreaCard("🛠️ Skills", 48));
            return sidebar;
        }
        
        private JPanel createAreaCard(String title, int progress) {
            RoundedPanel card = new RoundedPanel(15, PANEL_COLOR); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); card.setBorder(new EmptyBorder(15, 15, 15, 15));
            JLabel titleLabel = new JLabel(title); titleLabel.setFont(BOLD_FONT); titleLabel.setForeground(TEXT_COLOR); card.add(titleLabel);
            card.add(Box.createRigidArea(new Dimension(0, 10)));
            JProgressBar progressBar = new JProgressBar(0, 100); progressBar.setValue(progress); progressBar.setStringPainted(true); progressBar.setString(progress + "%"); progressBar.setUI(new StyledProgressBarUI()); progressBar.setFont(new Font("Segoe UI", Font.BOLD, 12)); progressBar.setForeground(ACCENT_COLOR_BLUE); progressBar.setBackground(BACKGROUND_COLOR); progressBar.setBorder(BorderFactory.createEmptyBorder());
            card.add(progressBar);
            return card;
        }
        
        private JPanel createAreasOfLifeTablePanel() {
            JPanel tableContainer = new JPanel(new BorderLayout(0,10)); tableContainer.setOpaque(false);
            JPanel toolbar = new JPanel(new BorderLayout()); toolbar.setOpaque(false);
            JLabel title = new JLabel("⚙️ Areas of Life"); title.setFont(new Font("Segoe UI", Font.BOLD, 22)); title.setForeground(TEXT_COLOR); toolbar.add(title, BorderLayout.WEST);
            JButton newButton = new JButton("✨ New"); newButton.setFont(BOLD_FONT); UITheme.stylePrimaryButton(newButton); newButton.addActionListener(e -> openNewAreaDialog()); toolbar.add(newButton, BorderLayout.EAST);
            tableContainer.add(toolbar, BorderLayout.NORTH);
            
            JTable table = new JTable(areasOfLifeTableModel);
            table.setBackground(BACKGROUND_COLOR); table.setForeground(TEXT_COLOR); table.setGridColor(PANEL_COLOR); table.setFont(PRIMARY_FONT); table.setRowHeight(60); table.setFillsViewportHeight(true);
            JTableHeader header = table.getTableHeader(); header.setBackground(BACKGROUND_COLOR); header.setForeground(TEXT_COLOR); header.setFont(BOLD_FONT); header.setPreferredSize(new Dimension(0, 40));
            ((DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
            table.getColumnModel().getColumn(2).setCellRenderer(new ProgressBarRenderer());
            table.getColumnModel().getColumn(1).setCellRenderer(new MultiLineCellRenderer());
            
            JScrollPane scrollPane = new JScrollPane(table); 
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setBorder(BorderFactory.createLineBorder(PANEL_COLOR));
            tableContainer.add(scrollPane, BorderLayout.CENTER);
            return tableContainer;
        }
        
        private void openNewAreaDialog() {
            JTextField areaField = new JTextField(); areaField.setFont(PRIMARY_FONT);
            JPanel dialogPanel = new JPanel(new GridLayout(0, 1, 0, 5));
            dialogPanel.add(new JLabel("Area Name:")); dialogPanel.add(areaField);
            int result = JOptionPane.showConfirmDialog(this, dialogPanel, "Create New Area of Life", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION && !areaField.getText().trim().isEmpty()) {
                areasOfLifeTableModel.addRow(new Object[]{areaField.getText().trim(), "", 0});
            }
        }
    }

    static class RoundedPanel extends JPanel {
        private Color backgroundColor; private final int cornerRadius;
        public RoundedPanel(int r, Color c) { super(); cornerRadius=r; backgroundColor=c; setOpaque(false); }
        public void setBackgroundColor(Color c) { this.backgroundColor = c; }
        @Override protected void paintComponent(Graphics g) { super.paintComponent(g); Graphics2D g2d = (Graphics2D) g.create(); g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2d.setColor(backgroundColor); g2d.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius); g2d.dispose(); }
    }

    static class StyledProgressBarUI extends BasicProgressBarUI {
        @Override protected void paintDeterminate(Graphics g, JComponent c) {
            Graphics2D g2d = (Graphics2D) g.create(); g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = progressBar.getWidth(); int height = progressBar.getHeight(); int arc = height;
            g2d.setColor(progressBar.getBackground().brighter()); g2d.fillRoundRect(0, 0, width, height, arc, arc);
            int amountFull = getAmountFull(progressBar.getInsets(), width, height); g2d.setColor(progressBar.getForeground()); g2d.fillRoundRect(0, 0, amountFull, height, arc, arc);
            if (progressBar.isStringPainted()) { g2d.setColor(BACKGROUND_COLOR); paintString(g2d, 0, 0, width, height, amountFull, progressBar.getInsets()); }
            g2d.dispose();
        }
    }

    static class ProgressBarRenderer extends JProgressBar implements TableCellRenderer {
        public ProgressBarRenderer() { super(0, 100); setStringPainted(true); setFont(new Font("Segoe UI", Font.BOLD, 12)); setForeground(ACCENT_COLOR_BLUE); setUI(new StyledProgressBarUI()); setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5)); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean is, boolean hf, int r, int c) {
            if (v instanceof Integer progress) {
                setValue(progress);
            } else {
                setValue(0);
            }
            setString(getValue()+"%"); 
            return this; 
        }
    }

    static class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {
        public MultiLineCellRenderer() { setLineWrap(true); setWrapStyleWord(true); setOpaque(true); setFont(new Font("Segoe UI", Font.PLAIN, 15)); setBorder(new EmptyBorder(5,5,5,5)); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean is, boolean hf, int r, int c) { setText(v==null?"":v.toString()); setBackground(is?t.getSelectionBackground():t.getBackground()); setForeground(is?t.getSelectionForeground():t.getForeground()); return this; }
    }
}
