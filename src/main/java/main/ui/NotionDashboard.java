/*
 * =============================================================================
 * COMPILE: javac NotionDashboard.java
 * RUN:     java NotionDashboard
 * =============================================================================
 *
 * This is a single-file Java Swing application that recreates a Notion-style
 * "Tasks Dashboard" based on the provided screenshots. It uses only the
 * standard JDK (Java 11+) and no external libraries.
 *
 * CHANGELOG (v15):
 * - Moved Delete Functionality: Removed the "Actions" column from Dashboard
 * tables (and associated Renderer/Editor).
 * - Added a "Delete" button to the HEADER of each DashboardColumnPanel,
 * next to the "New" button.
 * - This header "Delete" button now handles deleting selected rows from
 * its specific table view.
 */

// Core Swing and AWT imports
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicSeparatorUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D; // For drawing shapes
import java.awt.geom.RoundRectangle2D;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.EventObject; // For CellEditor
import java.util.ArrayList;
import java.util.Arrays; // Added for sorting delete indices
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional; // Added for new assignee logic
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Main application class for the Notion-style Tasks Dashboard.
 * This single file contains all necessary components, models, renderers,
 * and the main application entry point.
 *
 * @author Gemini, 100x Java Engineer
 */
public class NotionDashboard {

    // Enum for different views managed by CardLayout
    enum View { DASHBOARD, TASKS, AREAS }

    /**
     * Main entry point for the application.
     * Sets up the dark theme and creates the main frame.
     */
    public static void main(String[] args) {
        // Set up the global exception handler for Swing errors
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());

        // Run the GUI setup on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Apply the custom dark theme
                DarkTheme.apply();

                // Create and configure the main application frame
                JFrame frame = new JFrame("Tasks Dashboard");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setMinimumSize(new Dimension(1400, 900));
                frame.setSize(1600, 1000); // Reset default size
                frame.setLocationRelativeTo(null); // Center the window

                // Set the main content pane
                frame.setContentPane(new MainAppPanel());

                // Make the frame visible
                frame.setVisible(true);

            } catch (Exception e) {
                // Show a friendly dialog for any startup errors
                GlobalExceptionHandler.handle(e);
            }
        });
    }

    // =============================================================================
    // == DATA MODELS (Internal Static Classes) ==
    // =============================================================================

    /**
     * Represents a Person (Assignee, Owner, CreatedBy).
     */
    static class Person {
        String name;
        Icon avatar;
        Color color; // Used for the avatar background

        public Person(String name, Color color) {
            this.name = name;
            this.color = color;
            // Create a dynamic avatar
            this.avatar = IconFactory.createAvatarIcon(name.substring(0, 1), color);
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person) o;
            return name.equals(person.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    /**
     * Represents a high-level Area (e.g., HR, Sales).
     */
    static class Area {
        String name;
        Person owner;
        Icon icon;
        List<Task> tasks = new ArrayList<>();

        public Area(String name, Person owner, Icon icon) {
            this.name = name;
            this.owner = owner;
            this.icon = icon;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Area area = (Area) o;
            return name.equals(area.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    /**
     * Represents the Priority of a task.
     */
    static enum Priority {
        High(DarkTheme.COLOR_TAG_RED, DarkTheme.COLOR_TAG_RED_FG),
        Medium(DarkTheme.COLOR_TAG_ORANGE, DarkTheme.COLOR_TAG_ORANGE_FG),
        Low(DarkTheme.COLOR_TAG_BLUE, DarkTheme.COLOR_TAG_BLUE_FG),
        None(DarkTheme.COLOR_TAG_GRAY, DarkTheme.COLOR_TAG_GRAY_FG);

        final Color bg;
        final Color fg;
        Priority(Color bg, Color fg) { this.bg = bg; this.fg = fg; }
    }

    /**
     * Represents the Status of a task.
     */
    static enum Status {
        Completed(DarkTheme.COLOR_TAG_GREEN, DarkTheme.COLOR_TAG_GREEN_FG),
        InProgress(DarkTheme.COLOR_TAG_BLUE, DarkTheme.COLOR_TAG_BLUE_FG),
        Todo(DarkTheme.COLOR_TAG_GRAY, DarkTheme.COLOR_TAG_GRAY_FG);

        final Color bg;
        final Color fg;
        Status(Color bg, Color fg) { this.bg = bg; this.fg = fg; }
    }

    /**
     * Represents a single Task.
     */
    static class Task {
        String name;
        Area area;
        Person assignee;
        Priority priority;
        Status status;
        Date dueDate;
        Date deadline;
        Date createdTime;
        Person createdBy;
        boolean done; // Field remains for internal logic (filtering)
        String addToAction; // e.g., "Today", "This Week"

        public Task(String name, Area area, Person assignee, Priority priority,
                    Status status, Date deadline, Date createdTime, Person createdBy,
                    boolean done, String addToAction) {
            this.name = name;
            this.area = area;
            this.assignee = assignee;
            this.priority = priority;
            this.status = status;
            this.deadline = deadline;
            this.createdTime = createdTime;
            this.createdBy = createdBy;
            this.done = done; // Still used internally
            this.addToAction = addToAction;

            // Logic from screenshot for "Due Today"
            if (deadline != null && "Due Today".equals(getDaysLeft())) {
                this.dueDate = new Date(); // Set due date to today
            } else if (deadline != null) {
                this.dueDate = deadline;
            }
        }

        // Default constructor for "New" button
        public Task() {
            this.name = "New Task";
            this.area = null;
            this.assignee = null;
            this.priority = Priority.None;
            this.status = Status.Todo;
            this.deadline = null;
            this.createdTime = new Date();
            this.createdBy = MockData.getPeople().get(0); // Default to first person
            this.done = false; // Still used internally
            this.addToAction = "";
        }


        /**
         * Calculates the "Days Left" string as seen in the screenshot.
         */
        public String getDaysLeft() {
            if (deadline == null) {
                return "";
            }
            LocalDate today = LocalDate.now();
            LocalDate deadDate = deadline.toInstant()
                                     .atZone(ZoneId.systemDefault())
                                     .toLocalDate();
            long days = ChronoUnit.DAYS.between(today, deadDate);

            if (days == 0) {
                return "Due Today 🔥";
            } else if (days > 0) {
                return days + (days == 1 ? " Day Left" : " Days Left");
            } else {
                return Math.abs(days) + (days == -1 ? " Day Past Due ❗" : " Days Past Due ❗");
            }
        }
    }


    // =============================================================================
    // == MOCK DATA FACTORY ==
    // =============================================================================

    /**
     * Generates in-memory mock data to populate the UI.
     */
    static class MockData {
        private static List<Person> people;
        private static List<Area> areas;
        private static List<Task> tasks;

        // Helper to create dates
        private static Date date(int year, int month, int day) {
            return Date.from(LocalDate.of(year, month, day)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        public static List<Person> getPeople() {
            if (people == null) {
                people = new ArrayList<>();
                people.add(new Person("Abel Sunil", new Color(220, 80, 80)));
                people.add(new Person("Ali Rashidy", new Color(80, 150, 220)));
                people.add(new Person("Sarah Chen", new Color(80, 220, 150)));
                people.add(new Person("Mike Lee", new Color(200, 100, 200)));
            }
            return people;
        }

        public static List<Area> getAreas() {
            if (areas == null) {
                List<Person> p = getPeople();
                areas = new ArrayList<>();
                areas.add(new Area("HR", p.get(2), IconFactory.get("person")));
                areas.add(new Area("Growth", p.get(3), IconFactory.get("growth")));
                areas.add(new Area("Marketing", p.get(1), IconFactory.get("marketing")));
                areas.add(new Area("Finance", p.get(0), IconFactory.get("finance")));
                areas.add(new Area("Sales", p.get(3), IconFactory.get("sales")));
                areas.add(new Area("PR", p.get(2), IconFactory.get("pr")));
                areas.add(new Area("Development", p.get(0), IconFactory.get("dev")));
                areas.add(new Area("R&D", p.get(1), IconFactory.get("rnd")));
                areas.add(new Area("Organizing", p.get(1), IconFactory.get("organizing")));
                areas.add(new Area("Support", p.get(0), IconFactory.get("support")));
            }
            return areas;
        }

        public static List<Task> getTasks() {
            if (tasks == null) {
                tasks = new ArrayList<>();
                List<Person> p = getPeople();
                List<Area> a = getAreas();

                tasks.add(new Task(
                    "Integrate our Notion Pages with Notion Startup OS - A very long task name designed to test text wrapping capabilities within the table cell.",
                    a.get(8), // Organizing
                    p.get(1), // Ali Rashidy
                    Priority.High,
                    Status.Completed,
                    date(2022, 5, 26),
                    date(2022, 5, 15),
                    p.get(1),
                    true,
                    "Today"
                ));
                tasks.add(new Task(
                    "Test Task",
                    a.get(4), // Sales
                    p.get(1), // Ali Rashidy
                    Priority.High,
                    Status.InProgress,
                    date(2022, 4, 30),
                    date(2022, 4, 27),
                    p.get(1),
                    false,
                    "This Week"
                ));
                tasks.add(new Task(
                    "Write Q4 Marketing Report - This also needs to be longer to see how the wrapping behaves.",
                    a.get(2), // Marketing
                    p.get(2), // Sarah Chen
                    Priority.Medium,
                    Status.InProgress,
                    date(2025, 10, 26), // Tomorrow
                    date(2025, 10, 20),
                    p.get(0),
                    false,
                    "Tomorrow"
                ));
                tasks.add(new Task(
                    "Finalize budget for 2026",
                    a.get(3), // Finance
                    p.get(0), // Abel Sunil
                    Priority.High,
                    Status.Todo,
                    date(2025, 10, 31),
                    date(2025, 10, 22),
                    p.get(0),
                    false,
                    "This Month"
                ));
                tasks.add(new Task(
                    "Review new support tickets",
                    a.get(9), // Support
                    p.get(0), // Abel Sunil
                    Priority.Low,
                    Status.Todo,
                    date(2025, 10, 24), // Today
                    date(2025, 10, 24),
                    p.get(0),
                    false,
                    "Today"
                ));
                 tasks.add(new Task(
                    "Onboard new developer",
                    a.get(6), // Development
                    p.get(0), // Abel Sunil
                    Priority.Medium,
                    Status.InProgress,
                    date(2025, 10, 28),
                    date(2025, 10, 21),
                    p.get(1),
                    false,
                    "This Week"
                ));
                // Past due tasks from screenshot
                tasks.add(new Task(
                    "Ancient Task 1",
                    a.get(4), // Sales
                    p.get(0), // Abel Sunil
                    Priority.High,
                    Status.Todo,
                    date(2022, 7, 26),
                    date(2023, 7, 17),
                    p.get(0),
                    false,
                    "Today"
                ));
                tasks.add(new Task(
                    "Ancient Task 2",
                    a.get(1), // Growth
                    p.get(0), // Abel Sunil
                    Priority.High,
                    Status.Todo,
                    date(2022, 7, 30),
                    date(2023, 7, 17),
                    p.get(0),
                    false,
                    "This Week"
                ));
                // Task for today (from screenshot)
                tasks.add(new Task(
                    "A task for today",
                    null,
                    p.get(0), // Abel Sunil
                    Priority.None,
                    Status.Todo,
                    date(2025, 10, 24),
                    date(2025, 10, 24),
                    p.get(0),
                    false,
                    ""
                ));

                // Assign tasks to areas
                for(Task t : tasks) {
                    if (t.area != null) {
                        t.area.tasks.add(t);
                    }
                }
            }
            return tasks;
        }
    }


    // =============================================================================
    // == DARK THEME AND STYLING ==
    // =============================================================================

    /**
     * Manages the application's dark theme and color palette.
     */
    static class DarkTheme {
        // Main Palette
        static final Color COLOR_BG = new Color(27, 27, 27);
        static final Color COLOR_BG_LIGHTER = new Color(45, 45, 45); // Used for cards, alt rows
        static final Color COLOR_BG_ALT = new Color(35, 35, 35);    // Added for alternating rows
        static final Color COLOR_BG_LIGHTEST = new Color(60, 60, 60); // Used for header
        static final Color COLOR_FG = new Color(220, 220, 220);
        static final Color COLOR_FG_MUTED = new Color(150, 150, 150);
        static final Color COLOR_ACCENT_RED = new Color(235, 87, 87); // Red underline, Delete button
        static final Color COLOR_ACCENT_BLUE = new Color(45, 155, 245); // Selection
        static final Color COLOR_BORDER = new Color(70, 70, 70);
        static final Color COLOR_SIDEBAR_HOVER = new Color(55, 55, 55);

        // Tag Colors
        static final Color COLOR_TAG_GRAY = new Color(75, 75, 75);
        static final Color COLOR_TAG_GRAY_FG = new Color(210, 210, 210);
        static final Color COLOR_TAG_RED = new Color(131, 63, 62);
        static final Color COLOR_TAG_RED_FG = new Color(255, 195, 194);
        static final Color COLOR_TAG_ORANGE = new Color(133, 86, 47);
        static final Color COLOR_TAG_ORANGE_FG = new Color(254, 212, 164);
        static final Color COLOR_TAG_BLUE = new Color(47, 86, 133);
        static final Color COLOR_TAG_BLUE_FG = new Color(189, 211, 255);
        static final Color COLOR_TAG_GREEN = new Color(47, 103, 76);
        static final Color COLOR_TAG_GREEN_FG = new Color(191, 235, 213);

        // Fonts (Rolled back size)
        static final Font FONT_SANS = new Font("SansSerif", Font.PLAIN, 16);
        static final Font FONT_SANS_BOLD = new Font("SansSerif", Font.BOLD, 16);
        static final Font FONT_SANS_HEADER = new Font("SansSerif", Font.BOLD, 24);
        static final Font FONT_SANS_TITLE = new Font("SansSerif", Font.BOLD, 32);
        static final Font FONT_SANS_TABLE_HEADER = new Font("SansSerif", Font.BOLD, 17); // Header font


        /**
         * Applies the dark theme to the entire application via UIManager.
         */
        public static void apply() {
            try {
                // Set cross-platform look and feel
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

                // General component styling
                UIManager.put("Panel.background", COLOR_BG);
                UIManager.put("Panel.foreground", COLOR_FG);
                UIManager.put("Label.foreground", COLOR_FG);
                UIManager.put("Label.font", FONT_SANS);
                UIManager.put("Button.background", COLOR_BG_LIGHTER);
                UIManager.put("Button.foreground", COLOR_FG);
                UIManager.put("Button.font", FONT_SANS_BOLD);
                UIManager.put("Button.border", new LineBorder(COLOR_BORDER, 1));
                UIManager.put("Button.focus", new Color(0, 0, 0, 0)); // Disable focus ring
                // ToggleButton specific for Sidebar
                UIManager.put("ToggleButton.background", COLOR_BG); // Sidebar bg
                UIManager.put("ToggleButton.foreground", COLOR_FG_MUTED);
                UIManager.put("ToggleButton.select", COLOR_SIDEBAR_HOVER); // Selected bg
                UIManager.put("ToggleButton.focus", new Color(0, 0, 0, 0));
                UIManager.put("ToggleButton.border", BorderFactory.createEmptyBorder(10, 15, 10, 15));
                UIManager.put("ToggleButton.font", FONT_SANS_BOLD);

                // JScrollPane
                UIManager.put("ScrollPane.background", COLOR_BG);
                UIManager.put("ScrollPane.foreground", COLOR_FG);
                UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
                UIManager.put("ScrollBar.background", COLOR_BG);
                UIManager.put("ScrollBar.thumb", COLOR_BG_LIGHTEST);
                UIManager.put("ScrollBar.thumbDarkShadow", COLOR_BG_LIGHTEST);
                UIManager.put("ScrollBar.thumbHighlight", COLOR_BG_LIGHTEST);
                UIManager.put("ScrollBar.thumbShadow", COLOR_BG_LIGHTEST);
                UIManager.put("ScrollBar.track", COLOR_BG);
                UIManager.put("ScrollBar.width", 13);

                // JList
                UIManager.put("List.background", COLOR_BG);
                UIManager.put("List.foreground", COLOR_FG);
                UIManager.put("List.selectionBackground", COLOR_BG_LIGHTER);
                UIManager.put("List.selectionForeground", COLOR_FG);
                UIManager.put("List.font", FONT_SANS);

                // JTable (Rolled back size)
                UIManager.put("Table.background", COLOR_BG); // Base background
                UIManager.put("Table.foreground", COLOR_FG);
                UIManager.put("Table.gridColor", COLOR_BORDER);
                UIManager.put("Table.selectionBackground", COLOR_ACCENT_BLUE); // Brighter selection
                UIManager.put("Table.selectionForeground", COLOR_FG);
                UIManager.put("Table.font", FONT_SANS);
                UIManager.put("Table.rowHeight", 40); // Rolled back row height
                UIManager.put("Table.border", BorderFactory.createEmptyBorder());
                UIManager.put("Table.intercellSpacing", new Dimension(0, 0)); // Remove spacing for custom border look
                // Header Styling via UIManager (backup for custom renderer)
                UIManager.put("TableHeader.background", COLOR_BG_LIGHTEST);
                UIManager.put("TableHeader.foreground", COLOR_FG);
                UIManager.put("TableHeader.font", FONT_SANS_TABLE_HEADER);
                UIManager.put("TableHeader.cellBorder", BorderFactory.createCompoundBorder(
                                                            new MatteBorder(0, 0, 1, 1, COLOR_BORDER), // Bottom & Right lines
                                                            new EmptyBorder(8, 12, 8, 12))); // Padding

                // JToolBar
                UIManager.put("ToolBar.background", COLOR_BG);
                UIManager.put("ToolBar.foreground", COLOR_FG);
                UIManager.put("ToolBar.border", BorderFactory.createEmptyBorder(8, 8, 8, 8)); // More padding
                UIManager.put("ToolBar.separatorSize", new Dimension(12, 12));

                // JSeparator
                UIManager.put("Separator.background", COLOR_BORDER);
                UIManager.put("Separator.foreground", COLOR_BORDER);

                // ComboBox (for editors)
                UIManager.put("ComboBox.background", COLOR_BG_LIGHTER);
                UIManager.put("ComboBox.foreground", COLOR_FG);
                UIManager.put("ComboBox.selectionBackground", COLOR_ACCENT_BLUE);
                UIManager.put("ComboBox.selectionForeground", COLOR_FG);
                UIManager.put("ComboBox.border", new LineBorder(COLOR_BORDER));
                UIManager.put("ComboBox.font", FONT_SANS);

                // Text Fields & Areas
                UIManager.put("TextField.background", COLOR_BG_LIGHTER);
                UIManager.put("TextField.foreground", COLOR_FG);
                UIManager.put("TextField.caretForeground", COLOR_FG);
                UIManager.put("TextField.selectionBackground", COLOR_ACCENT_BLUE);
                UIManager.put("TextField.selectionForeground", COLOR_FG);
                UIManager.put("TextField.border", new LineBorder(COLOR_BORDER, 2));
                UIManager.put("TextArea.background", COLOR_BG_LIGHTER);
                UIManager.put("TextArea.foreground", COLOR_FG);
                UIManager.put("TextArea.caretForeground", COLOR_FG);
                UIManager.put("TextArea.selectionBackground", COLOR_ACCENT_BLUE);
                UIManager.put("TextArea.selectionForeground", COLOR_FG);
                UIManager.put("TextArea.border", new LineBorder(COLOR_BORDER, 2));


                // Other
                UIManager.put("Viewport.background", COLOR_BG);
                UIManager.put("SplitPane.background", COLOR_BG);
                UIManager.put("SplitPane.border", BorderFactory.createEmptyBorder());
                UIManager.put("SplitPaneDivider.border", BorderFactory.createEmptyBorder());
                // JOptionPane (ensure dark theme)
                UIManager.put("OptionPane.background", COLOR_BG);
                UIManager.put("OptionPane.messageForeground", COLOR_FG);
                UIManager.put("OptionPane.messageFont", FONT_SANS);
                UIManager.put("OptionPane.buttonFont", FONT_SANS_BOLD);


            } catch (Exception e) {
                System.err.println("Failed to initialize dark theme.");
                e.printStackTrace();
            }
        }
    }


    // =============================================================================
    // == CUSTOM ICON FACTORY (Vector Drawing - NO EMOJIS) ==
    // =============================================================================

    /**
     * Creates vector-based icons at runtime using Graphics2D, avoiding external
     * files and font dependencies.
     */
    static class IconFactory {
        private static final int ICON_SIZE = 18; // Rolled back size
        private static Map<String, Icon> iconCache = new HashMap<>();

        public static Icon get(String name) {
            return iconCache.computeIfAbsent(name, key -> createIcon(key));
        }

        public static Icon createAvatarIcon(String initial, Color color) {
            return new Icon() {
                private final int AVATAR_SIZE = 30; // Rolled back size
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(color);
                    g2.fill(new Ellipse2D.Double(x, y, AVATAR_SIZE, AVATAR_SIZE));
                    g2.setColor(Color.WHITE);
                    g2.setFont(DarkTheme.FONT_SANS_BOLD.deriveFont(14f)); // Rolled back font
                    FontMetrics fm = g2.getFontMetrics();
                    int textWidth = fm.stringWidth(initial);
                    int textHeight = fm.getAscent();
                    g2.drawString(initial, x + (AVATAR_SIZE - textWidth) / 2, y + (AVATAR_SIZE - textHeight) / 2 + fm.getAscent() - 2);
                    g2.dispose();
                }
                @Override public int getIconWidth() { return AVATAR_SIZE; }
                @Override public int getIconHeight() { return AVATAR_SIZE; }
            };
        }

        private static Icon createIcon(String name) {
            return new Icon() {
                private final int W = getIconWidth();
                private final int H = getIconHeight();

                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.translate(x, y); // Translate to icon position
                    g2.setColor(DarkTheme.COLOR_FG_MUTED); // Default icon color
                    g2.setStroke(new BasicStroke(1.5f)); // Standard stroke

                    // Draw based on name
                    switch (name) {
                        case "dashboard": // Simple bar chart
                            g2.fillRect(W/8, H*2/3, W/4, H/3);
                            g2.fillRect(W*3/8 + 1, H/3, W/4, H*2/3);
                            g2.fillRect(W*5/8 + 2, H/2, W/4, H/2);
                            break;
                        case "tasks": // Checkmark in box
                            g2.drawRect(W/8, H/8, W*6/8, H*6/8);
                            g2.drawLine(W*3/8, H*5/8, W*4/8, H*7/8);
                            g2.drawLine(W*4/8, H*7/8, W*7/8, H*3/8);
                            break;
                        case "areas": // Folder icon
                             g2.drawRoundRect(W/8, H/4, W*6/8, H*5/8, 4, 4);
                             // Explicit int casting for safety
                             int flapX = W / 8 - 1;
                             int flapY = H / 3;
                             int flapW = W * 6 / 8 + 2;
                             int flapH = H / 8;
                             g2.fillRect(flapX, flapY, flapW, flapH); // Top flap part
                             int clearX = W / 8 + 1;
                             int clearY = H / 3 + 1;
                             int clearW = W * 6 / 8 - 1;
                             int clearH = H / 8 - 1;
                             g2.clearRect(clearX, clearY, clearW, clearH); // Clear inside flap
                            break;
                        case "fire": // Flame
                            Path2D fire = new Path2D.Double();
                            fire.moveTo(W/2.0, H/8.0);
                            fire.curveTo(W*3.0/4.0, H/3.0, W*3.0/4.0, H*2.0/3.0, W/2.0, H*7.0/8.0);
                            fire.curveTo(W/4.0, H*2.0/3.0, W/4.0, H/3.0, W/2.0, H/8.0);
                            g2.setColor(DarkTheme.COLOR_ACCENT_RED);
                            g2.fill(fire);
                            break;
                        case "calendar":
                             g2.drawRect(W/8, H/4, W*6/8, H*5/8);
                             g2.drawLine(W/8, H/2, W*7/8, H/2);
                             g2.fillRect(W*2/8, H/8, W/8, H/4);
                             g2.fillRect(W*5/8, H/8, W/8, H/4);
                             // dots for days - with explicit casting
                             g2.fillRect((int)(W * 2.0 / 8.0), (int)(H * 5.0 / 8.0), W / 8, H / 8); // dot 1
                             g2.fillRect((int)(W * 3.5 / 8.0), (int)(H * 5.0 / 8.0), W / 8, H / 8); // dot 2 (FIXED CAST)
                             g2.fillRect((int)(W * 5.0 / 8.0), (int)(H * 5.0 / 8.0), W / 8, H / 8); // dot 3
                            break;
                        case "plus": // New button icon
                            g2.setColor(DarkTheme.COLOR_ACCENT_BLUE);
                            g2.setStroke(new BasicStroke(2f));
                            g2.drawLine(W/2, H/4, W/2, H*3/4); // Vertical
                            g2.drawLine(W/4, H/2, W*3/4, H/2); // Horizontal
                            break;
                         case "trash": // Delete button icon
                            g2.setColor(DarkTheme.COLOR_ACCENT_RED);
                            g2.setStroke(new BasicStroke(1.5f));
                            // Bin shape (using ints)
                            g2.drawRect(W/4, H/3, W/2, H*2/3 - H/8);
                            // Lid shape
                            g2.drawLine(W/8, H/3, W*7/8, H/3);
                            g2.drawLine(W*3/8, H/6, W*3/8, H/3);
                            g2.drawLine(W*5/8, H/6, W*5/8, H/3);
                            g2.drawLine(W*3/8, H/6, W*5/8, H/6);
                            // Lines inside bin
                            g2.drawLine(W*3/8, H/2, W*3/8, H*2/3);
                            g2.drawLine(W/2, H/2, W/2, H*2/3);
                            g2.drawLine(W*5/8, H/2, W*5/8, H*2/3);
                            break;
                        // Add more drawing logic for other icons...
                        case "person": g2.fillOval(W*3/8, H/8, W/4, W/4); g2.fillRect(W/4, H/2, W/2, H/3); break;
                        case "growth": g2.drawLine(W/8, H*7/8, W*7/8, H/8); g2.drawLine(W*5/8, H/8, W*7/8, H/8); g2.drawLine(W*7/8, H*3/8, W*7/8, H/8); break;
                        case "marketing": g2.drawRect(W/4, H/3, W/2, H/3); g2.drawLine(W*3/4, H/2, W*7/8, H/4); g2.drawLine(W*3/4, H/2, W*7/8, H*3/4); break; // Megaphone rough
                        case "finance": g2.setFont(DarkTheme.FONT_SANS_BOLD); g2.drawString("$", W*3/8, H*3/4); break; // Simple placeholder
                        case "sales": g2.drawOval(W/4,H/4,W/2,H/2); break; // Placeholder circle
                        case "pr": g2.drawOval(W/4,H/4,W/2,H/2); break; // Placeholder circle
                        case "dev": g2.drawRect(W/8, H/4, W*6/8, H/2); g2.drawLine(W/4, H*3/4, W*3/4, H*3/4); break; // Monitor
                        case "rnd": g2.drawOval(W/4, H/4, W/2, W/2); g2.drawLine(W*2/3, H*2/3, W*7/8, H*7/8); break; // Magnifying glass rough
                        case "organizing": g2.drawLine(W/8, H*7/8, W*7/8, H/8); g2.fillRect(W*5/8, H/8-1, W/4, W/4); break; // Pencil rough
                        case "support": g2.drawArc(W/8, H/8, W*6/8, H*6/8, 0, 180); g2.fillRect(W/8, H/2, W/8, H/4); g2.fillRect(W*6/8, H/2, W/8, H/4); break; // Headset rough
                        case "pointing": g2.drawOval(W/4,H/4,W/2,H/2); break; // Placeholder circle
                        case "clipboard": g2.drawRect(W/4, H/8, W/2, H*6/8); g2.fillRect(W*3/8, H/8-H/8, W/4, H/4); break;
                        case "briefcase": g2.drawRect(W/8, H/3, W*6/8, H/2); g2.drawRect(W*3/8, H/8, W/4, H/3); break;
                        default: // Fallback square
                            g2.drawRect(W/4, W/4, W/2, H/2);
                            break;
                    }

                    g2.dispose();
                }
                @Override public int getIconWidth() { return ICON_SIZE; }
                @Override public int getIconHeight() { return ICON_SIZE; }
            };
        }
    }


    // =============================================================================
    // == CUSTOM UI COMPONENTS AND RENDERERS ==
    // =============================================================================

    /**
     * A JLabel that renders a rounded tag, for Priority and Status.
     * Now includes symbols.
     */
    static class TagRenderer extends JLabel implements TableCellRenderer, ListCellRenderer {
        private int cornerRadius = 15; // More rounded

        public TagRenderer() {
            super();
            setOpaque(false);
            setHorizontalAlignment(CENTER);
            setBorder(new EmptyBorder(6, 12, 6, 12)); // Adjusted padding
            setFont(DarkTheme.FONT_SANS_BOLD.deriveFont(16f)); // Adjusted font
        }

        private void setData(Object value) {
            String text = "";
            String symbol = "";
            Color bg = DarkTheme.COLOR_TAG_GRAY;
            Color fg = DarkTheme.COLOR_TAG_GRAY_FG;

            if (value instanceof Priority) {
                Priority p = (Priority) value;
                text = p.name();
                bg = p.bg;
                fg = p.fg;
                switch(p) {
                    case High: symbol = "🔥 "; break;
                    case Medium: symbol = "🟠 "; break;
                    case Low: symbol = "🔵 "; break;
                    case None: symbol = "⚫ "; break;
                }
            } else if (value instanceof Status) {
                Status s = (Status) value;
                text = s.name();
                bg = s.bg;
                fg = s.fg;
                switch(s) {
                    case Completed: symbol = "✔️ "; break;
                    case InProgress: symbol = "▶️ "; break;
                    case Todo: symbol = "⬜ "; break;
                }
            } else if (value instanceof String) {
                text = (String) value;
                // Simple heuristic for "Add To" tags
                if (text.equals("Today")) {
                    bg = DarkTheme.COLOR_TAG_GREEN;
                    fg = DarkTheme.COLOR_TAG_GREEN_FG;
                } else if (text.equals("This Week")) {
                    bg = DarkTheme.COLOR_TAG_ORANGE;
                    fg = DarkTheme.COLOR_TAG_ORANGE_FG;
                } else if (!text.isEmpty()){
                    // Default for other strings like Area names
                    bg = DarkTheme.COLOR_TAG_GRAY;
                    fg = DarkTheme.COLOR_TAG_GRAY_FG;
                } else {
                    // Empty string, don't render
                    bg = new Color(0, 0, 0, 0); // transparent
                }
            }

            setText(symbol + text);
            setBackground(bg);
            setForeground(fg);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setData(value);
             // Ensure background respects selection/alternating rows
            if (isSelected) {
                 setOpaque(true); // Need opaque for selection background
                 setBackground(table.getSelectionBackground());
                 setForeground(table.getSelectionForeground());
            } else {
                 setOpaque(false); // Let paintComponent handle rounded bg
                 // Reset foreground in case it was changed by selection
                 // We rely on setData setting the correct fg based on value
                 // Re-call setData to ensure fg is correct for non-selected state
                 setData(value);
            }
            return this;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            setData(value);
            // Handle list selection similarly if needed
            if (isSelected) {
                 setOpaque(true);
                 setBackground(list.getSelectionBackground());
                 setForeground(list.getSelectionForeground());
            } else {
                 setOpaque(false);
                 setData(value); // Reset colors
            }
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            // Only paint rounded background if not selected (selection uses default rect)
             if (!isOpaque()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));
                g2.dispose();
             }
            super.paintComponent(g); // Paint text etc.
        }
    }

    /**
     * A JPanel that renders a Person with their avatar icon and name.
     * Now also handles plain Strings for the editable JComboBox.
     */
    static class PersonRenderer extends JPanel implements TableCellRenderer, ListCellRenderer<Object> {
        private JLabel avatarLabel = new JLabel();
        private JLabel nameLabel = new JLabel();

        public PersonRenderer() {
            super(new BorderLayout(10, 0));
            setOpaque(true);
            add(avatarLabel, BorderLayout.WEST);
            add(nameLabel, BorderLayout.CENTER);
            nameLabel.setFont(DarkTheme.FONT_SANS);
            setBorder(new EmptyBorder(4, 8, 4, 8));
        }

        private void setData(Object value, Color bg, Color fg) {
            setBackground(bg); // Set panel background
            nameLabel.setForeground(fg);
             // Make label background match panel for consistent look
            nameLabel.setOpaque(true);
            nameLabel.setBackground(bg);
            avatarLabel.setOpaque(true);
            avatarLabel.setBackground(bg);



            if (value instanceof Person) {
                Person p = (Person) value;
                avatarLabel.setIcon(p.avatar);
                nameLabel.setText(p.name);
                avatarLabel.setVisible(true);
            } else if (value instanceof String) {
                // Handle plain string (for editor)
                avatarLabel.setIcon(null);
                nameLabel.setText((String) value);
                avatarLabel.setVisible(false);
            } else {
                avatarLabel.setIcon(null);
                nameLabel.setText("");
                avatarLabel.setVisible(false);
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Color bg = isSelected ? table.getSelectionBackground() : table.getBackground();
            Color fg = isSelected ? table.getSelectionForeground() : table.getForeground();
             // Handle alternating row colors
            if (!isSelected) {
                 bg = (row % 2 == 0) ? DarkTheme.COLOR_BG_ALT : table.getBackground();
            }
            setData(value, bg, fg);
            return this;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            Color bg = isSelected ? list.getSelectionBackground() : list.getBackground();
            Color fg = isSelected ? list.getSelectionForeground() : list.getForeground();
            setData(value, bg, fg);
            return this;
        }
    }

    /**
     * Custom renderer for table headers.
     */
    static class ModernHeaderRenderer extends DefaultTableCellRenderer {
        public ModernHeaderRenderer() {
            setHorizontalAlignment(SwingConstants.LEFT);
            setOpaque(true);
            setBackground(DarkTheme.COLOR_BG_LIGHTEST);
            setForeground(DarkTheme.COLOR_FG);
            setFont(DarkTheme.FONT_SANS_TABLE_HEADER);
            setBorder(BorderFactory.createCompoundBorder(
                      new MatteBorder(0, 0, 1, 1, DarkTheme.COLOR_BORDER), // Bottom & Right lines
                      new EmptyBorder(8, 12, 8, 12))); // Padding
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            return this;
        }
    }


    // =============================================================================
    // == CUSTOM TABLE CELL EDITORS ==
    // =============================================================================

    /**
     * Custom editor for Priority column using a JComboBox.
     */
    static class PriorityEditor extends DefaultCellEditor {
        public PriorityEditor() {
            super(new JComboBox<>(Priority.values()));
            getComponent().setFont(DarkTheme.FONT_SANS);
            setClickCountToStart(1);
        }
    }

    /**
     * Custom editor for Status column using a JComboBox.
     */
    static class StatusEditor extends DefaultCellEditor {
        public StatusEditor() {
            super(new JComboBox<>(Status.values()));
            getComponent().setFont(DarkTheme.FONT_SANS);
            setClickCountToStart(1);
        }
    }

    /**
     * Custom editor for Person column using an EDITABLE JComboBox.
     */
    static class PersonEditor extends DefaultCellEditor {
        public PersonEditor() {
            super(new JComboBox<>(new Vector<>(MockData.getPeople())));
            JComboBox<Object> comboBox = (JComboBox<Object>) getComponent();
            comboBox.setEditable(true);
            comboBox.setRenderer(new PersonRenderer());
            setClickCountToStart(1);
        }

        @Override
        public Object getCellEditorValue() {
            Object selectedItem = super.getCellEditorValue();

            if (selectedItem instanceof Person) {
                return selectedItem;
            }

            if (selectedItem instanceof String) {
                String newName = (String) selectedItem;
                if (newName.trim().isEmpty()) {
                    return null;
                }

                // Check if this person already exists
                Optional<Person> existing = MockData.getPeople().stream()
                    .filter(p -> p.name.equalsIgnoreCase(newName))
                    .findFirst();

                if (existing.isPresent()) {
                    return existing.get();
                }

                // Not found, create a new one
                Color newColor = new Color((int)(Math.random() * 0x1000000));
                Person newPerson = new Person(newName, newColor);
                MockData.getPeople().add(newPerson);

                // Add to this combo box's model as well
                ((JComboBox<Object>) getComponent()).addItem(newPerson);

                return newPerson;
            }
            return null; // Or handle error
        }
    }

    /**
     * Custom editor for Area column using an EDITABLE JComboBox.
     */
    static class AreaEditor extends DefaultCellEditor {
        public AreaEditor() {
            super(new JComboBox<>(new Vector<>(MockData.getAreas())));
            JComboBox<Object> comboBox = (JComboBox<Object>) getComponent();
            comboBox.setEditable(true);
            // Use a simple renderer for Areas
            comboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value,
                        int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Area) {
                        setText(((Area)value).name);
                    } else if (value != null) {
                        setText(value.toString());
                    }
                    return this;
                }
            });
            setClickCountToStart(1);
        }

        @Override
        public Object getCellEditorValue() {
            Object selectedItem = super.getCellEditorValue();

            if (selectedItem instanceof Area) {
                return selectedItem;
            }

            if (selectedItem instanceof String) {
                String newName = (String) selectedItem;
                if (newName.trim().isEmpty()) {
                    return null;
                }

                // Check if this area already exists
                Optional<Area> existing = MockData.getAreas().stream()
                    .filter(a -> a.name.equalsIgnoreCase(newName))
                    .findFirst();

                if (existing.isPresent()) {
                    return existing.get();
                }

                // Not found, create a new one
                Area newArea = new Area(newName, null, IconFactory.get("default"));
                MockData.getAreas().add(newArea);

                // Add to this combo box's model as well
                ((JComboBox<Object>) getComponent()).addItem(newArea);

                return newArea;
            }
            return null;
        }
    }

    // REMOVED ActionsPanelRenderer and ActionsPanelEditor


    /**
     * A custom JLabel for the main section headers (e.g., "🔥 Urgent").
     */
    static class HeaderLabel extends JLabel {
        public HeaderLabel(String text, Icon icon) {
            super(text, icon, SwingConstants.LEADING);
            setFont(DarkTheme.FONT_SANS_HEADER);
            setForeground(DarkTheme.COLOR_FG);
            setBorder(new EmptyBorder(10, 5, 5, 5));
            setIconTextGap(10);
        }
    }

    /**
     * A custom JSeparator with the red accent color.
     */
    static class RedSeparator extends JSeparator {
        public RedSeparator() {
            super(HORIZONTAL);
            setForeground(DarkTheme.COLOR_ACCENT_RED);
            // Thicker line
            setUI(new BasicSeparatorUI() {
                @Override
                public void paint(Graphics g, JComponent c) {
                    Dimension s = c.getSize();
                    g.setColor(c.getForeground());
                    g.fillRect(0, 0, s.width, 2);
                }
                @Override
                public Dimension getPreferredSize(JComponent c) {
                    return new Dimension(super.getPreferredSize(c).width, 2);
                }
            });
        }
    }

    /**
     * Factory for creating RowFilters for the TaskTableModel.
     */
    static class TaskFilterFactory {

        public static RowFilter<TaskTableModel, Integer> getFilter(String filterType) {
            LocalDate today = LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);

            return new RowFilter<TaskTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends TaskTableModel, ? extends Integer> entry) {
                    TaskTableModel model = entry.getModel();
                    int row = entry.getIdentifier(); // Model index

                    // Get task from model (safer than using the list directly)
                    Task task = model.getTaskAt(row);
                    if (task == null) return false;

                    // Always filter out "done" tasks from dashboard
                    // Note: 'done' field still exists even if column is hidden
                    if (task.done) return false;

                    switch (filterType) {
                        case "Today":
                            return "Today".equals(task.addToAction) ||
                                   (task.deadline != null && task.deadline.toInstant()
                                   .atZone(ZoneId.systemDefault()).toLocalDate().isEqual(today));
                        case "Tomorrow":
                            return "Tomorrow".equals(task.addToAction) ||
                                   (task.deadline != null && task.deadline.toInstant()
                                   .atZone(ZoneId.systemDefault()).toLocalDate().isEqual(tomorrow));
                        case "This Week":
                            return "This Week".equals(task.addToAction);
                        case "This Month":
                            return "This Month".equals(task.addToAction);
                        case "Urgent":
                            return task.priority == Priority.High;
                        case "Leaderboard":
                            return task.status == Status.InProgress || task.status == Status.Completed;
                        case "My Tasks":
                            // Assuming "Abel Sunil" is "me" for this mock
                            return task.assignee != null && task.assignee.name.equals("Abel Sunil");
                        case "My Due Today":
                            boolean isDueToday = "Today".equals(task.addToAction) ||
                                   (task.deadline != null && task.deadline.toInstant()
                                   .atZone(ZoneId.systemDefault()).toLocalDate().isEqual(today));
                            boolean isMine = task.assignee != null && task.assignee.name.equals("Abel Sunil");
                            return isDueToday && isMine;
                        default:
                            return true; // No filter
                    }
                }
            };
        }
    }

    /**
     * A custom border that draws a rounded line.
     */
    static class RoundedLineBorder extends AbstractBorder {
        private int radius;
        private Color color;
        private int thickness;

        public RoundedLineBorder(Color color, int radius, int thickness) {
            this.color = color;
            this.radius = radius;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Double(x + thickness / 2.0, y + thickness / 2.0,
                                                width - thickness, height - thickness,
                                                radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.top = insets.right = insets.bottom = thickness;
            return insets;
        }
    }


    /**
     * A panel for one column in the dashboard (e.g., "Today's Tasks").
     * This now holds a JTable with a RowFilter on the main model.
     * Delete button moved to header.
     */
    static class DashboardColumnPanel extends JPanel {

        private TaskTableModel globalTaskModel;
        private JTable table; // Field to access for deletion

        public DashboardColumnPanel(String title, Icon icon,
                                    TaskTableModel model, RowFilter<TaskTableModel, Integer> filter) {
            super(new BorderLayout(0, 5));
            this.globalTaskModel = model;

            // Use lighter background for card effect
            setBackground(DarkTheme.COLOR_BG_LIGHTER);
            // Add rounded border
            setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(DarkTheme.COLOR_BORDER, 15, 1),
                new EmptyBorder(10, 15, 10, 15)
            ));
            setOpaque(true);


            // 1. Header with "New" and "Delete" buttons
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setOpaque(false); // Make transparent for card bg
            headerPanel.add(new HeaderLabel(title, icon), BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            buttonPanel.setOpaque(false);

            JButton newButton = new JButton("New");
            newButton.addActionListener(e -> globalTaskModel.addNewTask()); // Calls central method
            buttonPanel.add(newButton);

            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(e -> deleteSelectedTasks()); // Call method below
            buttonPanel.add(deleteButton);

            headerPanel.add(buttonPanel, BorderLayout.EAST);

            headerPanel.add(new RedSeparator(), BorderLayout.SOUTH);
            add(headerPanel, BorderLayout.NORTH);

            // 2. Content (Table)
            add(createTaskTableView(filter), BorderLayout.CENTER);
        }

        private JScrollPane createTaskTableView(RowFilter<TaskTableModel, Integer> filter) {
            // Create table from the GLOBAL model
            table = new JTable(globalTaskModel) {
                 // Implement alternating row colors (using card bg as base)
                 @Override
                 public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                     Component c = super.prepareRenderer(renderer, row, column);
                     // Get the actual model column index if columns were reordered
                     int modelColumn = -1;
                     try {
                          modelColumn = convertColumnIndexToModel(column);
                     } catch (IndexOutOfBoundsException e) {
                          // Can happen during table updates, ignore for rendering
                          modelColumn = column; // Fallback
                     }


                     if (!isRowSelected(row)) { // Use correct method here
                          // Alternate slightly darker/lighter than card background
                         c.setBackground(row % 2 == 0 ? DarkTheme.COLOR_BG_LIGHTER.darker() : DarkTheme.COLOR_BG_LIGHTER);
                     } else {
                         c.setBackground(DarkTheme.COLOR_ACCENT_BLUE); // Use selection color
                     }

                     // Ensure renderers respect background color
                     if (c instanceof JComponent) {
                         ((JComponent) c).setOpaque(true);
                     }
                      // Special handling for panels used as renderers (like ActionsPanelRenderer)
                     if (c instanceof JPanel) {
                         c.setBackground(row % 2 == 0 ? DarkTheme.COLOR_BG_LIGHTER.darker() : DarkTheme.COLOR_BG_LIGHTER);
                         if(isRowSelected(row)) c.setBackground(DarkTheme.COLOR_ACCENT_BLUE); // Use correct method
                         // Make sure sub-components are not opaque if they shouldn't be
                         for(Component comp : ((JPanel)c).getComponents()){
                              if (comp instanceof JButton) {
                                  // Let the button handle its own background (or make transparent)
                                  ((JButton)comp).setOpaque(false);
                              } else if(comp instanceof JLabel || comp instanceof JCheckBox){
                                 ((JComponent)comp).setOpaque(false);
                             }
                         }
                     }
                      // Ensure non-opaque renderers (like TagRenderer) get the right background when selected
                     if(isRowSelected(row) && !(c instanceof JPanel )) { // Removed JTextArea check
                          c.setBackground(DarkTheme.COLOR_ACCENT_BLUE);
                           // Need to make opaque to show selection color for some renderers
                          if(c instanceof JLabel) {
                              ((JLabel)c).setOpaque(true);
                          }
                     } else if (!isRowSelected(row) && !(c instanceof JPanel )) { // Removed JTextArea check
                         // Reset opaque state if needed when deselected
                         if (c instanceof TagRenderer) { // TagRenderer handles opaque itself
                            ((JLabel)c).setOpaque(false);
                         } else if (c instanceof JLabel) {
                             ((JLabel)c).setOpaque(false); // Assume default renderers aren't opaque
                         }
                     }


                     return c;
                 }
            }; // Assign to class field
            table.setOpaque(false); // Table itself transparent for card bg
            table.setBackground(DarkTheme.COLOR_BG_LIGHTER); // Base background for rows
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Enable horizontal scrolling
            table.setFillsViewportHeight(true);

            // Create a sorter and apply the filter
            TableRowSorter<TaskTableModel> sorter = new TableRowSorter<>(globalTaskModel);
            sorter.setRowFilter(filter);
            table.setRowSorter(sorter);

            // Apply modern header renderer
            table.getTableHeader().setDefaultRenderer(new ModernHeaderRenderer());
            table.getTableHeader().setOpaque(false); // Let renderer handle bg

            // Set shared renderers (NO text wrapping for Task now)
            // table.getColumnModel().getColumn(0).setCellRenderer(new TextAreaCellRenderer()); // REMOVED
            table.setDefaultRenderer(Person.class, new PersonRenderer());
            table.setDefaultRenderer(Priority.class, new TagRenderer());
            table.setDefaultRenderer(Status.class, new TagRenderer());
            table.setDefaultRenderer(Date.class, new TasksListPanel.DateRenderer()); // Reuse Date renderer

            // Set shared editors
            table.setDefaultEditor(Priority.class, new PriorityEditor());
            table.setDefaultEditor(Status.class, new StatusEditor());
            table.setDefaultEditor(Person.class, new PersonEditor());
            table.setDefaultEditor(Area.class, new AreaEditor());

            // Hide columns not needed for the dashboard view
            hideColumn(table, "Deadline");
            hideColumn(table, "Days Left");
            hideColumn(table, "Add To");
            hideColumn(table, "Created time");
            hideColumn(table, "Created by");

            // REMOVED Actions column addition

            // Set widths for visible columns from the MODEL
            TableColumnModel cm = table.getColumnModel();
            cm.getColumn(table.convertColumnIndexToView(0)).setPreferredWidth(500); // Task (Wider) - Use model index 0
            cm.getColumn(table.convertColumnIndexToView(1)).setPreferredWidth(150); // Area - Use model index 1
            cm.getColumn(table.convertColumnIndexToView(2)).setPreferredWidth(200); // Assignee - Use model index 2
            cm.getColumn(table.convertColumnIndexToView(3)).setPreferredWidth(130); // Priority - Use model index 3
            cm.getColumn(table.convertColumnIndexToView(4)).setPreferredWidth(150); // Status - Use model index 4


            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setOpaque(false); // Scrollpane transparent for card bg
            scrollPane.getViewport().setOpaque(false); // Viewport transparent
            scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
            return scrollPane;
        }

        private void hideColumn(JTable table, String colName) {
            try {
                 // Must convert name to MODEL index first if table columns might be reordered
                int modelIndex = globalTaskModel.findColumn(colName);
                if (modelIndex != -1) {
                     // Then convert MODEL index to VIEW index
                    int viewIndex = table.convertColumnIndexToView(modelIndex);
                    if (viewIndex != -1) { // Check if column is currently visible
                        TableColumn col = table.getColumnModel().getColumn(viewIndex);
                        col.setMinWidth(0);
                        col.setMaxWidth(0);
                        col.setPreferredWidth(0);
                    }
                }
            } catch (IllegalArgumentException e) {
                 System.err.println("Warning: Column not found to hide: " + colName);
            }
        }

        private void deleteSelectedTasks() {
            int[] selectedViewRows = table.getSelectedRows();
            if (selectedViewRows.length == 0) return;

            int choice = JOptionPane.showConfirmDialog(
                this, // Use 'this' (the panel) as the parent component
                "Delete " + selectedViewRows.length + " task(s)?",
                "Confirm Deletion",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (choice != JOptionPane.OK_OPTION) return;

            int[] modelRows = new int[selectedViewRows.length];
            for (int i = 0; i < selectedViewRows.length; i++) {
                // IMPORTANT: Convert view index to model index before deleting
                modelRows[i] = table.convertRowIndexToModel(selectedViewRows[i]);
            }

            Arrays.sort(modelRows);

            for (int i = modelRows.length - 1; i >= 0; i--) {
                globalTaskModel.removeTaskAt(modelRows[i]);
            }
        }
    }


    // =============================================================================
    // == SIDEBAR COMPONENTS ==
    // =============================================================================

    /**
     * Custom JToggleButton for the sidebar.
     */
    static class SidebarButton extends JToggleButton {
        private Color hoverColor = DarkTheme.COLOR_SIDEBAR_HOVER;
        private Color baseColor = DarkTheme.COLOR_BG; // Match sidebar bg

        public SidebarButton(String text, Icon icon) {
            super(text, icon);
            setContentAreaFilled(false);
            setOpaque(true); // Need opaque to paint background
            setBackground(baseColor);
            setForeground(DarkTheme.COLOR_FG_MUTED);
            setHorizontalAlignment(SwingConstants.LEFT);
            setIconTextGap(15);
            setFocusPainted(false);
            setBorder(UIManager.getBorder("ToggleButton.border")); // Use theme border/padding
            setFont(DarkTheme.FONT_SANS_BOLD);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!isSelected()) {
                        setBackground(hoverColor);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!isSelected()) {
                        setBackground(baseColor);
                    }
                }
            });

            // Handle selection color change
            addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    setBackground(hoverColor); // Use hover color for selected
                    setForeground(DarkTheme.COLOR_FG); // Brighter text when selected
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    setBackground(baseColor);
                    setForeground(DarkTheme.COLOR_FG_MUTED);
                }
            });
        }
    }

    /**
     * Panel for the sidebar navigation.
     */
    static class SidebarPanel extends JPanel {
        private CardLayout cardLayout;
        private JPanel cardPanel;

        public SidebarPanel(CardLayout cardLayout, JPanel cardPanel) {
            this.cardLayout = cardLayout;
            this.cardPanel = cardPanel;

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(DarkTheme.COLOR_BG);
            setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 0, 1, DarkTheme.COLOR_BORDER), // Right border
                BorderFactory.createEmptyBorder(20, 0, 0, 0)) // Top padding
            );

            ButtonGroup buttonGroup = new ButtonGroup();

            SidebarButton dashboardButton = createSidebarButton("Dashboard", IconFactory.get("dashboard"), View.DASHBOARD);
            SidebarButton tasksButton = createSidebarButton("Tasks List", IconFactory.get("tasks"), View.TASKS);
            SidebarButton areasButton = createSidebarButton("Areas", IconFactory.get("areas"), View.AREAS);

            buttonGroup.add(dashboardButton);
            buttonGroup.add(tasksButton);
            buttonGroup.add(areasButton);

            add(dashboardButton);
            add(Box.createRigidArea(new Dimension(0, 5)));
            add(tasksButton);
             add(Box.createRigidArea(new Dimension(0, 5)));
            add(areasButton);

            add(Box.createVerticalGlue()); // Pushes buttons to the top

            // Select Dashboard by default
            dashboardButton.setSelected(true);
        }

        private SidebarButton createSidebarButton(String text, Icon icon, View view) {
            SidebarButton button = new SidebarButton(text, icon);
            button.addActionListener(e -> cardLayout.show(cardPanel, view.name()));
            // Ensure button doesn't stretch horizontally
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
            return button;
        }

    }


    // =============================================================================
    // == MAIN APPLICATION PANELS (NOW USING SIDEBAR/CARDLAYOUT) ==
    // =============================================================================

    /**
     * The main container panel, now using BorderLayout with Sidebar and CardLayout.
     */
    static class MainAppPanel extends JPanel {

        private TaskTableModel globalTaskModel;
        private AreaTableModel globalAreaModel;

        public MainAppPanel() {
            super(new BorderLayout());
            setBackground(DarkTheme.COLOR_BG);

            // Create the single, global models
            this.globalTaskModel = new TaskTableModel(MockData.getTasks());
            this.globalAreaModel = new AreaTableModel(MockData.getAreas());

            // Create the main content panel using CardLayout
            CardLayout cardLayout = new CardLayout();
            JPanel cardPanel = new JPanel(cardLayout);
            cardPanel.setOpaque(false); // Let content panels define background

            // Add view panels to the CardLayout
            cardPanel.add(new DashboardPanel(globalTaskModel), View.DASHBOARD.name());
            cardPanel.add(new TasksListPanel(globalTaskModel), View.TASKS.name());
            cardPanel.add(new AreasPanel(globalAreaModel), View.AREAS.name());

            // Create the sidebar
            SidebarPanel sidebar = new SidebarPanel(cardLayout, cardPanel);

            // Add sidebar and card panel to the main layout
            add(sidebar, BorderLayout.WEST);
            add(cardPanel, BorderLayout.CENTER);
        }
    }

    /**
     * The "Dashboard" tab panel, showing a scrollable list of sections.
     */
    static class DashboardPanel extends JScrollPane {

        private TaskTableModel globalTaskModel;

        public DashboardPanel(TaskTableModel model) {
            super();
            this.globalTaskModel = model;

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBackground(DarkTheme.COLOR_BG); // Main BG for spacing
            mainPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

            // Add sections in 2-column rows
            // All panels now receive the SAME model but with DIFFERENT filters
            mainPanel.add(createRowPanel(
                new DashboardColumnPanel("Today's Tasks", IconFactory.get("calendar"),
                    globalTaskModel, TaskFilterFactory.getFilter("Today")),
                new DashboardColumnPanel("Tomorrow", IconFactory.get("landscape"),
                    globalTaskModel, TaskFilterFactory.getFilter("Tomorrow"))
            ));

            mainPanel.add(createRowPanel(
                new DashboardColumnPanel("This Week", IconFactory.get("calendar"),
                    globalTaskModel, TaskFilterFactory.getFilter("This Week")),
                new DashboardColumnPanel("This Month", IconFactory.get("calendar"),
                    globalTaskModel, TaskFilterFactory.getFilter("This Month"))
            ));

            mainPanel.add(createRowPanel(
                new DashboardColumnPanel("Urgent", IconFactory.get("fire"),
                    globalTaskModel, TaskFilterFactory.getFilter("Urgent")),
                new DashboardColumnPanel("Leaderboard", IconFactory.get("trophy"),
                    globalTaskModel, TaskFilterFactory.getFilter("Leaderboard"))
            ));

             mainPanel.add(createRowPanel(
                new DashboardColumnPanel("My Tasks", IconFactory.get("pointing"),
                    globalTaskModel, TaskFilterFactory.getFilter("My Tasks")),
                new DashboardColumnPanel("My Due Today", IconFactory.get("fire"),
                    globalTaskModel, TaskFilterFactory.getFilter("My Due Today"))
            ));

            setViewportView(mainPanel);
            setBorder(BorderFactory.createEmptyBorder());
            setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            getVerticalScrollBar().setUnitIncrement(16);
        }


        private JPanel createRowPanel(JPanel panel1, JPanel panel2) {
            JPanel rowPanel = new JPanel(new GridLayout(1, 2, 20, 0));
            rowPanel.setOpaque(false); // Show main background
            rowPanel.add(panel1);
            rowPanel.add(panel2);
            rowPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
            return rowPanel;
        }
    }

    /**
     * The "Tasks List" tab panel, showing a JTable of all tasks.
     */
    static class TasksListPanel extends JPanel {

        // Simple date renderer shared by multiple columns
        static class DateRenderer extends DefaultTableCellRenderer {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (value != null) {
                    setText(sdf.format((Date) value));
                } else {
                    setText("");
                }
                setBorder(new EmptyBorder(0, 10, 0, 10)); // Added padding
                return this;
            }
        }

        private TaskTableModel globalTaskModel;
        private JTable table; // Make table a field to access for deletion

        public TasksListPanel(TaskTableModel model) {
            super(new BorderLayout(0, 10));
            this.globalTaskModel = model;

            setBackground(DarkTheme.COLOR_BG);
            setBorder(new EmptyBorder(10, 20, 20, 20));

            // 1. Header
            JLabel title = new JLabel("Tasks List", IconFactory.get("tasks"), SwingConstants.LEADING);
            title.setFont(DarkTheme.FONT_SANS_TITLE);
            title.setIconTextGap(10);
            title.setBorder(new EmptyBorder(0, 0, 5, 0));

            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setOpaque(false);
            headerPanel.add(title, BorderLayout.NORTH);
            headerPanel.add(new RedSeparator(), BorderLayout.SOUTH);
            add(headerPanel, BorderLayout.NORTH);

            // 2. Toolbar (Modified per request)
            JToolBar toolBar = new JToolBar();
            toolBar.setFloatable(false);
            toolBar.setOpaque(false);

            JButton tableViewBtn = new JButton("Table View");
            tableViewBtn.setEnabled(false); // Disabled as it's the only view
            tableViewBtn.setBackground(DarkTheme.COLOR_ACCENT_BLUE); // Show as "selected"
            tableViewBtn.setForeground(Color.WHITE);
            toolBar.add(tableViewBtn);

            toolBar.add(Box.createHorizontalGlue());

            JButton newButton = new JButton("New");
            newButton.addActionListener(e -> globalTaskModel.addNewTask()); // Calls central method
            toolBar.add(newButton);

            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(e -> deleteSelectedTasks());
            toolBar.add(deleteButton);

            add(toolBar, BorderLayout.CENTER);

            // 3. Table (uses the one global model)
            table = new JTable(globalTaskModel) {
                 // Implement alternating row colors
                 @Override
                 public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                     Component c = super.prepareRenderer(renderer, row, column);
                     if (!isRowSelected(row)) { // FIX: Use correct method
                         c.setBackground(row % 2 == 0 ? DarkTheme.COLOR_BG_ALT : getBackground());
                     } else {
                          c.setBackground(getSelectionBackground()); // Use selection color
                     }
                      // Ensure renderers respect background color
                     if (c instanceof JComponent) {
                         ((JComponent) c).setOpaque(true);
                     }
                      // Special handling for panels used as renderers
                     if (c instanceof JPanel) {
                          c.setBackground(row % 2 == 0 ? DarkTheme.COLOR_BG_ALT : getBackground());
                          if(isRowSelected(row)) c.setBackground(getSelectionBackground()); // FIX: Use correct method
                         // Make sure sub-components are not opaque if they shouldn't be
                         for(Component comp : ((JPanel)c).getComponents()){
                             if(comp instanceof JLabel || comp instanceof JCheckBox){ // Or specific renderers
                                 ((JComponent)comp).setOpaque(false);
                             }
                         }
                     }
                      // Ensure non-opaque renderers get the right background when selected
                     if(isRowSelected(row) && !(c instanceof JPanel )) { // FIX: Use correct method, Removed JTextArea check
                          c.setBackground(getSelectionBackground());
                           // Need to make opaque to show selection color for some renderers
                          if(c instanceof JLabel) {
                              ((JLabel)c).setOpaque(true);
                          }
                     } else if (!isRowSelected(row) && !(c instanceof JPanel )) { // FIX: Use correct method, Removed JTextArea check
                         // Reset opaque state if needed when deselected
                         if (c instanceof TagRenderer) { // TagRenderer handles opaque itself
                            ((JLabel)c).setOpaque(false);
                         } else if (c instanceof JLabel) {
                             ((JLabel)c).setOpaque(false); // Assume default renderers aren't opaque
                         }
                     }

                     return c;
                 }
            }; // Assign to class field
            table.setFillsViewportHeight(true);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Enable horizontal scrolling
            table.setAutoCreateRowSorter(true); // Add sorter for the main view

            // Apply modern header renderer
            table.getTableHeader().setDefaultRenderer(new ModernHeaderRenderer());

            // Set custom renderers (NO text wrapping for Task now)
            // table.getColumnModel().getColumn(0).setCellRenderer(new TextAreaCellRenderer()); // REMOVED
            table.setDefaultRenderer(Person.class, new PersonRenderer());
            table.setDefaultRenderer(Priority.class, new TagRenderer());
            table.setDefaultRenderer(Status.class, new TagRenderer());
            table.setDefaultRenderer(Date.class, new DateRenderer());

            // Set custom editors
            table.setDefaultEditor(Priority.class, new PriorityEditor());
            table.setDefaultEditor(Status.class, new StatusEditor());
            table.setDefaultEditor(Person.class, new PersonEditor());
            table.setDefaultEditor(Area.class, new AreaEditor());

            // Special renderer for "Add To" column (which is a String)
            table.getColumn("Add To").setCellRenderer(new TagRenderer());

            // Set column widths (indices adjusted after removing "Done?")
            TableColumnModel cm = table.getColumnModel();
            cm.getColumn(0).setPreferredWidth(500); // Task (Wider)
            cm.getColumn(1).setPreferredWidth(150); // Area
            cm.getColumn(2).setPreferredWidth(200); // Assignee (Increased more)
            cm.getColumn(3).setPreferredWidth(130); // Priority (Wider for symbol)
            cm.getColumn(4).setPreferredWidth(150); // Status (Wider for symbol)
            cm.getColumn(5).setPreferredWidth(130); // Deadline
            cm.getColumn(6).setPreferredWidth(170); // Days Left
            cm.getColumn(7).setPreferredWidth(120); // Add To
            // Column 8 (Done?) removed
           
            cm.getColumn(8).setPreferredWidth(150); // Created time (was 9)
            cm.getColumn(9).setPreferredWidth(200); // Created by (was 10, Increased more)

            JScrollPane scrollPane = new JScrollPane(table);
            add(scrollPane, BorderLayout.SOUTH);
        }

        private void deleteSelectedTasks() {
            int[] selectedViewRows = table.getSelectedRows();
            if (selectedViewRows.length == 0) {
                return; // Nothing selected
            }

            int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete " + selectedViewRows.length + " task(s)?",
                "Confirm Deletion",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (choice != JOptionPane.OK_OPTION) {
                return;
            }

            // Convert view indices to model indices
            int[] modelRows = new int[selectedViewRows.length];
            for (int i = 0; i < selectedViewRows.length; i++) {
                modelRows[i] = table.convertRowIndexToModel(selectedViewRows[i]);
            }

            // Sort indices in reverse order to avoid shifting
            Arrays.sort(modelRows);

            // Delete from the model, starting from the end
            for (int i = modelRows.length - 1; i >= 0; i--) {
                globalTaskModel.removeTaskAt(modelRows[i]);
            }
        }
    }

    /**
     * TableModel for the FULL Tasks list.
     * This is now the ONLY task model.
     */
    static class TaskTableModel extends AbstractTableModel {
        private final List<Task> tasks;
        // REMOVED "Done?" from column names
        private final String[] columnNames = {
            "Task", "Area", "Assignee", "Priority", "Status",
            "Deadline", "Days Left", "Add To", "Created time", "Created by"
        };
        // REMOVED Boolean.class from column types
        private final Class<?>[] columnClasses = {
            String.class, Area.class, Person.class, Priority.class, Status.class,
            Date.class, String.class, String.class, Date.class, Person.class
        };

        public TaskTableModel(List<Task> tasks) {
            this.tasks = tasks;
        }

        /**
         * Centralized method to add a new task.
         * This notifies the model, and all tables update.
         */
        public void addNewTask() {
            Task newTask = new Task();
            this.tasks.add(newTask);
            // Notify the model
            fireTableRowsInserted(this.tasks.size() - 1, this.tasks.size() - 1);
        }

        /**
         * Centralized method to remove a task.
         */
        public void removeTaskAt(int row) {
            if (row >= 0 && row < tasks.size()) {
                tasks.remove(row);
                fireTableRowsDeleted(row, row);
            }
        }

        /**
         * Helper to get a task (needed by filter).
         */
        public Task getTaskAt(int row) {
            if (row >= 0 && row < tasks.size()) {
                return tasks.get(row);
            }
            return null;
        }

        // Helper method to find column index by name (needed for hiding columns reliably)
        public int findColumn(String columnName) {
            for (int i = 0; i < getColumnCount(); i++) {
                if (getColumnName(i).equals(columnName)) {
                    return i;
                }
            }
            return -1; // Not found
        }


        @Override public int getRowCount() { return tasks.size(); }
        @Override public int getColumnCount() { return columnNames.length; }
        @Override public String getColumnName(int col) { return columnNames[col]; }
        @Override public Class<?> getColumnClass(int col) { return columnClasses[col]; }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col != 6; // "Days Left" is computed, so not editable
        }

        @Override
        public Object getValueAt(int row, int col) {
            Task task = tasks.get(row);
            switch (col) {
                case 0: return task.name;
                case 1: return task.area;
                case 2: return task.assignee;
                case 3: return task.priority;
                case 4: return task.status;
                case 5: return task.deadline;
                case 6: return task.getDaysLeft();
                case 7: return task.addToAction;
                // case 8 (Done?) removed
                case 8: return task.createdTime; // was 9
                case 9: return task.createdBy;   // was 10
                default: return null;
            }
        }

        @Override
        public void setValueAt(Object aValue, int row, int col) {
            Task task = tasks.get(row);
            try {
                switch (col) {
                    case 0: task.name = (String)aValue; break;
                    case 1: task.area = (Area)aValue; break;
                    case 2: task.assignee = (Person)aValue; break;
                    case 3: task.priority = (Priority)aValue; break;
                    case 4: task.status = (Status)aValue; break;
                    case 5: task.deadline = (Date)aValue; break;
                    // case 6 (Days Left) is not editable
                    case 7: task.addToAction = (String)aValue; break;
                    // case 8 (Done?) removed
                    case 8: task.createdTime = (Date)aValue; break; // was 9
                    case 9: task.createdBy = (Person)aValue; break; // was 10
                }
                fireTableCellUpdated(row, col);

                // Special case: if deadline changed, update "Days Left"
                if (col == 5) {
                    fireTableCellUpdated(row, 6);
                }
            } catch (Exception e) {
                GlobalExceptionHandler.handle(e);
            }
        }
    }

    /**
     * The "Areas" tab panel, showing a JTable of all areas.
     */
    static class AreasPanel extends JPanel {
        private AreaTableModel model;
        private JTable table; // Make table a field

        public AreasPanel(AreaTableModel model) {
            super(new BorderLayout(0, 10));
            this.model = model;

            setBackground(DarkTheme.COLOR_BG);
            setBorder(new EmptyBorder(10, 20, 20, 20));

            // 1. Header
            JLabel title = new JLabel("Areas", IconFactory.get("areas"), SwingConstants.LEADING); // Updated Icon
            title.setFont(DarkTheme.FONT_SANS_TITLE);
            title.setIconTextGap(10);
            title.setBorder(new EmptyBorder(0, 0, 5, 0));

            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setOpaque(false);
            headerPanel.add(title, BorderLayout.NORTH);
            headerPanel.add(new RedSeparator(), BorderLayout.SOUTH);
            add(headerPanel, BorderLayout.NORTH);

            // 2. Toolbar (simplified)
            JToolBar toolBar = new JToolBar();
            toolBar.setFloatable(false);
            toolBar.setOpaque(false);
            toolBar.add(new JButton("Table View"));
            toolBar.add(new JButton("Board View"));
            toolBar.add(Box.createHorizontalGlue());

            JButton newButton = new JButton("New");
            newButton.addActionListener(e -> model.addNewArea());
            toolBar.add(newButton);

            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(e -> deleteSelectedAreas());
            toolBar.add(deleteButton);

            add(toolBar, BorderLayout.CENTER);

            // 3. Table
            table = new JTable(model) {
                 // Implement alternating row colors
                 @Override
                 public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                     Component c = super.prepareRenderer(renderer, row, column);
                     if (!isRowSelected(row)) { // FIX: Use correct method
                         c.setBackground(row % 2 == 0 ? DarkTheme.COLOR_BG_ALT : getBackground());
                     } else {
                          c.setBackground(getSelectionBackground()); // Use selection color
                     }
                      // Ensure renderers respect background color
                     if (c instanceof JComponent) {
                         ((JComponent) c).setOpaque(true);
                     }
                      // Special handling for panels used as renderers
                     if (c instanceof JPanel) {
                          c.setBackground(row % 2 == 0 ? DarkTheme.COLOR_BG_ALT : getBackground());
                          if(isRowSelected(row)) c.setBackground(getSelectionBackground()); // FIX: Use correct method
                         // Make sure sub-components are not opaque if they shouldn't be
                         for(Component comp : ((JPanel)c).getComponents()){
                             if(comp instanceof JLabel || comp instanceof JCheckBox){ // Or specific renderers
                                 ((JComponent)comp).setOpaque(false);
                             }
                         }
                     }
                     // Ensure non-opaque renderers get the right background when selected
                     if(isRowSelected(row) && !(c instanceof JPanel || c instanceof JTextArea)) { // FIX: Use correct method, Removed JTextArea check
                          c.setBackground(getSelectionBackground());
                           // Need to make opaque to show selection color for some renderers
                          if(c instanceof JLabel) {
                              ((JLabel)c).setOpaque(true);
                          }
                     } else if (!isRowSelected(row) && !(c instanceof JPanel || c instanceof JTextArea)) { // FIX: Use correct method, Removed JTextArea check
                         // Reset opaque state if needed when deselected
                         if (c instanceof TagRenderer) { // TagRenderer handles opaque itself
                            ((JLabel)c).setOpaque(false);
                         } else if (c instanceof JLabel) {
                             ((JLabel)c).setOpaque(false); // Assume default renderers aren't opaque
                         }
                     }

                     return c;
                 }
            }; // Assign to field
            table.setFillsViewportHeight(true);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Enable horizontal scrolling
            table.setAutoCreateRowSorter(true);

            // Apply modern header renderer
            table.getTableHeader().setDefaultRenderer(new ModernHeaderRenderer());


            // Set custom renderers
            table.setDefaultRenderer(Person.class, new PersonRenderer());
            table.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
                 public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int col) {
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                    // Add icon for the "Area" column
                    if (col == 0) {
                        // Get correct row from model after sorting
                        int modelRow = table.convertRowIndexToModel(row);
                        Icon icon = model.getAreaAt(modelRow).icon;
                        setIcon(icon != null ? icon : IconFactory.get("default"));
                        setIconTextGap(8);
                    } else {
                        setIcon(null);
                    }
                    setBorder(new EmptyBorder(0, 10, 0, 10)); // Added padding
                    return this;
                 }
            });
            table.setDefaultRenderer(List.class, new DefaultTableCellRenderer() {
                 public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int col) {
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                    List<Task> tasks = (List<Task>) value;
                    if (tasks != null && !tasks.isEmpty()) {
                        setText(tasks.stream().map(t -> t.name).collect(Collectors.joining(", ")));
                    } else {
                        setText("");
                    }
                    setBorder(new EmptyBorder(0, 10, 0, 10)); // Added padding
                    return this;
                 }
            });

            // Set custom editors
            table.setDefaultEditor(Person.class, new PersonEditor());

            // Set column widths
            TableColumnModel cm = table.getColumnModel();
            cm.getColumn(0).setPreferredWidth(200); // Area
            cm.getColumn(1).setPreferredWidth(200); // Owner
            cm.getColumn(2).setPreferredWidth(400); // Tasks
            cm.getColumn(3).setPreferredWidth(200); // Notes

            JScrollPane scrollPane = new JScrollPane(table);
            add(scrollPane, BorderLayout.SOUTH);
        }

        private void deleteSelectedAreas() {
            int[] selectedViewRows = table.getSelectedRows();
            if (selectedViewRows.length == 0) return;

            int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete " + selectedViewRows.length + " area(s)?\n(This will not delete associated tasks)",
                "Confirm Deletion",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (choice != JOptionPane.OK_OPTION) return;

            int[] modelRows = new int[selectedViewRows.length];
            for (int i = 0; i < selectedViewRows.length; i++) {
                modelRows[i] = table.convertRowIndexToModel(selectedViewRows[i]);
            }

            Arrays.sort(modelRows);

            for (int i = modelRows.length - 1; i >= 0; i--) {
                model.removeAreaAt(modelRows[i]);
            }
        }
    }

    /**
     * TableModel for the Areas list.
     */
    static class AreaTableModel extends AbstractTableModel {
        private final List<Area> areas;
        private final String[] columnNames = {"Area", "Owner", "Tasks", "Notes"};
        private final Class<?>[] columnClasses = {String.class, Person.class, List.class, String.class};

        public AreaTableModel(List<Area> areas) {
            this.areas = areas;
        }

        public void addNewArea() {
            Area newArea = new Area("New Area", null, IconFactory.get("default"));
            this.areas.add(newArea);
            fireTableRowsInserted(this.areas.size() - 1, this.areas.size() - 1);
        }

        public void removeAreaAt(int row) {
            if (row >= 0 && row < areas.size()) {
                areas.remove(row);
                fireTableRowsDeleted(row, row);
            }
        }

        public Area getAreaAt(int row) {
            if (row >= 0 && row < areas.size()) {
                return areas.get(row);
            }
            return null;
        }


        @Override public int getRowCount() { return areas.size(); }
        @Override public int getColumnCount() { return columnNames.length; }
        @Override public String getColumnName(int col) { return columnNames[col]; }
        @Override public Class<?> getColumnClass(int col) { return columnClasses[col]; }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col != 2; // "Tasks" is computed, not editable
        }

        @Override
        public Object getValueAt(int row, int col) {
            Area area = areas.get(row);
            switch (col) {
                case 0: return area.name;
                case 1: return area.owner;
                case 2: return area.tasks;
                case 3: return ""; // Notes column is empty in screenshot
                default: return null;
            }
        }

        @Override
        public void setValueAt(Object aValue, int row, int col) {
            Area area = areas.get(row);
            try {
                switch (col) {
                    case 0:
                        area.name = (String)aValue;
                        break;
                    case 1:
                        area.owner = (Person)aValue;
                        break;
                    // case 2 (Tasks) is not editable
                    case 3:
                        // We don't have a notes field, but we can fake it for the table
                        break;
                }
                fireTableCellUpdated(row, col);
            } catch (Exception e) {
                GlobalExceptionHandler.handle(e);
            }
        }
    }


    // --- ADDED: embed/show API so NotionDashboard can be used as a quick-start template ---
    /**
     * Create a JPanel that embeds the NotionDashboard UI.
     * Use this from other screens to show the dashboard inside a container.
     * @param preferredWidth preferred width (<=0 uses default)
     * @return JPanel containing the dashboard UI
     */
    public static JPanel createEmbeddedPanel(int preferredWidth) {
        MainAppPanel panel = new MainAppPanel();
        int w = preferredWidth > 0 ? preferredWidth : 1100;
        panel.setPreferredSize(new Dimension(w, 700));
        return panel;
    }

    /**
     * Show the NotionDashboard in its own JFrame (used by quick-start launchers).
     * This method matches the launch helpers in other templates.
     */
    public static void show() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Notion Dashboard");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(new MainAppPanel());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    // =============================================================================
    // == GLOBAL EXCEPTION HANDLER ==
    // =============================================================================

    /**
     * A global exception handler to catch uncaught Swing exceptions
     * and display them in a debug dialog.
     */
    static class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            handle(e);
        }

        public static void handle(Throwable e) {
            e.printStackTrace(); // Always print to console

            // Convert stack trace to string
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();

            // Create a scrollable text area for the stack trace
            JTextArea textArea = new JTextArea(stackTrace);
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 400));

            // Show the error in a dialog
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                    null,
                    scrollPane,
                    "An Unexpected Error Occurred",
                    JOptionPane.ERROR_MESSAGE
                );
            });
        }
    }
}