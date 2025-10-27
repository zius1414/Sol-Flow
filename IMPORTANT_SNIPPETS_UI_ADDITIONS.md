# Additional UI snippets — Sol-Flow

Generated: October 27, 2025

This file contains sanitized, comment-free snippets for additional UI classes you asked to add (ContentCreatorApp, Docupload, InbuiltJavaTemplate1 planner/daily pages, MailOrganize).

---

`src/main/java/main/ui/ContentCreatorApp.java`

```java
package main.ui;

import javax.swing.*;
import java.awt.*;

public class ContentCreatorApp extends JPanel {
    private final JLayeredPane canvas;
    private final JScrollPane canvasScroll;
    private JButton addBtn;
    private JButton googleBtn;

    public ContentCreatorApp() {
        setLayout(new BorderLayout());
        this.canvas = new JLayeredPane();
        this.canvasScroll = new JScrollPane(canvas);
        add(NavigationBar.wrap(new JPanel(new BorderLayout())), BorderLayout.CENTER);
    }

    private void addCard(String title, int x, int y, int w, int h) {
        DraggableCard card = new DraggableCard(title, x, y, w, h);
        canvas.add(card, JLayeredPane.DEFAULT_LAYER);
        canvas.revalidate();
        canvas.repaint();
    }

    private class DraggableCard extends JPanel {
        public DraggableCard(String title, int x, int y, int w, int h) {
            setLayout(new BorderLayout());
            setBounds(x, y, w, h);
            JButton removeButton = new JButton("✖");
            removeButton.addActionListener(e -> { canvas.remove(this); canvas.revalidate(); canvas.repaint(); });
            add(removeButton, BorderLayout.NORTH);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Customize the Contents (Preview)");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(1200, 800);
            f.add(new ContentCreatorApp(), BorderLayout.CENTER);
            f.setVisible(true);
        });
    }
}
```

`src/main/java/main/ui/Docupload.java`

```java
package main.ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Docupload extends JPanel {
    private final JPanel listPanel = new JPanel();
    private final java.util.List<File> uploadedFiles = new java.util.ArrayList<>();
    private final int workflowId;

    public Docupload() { this(0); }
    public Docupload(int workflowId) {
        this.workflowId = workflowId;
        setLayout(new BorderLayout());
        JPanel pageContent = new JPanel(new BorderLayout());
        JButton uploadBtn = createUploadButton("Upload", UITheme.ACCENT, UITheme.SURFACE);
        pageContent.add(uploadBtn, BorderLayout.NORTH);
        add(NavigationBar.wrap(pageContent), BorderLayout.CENTER);
        rebuildList();
    }

    private void rebuildList() {
        listPanel.removeAll();
        synchronized (uploadedFiles) {
            for (File f : uploadedFiles) listPanel.add(new JLabel(f.getName()));
        }
        listPanel.revalidate(); listPanel.repaint();
    }

    private JButton createUploadButton(String text, Color accent, Color surface) {
        JButton btn = new JButton(text);
        btn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setMultiSelectionEnabled(true);
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                for (File f : fc.getSelectedFiles()) { if (!uploadedFiles.contains(f)) uploadedFiles.add(f); }
                rebuildList();
            }
        });
        return btn;
    }
}
```

`src/main/java/main/ui/InbuiltJavaTemplate1.java` (planner + daily)

```java
package main.ui;

import javax.swing.*;
import java.awt.*;
import main.db.TaskDAO;
import java.util.List;

public class InbuiltJavaTemplate1 {
    public static JPanel createEmbeddedPanel(int workflowId) {
        InbuiltJavaTemplate1 t = new InbuiltJavaTemplate1(workflowId);
        return NavigationBar.wrap(t.getPanel());
    }

    private JPanel getPanel() { return new JPanel(); }

    static class PlannerTasksPage extends JPanel {
        private final int workflowId;
        public PlannerTasksPage(Runnable backAction, int workflowId) {
            this.workflowId = workflowId;
            setLayout(new BorderLayout());
            loadTasksForWorkflow();
        }
        private void loadTasksForWorkflow() {
            List<TaskDAO.TaskRecord> tasks = TaskDAO.listForWorkflow(this.workflowId <= 0 ? 0 : this.workflowId);
        }
        private void createNewTask(String text) {
            if (text == null || text.trim().isEmpty()) return;
            int ord = 0;
            int id = TaskDAO.insert(text, false, ord, this.workflowId <= 0 ? 0 : this.workflowId);
            if (id != -1) loadTasksForWorkflow();
        }
    }

    static class DailyRoutinesPage extends JPanel {
        public DailyRoutinesPage(Runnable backAction) {
            setLayout(new BorderLayout());
            createRoutineList();
        }
        private void createRoutineList() {
            add(new JLabel("🧘 Meditate"));
            add(new JLabel("💪 Exercise"));
        }
    }
}
```

`src/main/java/main/ui/MailOrganize.java`

```java
package main.ui;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class MailOrganize extends JPanel {
    public MailOrganize() {
        setLayout(new BorderLayout());
        JPanel pageContent = new JPanel(new BorderLayout());
        add(NavigationBar.wrap(pageContent), BorderLayout.CENTER);
    }

    private void loadCsvFile(File f) {
        if (f == null || !f.exists()) return;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)))) {
            String line; java.util.List<String[]> rows = new java.util.ArrayList<>();
            while ((line = br.readLine()) != null) rows.add(line.split(","));
        } catch (IOException ex) { ex.printStackTrace(); }
    }
}
```
