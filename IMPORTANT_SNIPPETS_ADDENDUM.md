# IMPORTANT_SNIPPETS Addendum — additional UI snippets

Generated: October 27, 2025

This addendum contains additional concise, comment-free snippets requested by the user (NotionDashboard, HashUtil, UITheme, DashboardUI, ContentCreatorApp). Use this together with `IMPORTANT_SNIPPETS.md`.

---

## NotionDashboard (core types)

```java
public class NotionDashboard {
    static class Person { String name; Icon avatar; Color color; public Person(String name, Color color) { this.name = name; this.color = color; this.avatar = IconFactory.createAvatarIcon(name.substring(0,1), color); } }
    static class Area { String name; Person owner; Icon icon; List<Task> tasks = new ArrayList<>(); public Area(String name, Person owner, Icon icon) { this.name = name; this.owner = owner; this.icon = icon; } }
    static enum Priority { High, Medium, Low, None }
    static enum Status { Completed, InProgress, Todo }
    static class Task { String name; Area area; Person assignee; Priority priority; Status status; Date deadline; Date createdTime; Person createdBy; boolean done; String addToAction; public Task() { this.name = "New Task"; this.priority = Priority.None; this.status = Status.Todo; this.createdTime = new Date(); this.createdBy = MockData.getPeople().get(0); this.done = false; this.addToAction = ""; } public String getDaysLeft() { if (deadline == null) return ""; LocalDate today = LocalDate.now(); LocalDate deadDate = deadline.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(); long days = ChronoUnit.DAYS.between(today, deadDate); if (days == 0) return "Due Today 🔥"; if (days > 0) return days + (days==1?" Day Left":" Days Left"); return Math.abs(days) + (days==-1?" Day Past Due ❗":" Days Past Due ❗"); } }
    static class MockData { public static List<Person> getPeople() { List<Person> people = new ArrayList<>(); people.add(new Person("Abel Sunil", new Color(220,80,80))); people.add(new Person("Ali Rashidy", new Color(80,150,220))); return people; } public static List<Area> getAreas() { List<Area> areas = new ArrayList<>(); areas.add(new Area("HR", getPeople().get(0), IconFactory.get("person"))); return areas; } public static List<Task> getTasks() { List<Task> tasks = new ArrayList<>(); tasks.add(new Task()); return tasks; } }
    static class DarkTheme { static final Color COLOR_BG = new Color(27,27,27); static void apply() { try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); UIManager.put("Panel.background", COLOR_BG); } catch (Exception ignored) {} } }
    static class IconFactory { public static Icon get(String name) { return new ImageIcon(); } public static Icon createAvatarIcon(String initial, Color color) { return new Icon() { public void paintIcon(Component c, Graphics g, int x, int y) { } public int getIconWidth() { return 30; } public int getIconHeight() { return 30; } }; }
}
```

---

## HashUtil

```java
package main.ui;
import java.security.MessageDigest; import java.security.SecureRandom; import java.util.Base64;
public final class HashUtil { private HashUtil() {} public static String generateSalt() { byte[] b = new byte[16]; new SecureRandom().nextBytes(b); return Base64.getEncoder().encodeToString(b); } public static String hashWithSalt(String password, String salt) { try { MessageDigest md = MessageDigest.getInstance("SHA-256"); md.update(salt.getBytes("UTF-8")); byte[] digest = md.digest(password.getBytes("UTF-8")); return Base64.getEncoder().encodeToString(digest); } catch (Exception e) { throw new RuntimeException(e); } } }
```

---

## UITheme

```java
package main.ui;
import javax.swing.*; import javax.swing.border.LineBorder; import java.awt.*; import java.awt.event.MouseAdapter; import java.awt.event.MouseEvent;
public final class UITheme { public static final Color BG = new Color(24,24,24); public static final Color SURFACE = new Color(34,36,40); public static final Color MUTED = new Color(140,140,150); public static final Color ACCENT = new Color(88,101,242); public static final Color ACCENT_HOVER = new Color(102,115,250); public static final Font UI_FONT = new Font("Inter", Font.PLAIN, 13); public static final Font UI_FONT_BOLD = new Font("Inter", Font.BOLD, 13); private UITheme() {} public static void applyGlobalTheme() { UIManager.put("Label.foreground", Color.WHITE); UIManager.put("Panel.background", BG); UIManager.put("TextField.background", new Color(40,40,40)); } public static void stylePrimaryButton(JButton b) { b.setBackground(ACCENT); b.setForeground(Color.WHITE); b.setFocusPainted(false); } public static void styleNavButton(JButton b) { b.setBackground(new Color(48,51,57)); b.setForeground(Color.WHITE); b.setFocusPainted(false); } }
```

---

## DashboardUI (key panels)

```java
package main.ui;
import javax.swing.*; import javax.swing.table.DefaultTableModel; import java.awt.*; import java.util.*;
public class DashboardUI extends JFrame {
    public DashboardUI() { initFrame(); createMainContainer(); }
    private void initFrame() { setTitle("Dashboard"); setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); }
    private void createMainContainer() { CardLayout cardLayout = new CardLayout(); JPanel main = new JPanel(cardLayout); TaskTableModel taskModel = new TaskTableModel(MockData.getTasks()); main.add(new DashboardPanel(taskModel)); add(main); }
    static class DashboardPanel extends JScrollPane { public DashboardPanel(TaskTableModel model) { JPanel mainPanel = new JPanel(); mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS)); mainPanel.add(new DashboardColumnPanel("Today's Tasks", model)); setViewportView(mainPanel); } }
    static class DashboardColumnPanel extends JPanel { private TaskTableModel model; public DashboardColumnPanel(String title, TaskTableModel model) { this.model = model; setLayout(new BorderLayout()); add(new JLabel(title), BorderLayout.NORTH); add(new JScrollPane(new JTable(model)), BorderLayout.CENTER); } }
    static class TaskTableModel extends AbstractTableModel { private final List<NotionDashboard.Task> tasks; private final String[] cols = {"Task","Area"}; public TaskTableModel(List<NotionDashboard.Task> tasks) { this.tasks = tasks; } public int getRowCount() { return tasks.size(); } public int getColumnCount() { return cols.length; } public String getColumnName(int c) { return cols[c]; } public Object getValueAt(int r, int c) { NotionDashboard.Task t = tasks.get(r); if (c==0) return t.name; return t.area==null?"":t.area.name; } public void addNewTask() { tasks.add(new NotionDashboard.Task()); fireTableRowsInserted(tasks.size()-1,tasks.size()-1); } public void removeTaskAt(int idx) { tasks.remove(idx); fireTableRowsDeleted(idx, idx); } }
}
```

---

## ContentCreatorApp (constructor + addCard)

```java
package main.ui;
import javax.swing.*; import java.awt.*; import java.io.File; import javax.imageio.ImageIO;
public class ContentCreatorApp extends JPanel {
    private final JLayeredPane canvas;
    private final JScrollPane canvasScroll;
    private JButton addBtn; private JButton googleBtn;
    private static final String GOOGLE_ICON_RESOURCE = "/assets/google.png";
    private static final String GOOGLE_ICON_FALLBACK = System.getProperty("user.home") + File.separator + "...";
    public ContentCreatorApp() {
        setLayout(new BorderLayout()); canvas = new JLayeredPane(); canvas.setPreferredSize(new Dimension(1200,900)); canvasScroll = new JScrollPane(canvas); JPanel pageContent = new JPanel(new BorderLayout()); addBtn = new JButton("+"); googleBtn = createImageButton(62,62); pageContent.add(canvasScroll, BorderLayout.CENTER); add(NavigationBar.wrap(pageContent), BorderLayout.CENTER);
        addBtn.addActionListener(e -> addCard("Block", 80, 120, 360, 200));
    }
    private JButton createImageButton(int w, int h) { JButton b = new JButton(); try { java.net.URL res = getClass().getResource(GOOGLE_ICON_RESOURCE); if (res != null) b.setIcon(new ImageIcon(ImageIO.read(res).getScaledInstance(w-12,h-12,Image.SCALE_SMOOTH))); else b.setText("G"); } catch (Exception ex){ b.setText("G"); } return b; }
    private void addCard(String title, int x, int y, int w, int h) { JPanel card = new JPanel(); card.setBounds(x,y,w,h); card.add(new JLabel(title)); canvas.add(card, JLayeredPane.DEFAULT_LAYER); canvas.revalidate(); canvas.repaint(); }
}
```

---

If you'd like these snippets merged into `IMPORTANT_SNIPPETS.md` instead of an addendum, I can attempt that next (the earlier in-place patch failed). I can also:

- Add pre-check/DAO handling for the registration UNIQUE constraint (quick fix).
- Expand snippet coverage to other files (UITheme utilities, MailOrganize full methods, TaskDAO internals).

Which do you want me to do next? (If merging into the main MD is required, I will retry and explain the safer approach.)

---

## MailOrganize (key methods)

```java
package main.ui;
import main.db.FileDAO;
import javax.swing.*;import javax.swing.border.EmptyBorder;import java.awt.*;import java.io.*;import java.util.*;
public class MailOrganize extends JPanel {
    private final DynamicTableModel tableModel = new DynamicTableModel();
    private final JTable table = new JTable(tableModel);
    public MailOrganize() {
        setLayout(new BorderLayout()); setBackground(UITheme.BG); setBorder(new EmptyBorder(12,12,12,12));
        JPanel pageContent = new JPanel(new BorderLayout()); pageContent.setOpaque(false);
        JPanel top = new JPanel(new BorderLayout(12,12)); top.setBackground(UITheme.BG);
        JButton importBtn = createToolbarBtn("Import CSV", "/assets/csv-file-format-extension.png", new Color(64,160,255));
        JButton saveBtn = createToolbarBtn("Save", "/assets/google.png", new Color(0,150,136));
        top.add(importBtn, BorderLayout.EAST); pageContent.add(top, BorderLayout.NORTH);
        table.setFillsViewportHeight(true); table.setRowHeight(34); table.setShowGrid(true);
        JScrollPane tableScroll = new JScrollPane(table); tableScroll.setBorder(BorderFactory.createEmptyBorder());
        JPanel card = new JPanel(new BorderLayout()); card.setBackground(UITheme.SURFACE); card.add(tableScroll, BorderLayout.CENTER);
        pageContent.add(card, BorderLayout.CENTER);
        add(NavigationBar.wrap(pageContent), BorderLayout.CENTER);
        importBtn.addActionListener(e -> { JFileChooser fc = new JFileChooser(); if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) loadCsvFile(fc.getSelectedFile()); });
        saveBtn.addActionListener(e -> { try { String csv = tableModelToCsv(tableModel); int id = main.db.SheetDAO.saveSheet("active_sheet", csv); JOptionPane.showMessageDialog(this, "Saved sheet (id="+id+")"); } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Failed saving sheet: " + ex.getMessage()); } });
    }
    private void loadCsvFile(File f) {
        if (f == null || !f.exists()) return; try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)))) { String line; List<String[]> rows = new ArrayList<>(); while ((line = br.readLine()) != null) rows.add(parseCsvLine(line)); if (rows.isEmpty()) return; int maxCols = rows.stream().mapToInt(r->r.length).max().orElse(1); tableModel.clear(); for (int c=0;c<maxCols;c++) tableModel.addColumn("C"+(c+1)); for (String[] r : rows) tableModel.addRow(r); try { FileDAO.insertOrUpdate(f); } catch (Exception ignored) {} } catch (IOException ex) { JOptionPane.showMessageDialog(this, "Failed to load CSV: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); } }
    private String[] parseCsvLine(String line) { List<String> cells=new ArrayList<>(); StringBuilder cur=new StringBuilder(); boolean inQuote=false; for (int i=0;i<line.length();i++){char ch=line.charAt(i); if (ch=='"'){ if (inQuote && i+1<line.length() && line.charAt(i+1)=='"'){ cur.append('"'); i++; } else inQuote=!inQuote; } else if (ch==',' && !inQuote){ cells.add(cur.toString()); cur.setLength(0); } else cur.append(ch); } cells.add(cur.toString()); return cells.toArray(new String[0]); }
}
```

## Docupload (key methods)

```java
package main.ui;
import main.db.FileDAO;import javax.swing.*;import javax.swing.border.EmptyBorder;import java.awt.*;import java.io.File;import java.util.*;
public class Docupload extends JPanel {
    private final List<File> uploadedFiles = new ArrayList<>(); private final int workflowId;
    public Docupload() { this(0); }
    public Docupload(int workflowId) { this.workflowId = workflowId; initUI(); }
    private void initUI() {
        setLayout(new BorderLayout()); setBorder(new EmptyBorder(12,12,12,12)); JPanel pageContent = new JPanel(new BorderLayout(12,12)); pageContent.setOpaque(false);
        JButton uploadBtn = createUploadButton("Upload", UITheme.ACCENT, UITheme.SURFACE); pageContent.add(uploadBtn, BorderLayout.NORTH);
        JPanel listPanel = new JPanel(); listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS)); JScrollPane scroll = new JScrollPane(listPanel); pageContent.add(scroll, BorderLayout.CENTER);
        add(NavigationBar.wrap(pageContent), BorderLayout.CENTER);
        uploadBtn.addActionListener(e -> { JFileChooser fc = new JFileChooser(); fc.setMultiSelectionEnabled(true); if (fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) { File[] files = fc.getSelectedFiles(); int added=0; synchronized (uploadedFiles) { for (File f:files) { if (f!=null && f.exists() && !containsFile(uploadedFiles,f)) { try { FileDAO.insertOrUpdate(f, this.workflowId); } catch (Exception ignored) {} uploadedFiles.add(f); added++; } } } if (added>0) rebuildList(); } });
    }
    private boolean containsFile(List<File> list, File f){ for(File e:list) if (e.getAbsolutePath().equals(f.getAbsolutePath())) return true; return false; }
    private void rebuildList(){ /* simplified: UI rebuild omitted for brevity */ }
}
```

## WorkflowPage (key methods)

```java
package main.ui;
import main.db.TaskDAO;import javax.swing.*;import javax.swing.border.EmptyBorder;import java.awt.*;import java.util.List;
public class WorkflowPage extends JPanel {
    private final int workflowId; private JPanel listContentPanel; private JSplitPane splitPane;
    public WorkflowPage() { this(0, false); }
    public WorkflowPage(int workflowId, boolean createBlank) { this.workflowId = workflowId; initUI(!createBlank); }
    private void initUI(boolean loadTasks) {
        setLayout(new BorderLayout()); setBackground(UITheme.BG);
        JPanel listPanel = new JPanel(); listPanel.setLayout(new BorderLayout()); listPanel.setBorder(new EmptyBorder(25,25,25,25));
        listContentPanel = new JPanel(); listContentPanel.setLayout(new BoxLayout(listContentPanel, BoxLayout.Y_AXIS)); JScrollPane scroll = new JScrollPane(listContentPanel); listPanel.add(scroll, BorderLayout.CENTER);
        if (loadTasks) { try { List<TaskDAO.TaskRecord> tasks = (workflowId>0)?TaskDAO.listForWorkflow(workflowId):TaskDAO.listAll(); if (tasks.isEmpty()) addListItemNew("Enter a new task..."); else for (TaskDAO.TaskRecord tr:tasks) addListItem(tr); } catch (Exception ex) { addListItemNew("Enter a new task..."); } } else addListItemNew("");
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, new JPanel()); splitPane.setResizeWeight(0.6); add(splitPane, BorderLayout.CENTER);
    }
    private void addListItemNew(String text){ int ord = Math.max(0, listContentPanel.getComponentCount()/2); int id; try { id = TaskDAO.insert(text==null?"":text, false, ord, workflowId); } catch (Exception ex) { id = -1; } long now = System.currentTimeMillis()/1000L; TaskDAO.TaskRecord tr = new TaskDAO.TaskRecord(id, text==null?"":text, false, ord, workflowId, now, 0L, 0); addListItem(tr); }
    private void addListItem(TaskDAO.TaskRecord tr){ JPanel itemPanel = new JPanel(new BorderLayout(10,0)); itemPanel.setOpaque(false); JCheckBox checkBox = new JCheckBox(); checkBox.setOpaque(false); JTextField textField = new JTextField(tr.text==null||tr.text.isEmpty()?"Enter a new task...":tr.text); textField.addFocusListener(new java.awt.event.FocusAdapter(){ public void focusLost(java.awt.event.FocusEvent e){ String newText = textField.getText().trim(); if (tr.id>0) TaskDAO.update(tr.id, newText, checkBox.isSelected(), tr.ord); tr.text = newText; } }); itemPanel.add(checkBox, BorderLayout.WEST); itemPanel.add(textField, BorderLayout.CENTER); add(itemPanel); listContentPanel.add(Box.createVerticalStrut(10)); listContentPanel.add(itemPanel); listContentPanel.revalidate(); }
}
```

## InbuiltJavaTemplate1 (entry and embedding helper)

```java
package main.ui;
import javax.swing.*;import java.awt.*;
public class InbuiltJavaTemplate1 {
    private JFrame frame; private CardLayout cardLayout; private JPanel mainContentPanel; private int workflowId;
    public InbuiltJavaTemplate1() { this(-1); }
    public InbuiltJavaTemplate1(int workflowId) { this.workflowId = workflowId; initUI(); }
    private void initUI(){ frame = new JFrame("Productivity Hub"); frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); frame.setSize(1200,800); frame.setLocationRelativeTo(null); cardLayout = new CardLayout(); mainContentPanel = new JPanel(cardLayout); frame.add(createSidebar(), BorderLayout.WEST); addPagePanels(); frame.add(mainContentPanel, BorderLayout.CENTER); cardLayout.show(mainContentPanel, "PAGE1_DASHBOARD"); }
    private void addPagePanels(){ Page1DashboardPanel page1Dashboard = new Page1DashboardPanel(() -> cardLayout.show(mainContentPanel, "PLANNER_TASKS_PAGE"), ()->{} , ()->{}, ()->{}); PlannerTasksPage planner = new PlannerTasksPage(() -> cardLayout.show(mainContentPanel, "PAGE1_DASHBOARD"), this.workflowId); mainContentPanel.add(page1Dashboard, "PAGE1_DASHBOARD"); mainContentPanel.add(planner, "PLANNER_TASKS_PAGE"); }
    public JPanel getPanel(){ if (mainContentPanel==null) initUI(); return mainContentPanel; }
    public static JPanel createEmbeddedPanel(int workflowId){ InbuiltJavaTemplate1 t = new InbuiltJavaTemplate1(workflowId); return NavigationBar.wrap(t.getPanel()); }
}
```

## NewTemplate (main runner + embedding helper)

```java
import javax.swing.*;import java.awt.*;
public class NewTemplate {
    public static void main(String[] args){ SwingUtilities.invokeLater(() -> { JFrame frame = new JFrame("Work & Career Hub"); frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); frame.setSize(1400,900); frame.setLocationRelativeTo(null); frame.getContentPane().setBackground(Color.BLACK); frame.setLayout(new BorderLayout()); frame.add(new NewTemplatePanel(), BorderLayout.CENTER); frame.setVisible(true); }); }
    public static JPanel createEmbeddedPanel(){ return new NewTemplatePanel(); }
}
```
