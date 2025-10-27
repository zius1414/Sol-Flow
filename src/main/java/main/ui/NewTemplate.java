/*
 * Compile: javac NewTemplate.java
 * Run:     java NewTemplate
 */

// Imports remain the same...
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A single-file Java Swing application that mimics a Notion-style
 * Work & Career Hub, built with a modern dark theme.
 * This application uses only the standard JDK (Java 11+) and Swing/AWT.
 * * It contains three main modules:
 * 1. JobSearchModule: Tracks job applications (Kanban, Grid, Table).
 * 2. CareerGoalsModule: Tracks career goals (Grid).
 * 3. ActionItemsModule: Tracks specific action items related to goals (Grid).
 * * @author Gemini (100x Java Engineer)
 */
public class NewTemplate {

    /**
     * Main entry point for the application.
     * Sets up the dark theme and creates the main frame.
     */
    public static void main(String[] args) {
        // UIManager setup remains the same...
        try {
            UIManager.put("Panel.background", Theme.BACKGROUND);
            UIManager.put("Label.foreground", Theme.TEXT);
            UIManager.put("Button.foreground", Theme.TEXT);
            UIManager.put("Button.background", Theme.CARD_BACKGROUND);
            UIManager.put("Button.border", new LineBorder(Theme.BORDER));
            UIManager.put("ToggleButton.foreground", Theme.TEXT);
            UIManager.put("ToggleButton.background", Theme.BACKGROUND);
            UIManager.put("ToggleButton.select", Theme.CARD_BACKGROUND); // Background when selected
            UIManager.put("ToggleButton.border", new EmptyBorder(5, 12, 5, 12));
            UIManager.put("ToggleButton.focus", new Color(0, 0, 0, 0)); // Disable focus paint
            UIManager.put("ScrollPane.background", Theme.BACKGROUND);
            UIManager.put("ScrollPane.border", new EmptyBorder(0, 0, 0, 0));
            UIManager.put("Viewport.background", Theme.BACKGROUND);
            UIManager.put("List.background", Theme.INPUT_BACKGROUND);
            UIManager.put("List.foreground", Theme.TEXT);
            UIManager.put("List.selectionBackground", Theme.PRIMARY_LIGHT);
            UIManager.put("List.selectionForeground", Theme.TEXT);
            UIManager.put("TextField.background", Theme.INPUT_BACKGROUND);
            UIManager.put("TextField.foreground", Theme.TEXT);
            UIManager.put("TextField.border", new CompoundBorder(new LineBorder(Theme.BORDER), new EmptyBorder(5, 5, 5, 5)));
            UIManager.put("TextField.caretForeground", Theme.TEXT);
            UIManager.put("ComboBox.background", Theme.INPUT_BACKGROUND);
            UIManager.put("ComboBox.foreground", Theme.TEXT);
            UIManager.put("ComboBox.border", new LineBorder(Theme.BORDER));
            UIManager.put("ComboBox.buttonBackground", Theme.CARD_BACKGROUND);
            UIManager.put("ComboBox.selectionBackground", Theme.PRIMARY);
            UIManager.put("ComboBox.selectionForeground", Theme.TEXT);
            UIManager.put("CheckBox.background", Theme.PANEL_BACKGROUND);
            UIManager.put("CheckBox.foreground", Theme.TEXT);
            UIManager.put("Slider.background", Theme.PANEL_BACKGROUND);
            UIManager.put("Slider.foreground", Theme.TEXT);
            UIManager.put("OptionPane.background", Theme.PANEL_BACKGROUND);
            UIManager.put("OptionPane.messageForeground", Theme.TEXT);
            UIManager.put("OptionPane.messageAreaBorder", new EmptyBorder(10, 10, 10, 10));
            UIManager.put("OptionPane.buttonAreaBorder", new EmptyBorder(10, 10, 10, 10));
            UIManager.put("Dialog.background", Theme.PANEL_BACKGROUND);
            UIManager.put("Table.background", Theme.BACKGROUND);
            UIManager.put("Table.foreground", Theme.TEXT);
            UIManager.put("Table.gridColor", Theme.BORDER);
            UIManager.put("Table.selectionBackground", Theme.PRIMARY_LIGHT);
            UIManager.put("Table.selectionForeground", Theme.TEXT);
            UIManager.put("Table.border", new EmptyBorder(0, 0, 0, 0));
            UIManager.put("Table.focusCellHighlightBorder", new LineBorder(Theme.PRIMARY));
            UIManager.put("TableHeader.background", Theme.BACKGROUND);
            UIManager.put("TableHeader.foreground", Theme.TEXT); // Brighter header text
            UIManager.put("TableHeader.border", new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, Theme.BORDER), // Bottom border
                new EmptyBorder(8, 8, 8, 8)));
            UIManager.put("PopupMenu.background", Theme.PANEL_BACKGROUND);
            UIManager.put("PopupMenu.border", new LineBorder(Theme.BORDER));
            UIManager.put("MenuItem.background", Theme.PANEL_BACKGROUND);
            UIManager.put("MenuItem.foreground", Theme.TEXT);
            UIManager.put("MenuItem.selectionBackground", Theme.CARD_BACKGROUND);
            UIManager.put("MenuItem.selectionForeground", Theme.TEXT);
            UIManager.put("MenuItem.border", new EmptyBorder(5, 10, 5, 10));

            // Set anti-aliasing for text
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");

        } catch (Exception e) {
            System.err.println("Error setting dark theme UIManager properties:");
            e.printStackTrace();
        }


        // Run the application on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Work & Career Hub");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1400, 900);
            frame.setLocationRelativeTo(null); // Center the window
            frame.getContentPane().setBackground(Theme.BACKGROUND);
            frame.setLayout(new BorderLayout());

            // Add the main application panel
            frame.add(new NewTemplatePanel(), BorderLayout.CENTER);

            frame.setVisible(true);
        });
    }

    // --- Inner Classes ---

    // Theme class remains the same...
    private static class Theme {
        // Colors
        static final Color BACKGROUND = new Color(0x19, 0x19, 0x19); // Very dark gray
        static final Color PANEL_BACKGROUND = new Color(0x20, 0x20, 0x20); // Slightly lighter
        static final Color CARD_BACKGROUND = new Color(0x2F, 0x2F, 0x2F); // Lighter gray for cards
        static final Color INPUT_BACKGROUND = new Color(0x26, 0x26, 0x26);
        static final Color BORDER = new Color(0x3A, 0x3A, 0x3A);
        static final Color TEXT = new Color(0xE0, 0xE0, 0xE0); // Light gray
        static final Color TEXT_MUTED = new Color(0x8A, 0x8A, 0x8A); // Medium gray
        static final Color PRIMARY = new Color(0x0A, 0x84, 0xFF); // Blue for buttons
        static final Color PRIMARY_LIGHT = new Color(0x3A, 0x9D, 0xFF, 50); // Lighter blue

        // Column/Tag Colors
        static final Color WISHLIST = new Color(0xE2, 0x4D, 0x9A);
        static final Color TO_APPLY = new Color(0xA0, 0x62, 0xD7);
        static final Color APPLIED = new Color(0xB3, 0x90, 0x31);
        static final Color INTERVIEW = new Color(0x3A, 0x89, 0xD3);
        static final Color OFFER = new Color(0x48, 0xA3, 0x6A);
        static final Color REJECTED = new Color(0xD9, 0x53, 0x4F);

        // Goal Colors
        static final Color GOAL_ONGOING = new Color(0x0A, 0x84, 0xFF);
        static final Color GOAL_CAREER = new Color(0xD9, 0x53, 0x4F);

        // Fonts - Increased base size
        static final Font SANS_SERIF = new Font("SansSerif", Font.PLAIN, 13); // Base size 13
        static final Font BOLD_FONT = SANS_SERIF.deriveFont(Font.BOLD);
        static final Font TITLE_FONT = SANS_SERIF.deriveFont(Font.BOLD, 22f); // 20f -> 22f
        static final Font HEADER_FONT = SANS_SERIF.deriveFont(Font.BOLD, 15f); // 14f -> 15f
        static final Font CARD_TITLE_FONT = SANS_SERIF.deriveFont(Font.BOLD, 14f); // 13f -> 14f
        static final Font BODY_FONT = SANS_SERIF.deriveFont(13f); // 12f -> 13f
        static final Font MUTED_FONT = SANS_SERIF.deriveFont(12f); // 11f -> 12f
        static final Font TABLE_HEADER_FONT = SANS_SERIF.deriveFont(Font.BOLD, 14f); // 11f -> 14f
    }


    // --- TOP LEVEL APP FRAME ---

    /**
     * The main application panel, organizing all sub-modules.
     */
    private static class NewTemplatePanel extends JPanel {
        private final CardLayout cardLayout = new CardLayout();
        private final JPanel cardPanel = new JPanel(cardLayout);
        private final BreadcrumbPanel breadcrumbPanel;

        // Data models shared across modules if needed, or instantiate within modules
        private final JobDataModel jobDataModel = new JobDataModel();
        private final GoalDataModel goalDataModel = new GoalDataModel();
        private final ActionItemDataModel actionItemDataModel = new ActionItemDataModel(); // New

        NewTemplatePanel() {
            super(new BorderLayout(0, 0));
            setBackground(Theme.BACKGROUND);

            // 1. Create Navigation / Breadcrumb
            breadcrumbPanel = new BreadcrumbPanel(this::showModule);
            add(breadcrumbPanel, BorderLayout.NORTH);

            // 2. Create Main Content Area (with CardLayout)
            JobSearchModule jobSearchModule = new JobSearchModule(jobDataModel);
            CareerGoalsModule careerGoalsModule = new CareerGoalsModule(goalDataModel);
            ActionItemsModule actionItemsModule = new ActionItemsModule(actionItemDataModel); // New

            cardPanel.setBackground(Theme.BACKGROUND);
            cardPanel.add(jobSearchModule, "JOB_SEARCH");
            cardPanel.add(careerGoalsModule, "CAREER_GOALS");
            cardPanel.add(actionItemsModule, "ACTION_ITEMS"); // New

            add(cardPanel, BorderLayout.CENTER);

            // Initial view
            showModule("JOB_SEARCH"); // Default to Job Search
        }

        /**
         * Switches the main CardLayout to show the selected module.
         * @param moduleName The name of the card to show.
         */
        void showModule(String moduleName) {
            cardLayout.show(cardPanel, moduleName);
            breadcrumbPanel.setActiveModule(moduleName);
        }
    }

    /**
     * Return a ready-to-embed panel for hosting inside other windows.
     * This gives external callers a public, supported way to embed the template
     * without using reflection to instantiate non-public inner classes.
     */
    public static JPanel createEmbeddedPanel() {
        return new NewTemplatePanel();
    }

    /**
     * A shared breadcrumb panel with navigation via popup menu.
     */
    private static class BreadcrumbPanel extends JPanel {
    private final JLabel activeModuleLabel = new JLabel("Sales");
        private final JPopupMenu navMenu = new JPopupMenu();
        private final java.util.function.Consumer<String> onNavigate;

        BreadcrumbPanel(java.util.function.Consumer<String> onNavigate) {
            super(new BorderLayout(10, 10));
            this.onNavigate = onNavigate;
            setBackground(Theme.BACKGROUND);
            setBorder(new EmptyBorder(15, 20, 10, 20));

            // Create navigation menu entries
            navMenu.add(createNavItem("Opportunities", "JOB_SEARCH"));
            navMenu.add(createNavItem("Clients", "CAREER_GOALS"));
            navMenu.add(createNavItem("Activity", "ACTION_ITEMS")); // New

            // Left side: Breadcrumbs
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
            activeModuleLabel.setForeground(Theme.TEXT); // Active page
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
                case "ACTION_ITEMS": activeModuleLabel.setText("Activity"); break; // New
            }
        }
    }


    // --- MODULE 1: OPPORTUNITIES / SALES ---
    // Business-oriented status enum (simple pipeline)
    private enum BusinessStatus {
        IN_PROGRESS("In Progress", Theme.PRIMARY),
        SCHEDULING("Scheduling", Theme.TO_APPLY),
        DONE("Done", Theme.OFFER);

        final String displayName;
        final Color color;

        BusinessStatus(String n, Color c) { displayName = n; color = c; }

        @Override public String toString() { return displayName; }
    }
    private enum LocationType { REMOTE("Remote", Theme.TEXT_MUTED), HYBRID("Hybrid", Theme.TEXT_MUTED), ON_SITE("On-Site", Theme.TEXT_MUTED); final String displayName; final Color color; LocationType(String n, Color c){displayName=n; color=c;} @Override public String toString(){return displayName;}}
    private enum EmploymentType { FULL_TIME("Full Time"), PART_TIME("Part Time"), CONTRACT("Contract"), INTERNSHIP("Internship"); final String displayName; EmploymentType(String n){displayName=n;} @Override public String toString(){return displayName;}}
    private enum PayType { SALARY("Salary"), HOURLY("Hourly"), STIPEND("Stipend"); final String displayName; PayType(String n){displayName=n;} @Override public String toString(){return displayName;}}
    // (Opportunity model adapted from JobApplication)
    private static class JobApplication {
        String id;
        String title, company, companyIconUrl, industry;
        EmploymentType employmentType;
        LocationType locationType;
        PayType payType;
        double compensation;
        String vacation, city, url, hiringManager, email;
        boolean healthBenefits, stockOptions, needsFollowUp;
        BusinessStatus status;
        LocalDate applicationDate, lastContactDate;
        int interest;

        JobApplication(String title, String company, BusinessStatus status, String industry, EmploymentType employmentType, LocationType locationType, PayType payType, double compensation, String vacation, boolean healthBenefits, boolean stockOptions, String city, String url, String hiringManager, String email, LocalDate applicationDate, LocalDate lastContactDate, int interest, boolean needsFollowUp) {
            this.id = java.util.UUID.randomUUID().toString();
            this.title = title;
            this.company = company;
            this.companyIconUrl = company.isEmpty() ? "?" : company.substring(0, 1);
            this.status = status;
            this.industry = industry;
            this.employmentType = employmentType;
            this.locationType = locationType;
            this.payType = payType;
            this.compensation = compensation;
            this.vacation = vacation;
            this.healthBenefits = healthBenefits;
            this.stockOptions = stockOptions;
            this.city = city;
            this.url = url;
            this.hiringManager = hiringManager;
            this.email = email;
            this.applicationDate = applicationDate;
            this.lastContactDate = lastContactDate;
            this.interest = interest;
            this.needsFollowUp = needsFollowUp;
        }

        JobApplication(String title, String company, double compensation, BusinessStatus status, LocationType locationType) {
            this(title, company, status, "", EmploymentType.FULL_TIME, locationType, PayType.SALARY, compensation, "Empty", false, false, "", "", "N/A", "N/A", null, null, 0, false);
        }
    }
    // (JobDataModel class remains the same)
    private static class JobDataModel { private final List<JobApplication> jobs = new ArrayList<>(); private final List<Runnable> updateListeners = new ArrayList<>(); public JobDataModel(){
        jobs.add(new JobApplication("Acme Corp - Q4 Renewal","Acme Corp",BusinessStatus.IN_PROGRESS,"Technology",EmploymentType.FULL_TIME,LocationType.REMOTE,PayType.SALARY,121500,"N/A",true,false,"Remote","acme.com","Sophia Wu","sophia.wu@acme.com",null,null,2,false));
        jobs.add(new JobApplication("Global Retail Lead","RetailCo",BusinessStatus.SCHEDULING,"Retail",EmploymentType.FULL_TIME,LocationType.HYBRID,PayType.SALARY,220000,"N/A",true,true,"Palo Alto, CA","retailco.com","Layla Khan","layla.khan@retailco.com",LocalDate.of(2025,12,1),LocalDate.of(2025,12,1),4,true));
        jobs.add(new JobApplication("Shopify Expansion","Shopify",BusinessStatus.SCHEDULING,"E-commerce",EmploymentType.FULL_TIME,LocationType.REMOTE,PayType.SALARY,140500,"N/A",true,true,"Remote","shopify.com","Daphne Wong","daphne.wong@shopify.com",null,null,3,false));
        jobs.add(new JobApplication("Dropbox Partnership","Dropbox",BusinessStatus.IN_PROGRESS,"Technology",EmploymentType.FULL_TIME,LocationType.REMOTE,PayType.SALARY,119000,"N/A",true,true,"Remote","dropbox.com","Darlene Lowe","darlene.lowe@dropbox.com",LocalDate.of(2026,1,6),null,3,true));
        jobs.add(new JobApplication("Google Strategic","Google",BusinessStatus.SCHEDULING,"Technology",EmploymentType.FULL_TIME,LocationType.REMOTE,PayType.SALARY,150000,"N/A",true,true,"Mountain View, CA","google.com","Marisol Brown","marisol.brown@google.com",LocalDate.of(2025,12,8),LocalDate.of(2025,12,22),4,true));
        jobs.add(new JobApplication("Descript Campaign","Descript",BusinessStatus.DONE,"Media",EmploymentType.FULL_TIME,LocationType.ON_SITE,PayType.SALARY,190000,"N/A",true,false,"San Francisco, CA","descript.com","Levi Lee","levi.lee@descript.com",LocalDate.of(2025,12,2),LocalDate.of(2025,12,2),3,false));
        jobs.add(new JobApplication("Figma Outreach","Figma",BusinessStatus.DONE,"Design",EmploymentType.FULL_TIME,LocationType.REMOTE,PayType.SALARY,135000,"N/A",true,true,"Remote","figma.com","Kenji Tanaka","kenji.tanaka@figma.com",LocalDate.of(2025,11,20),LocalDate.of(2025,11,30),2,false));
    } public List<JobApplication> getAllJobs(){return new ArrayList<>(jobs);} public JobApplication getJobAt(int index){return(index>=0&&index<jobs.size())?jobs.get(index):null;} public void addJob(JobApplication job){jobs.add(job); notifyListeners();} public void updateJob(JobApplication updatedJob){for(int i=0;i<jobs.size();i++){if(jobs.get(i).id.equals(updatedJob.id)){jobs.set(i,updatedJob);break;}} notifyListeners();} public void addUpdateListener(Runnable listener){updateListeners.add(listener);} private void notifyListeners(){for(Runnable listener:updateListeners){listener.run();}}}
    // (JobSearchModule, StatusBoardView, FollowUpView, RolesView, JobTableModel, Renderers, JobCard, JobFormDialog classes remain the same...)
    private static class JobSearchModule extends JPanel { /* ... Same as previous version ... */ private final CardLayout cardLayout = new CardLayout(); private final JPanel cardPanel = new JPanel(cardLayout); private final JobDataModel dataModel; private StatusBoardView statusBoardView; private FollowUpView followUpView; private RolesView rolesView; private final JFrame topFrame; JobSearchModule(JobDataModel dataModel){ super(new BorderLayout(0,0)); setBackground(Theme.BACKGROUND); this.dataModel=dataModel; this.topFrame=(JFrame)SwingUtilities.getWindowAncestor(this); add(createHeaderPanel(),BorderLayout.NORTH); statusBoardView=new StatusBoardView(dataModel,this::showJobDialog); followUpView=new FollowUpView(dataModel,this::showJobDialog); rolesView=new RolesView(dataModel,this::showJobDialog); cardPanel.setBackground(Theme.BACKGROUND); cardPanel.add(statusBoardView,"STATUS_BOARD"); cardPanel.add(followUpView,"FOLLOW_UP"); cardPanel.add(rolesView,"ROLES"); add(cardPanel,BorderLayout.CENTER); dataModel.addUpdateListener(this::refreshViews); cardLayout.show(cardPanel,"STATUS_BOARD");} private JPanel createHeaderPanel(){JPanel headerPanel=new JPanel(new BorderLayout(10,10)); headerPanel.setBackground(Theme.BACKGROUND); headerPanel.setBorder(new EmptyBorder(10,20,15,20)); JLabel titleLabel=new JLabel("Sales Overview"); titleLabel.setFont(Theme.TITLE_FONT); titleLabel.setForeground(Theme.TEXT); headerPanel.add(titleLabel,BorderLayout.WEST); JPanel controlsPanel=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0)); controlsPanel.setOpaque(false); JButton newButton=new JButton("New Opportunity"); newButton.setBackground(Theme.PRIMARY); newButton.setForeground(Color.WHITE); newButton.setFont(Theme.BOLD_FONT); newButton.setBorder(new EmptyBorder(8,15,8,15)); newButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); newButton.addActionListener(e -> showJobDialog(null)); controlsPanel.add(newButton); headerPanel.add(controlsPanel,BorderLayout.EAST); headerPanel.add(createViewSwitcherPanel(),BorderLayout.SOUTH); return headerPanel;} private JPanel createViewSwitcherPanel(){JPanel switcherPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); switcherPanel.setOpaque(false); switcherPanel.setBorder(new EmptyBorder(10,0,5,0)); JToggleButton statusButton=createViewToggleButton("Pipeline"); JToggleButton followupButton=createViewToggleButton("Follow-Ups"); JToggleButton rolesButton=createViewToggleButton("Clients"); ButtonGroup group=new ButtonGroup(); group.add(statusButton); group.add(followupButton); group.add(rolesButton); statusButton.setSelected(true); statusButton.addActionListener(e -> cardLayout.show(cardPanel,"STATUS_BOARD")); followupButton.addActionListener(e -> cardLayout.show(cardPanel,"FOLLOW_UP")); rolesButton.addActionListener(e -> cardLayout.show(cardPanel,"ROLES")); switcherPanel.add(statusButton); switcherPanel.add(followupButton); switcherPanel.add(rolesButton); return switcherPanel;} private JToggleButton createViewToggleButton(String text){JToggleButton button=new JToggleButton(text){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); if(isSelected()){g2.setColor(Theme.PRIMARY); g2.fillRect(0,getHeight()-3,getWidth(),3); setForeground(Theme.TEXT);} else{setForeground(Theme.TEXT_MUTED);} g2.dispose(); super.paintComponent(g);}}; button.setFont(Theme.BOLD_FONT.deriveFont(14f)); button.setOpaque(false); button.setBorderPainted(false); button.setContentAreaFilled(false); button.setFocusPainted(false); button.setBorder(new EmptyBorder(8,12,8,12)); button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return button;} private void showJobDialog(JobApplication job){JFrame parentFrame=(JFrame)SwingUtilities.getWindowAncestor(this); JobFormDialog dialog=new JobFormDialog(parentFrame,dataModel,job); dialog.setVisible(true);} private void refreshViews(){statusBoardView.refreshView(); followUpView.refreshView(); rolesView.refreshView();}}
    private static class StatusBoardView extends JPanel { /* ... Same as previous version ... */ private final JobDataModel dataModel; private final JPanel columnsPanel; private final Map<BusinessStatus,JPanel> cardListPanels = new HashMap<>(); private final java.util.function.Consumer<JobApplication> onCardClick; StatusBoardView(JobDataModel model, java.util.function.Consumer<JobApplication> onCardClick){ super(new BorderLayout()); this.dataModel=model; this.onCardClick=onCardClick; setBackground(Theme.BACKGROUND); columnsPanel=new JPanel(); columnsPanel.setLayout(new BoxLayout(columnsPanel,BoxLayout.X_AXIS)); columnsPanel.setBackground(Theme.BACKGROUND); columnsPanel.setBorder(new EmptyBorder(0,20,0,20)); for(BusinessStatus status:BusinessStatus.values()){columnsPanel.add(createColumnPanel(status)); columnsPanel.add(Box.createHorizontalStrut(10));} JScrollPane scrollPane=new JScrollPane(columnsPanel); scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER); scrollPane.setBorder(null); scrollPane.getHorizontalScrollBar().setUnitIncrement(16); scrollPane.getHorizontalScrollBar().setUI(new CustomScrollBarUI()); scrollPane.getHorizontalScrollBar().setBackground(Theme.BACKGROUND); add(scrollPane,BorderLayout.CENTER); refreshView();} private JPanel createColumnPanel(BusinessStatus status){JPanel columnPanel=new JPanel(new BorderLayout(0,10)); columnPanel.setBackground(Theme.PANEL_BACKGROUND); columnPanel.setBorder(new EmptyBorder(10,10,10,10)); columnPanel.setMinimumSize(new Dimension(280,200)); columnPanel.setPreferredSize(new Dimension(280,200)); columnPanel.setMaximumSize(new Dimension(280,Integer.MAX_VALUE)); JPanel headerPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); headerPanel.setOpaque(false); headerPanel.add(new ColorDotLabel(status.color)); JLabel titleLabel=new JLabel(status.displayName); titleLabel.setFont(Theme.HEADER_FONT); titleLabel.setForeground(Theme.TEXT); headerPanel.add(titleLabel); columnPanel.add(headerPanel,BorderLayout.NORTH); JPanel cardList=new JPanel(); cardList.setLayout(new BoxLayout(cardList,BoxLayout.Y_AXIS)); cardList.setOpaque(false); cardListPanels.put(status,cardList); JPanel cardListWrapper=new JPanel(new BorderLayout()); cardListWrapper.setOpaque(false); cardListWrapper.add(cardList,BorderLayout.NORTH); JScrollPane cardScrollPane=new JScrollPane(cardListWrapper); cardScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); cardScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); cardScrollPane.setBorder(null); cardScrollPane.getVerticalScrollBar().setUnitIncrement(12); cardScrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI()); cardScrollPane.getVerticalScrollBar().setBackground(Theme.PANEL_BACKGROUND); cardScrollPane.getViewport().setBackground(Theme.PANEL_BACKGROUND); columnPanel.add(cardScrollPane,BorderLayout.CENTER); JButton newCardButton=new FlatButton("+ New page"); newCardButton.setHorizontalAlignment(SwingConstants.LEFT); newCardButton.addActionListener(e -> {JobApplication newJob=new JobApplication("","",0,status,LocationType.REMOTE); onCardClick.accept(newJob);}); columnPanel.add(newCardButton,BorderLayout.SOUTH); return columnPanel;} void refreshView(){for(JPanel panel:cardListPanels.values()){panel.removeAll();} List<JobApplication> jobs=dataModel.getAllJobs(); for(JobApplication job:jobs){JPanel cardList=cardListPanels.get(job.status); if(cardList!=null){cardList.add(new JobCard(job,onCardClick,false)); cardList.add(Box.createVerticalStrut(10));}} revalidate(); repaint();}}
    private static class FollowUpView extends JPanel { /* ... Same as previous version ... */ private final JobDataModel dataModel; private final JPanel gridPanel; private final java.util.function.Consumer<JobApplication> onCardClick; FollowUpView(JobDataModel model, java.util.function.Consumer<JobApplication> onCardClick){ super(new BorderLayout()); this.dataModel=model; this.onCardClick=onCardClick; setBackground(Theme.BACKGROUND); gridPanel=new JPanel(new GridBagLayout()); gridPanel.setBackground(Theme.BACKGROUND); gridPanel.setBorder(new EmptyBorder(10,20,10,20)); JScrollPane scrollPane=new JScrollPane(gridPanel); scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); scrollPane.setBorder(null); scrollPane.getVerticalScrollBar().setUnitIncrement(16); scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI()); scrollPane.getVerticalScrollBar().setBackground(Theme.BACKGROUND); scrollPane.getViewport().setBackground(Theme.BACKGROUND); add(scrollPane,BorderLayout.CENTER); addComponentListener(new ComponentAdapter(){@Override public void componentResized(ComponentEvent e){refreshView();}}); refreshView();} void refreshView(){gridPanel.removeAll(); int panelWidth=getWidth(); int columns=Math.max(1,(panelWidth-40)/320); GridBagConstraints gbc=new GridBagConstraints(); gbc.insets=new Insets(10,10,10,10); gbc.fill=GridBagConstraints.HORIZONTAL; gbc.anchor=GridBagConstraints.NORTH; gbc.weightx=1.0; List<JobApplication> jobs=dataModel.getAllJobs(); int row=0, col=0; for(JobApplication job:jobs){gbc.gridx=col; gbc.gridy=row; gridPanel.add(new JobCard(job,onCardClick,true),gbc); col++; if(col>=columns){col=0; row++;}} gbc.gridx=0; gbc.gridy=row+1; gbc.gridwidth=columns; gbc.weighty=1.0; gbc.fill=GridBagConstraints.BOTH; gridPanel.add(Box.createGlue(),gbc); revalidate(); repaint();}}
    private static class RolesView extends JPanel { /* ... Same as previous version ... */ private final JobDataModel dataModel; private final JobTableModel tableModel; private final JTable table; private final java.util.function.Consumer<JobApplication> onEditJob; RolesView(JobDataModel model, java.util.function.Consumer<JobApplication> onEditJob){ super(new BorderLayout(0,0)); this.dataModel=model; this.onEditJob=onEditJob; setBackground(Theme.BACKGROUND); setBorder(new EmptyBorder(0,20,10,20)); tableModel=new JobTableModel(dataModel); table=new JTable(tableModel); table.setRowHeight(48); table.setShowGrid(true); table.setShowVerticalLines(false); table.setShowHorizontalLines(true); table.setGridColor(Theme.TEXT_MUTED); table.setIntercellSpacing(new Dimension(0,0)); table.setFillsViewportHeight(true); table.setDefaultRenderer(Object.class,new RolesCellRenderer()); table.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer()); table.getColumnModel().getColumn(5).setCellRenderer(new InterestCellRenderer()); table.getColumnModel().getColumn(0).setPreferredWidth(200); table.getColumnModel().getColumn(1).setPreferredWidth(100); table.getColumnModel().getColumn(2).setPreferredWidth(100); table.getColumnModel().getColumn(3).setPreferredWidth(80); table.getColumnModel().getColumn(4).setPreferredWidth(100); table.getColumnModel().getColumn(5).setPreferredWidth(80); JTableHeader header=table.getTableHeader(); header.setDefaultRenderer(new HeaderCellRenderer(header.getDefaultRenderer())); header.setFont(Theme.TABLE_HEADER_FONT); header.setReorderingAllowed(false); JScrollPane scrollPane=new JScrollPane(table); scrollPane.setBorder(new MatteBorder(1,0,0,0,Theme.BORDER)); scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI()); scrollPane.getHorizontalScrollBar().setUI(new CustomScrollBarUI()); scrollPane.getViewport().setBackground(Theme.BACKGROUND); add(scrollPane,BorderLayout.CENTER); JButton newCardButton=new FlatButton("+ New page"); newCardButton.setHorizontalAlignment(SwingConstants.LEFT); newCardButton.setBorder(new EmptyBorder(10,0,0,0)); newCardButton.addActionListener(e -> onEditJob.accept(null)); add(newCardButton,BorderLayout.SOUTH); table.addMouseListener(new MouseAdapter(){public void mouseClicked(MouseEvent e){if(e.getClickCount()==2){int row=table.convertRowIndexToModel(table.getSelectedRow()); if(row>=0){onEditJob.accept(dataModel.getJobAt(row));}}}});} void refreshView(){tableModel.fireTableDataChanged();}}
    private static class JobTableModel extends AbstractTableModel { /* ... Same as previous version ... */ private final JobDataModel dataModel; private final String[] columnNames={"Position","Company","Compensation","Pay Type","Status","Interest"}; JobTableModel(JobDataModel model){this.dataModel=model; dataModel.addUpdateListener(this::fireTableDataChanged);} @Override public int getRowCount(){return dataModel.getAllJobs().size();} @Override public int getColumnCount(){return columnNames.length;} @Override public String getColumnName(int column){return columnNames[column];} @Override public Object getValueAt(int rowIndex,int columnIndex){JobApplication job=dataModel.getAllJobs().get(rowIndex); switch(columnIndex){case 0:return job;case 1:return job;case 2:return job.compensation;case 3:return job.payType;case 4:return job.status;case 5:return job.interest;default:return null;}} @Override public Class<?> getColumnClass(int columnIndex){if(columnIndex==0||columnIndex==1)return JobApplication.class; if(columnIndex==2)return Double.class; if(columnIndex==3)return PayType.class; if(columnIndex==4)return BusinessStatus.class; if(columnIndex==5)return Integer.class; return Object.class;}}
    // RolesCellRenderer UPDATED with padding for Compensation
    private static class RolesCellRenderer extends DefaultTableCellRenderer { private CompanyIcon companyIcon=new CompanyIcon("?"); private IconOnlyButton docIcon=new IconOnlyButton(IconType.DOCUMENT); private JPanel iconPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); private final Border defaultPadding=new EmptyBorder(8,10,8,10); private final Border compensationPadding=new EmptyBorder(8,10,8,15); RolesCellRenderer(){super(); setOpaque(true); setBorder(defaultPadding); iconPanel.setOpaque(false); docIcon.setPreferredSize(new Dimension(18,18));} @Override public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column){setFont(Theme.BODY_FONT); setHorizontalAlignment(SwingConstants.LEFT); setBorder(defaultPadding); setText(""); iconPanel.removeAll(); if(isSelected){setBackground(Theme.PRIMARY_LIGHT); setForeground(Theme.TEXT);} else{setBackground(Theme.BACKGROUND); setForeground(Theme.TEXT);} if(value instanceof JobApplication){JobApplication job=(JobApplication)value; if(column==0){setText(job.title); iconPanel.add(docIcon); iconPanel.add(this); iconPanel.setBackground(getBackground()); docIcon.setHover(isSelected); return iconPanel;} else if(column==1){setText(job.company); companyIcon.setInitial(job.companyIconUrl); iconPanel.add(companyIcon); iconPanel.add(this); iconPanel.setBackground(getBackground()); return iconPanel;}} else if(value instanceof Double){NumberFormat currencyFormatter=NumberFormat.getCurrencyInstance(Locale.US); currencyFormatter.setMaximumFractionDigits(0); setText(currencyFormatter.format(value)); setHorizontalAlignment(SwingConstants.RIGHT); setBorder(compensationPadding);} else if(value!=null){setText(value.toString());} return this;}}
    private static class StatusCellRenderer implements TableCellRenderer { /* ... Same as previous version ... */ private TagLabel tag=new TagLabel("",Theme.BACKGROUND); @Override public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column){if(value instanceof BusinessStatus){BusinessStatus status=(BusinessStatus)value; tag.setText(status.displayName); tag.setColors(status.color,status.color.brighter());} JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); panel.setBackground(isSelected?Theme.PRIMARY_LIGHT:Theme.BACKGROUND); panel.setBorder(new EmptyBorder(10,10,10,10)); panel.add(tag); return panel;}}
    private static class InterestCellRenderer extends JPanel implements TableCellRenderer { /* ... Same as previous version ... */ private int interestLevel=0; InterestCellRenderer(){super(); setOpaque(true); setBorder(new EmptyBorder(14,10,14,10));} @Override public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column){if(value instanceof Integer)this.interestLevel=(Integer)value; setBackground(isSelected?Theme.PRIMARY_LIGHT:Theme.BACKGROUND); return this;} @Override protected void paintComponent(Graphics g){super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); int w=getWidth()-20; int h=getHeight()-28; int barWidth=(w-(3*3))/4; int x=10; for(int i=0;i<4;i++){g2.setColor(i<interestLevel?Theme.TEXT:Theme.BORDER); g2.fill(new RoundRectangle2D.Float(x,14,barWidth,h,4,4)); x+=(barWidth+3);} g2.dispose();}}
    private static class HeaderCellRenderer implements TableCellRenderer { /* ... Same as previous version ... */ private TableCellRenderer defaultRenderer; HeaderCellRenderer(TableCellRenderer defaultRenderer){this.defaultRenderer=defaultRenderer;} @Override public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column){Component c=defaultRenderer.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column); if(c instanceof JLabel){JLabel label=(JLabel)c; JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT,5,0)); panel.setOpaque(false); IconType iconType=null; switch(column){case 0:iconType=IconType.POSITION;break; case 1:iconType=IconType.COMPANY;break; case 2:iconType=IconType.COMPENSATION;break; case 3:iconType=IconType.PAY_TYPE;break; case 4:iconType=IconType.STATUS;break; case 5:iconType=IconType.INTEREST;break;} if(iconType!=null){IconOnlyButton icon=new IconOnlyButton(iconType); icon.setPreferredSize(new Dimension(16,16)); panel.add(icon);} label.setText(value.toString()); label.setFont(Theme.TABLE_HEADER_FONT); panel.add(label); return panel;} return c;}}
    private static class JobCard extends RoundedPanel { /* ... Same as previous version ... */ JobCard(JobApplication job, java.util.function.Consumer<JobApplication> onCardClick, boolean isGridView){ super(new BorderLayout(),8,Theme.CARD_BACKGROUND); setBorder(new EmptyBorder(12,12,12,12)); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); JPanel content=new JPanel(); content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS)); content.setOpaque(false); JLabel titleLabel=new JLabel(job.title); titleLabel.setFont(Theme.CARD_TITLE_FONT); titleLabel.setForeground(Theme.TEXT); content.add(titleLabel); content.add(Box.createVerticalStrut(5)); JPanel companyPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,5,0)); companyPanel.setOpaque(false); companyPanel.add(new CompanyIcon(job.companyIconUrl)); JLabel companyLabel=new JLabel(job.company); companyLabel.setFont(Theme.BODY_FONT); companyLabel.setForeground(Theme.TEXT_MUTED); companyPanel.add(companyLabel); content.add(companyPanel); content.add(Box.createVerticalStrut(8)); JPanel detailsPanel=new JPanel(new GridBagLayout()); detailsPanel.setOpaque(false); GridBagConstraints gbc=new GridBagConstraints(); gbc.anchor=GridBagConstraints.WEST; gbc.insets=new Insets(2,0,2,10); gbc.gridx=0; gbc.gridy=0; java.util.function.BiConsumer<String,String> addDetail=(key,value) -> {if(value==null||value.equals("N/A")||value.isEmpty())return; JLabel keyLabel=new JLabel(key); keyLabel.setFont(Theme.MUTED_FONT); keyLabel.setForeground(Theme.TEXT_MUTED); gbc.gridx=0; gbc.weightx=0; detailsPanel.add(keyLabel,gbc); JLabel valueLabel=new JLabel(value); valueLabel.setFont(Theme.BODY_FONT); valueLabel.setForeground(Theme.TEXT); gbc.gridx=1; gbc.weightx=1; detailsPanel.add(valueLabel,gbc); gbc.gridy++;}; DateTimeFormatter formatter=DateTimeFormatter.ofPattern("MMM d, yyyy"); NumberFormat currencyFormatter=NumberFormat.getCurrencyInstance(Locale.US); currencyFormatter.setMaximumFractionDigits(0); addDetail.accept("Salary:",currencyFormatter.format(job.compensation)); addDetail.accept("Hiring Manager:",job.hiringManager); if(isGridView){addDetail.accept("Email:",job.email); if(job.applicationDate!=null)addDetail.accept("Applied:",job.applicationDate.format(formatter)); if(job.lastContactDate!=null)addDetail.accept("Last Contact:",job.lastContactDate.format(formatter));} else{if(job.applicationDate!=null)addDetail.accept("Applied:",job.applicationDate.format(formatter));} content.add(detailsPanel); JPanel footerPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,5,0)); footerPanel.setOpaque(false); footerPanel.setBorder(new EmptyBorder(8,0,0,0)); if(isGridView){footerPanel.add(new TagLabel(job.status.displayName,job.status.color));} if(job.locationType!=null){footerPanel.add(new TagLabel(job.locationType.displayName,Theme.CARD_BACKGROUND));} content.add(footerPanel); add(content,BorderLayout.CENTER); addMouseListener(new MouseAdapter(){@Override public void mouseClicked(MouseEvent e){onCardClick.accept(job);}});}}
    private static class JobFormDialog extends JDialog { /* ... Same as previous version ... */ private final JobDataModel dataModel; private final JobApplication currentJob; private final boolean isEditing; private JTextField titleField, companyField, industryField, compensationField, vacationField, cityField, urlField, managerField, emailField, applicationDateField, lastContactField; private JComboBox<BusinessStatus> statusBox; private JComboBox<EmploymentType> employmentTypeBox; private JComboBox<LocationType> locationTypeBox; private JComboBox<PayType> payTypeBox; private JCheckBox healthBenefitsBox, stockOptionsBox, needsFollowUpBox; private JSlider interestSlider; JobFormDialog(JFrame owner, JobDataModel model, JobApplication job){ super(owner,"Opportunity",true); this.dataModel=model; if(job==null){this.currentJob=new JobApplication("","",0,BusinessStatus.SCHEDULING,LocationType.REMOTE); this.isEditing=false; setTitle("Add New Opportunity");} else{this.currentJob=job; this.isEditing=true; setTitle("Edit Opportunity");} initUI(); populateForm(); setSize(new Dimension(600,800)); setResizable(false); setLocationRelativeTo(owner);} private void initUI(){JPanel formPanel=new JPanel(new GridBagLayout()); formPanel.setBackground(Theme.PANEL_BACKGROUND); formPanel.setBorder(new EmptyBorder(20,20,20,20)); GridBagConstraints gbc=new GridBagConstraints(); gbc.insets=new Insets(8,5,8,5); gbc.anchor=GridBagConstraints.WEST; java.util.function.BiConsumer<String,JComponent> addRow=(label,component) -> {gbc.gridx=0; gbc.weightx=0; gbc.fill=GridBagConstraints.NONE; JLabel l=new JLabel(label); l.setFont(Theme.BODY_FONT); l.setForeground(Theme.TEXT_MUTED); formPanel.add(l,gbc); gbc.gridx=1; gbc.weightx=1; gbc.fill=GridBagConstraints.HORIZONTAL; formPanel.add(component,gbc); gbc.gridy++;}; gbc.gridy=0; titleField=new JTextField(25); addRow.accept("Title:",titleField); companyField=new JTextField(); addRow.accept("Company:",companyField); statusBox=new JComboBox<>(BusinessStatus.values()); addRow.accept("Status:",statusBox); industryField=new JTextField(); addRow.accept("Industry:",industryField); employmentTypeBox=new JComboBox<>(EmploymentType.values()); addRow.accept("Employment Type:",employmentTypeBox); locationTypeBox=new JComboBox<>(LocationType.values()); addRow.accept("Location:",locationTypeBox); payTypeBox=new JComboBox<>(PayType.values()); addRow.accept("Pay Type:",payTypeBox); compensationField=new JTextField(); addRow.accept("Compensation:",compensationField); vacationField=new JTextField(); addRow.accept("Vacation:",vacationField); healthBenefitsBox=new JCheckBox("Health Benefits"); healthBenefitsBox.setFont(Theme.BODY_FONT); gbc.gridx=1; gbc.weightx=1; gbc.fill=GridBagConstraints.HORIZONTAL; formPanel.add(healthBenefitsBox,gbc); gbc.gridy++; stockOptionsBox=new JCheckBox("Stock Options"); stockOptionsBox.setFont(Theme.BODY_FONT); gbc.gridx=1; gbc.weightx=1; gbc.fill=GridBagConstraints.HORIZONTAL; formPanel.add(stockOptionsBox,gbc); gbc.gridy++; cityField=new JTextField(); addRow.accept("City:",cityField); urlField=new JTextField(); addRow.accept("URL:",urlField); managerField=new JTextField(); addRow.accept("Hiring Manager:",managerField); emailField=new JTextField(); addRow.accept("Email:",emailField); applicationDateField=new JTextField(); addRow.accept("Application Date (YYYY-MM-DD):",applicationDateField); lastContactField=new JTextField(); addRow.accept("Last Contact (YYYY-MM-DD):",lastContactField); interestSlider=new JSlider(0,4,0); interestSlider.setMajorTickSpacing(1); interestSlider.setPaintTicks(true); interestSlider.setPaintLabels(true); interestSlider.setSnapToTicks(true); addRow.accept("Interest:",interestSlider); needsFollowUpBox=new JCheckBox("Needs Follow-up"); needsFollowUpBox.setFont(Theme.BODY_FONT); gbc.gridx=1; gbc.weightx=1; gbc.fill=GridBagConstraints.HORIZONTAL; formPanel.add(needsFollowUpBox,gbc); gbc.gridy++; JPanel buttonPanel=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0)); buttonPanel.setOpaque(false); JButton cancelButton=new JButton("Cancel"); cancelButton.setFont(Theme.BOLD_FONT); cancelButton.addActionListener(e -> dispose()); buttonPanel.add(cancelButton); JButton saveButton=new JButton("Save"); saveButton.setFont(Theme.BOLD_FONT); saveButton.setBackground(Theme.PRIMARY); saveButton.setForeground(Color.WHITE); saveButton.addActionListener(e -> saveJob()); buttonPanel.add(saveButton); gbc.gridx=0; gbc.gridwidth=2; gbc.anchor=GridBagConstraints.EAST; gbc.fill=GridBagConstraints.NONE; gbc.insets=new Insets(20,0,0,0); formPanel.add(buttonPanel,gbc); JScrollPane scrollPane=new JScrollPane(formPanel); scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); scrollPane.setBorder(null); scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI()); scrollPane.getVerticalScrollBar().setBackground(Theme.PANEL_BACKGROUND); scrollPane.getViewport().setBackground(Theme.PANEL_BACKGROUND); getContentPane().add(scrollPane,BorderLayout.CENTER); getContentPane().setBackground(Theme.PANEL_BACKGROUND);} private void populateForm(){titleField.setText(currentJob.title); companyField.setText(currentJob.company); statusBox.setSelectedItem(currentJob.status); industryField.setText(currentJob.industry); employmentTypeBox.setSelectedItem(currentJob.employmentType); locationTypeBox.setSelectedItem(currentJob.locationType); payTypeBox.setSelectedItem(currentJob.payType); compensationField.setText(String.valueOf(currentJob.compensation)); vacationField.setText(currentJob.vacation); healthBenefitsBox.setSelected(currentJob.healthBenefits); stockOptionsBox.setSelected(currentJob.stockOptions); cityField.setText(currentJob.city); urlField.setText(currentJob.url); managerField.setText(currentJob.hiringManager); emailField.setText(currentJob.email); DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd"); if(currentJob.applicationDate!=null)applicationDateField.setText(currentJob.applicationDate.format(formatter)); if(currentJob.lastContactDate!=null)lastContactField.setText(currentJob.lastContactDate.format(formatter)); interestSlider.setValue(currentJob.interest); needsFollowUpBox.setSelected(currentJob.needsFollowUp);} private void saveJob(){try{currentJob.title=titleField.getText(); currentJob.company=companyField.getText(); currentJob.companyIconUrl=currentJob.company.isEmpty()?"?":currentJob.company.substring(0,1); currentJob.status=(BusinessStatus)Objects.requireNonNull(statusBox.getSelectedItem()); currentJob.industry=industryField.getText(); currentJob.employmentType=(EmploymentType)Objects.requireNonNull(employmentTypeBox.getSelectedItem()); currentJob.locationType=(LocationType)Objects.requireNonNull(locationTypeBox.getSelectedItem()); currentJob.payType=(PayType)Objects.requireNonNull(payTypeBox.getSelectedItem()); currentJob.compensation=Double.parseDouble(compensationField.getText()); currentJob.vacation=vacationField.getText(); currentJob.healthBenefits=healthBenefitsBox.isSelected(); currentJob.stockOptions=stockOptionsBox.isSelected(); currentJob.city=cityField.getText(); currentJob.url=urlField.getText(); currentJob.hiringManager=managerField.getText(); currentJob.email=emailField.getText(); DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd"); currentJob.applicationDate=applicationDateField.getText().isBlank()?null:LocalDate.parse(applicationDateField.getText(),formatter); currentJob.lastContactDate=lastContactField.getText().isBlank()?null:LocalDate.parse(lastContactField.getText(),formatter); currentJob.interest=interestSlider.getValue(); currentJob.needsFollowUp=needsFollowUpBox.isSelected(); if(isEditing)dataModel.updateJob(currentJob); else dataModel.addJob(currentJob); dispose();} catch(NumberFormatException ex){showErrorDialog("Please enter a valid number for Compensation.");} catch(java.time.format.DateTimeParseException ex){showErrorDialog("Please use YYYY-MM-DD format for dates.");} catch(Exception ex){ex.printStackTrace(); showErrorDialog("Error saving job: "+ex.getMessage());}} private void showErrorDialog(String message){JOptionPane.showMessageDialog(this,message,"Input Error",JOptionPane.ERROR_MESSAGE);}}

    // --- MODULE 2: CAREER GOALS ---
    // (GoalType, GoalArea Enums remain the same)
    private enum GoalType { ONGOING("Ongoing",Theme.GOAL_ONGOING), PROJECT("Project",Theme.PRIMARY); final String displayName; final Color color; GoalType(String n,Color c){displayName=n; color=c;} @Override public String toString(){return displayName;}}
    private enum GoalArea { CAREER("Career",Theme.GOAL_CAREER), PERSONAL("Personal",Theme.WISHLIST), FINANCE("Finance",Theme.APPLIED); final String displayName; final Color color; GoalArea(String n,Color c){displayName=n; color=c;} @Override public String toString(){return displayName;}}
    // (CareerGoal model class remains the same)
    private static class CareerGoal { String id; String title; GoalType type; GoalArea areaOfLife; List<String> actionItems; LocalDate toCompleteBy; LocalDate completionDate; int progress; boolean isDone; CareerGoal(String title, GoalType type, GoalArea area, LocalDate toCompleteBy, int progress, boolean isDone){ this.id=java.util.UUID.randomUUID().toString(); this.title=title; this.type=type; this.areaOfLife=area; this.toCompleteBy=toCompleteBy; this.progress=progress; this.isDone=isDone; this.actionItems=new ArrayList<>();}}
    // (GoalDataModel class remains the same)
    private static class GoalDataModel { private final List<CareerGoal> goals = new ArrayList<>(); private final List<Runnable> updateListeners = new ArrayList<>(); public GoalDataModel(){ CareerGoal g1=new CareerGoal("Grow Personal Brand",GoalType.ONGOING,GoalArea.CAREER,LocalDate.of(2026,1,7),60,false); g1.actionItems.add("Add New Projects to Portfolio"); g1.actionItems.add("Attend 10 Networking Events"); goals.add(g1); CareerGoal g2=new CareerGoal("Get Promotion",GoalType.PROJECT,GoalArea.CAREER,LocalDate.of(2026,1,20),30,false); g2.actionItems.add("Complete Q4 Project"); g2.actionItems.add("Mentor Junior Dev"); goals.add(g2); CareerGoal g3=new CareerGoal("Launch Side Project",GoalType.PROJECT,GoalArea.PERSONAL,LocalDate.of(2026,3,1),0,true); g3.completionDate=LocalDate.of(2026,2,15); goals.add(g3);} public List<CareerGoal> getAllGoals(){return new ArrayList<>(goals);} public List<CareerGoal> getGoalsByStatus(boolean isDone){return goals.stream().filter(g -> g.isDone==isDone).collect(Collectors.toList());} public void addGoal(CareerGoal goal){goals.add(goal); notifyListeners();} public void updateGoal(CareerGoal updatedGoal){for(int i=0;i<goals.size();i++){if(goals.get(i).id.equals(updatedGoal.id)){goals.set(i,updatedGoal);break;}} notifyListeners();} public void addUpdateListener(Runnable listener){updateListeners.add(listener);} private void notifyListeners(){for(Runnable listener:updateListeners){listener.run();}}}
    // (CareerGoalsModule, GoalGridView, GoalCard, GoalProgressBar, GoalFormDialog classes remain the same...)
    private static class CareerGoalsModule extends JPanel { /* ... Same as previous version ... */ private final CardLayout cardLayout = new CardLayout(); private final JPanel cardPanel = new JPanel(cardLayout); private final GoalDataModel dataModel; private GoalGridView inProgressView; private GoalGridView completeView; CareerGoalsModule(GoalDataModel dataModel){ super(new BorderLayout(0,0)); setBackground(Theme.BACKGROUND); this.dataModel=dataModel; add(createHeaderPanel(),BorderLayout.NORTH); inProgressView=new GoalGridView(dataModel,false,this::showGoalDialog); completeView=new GoalGridView(dataModel,true,this::showGoalDialog); cardPanel.setBackground(Theme.BACKGROUND); cardPanel.add(inProgressView,"IN_PROGRESS"); cardPanel.add(completeView,"COMPLETE"); add(cardPanel,BorderLayout.CENTER); dataModel.addUpdateListener(this::refreshViews); cardLayout.show(cardPanel,"IN_PROGRESS");} private JPanel createHeaderPanel(){JPanel headerPanel=new JPanel(new BorderLayout(10,10)); headerPanel.setBackground(Theme.BACKGROUND); headerPanel.setBorder(new EmptyBorder(10,20,15,20)); JLabel titleLabel=new JLabel("Career Goals Overview"); titleLabel.setFont(Theme.TITLE_FONT); titleLabel.setForeground(Theme.TEXT); headerPanel.add(titleLabel,BorderLayout.WEST); JPanel controlsPanel=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0)); controlsPanel.setOpaque(false); JButton newButton=new JButton("New"); newButton.setBackground(Theme.PRIMARY); newButton.setForeground(Color.WHITE); newButton.setFont(Theme.BOLD_FONT); newButton.setBorder(new EmptyBorder(8,15,8,15)); newButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); newButton.addActionListener(e -> showGoalDialog(null)); controlsPanel.add(newButton); headerPanel.add(controlsPanel,BorderLayout.EAST); headerPanel.add(createViewSwitcherPanel(),BorderLayout.SOUTH); return headerPanel;} private JPanel createViewSwitcherPanel(){JPanel switcherPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); switcherPanel.setOpaque(false); switcherPanel.setBorder(new EmptyBorder(10,0,5,0)); JToggleButton inProgressButton=createViewToggleButton("In Progress"); JToggleButton completeButton=createViewToggleButton("Complete"); ButtonGroup group=new ButtonGroup(); group.add(inProgressButton); group.add(completeButton); inProgressButton.setSelected(true); inProgressButton.addActionListener(e -> cardLayout.show(cardPanel,"IN_PROGRESS")); completeButton.addActionListener(e -> cardLayout.show(cardPanel,"COMPLETE")); switcherPanel.add(inProgressButton); switcherPanel.add(completeButton); return switcherPanel;} private JToggleButton createViewToggleButton(String text){JToggleButton button=new JToggleButton(text){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); if(isSelected()){g2.setColor(Theme.PRIMARY); g2.fillRect(0,getHeight()-3,getWidth(),3); setForeground(Theme.TEXT);} else{setForeground(Theme.TEXT_MUTED);} g2.dispose(); super.paintComponent(g);}}; button.setFont(Theme.BOLD_FONT.deriveFont(14f)); button.setOpaque(false); button.setBorderPainted(false); button.setContentAreaFilled(false); button.setFocusPainted(false); button.setBorder(new EmptyBorder(8,12,8,12)); button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return button;} private void showGoalDialog(CareerGoal goal){JFrame parentFrame=(JFrame)SwingUtilities.getWindowAncestor(this); GoalFormDialog dialog=new GoalFormDialog(parentFrame,dataModel,goal); dialog.setVisible(true);} private void refreshViews(){inProgressView.refreshView(); completeView.refreshView();}}
    private static class GoalGridView extends JPanel { /* ... Same as previous version ... */ private final GoalDataModel dataModel; private final boolean showDone; private final JPanel gridPanel; private final java.util.function.Consumer<CareerGoal> onCardClick; GoalGridView(GoalDataModel model, boolean showDone, java.util.function.Consumer<CareerGoal> onCardClick){ super(new BorderLayout()); this.dataModel=model; this.showDone=showDone; this.onCardClick=onCardClick; setBackground(Theme.BACKGROUND); JPanel contentPanel=new JPanel(new BorderLayout()); contentPanel.setOpaque(false); gridPanel=new JPanel(new GridBagLayout()); gridPanel.setBackground(Theme.BACKGROUND); gridPanel.setBorder(new EmptyBorder(10,20,10,20)); contentPanel.add(gridPanel,BorderLayout.NORTH); JPanel footerPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); footerPanel.setOpaque(false); footerPanel.setBorder(new EmptyBorder(0,30,10,0)); JButton newCardButton=new FlatButton("+ New page"); newCardButton.setHorizontalAlignment(SwingConstants.LEFT); newCardButton.addActionListener(e -> onCardClick.accept(null)); footerPanel.add(newCardButton); contentPanel.add(footerPanel,BorderLayout.SOUTH); JScrollPane scrollPane=new JScrollPane(contentPanel); scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); scrollPane.setBorder(null); scrollPane.getVerticalScrollBar().setUnitIncrement(16); scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI()); scrollPane.getVerticalScrollBar().setBackground(Theme.BACKGROUND); scrollPane.getViewport().setBackground(Theme.BACKGROUND); add(scrollPane,BorderLayout.CENTER); addComponentListener(new ComponentAdapter(){@Override public void componentResized(ComponentEvent e){refreshView();}}); refreshView();} void refreshView(){gridPanel.removeAll(); int panelWidth=getWidth(); int columns=Math.max(1,(panelWidth-40)/360); GridBagConstraints gbc=new GridBagConstraints(); gbc.insets=new Insets(10,10,10,10); gbc.fill=GridBagConstraints.HORIZONTAL; gbc.anchor=GridBagConstraints.NORTH; gbc.weightx=1.0; List<CareerGoal> goals=dataModel.getGoalsByStatus(showDone); int row=0, col=0; for(CareerGoal goal:goals){gbc.gridx=col; gbc.gridy=row; gridPanel.add(new GoalCard(goal,onCardClick),gbc); col++; if(col>=columns){col=0; row++;}} gbc.gridx=0; gbc.gridy=row+1; gbc.gridwidth=columns; gbc.weighty=1.0; gbc.fill=GridBagConstraints.BOTH; gridPanel.add(Box.createGlue(),gbc); revalidate(); repaint();}}
    private static class GoalCard extends RoundedPanel { /* ... Same as previous version ... */ GoalCard(CareerGoal goal, java.util.function.Consumer<CareerGoal> onCardClick){ super(new BorderLayout(),8,Theme.CARD_BACKGROUND); setBorder(new EmptyBorder(12,12,12,12)); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); JPanel content=new JPanel(); content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS)); content.setOpaque(false); JLabel titleLabel=new JLabel(goal.title); titleLabel.setFont(Theme.HEADER_FONT); titleLabel.setForeground(Theme.TEXT); content.add(titleLabel); content.add(Box.createVerticalStrut(10)); content.add(new GoalProgressBar(goal.progress)); content.add(Box.createVerticalStrut(10)); DateTimeFormatter formatter=DateTimeFormatter.ofPattern("MMM d, yyyy"); String dateStr=goal.isDone?(goal.completionDate!=null?"Completed: "+goal.completionDate.format(formatter):"Complete"):(goal.toCompleteBy!=null?"To complete by "+goal.toCompleteBy.format(formatter):"No due date"); JLabel dateLabel=new JLabel(dateStr); dateLabel.setFont(Theme.MUTED_FONT); dateLabel.setForeground(Theme.TEXT_MUTED); content.add(dateLabel); add(content,BorderLayout.CENTER); addMouseListener(new MouseAdapter(){@Override public void mouseClicked(MouseEvent e){onCardClick.accept(goal);}});}}
    private static class GoalProgressBar extends JComponent { /* ... Same as previous version ... */ private int progress; GoalProgressBar(int progress){this.progress=progress; setPreferredSize(new Dimension(100,24));} @Override protected void paintComponent(Graphics g){super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); int totalWidth=getWidth()-60; int barHeight=10; int y=(getHeight()-barHeight)/2; int barWidth=(totalWidth-(9*2))/10; int x=0; int filledBars=progress/10; for(int i=0;i<10;i++){g2.setColor(i<filledBars?Theme.TEXT_MUTED:Theme.BORDER); g2.fill(new RoundRectangle2D.Float(x,y,barWidth,barHeight,4,4)); x+=(barWidth+2);} String progressText=progress+"%"; g2.setColor(Theme.TEXT_MUTED); g2.setFont(Theme.BODY_FONT); FontMetrics fm=g2.getFontMetrics(); int textY=(getHeight()-fm.getHeight())/2+fm.getAscent(); g2.drawString(progressText,x+5,textY); g2.dispose();}}
    private static class GoalFormDialog extends JDialog { /* ... Same as previous version ... */ private final GoalDataModel dataModel; private final CareerGoal currentGoal; private final boolean isEditing; private JTextField titleField, toCompleteDateField, completionDateField; private JComboBox<GoalType> typeBox; private JComboBox<GoalArea> areaBox; private JSlider progressSlider; private JCheckBox isDoneBox; private JList<String> actionItemsList; private DefaultListModel<String> actionItemsModel; GoalFormDialog(JFrame owner, GoalDataModel model, CareerGoal goal){ super(owner,"Career Goal",true); this.dataModel=model; if(goal==null){this.currentGoal=new CareerGoal("",GoalType.ONGOING,GoalArea.CAREER,null,0,false); this.isEditing=false; setTitle("Add New Career Goal");} else{this.currentGoal=goal; this.isEditing=true; setTitle("Edit Career Goal");} initUI(); populateForm(); setSize(new Dimension(600,700)); setResizable(false); setLocationRelativeTo(owner);} private void initUI(){JPanel formPanel=new JPanel(new GridBagLayout()); formPanel.setBackground(Theme.PANEL_BACKGROUND); formPanel.setBorder(new EmptyBorder(20,20,20,20)); GridBagConstraints gbc=new GridBagConstraints(); gbc.insets=new Insets(8,5,8,5); gbc.anchor=GridBagConstraints.WEST; java.util.function.BiConsumer<String,JComponent> addRow=(label,component) -> {gbc.gridx=0; gbc.weightx=0; gbc.fill=GridBagConstraints.NONE; JLabel l=new JLabel(label); l.setFont(Theme.BODY_FONT); l.setForeground(Theme.TEXT_MUTED); formPanel.add(l,gbc); gbc.gridx=1; gbc.weightx=1; gbc.fill=GridBagConstraints.HORIZONTAL; formPanel.add(component,gbc); gbc.gridy++;}; gbc.gridy=0; titleField=new JTextField(25); addRow.accept("Title:",titleField); typeBox=new JComboBox<>(GoalType.values()); addRow.accept("Type:",typeBox); areaBox=new JComboBox<>(GoalArea.values()); addRow.accept("Area of Life:",areaBox); gbc.gridx=0; gbc.weightx=0; gbc.fill=GridBagConstraints.NONE; JLabel l=new JLabel("Action Items:"); l.setFont(Theme.BODY_FONT); l.setForeground(Theme.TEXT_MUTED); formPanel.add(l,gbc); actionItemsModel=new DefaultListModel<>(); actionItemsList=new JList<>(actionItemsModel); actionItemsList.setBackground(Theme.INPUT_BACKGROUND); actionItemsList.setFont(Theme.BODY_FONT); JScrollPane listScrollPane=new JScrollPane(actionItemsList); listScrollPane.setPreferredSize(new Dimension(100,100)); gbc.gridx=1; gbc.weightx=1; gbc.fill=GridBagConstraints.BOTH; gbc.weighty=1.0; formPanel.add(listScrollPane,gbc); gbc.gridy++; JPanel listButtonsPanel=new JPanel(new FlowLayout(FlowLayout.RIGHT,5,0)); listButtonsPanel.setOpaque(false); JButton removeButton=new JButton("Remove Selected"); removeButton.setFont(Theme.BODY_FONT); removeButton.setEnabled(false); removeButton.addActionListener(e -> {int[] selected=actionItemsList.getSelectedIndices(); for(int i=selected.length-1;i>=0;i--){actionItemsModel.removeElementAt(selected[i]);}}); JButton addButton=new JButton("Add Item"); addButton.setFont(Theme.BODY_FONT); addButton.addActionListener(e -> {String newItem=JOptionPane.showInputDialog(this,"Enter action item:","Add Item",JOptionPane.PLAIN_MESSAGE); if(newItem!=null&&!newItem.isBlank()){actionItemsModel.addElement(newItem);}}); listButtonsPanel.add(removeButton); listButtonsPanel.add(addButton); gbc.gridx=1; gbc.weightx=1; gbc.fill=GridBagConstraints.HORIZONTAL; gbc.weighty=0; formPanel.add(listButtonsPanel,gbc); gbc.gridy++; actionItemsList.addListSelectionListener(e -> removeButton.setEnabled(!actionItemsList.isSelectionEmpty())); toCompleteDateField=new JTextField(); addRow.accept("To Complete By (YYYY-MM-DD):",toCompleteDateField); completionDateField=new JTextField(); addRow.accept("Completion Date (YYYY-MM-DD):",completionDateField); progressSlider=new JSlider(0,100,0); progressSlider.setMajorTickSpacing(25); progressSlider.setPaintTicks(true); progressSlider.setPaintLabels(true); addRow.accept("Progress:",progressSlider); isDoneBox=new JCheckBox("Done?"); isDoneBox.setFont(Theme.BODY_FONT); gbc.gridx=1; gbc.weightx=1; gbc.fill=GridBagConstraints.HORIZONTAL; formPanel.add(isDoneBox,gbc); gbc.gridy++; JPanel buttonPanel=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0)); buttonPanel.setOpaque(false); JButton cancelButton=new JButton("Cancel"); cancelButton.setFont(Theme.BOLD_FONT); cancelButton.addActionListener(e -> dispose()); buttonPanel.add(cancelButton); JButton saveButton=new JButton("Save"); saveButton.setFont(Theme.BOLD_FONT); saveButton.setBackground(Theme.PRIMARY); saveButton.setForeground(Color.WHITE); saveButton.addActionListener(e -> saveGoal()); buttonPanel.add(saveButton); gbc.gridx=0; gbc.gridwidth=2; gbc.anchor=GridBagConstraints.EAST; gbc.fill=GridBagConstraints.NONE; gbc.insets=new Insets(20,0,0,0); formPanel.add(buttonPanel,gbc); JScrollPane scrollPane=new JScrollPane(formPanel); scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); scrollPane.setBorder(null); scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI()); scrollPane.getVerticalScrollBar().setBackground(Theme.PANEL_BACKGROUND); scrollPane.getViewport().setBackground(Theme.PANEL_BACKGROUND); getContentPane().add(scrollPane,BorderLayout.CENTER); getContentPane().setBackground(Theme.PANEL_BACKGROUND);} private void populateForm(){titleField.setText(currentGoal.title); typeBox.setSelectedItem(currentGoal.type); areaBox.setSelectedItem(currentGoal.areaOfLife); progressSlider.setValue(currentGoal.progress); isDoneBox.setSelected(currentGoal.isDone); actionItemsModel.removeAllElements(); if(currentGoal.actionItems!=null){currentGoal.actionItems.forEach(actionItemsModel::addElement);} DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd"); if(currentGoal.toCompleteBy!=null)toCompleteDateField.setText(currentGoal.toCompleteBy.format(formatter)); if(currentGoal.completionDate!=null)completionDateField.setText(currentGoal.completionDate.format(formatter));} private void saveGoal(){try{currentGoal.title=titleField.getText(); currentGoal.type=(GoalType)Objects.requireNonNull(typeBox.getSelectedItem()); currentGoal.areaOfLife=(GoalArea)Objects.requireNonNull(areaBox.getSelectedItem()); currentGoal.progress=progressSlider.getValue(); currentGoal.isDone=isDoneBox.isSelected(); currentGoal.actionItems.clear(); IntStream.range(0,actionItemsModel.size()).mapToObj(actionItemsModel::getElementAt).forEach(currentGoal.actionItems::add); DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd"); currentGoal.toCompleteBy=toCompleteDateField.getText().isBlank()?null:LocalDate.parse(toCompleteDateField.getText(),formatter); currentGoal.completionDate=completionDateField.getText().isBlank()?null:LocalDate.parse(completionDateField.getText(),formatter); if(isEditing)dataModel.updateGoal(currentGoal); else dataModel.addGoal(currentGoal); dispose();} catch(java.time.format.DateTimeParseException ex){JOptionPane.showMessageDialog(this,"Please use YYYY-MM-DD format for dates.","Input Error",JOptionPane.ERROR_MESSAGE);} catch(Exception ex){ex.printStackTrace(); JOptionPane.showMessageDialog(this,"Error saving goal: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}}}

    // --- MODULE 3: ACTION ITEMS ---

    // --- Data Models for Action Items ---

    /**
     * Data model for a single Action Item.
     */
    private static class ActionItem {
        String id;
        String title;
        String relatedGoalTitle; // Just storing the title for simplicity
        String successMetric;
        int currentValue;
        int objectiveValue;
        boolean isDone;
        // Progress calculated as (currentValue * 100) / objectiveValue

        ActionItem(String title, String relatedGoal, String metric, int current, int objective, boolean done) {
            this.id = java.util.UUID.randomUUID().toString();
            this.title = title;
            this.relatedGoalTitle = relatedGoal;
            this.successMetric = metric;
            this.currentValue = current;
            this.objectiveValue = objective;
            this.isDone = done;
        }

        int getProgress() {
            if (objectiveValue <= 0) return 0;
            int progress = (int) Math.round(((double) currentValue / objectiveValue) * 100);
            return Math.min(100, Math.max(0, progress)); // Clamp between 0 and 100
        }
    }

    /**
     * Manages the in-memory list of action items.
     */
    private static class ActionItemDataModel {
        private final List<ActionItem> items = new ArrayList<>();
        private final List<Runnable> updateListeners = new ArrayList<>();

        public ActionItemDataModel() {
            // Load initial mock data based on screenshot
            items.add(new ActionItem("Attend 10 Networking Events", "Grow Personal Brand",
                "Events Attended", 8, 10, false));
            items.add(new ActionItem("Add New Projects to Portfolio", "Grow Personal Brand",
                "New Projects On Portfolio", 4, 10, false));
             items.add(new ActionItem("Put Together Action Plan", "Get Promotion",
                "All Steps of Plan Complete", 3, 10, false));
             // Add a completed item for testing
             items.add(new ActionItem("Complete Q4 Project", "Get Promotion",
                "Project Delivered", 1, 1, true));
        }

        public List<ActionItem> getAllItems() { return new ArrayList<>(items); }
        public List<ActionItem> getItemsByStatus(boolean isDone) {
            return items.stream()
                .filter(item -> item.isDone == isDone)
                .collect(Collectors.toList());
        }
        public void addItem(ActionItem item) {
            items.add(item);
            notifyListeners();
        }
        public void updateItem(ActionItem updatedItem) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).id.equals(updatedItem.id)) {
                    items.set(i, updatedItem);
                    break;
                }
            }
            notifyListeners();
        }
        public void addUpdateListener(Runnable listener) { updateListeners.add(listener); }
        private void notifyListeners() {
            for (Runnable listener : updateListeners) { listener.run(); }
        }
    }

    // --- UI Components for Action Items ---

    /**
     * The main panel for the Action Items module.
     */
    private static class ActionItemsModule extends JPanel {
        private final CardLayout cardLayout = new CardLayout();
        private final JPanel cardPanel = new JPanel(cardLayout);
        private final ActionItemDataModel dataModel;
        private ActionItemGridView inProgressView;
        private ActionItemGridView completeView;

        ActionItemsModule(ActionItemDataModel dataModel) {
            super(new BorderLayout(0, 0));
            setBackground(Theme.BACKGROUND);
            this.dataModel = dataModel;

            add(createHeaderPanel(), BorderLayout.NORTH);

            inProgressView = new ActionItemGridView(dataModel, false, this::showActionItemDialog);
            completeView = new ActionItemGridView(dataModel, true, this::showActionItemDialog);

            cardPanel.setBackground(Theme.BACKGROUND);
            cardPanel.add(inProgressView, "IN_PROGRESS");
            cardPanel.add(completeView, "COMPLETE");
            add(cardPanel, BorderLayout.CENTER);

            dataModel.addUpdateListener(this::refreshViews);
            cardLayout.show(cardPanel, "IN_PROGRESS");
        }

        private JPanel createHeaderPanel() {
            JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
            headerPanel.setBackground(Theme.BACKGROUND);
            headerPanel.setBorder(new EmptyBorder(10, 20, 15, 20));

            JLabel titleLabel = new JLabel("Action Items"); // Title
            titleLabel.setFont(Theme.TITLE_FONT);
            titleLabel.setForeground(Theme.TEXT);
            headerPanel.add(titleLabel, BorderLayout.WEST);

            JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            controlsPanel.setOpaque(false);
            JButton newButton = new JButton("New");
            newButton.setBackground(Theme.PRIMARY);
            newButton.setForeground(Color.WHITE);
            newButton.setFont(Theme.BOLD_FONT);
            newButton.setBorder(new EmptyBorder(8, 15, 8, 15));
            newButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            newButton.addActionListener(e -> showActionItemDialog(null));
            controlsPanel.add(newButton);
            headerPanel.add(controlsPanel, BorderLayout.EAST);

            headerPanel.add(createViewSwitcherPanel(), BorderLayout.SOUTH);
            return headerPanel;
        }

        private JPanel createViewSwitcherPanel() {
            JPanel switcherPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            switcherPanel.setOpaque(false);
            switcherPanel.setBorder(new EmptyBorder(10, 0, 5, 0));

            JToggleButton inProgressButton = createViewToggleButton("In Progress");
            JToggleButton completeButton = createViewToggleButton("Complete");

            ButtonGroup group = new ButtonGroup();
            group.add(inProgressButton);
            group.add(completeButton);

            inProgressButton.setSelected(true);
            inProgressButton.addActionListener(e -> cardLayout.show(cardPanel, "IN_PROGRESS"));
            completeButton.addActionListener(e -> cardLayout.show(cardPanel, "COMPLETE"));

            switcherPanel.add(inProgressButton);
            switcherPanel.add(completeButton);
            return switcherPanel;
        }

        private JToggleButton createViewToggleButton(String text) {
             JToggleButton button = new JToggleButton(text) { /* ... Same paint method as others ... */ @Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); if(isSelected()){g2.setColor(Theme.PRIMARY); g2.fillRect(0,getHeight()-3,getWidth(),3); setForeground(Theme.TEXT);} else{setForeground(Theme.TEXT_MUTED);} g2.dispose(); super.paintComponent(g);}};
            button.setFont(Theme.BOLD_FONT.deriveFont(14f));
            button.setOpaque(false); button.setBorderPainted(false); button.setContentAreaFilled(false);
            button.setFocusPainted(false); button.setBorder(new EmptyBorder(8, 12, 8, 12));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return button;
        }

        private void showActionItemDialog(ActionItem item) {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            ActionItemFormDialog dialog = new ActionItemFormDialog(parentFrame, dataModel, item);
            dialog.setVisible(true);
        }

        private void refreshViews() {
            inProgressView.refreshView();
            completeView.refreshView();
        }
    }


    /**
     * A grid view for displaying Action Item Cards.
     */
    private static class ActionItemGridView extends JPanel {
        private final ActionItemDataModel dataModel;
        private final boolean showDone;
        private final JPanel gridPanel;
        private final java.util.function.Consumer<ActionItem> onCardClick;

        ActionItemGridView(ActionItemDataModel model, boolean showDone, java.util.function.Consumer<ActionItem> onCardClick) {
            super(new BorderLayout());
            this.dataModel = model;
            this.showDone = showDone;
            this.onCardClick = onCardClick;
            setBackground(Theme.BACKGROUND);

            JPanel contentPanel = new JPanel(new BorderLayout());
            contentPanel.setOpaque(false);

            gridPanel = new JPanel(new GridBagLayout());
            gridPanel.setBackground(Theme.BACKGROUND);
            gridPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
            contentPanel.add(gridPanel, BorderLayout.NORTH);

            // Add "+ New page" button at the bottom
            JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            footerPanel.setOpaque(false);
            footerPanel.setBorder(new EmptyBorder(0, 30, 10, 0));
            JButton newCardButton = new FlatButton("+ New page");
            newCardButton.setHorizontalAlignment(SwingConstants.LEFT);
            newCardButton.addActionListener(e -> onCardClick.accept(null));
            footerPanel.add(newCardButton);
            contentPanel.add(footerPanel, BorderLayout.SOUTH);

            JScrollPane scrollPane = new JScrollPane(contentPanel);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());
            scrollPane.getVerticalScrollBar().setBackground(Theme.BACKGROUND);
            scrollPane.getViewport().setBackground(Theme.BACKGROUND);
            add(scrollPane, BorderLayout.CENTER);

            addComponentListener(new ComponentAdapter() {
                @Override public void componentResized(ComponentEvent e) { refreshView(); }
            });
            refreshView();
        }

        void refreshView() {
            gridPanel.removeAll();
            int panelWidth = getWidth();
            // Adjust card width calculation if needed
            int columns = Math.max(1, (panelWidth - 40) / 400); // Slightly wider cards

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.NORTH;
            gbc.weightx = 1.0;

            List<ActionItem> items = dataModel.getItemsByStatus(showDone);
            int row = 0, col = 0;

            for (ActionItem item : items) {
                gbc.gridx = col;
                gbc.gridy = row;
                gridPanel.add(new ActionItemCard(item, onCardClick), gbc);
                col++;
                if (col >= columns) {
                    col = 0;
                    row++;
                }
            }

            gbc.gridx = 0; gbc.gridy = row + 1; gbc.gridwidth = columns;
            gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
            gridPanel.add(Box.createGlue(), gbc);

            revalidate();
            repaint();
        }
    }


    /**
     * A custom card for displaying an ActionItem.
     */
    private static class ActionItemCard extends RoundedPanel {
        ActionItemCard(ActionItem item, java.util.function.Consumer<ActionItem> onCardClick) {
            super(new BorderLayout(), 8, Theme.CARD_BACKGROUND);
            setBorder(new EmptyBorder(12, 12, 12, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setOpaque(false);

            // Row 1: Title
            JLabel titleLabel = new JLabel(item.title);
            titleLabel.setFont(Theme.HEADER_FONT);
            titleLabel.setForeground(Theme.TEXT);
            content.add(titleLabel);
            content.add(Box.createVerticalStrut(8));

            // Row 2: Related Goal
            JLabel goalLabel = new JLabel("<html>Goal: <i>" + item.relatedGoalTitle + "</i></html>"); // Italicize
            goalLabel.setFont(Theme.MUTED_FONT);
            goalLabel.setForeground(Theme.TEXT_MUTED);
            content.add(goalLabel);
            content.add(Box.createVerticalStrut(10));

            // GridBagLayout for Metric, Current, Objective
            JPanel detailsPanel = new JPanel(new GridBagLayout());
            detailsPanel.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(2, 0, 2, 10);
            gbc.gridx = 0; gbc.gridy = 0;

             // Helper to add a detail row
            java.util.function.BiConsumer<String, String> addDetail = (key, value) -> {
                if (value == null || value.isEmpty()) return;
                JLabel keyLabel = new JLabel(key);
                keyLabel.setFont(Theme.MUTED_FONT);
                keyLabel.setForeground(Theme.TEXT_MUTED);
                gbc.gridx = 0; gbc.weightx = 0;
                detailsPanel.add(keyLabel, gbc);
                JLabel valueLabel = new JLabel(value);
                valueLabel.setFont(Theme.BODY_FONT);
                valueLabel.setForeground(Theme.TEXT);
                gbc.gridx = 1; gbc.weightx = 1;
                detailsPanel.add(valueLabel, gbc);
                gbc.gridy++;
            };

            addDetail.accept("Success Metric:", item.successMetric);
            addDetail.accept("Current:", String.valueOf(item.currentValue));
            addDetail.accept("Objective:", String.valueOf(item.objectiveValue));

            content.add(detailsPanel);
            content.add(Box.createVerticalStrut(10));

            // Row 5: Progress Bar
            content.add(new GoalProgressBar(item.getProgress())); // Reuse GoalProgressBar

            add(content, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { onCardClick.accept(item); }
            });
        }
    }

    /**
     * Modal dialog for creating or editing an Action Item.
     */
    private static class ActionItemFormDialog extends JDialog {
        private final ActionItemDataModel dataModel;
        private final ActionItem currentItem;
        private final boolean isEditing;

        private JTextField titleField, goalField, metricField, currentField, objectiveField;
        private JCheckBox isDoneBox;

        ActionItemFormDialog(JFrame owner, ActionItemDataModel model, ActionItem item) {
            super(owner, "Action Item", true);
            this.dataModel = model;

            if (item == null) {
                this.currentItem = new ActionItem("", "", "", 0, 10, false); // Default objective 10
                this.isEditing = false;
                setTitle("Add New Action Item");
            } else {
                this.currentItem = item;
                this.isEditing = true;
                setTitle("Edit Action Item");
            }
            initUI();
            populateForm();
            setSize(new Dimension(500, 450)); // Smaller dialog
            setResizable(false);
            setLocationRelativeTo(owner);
        }

        private void initUI() {
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(Theme.PANEL_BACKGROUND);
            formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 5, 8, 5);
            gbc.anchor = GridBagConstraints.WEST;

            java.util.function.BiConsumer<String, JComponent> addRow = (label, component) -> {
                gbc.gridx = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
                JLabel l = new JLabel(label);
                l.setFont(Theme.BODY_FONT);
                l.setForeground(Theme.TEXT_MUTED);
                formPanel.add(l, gbc);
                gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
                formPanel.add(component, gbc);
                gbc.gridy++;
            };

            gbc.gridy = 0;
            titleField = new JTextField(25); addRow.accept("Title:", titleField);
            goalField = new JTextField(); addRow.accept("Related Goal:", goalField);
            metricField = new JTextField(); addRow.accept("Success Metric:", metricField);
            currentField = new JTextField(); addRow.accept("Current Value:", currentField);
            objectiveField = new JTextField(); addRow.accept("Objective Value:", objectiveField);

            isDoneBox = new JCheckBox("Done?");
            isDoneBox.setFont(Theme.BODY_FONT);
            gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(isDoneBox, gbc); gbc.gridy++;

            // --- Save/Cancel Buttons ---
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            buttonPanel.setOpaque(false);
            JButton cancelButton = new JButton("Cancel");
            cancelButton.setFont(Theme.BOLD_FONT);
            cancelButton.addActionListener(e -> dispose());
            buttonPanel.add(cancelButton);
            JButton saveButton = new JButton("Save");
            saveButton.setFont(Theme.BOLD_FONT);
            saveButton.setBackground(Theme.PRIMARY);
            saveButton.setForeground(Color.WHITE);
            saveButton.addActionListener(e -> saveItem());
            buttonPanel.add(saveButton);

            gbc.gridx = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.EAST;
            gbc.fill = GridBagConstraints.NONE; gbc.insets = new Insets(20, 0, 0, 0);
            formPanel.add(buttonPanel, gbc);

            getContentPane().add(formPanel, BorderLayout.CENTER); // No scroll pane needed
            getContentPane().setBackground(Theme.PANEL_BACKGROUND);
        }

        private void populateForm() {
            titleField.setText(currentItem.title);
            goalField.setText(currentItem.relatedGoalTitle);
            metricField.setText(currentItem.successMetric);
            currentField.setText(String.valueOf(currentItem.currentValue));
            objectiveField.setText(String.valueOf(currentItem.objectiveValue));
            isDoneBox.setSelected(currentItem.isDone);
        }

        private void saveItem() {
            try {
                currentItem.title = titleField.getText();
                currentItem.relatedGoalTitle = goalField.getText();
                currentItem.successMetric = metricField.getText();
                currentItem.currentValue = Integer.parseInt(currentField.getText());
                currentItem.objectiveValue = Integer.parseInt(objectiveField.getText());
                currentItem.isDone = isDoneBox.isSelected();

                if (currentItem.objectiveValue <= 0) {
                     JOptionPane.showMessageDialog(this, "Objective Value must be greater than zero.", "Input Error", JOptionPane.ERROR_MESSAGE);
                     return;
                }

                if (isEditing) dataModel.updateItem(currentItem);
                else dataModel.addItem(currentItem);
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for Current and Objective Values.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving action item: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    // --- COMMON UTILITY COMPONENTS ---

    // (RoundedPanel, TagLabel, ColorDotLabel, CompanyIcon, FlatButton, IconType, IconOnlyButton, CustomScrollBarUI)
    // remain the same as the previous version...
    private static class RoundedPanel extends JPanel { private final int arc; private final Color bgColor; RoundedPanel(LayoutManager layout,int arc,Color bgColor){super(layout); this.arc=arc; this.bgColor=bgColor; setOpaque(false);} @Override protected void paintComponent(Graphics g){super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(bgColor); g2.fill(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,arc,arc)); g2.dispose();}}
    private static class TagLabel extends JLabel { private Color bgColor; TagLabel(String text,Color bgColor){super(text); setColors(bgColor,Color.WHITE); setFont(Theme.MUTED_FONT.deriveFont(Font.BOLD)); setBorder(new EmptyBorder(4,8,4,8)); setOpaque(false);} void setColors(Color bg,Color defaultFg){this.bgColor=bg; double luminance=(0.299*bg.getRed()+0.587*bg.getGreen()+0.114*bg.getBlue())/255; if(luminance>0.5)setForeground(Color.BLACK); else{setForeground(Theme.TEXT); if(bg.equals(Theme.CARD_BACKGROUND)){setForeground(Theme.TEXT_MUTED); this.bgColor=new Color(0x3A,0x3A,0x3A);}}} @Override protected void paintComponent(Graphics g){if(!isOpaque()){Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(bgColor); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),6,6)); g2.dispose();} super.paintComponent(g);}}
    private static class ColorDotLabel extends JLabel { private final Color color; ColorDotLabel(Color color){this.color=color; setPreferredSize(new Dimension(8,16));} @Override protected void paintComponent(Graphics g){super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(color); g2.fill(new Ellipse2D.Float(0,(getHeight()/2f)-4,8,8)); g2.dispose();}}
    private static class CompanyIcon extends JLabel { private String initial; CompanyIcon(String initial){setInitial(initial); setPreferredSize(new Dimension(20,20)); setFont(Theme.MUTED_FONT.deriveFont(Font.BOLD)); setForeground(Theme.TEXT);} void setInitial(String initial){this.initial=(initial!=null&&!initial.isEmpty())?initial.substring(0,1).toUpperCase():"?";} @Override protected void paintComponent(Graphics g){super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(Theme.BORDER); g2.fill(new Ellipse2D.Float(0,0,20,20)); FontMetrics fm=g2.getFontMetrics(); int x=(20-fm.stringWidth(initial))/2; int y=(fm.getAscent()+(20-(fm.getAscent()+fm.getDescent()))/2); g2.setColor(Theme.TEXT); g2.drawString(initial,x,y); g2.dispose();}}
    private static class FlatButton extends JButton { private boolean isHovered=false; FlatButton(String text){super(text); setFont(Theme.BODY_FONT); setForeground(Theme.TEXT_MUTED); setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); setBorder(new EmptyBorder(8,5,8,5)); addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){isHovered=true; setForeground(Theme.TEXT); repaint();} @Override public void mouseExited(MouseEvent e){isHovered=false; setForeground(Theme.TEXT_MUTED); repaint();}});} @Override protected void paintComponent(Graphics g){if(isHovered){Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(Theme.CARD_BACKGROUND); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),8,8)); g2.dispose();} super.paintComponent(g);}}
    private enum IconType { BRIEFCASE, PLUS, THREE_DOTS, POSITION, COMPANY, COMPENSATION, PAY_TYPE, STATUS, INTEREST, DOCUMENT }
    private static class IconOnlyButton extends JButton { private final IconType iconType; private boolean isHovered=false; IconOnlyButton(IconType iconType){this.iconType=iconType; setPreferredSize(new Dimension(28,28)); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){isHovered=true; repaint();} @Override public void mouseExited(MouseEvent e){isHovered=false; repaint();}});} public void setHover(boolean hover){this.isHovered=hover; repaint();} @Override protected void paintComponent(Graphics g){super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); if(isHovered){g2.setColor(Theme.CARD_BACKGROUND); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),8,8));} g2.setColor(Theme.TEXT_MUTED); g2.setStroke(new BasicStroke(1.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND)); int w=getWidth(), h=getHeight(); int p=(w>20)?8:2; switch(iconType){case BRIEFCASE: g2.setColor(Theme.PRIMARY); g2.draw(new RoundRectangle2D.Float(p,p+2,w-p*2,h-p*2-2,4,4)); g2.draw(new Rectangle.Float(p+4,p-2,w-p*2-8,4)); break; case DOCUMENT: case POSITION: g2.draw(new Rectangle2D.Float(p,p,w-p*2,h-p*2)); g2.drawLine(p+2,p+4,w-p-2,p+4); g2.drawLine(p+2,p+7,w-p-2,p+7); break; case COMPANY: g2.draw(new Rectangle2D.Float(p,p,w-p*2,h-p*2)); g2.drawLine(p,h-p,w-p,p); break; case COMPENSATION: case PAY_TYPE: g2.draw(new Ellipse2D.Float(p,p,w-p*2,h-p*2)); g2.drawLine(w/2,p+2,w/2,h-p-2); g2.drawLine(p+2,h/2,w/2,h/2-3); g2.drawLine(w/2,h/2-3,w-p-2,h/2); break; case STATUS: g2.draw(new Ellipse2D.Float(p,p,3,3)); g2.draw(new Ellipse2D.Float(p,h/2-1,3,3)); g2.draw(new Ellipse2D.Float(p,h-p-2,3,3)); break; case INTEREST: g2.drawLine(p,h-p,p,p); g2.drawLine(p+4,h-p,p+4,p+3); g2.drawLine(p+8,h-p,p+8,p+6); break;} g2.dispose();}}
    private static class CustomScrollBarUI extends BasicScrollBarUI { @Override protected void configureScrollBarColors(){this.thumbColor=Theme.BORDER; this.thumbDarkShadowColor=new Color(0,0,0,0); this.thumbHighlightColor=new Color(0,0,0,0); this.thumbLightShadowColor=new Color(0,0,0,0); if(scrollbar.getParent()!=null&&scrollbar.getParent().getParent()!=null){this.trackColor=scrollbar.getParent().getParent().getBackground();} else{this.trackColor=Theme.PANEL_BACKGROUND;}} @Override protected JButton createDecreaseButton(int orientation){return createZeroButton();} @Override protected JButton createIncreaseButton(int orientation){return createZeroButton();} private JButton createZeroButton(){JButton b=new JButton(); b.setPreferredSize(new Dimension(0,0)); b.setMinimumSize(new Dimension(0,0)); b.setMaximumSize(new Dimension(0,0)); return b;} @Override protected void paintThumb(Graphics g,JComponent c,Rectangle thumbBounds){if(thumbBounds.isEmpty()||!scrollbar.isEnabled())return; Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(thumbColor); int arc=8; if(scrollbar.getOrientation()==JScrollBar.VERTICAL){g2.fill(new RoundRectangle2D.Float(thumbBounds.x+2,thumbBounds.y,thumbBounds.width-4,thumbBounds.height,arc,arc));} else{g2.fill(new RoundRectangle2D.Float(thumbBounds.x,thumbBounds.y+2,thumbBounds.width,thumbBounds.height-4,arc,arc));} g2.dispose();} @Override protected void paintTrack(Graphics g,JComponent c,Rectangle trackBounds){g.setColor(trackColor); g.fillRect(trackBounds.x,trackBounds.y,trackBounds.width,trackBounds.height);}}

} // End of NewTemplate class