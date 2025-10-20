package main.ui;

import main.db.FileDAO;
import main.db.SheetDAO;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;
import java.net.URL;

/**
 * Modernized MailOrganize panel - refreshed toolbar, polished tree and table visuals.
 * Functionality unchanged: file explorer (left), editable sheet (right), CSV/Excel import and DB save.
 */
public class MailOrganize extends JPanel {
    private static final String ACTIVE_SHEET_NAME = "active_sheet";
    private final DynamicTableModel tableModel = new DynamicTableModel();
    private final JTable table = new JTable(tableModel);

    private Timer autosaveTimer;
    private final DefaultMutableTreeNode rootNode;
    private final JTree tree;

    // Modern theme colors
    private static final Color APP_BG = UITheme.BG;
    private static final Color SURFACE = UITheme.SURFACE;
    private static final Color ACCENT = UITheme.ACCENT;
    private static final Color ACCENT_DARK = new Color(72, 0, 180);
    private static final Color MUTED = UITheme.MUTED;
    private static final Font HEAD_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font BTN_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);

public MailOrganize() {
    // build page content and then wrap with shared NavigationBar
    setLayout(new BorderLayout());
    setBackground(APP_BG);
    setBorder(new EmptyBorder(12,12,12,12));

    JPanel pageContent = new JPanel(new BorderLayout());
    pageContent.setOpaque(false);

    // Top toolbar
    JPanel top = new JPanel(new BorderLayout(12, 12));
    top.setBackground(APP_BG);
    top.setBorder(new EmptyBorder(6, 6, 6, 6));

        JLabel title = new JLabel("Sheets");
        title.setFont(HEAD_FONT);
        title.setForeground(new Color(26,26,26));
        top.add(title, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        JTextField search = new JTextField(22);
        search.setPreferredSize(new Dimension(260, 32));
        search.setFont(BTN_FONT);
        search.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220,220,220),1, true),
                new EmptyBorder(6,8,6,8))
        );
        search.setToolTipText("Search files / sheets");
        controls.add(search);

    JButton importBtn = createToolbarBtn("Import CSV", "/assets/csv-file-format-extension.png", new Color(64,160,255)); UITheme.stylePrimaryButton(importBtn);
    JButton importXls = createToolbarBtn("Import Excel", "/assets/excel.png", new Color(40,120,40)); UITheme.stylePrimaryButton(importXls);
    JButton saveBtn = createToolbarBtn("Save", "/assets/google.png", new Color(0,150,136)); UITheme.stylePrimaryButton(saveBtn);
        controls.add(importBtn);
        controls.add(importXls);
        controls.add(saveBtn);

        top.add(controls, BorderLayout.EAST);
        pageContent.add(top, BorderLayout.NORTH);

        // Left: File explorer tree
        File projectRoot = new File(System.getProperty("user.dir"));
        rootNode = new DefaultMutableTreeNode(new FileNode(projectRoot));
        tree = new JTree(rootNode);
        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new ModernFileTreeRenderer());
        tree.addTreeWillExpandListener(new DirExpandListener());
        tree.addMouseListener(new TreeMouseListener());
        populateChildren(rootNode);

        JScrollPane leftScroll = new JScrollPane(tree);
        leftScroll.setPreferredSize(new Dimension(320, 640));
        leftScroll.setBorder(BorderFactory.createEmptyBorder());
        leftScroll.getViewport().setBackground(APP_BG);

        // Right: sheet area with subtle card background
        JPanel right = new JPanel(new BorderLayout(10,10));
        right.setBackground(APP_BG);

        JPanel card = new JPanel(new BorderLayout(8,8));
        card.setBackground(SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230,230,235),1,true),
                new EmptyBorder(12,12,12,12)
        ));

        // small contextual toolbar inside card
        JPanel tbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        tbPanel.setOpaque(false);

        JButton addRow = smallToolBtn("Add row", "/assets/row.png", ACCENT);
        JButton addCol = smallToolBtn("Add col", "/assets/columns.png", ACCENT);
        JButton delRow = smallToolBtn("Del row", "/assets/delete-row.png", new Color(200,220,200));
        JButton delCol = smallToolBtn("Del col", "/assets/delete-col.png", new Color(200,220,200));
        tbPanel.add(addRow);
        tbPanel.add(addCol);
        tbPanel.add(Box.createHorizontalStrut(6));
        tbPanel.add(delRow);
        tbPanel.add(delCol);
        tbPanel.add(Box.createHorizontalStrut(8));

        JButton loadCsv = smallToolBtn("CSV", "/assets/csv-file-format-extension.png", new Color(64,160,255));
        JButton loadExcel = smallToolBtn("XLSX", "/assets/excel.png", new Color(40,120,40));
        tbPanel.add(loadCsv);
        tbPanel.add(loadExcel);

        tbPanel.add(Box.createHorizontalGlue());
        JLabel status = new JLabel("Ready");
        status.setForeground(MUTED);
        status.setFont(BTN_FONT);
        tbPanel.add(status);

        card.add(tbPanel, BorderLayout.NORTH);

        // Table styling
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFont(TABLE_FONT);
        table.setRowHeight(34); // taller rows for readability
        // use grid lines (subtle) instead of large intercell gaps
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setShowGrid(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(230, 230, 235));
        table.setSelectionBackground(new Color(220, 235, 255));
        table.setSelectionForeground(Color.BLACK);
        table.setOpaque(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(table.getTableHeader().getPreferredSize().width, 36));

        // custom header renderer
        JTableHeaderRenderer headerRenderer = new JTableHeaderRenderer();
        table.getTableHeader().setDefaultRenderer(headerRenderer);

        // alternating row renderer with subtle bottom divider
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(tableScroll, BorderLayout.CENTER);

        // wire buttons
        addRow.addActionListener(e -> { tableModel.addRow(); scrollToBottom(); });
        addCol.addActionListener(e -> { tableModel.addColumn("Col " + (tableModel.getColumnCount()+1)); });
        delRow.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) tableModel.removeRow(r);
        });
        delCol.addActionListener(e -> {
            int c = table.getSelectedColumn();
            if (c >= 0) tableModel.removeColumn(c);
        });

        loadCsv.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File sel = fc.getSelectedFile();
                loadCsvFile(sel);
                try { FileDAO.insertOrUpdate(sel); } catch (Exception ignored) {}
                status.setText("Loaded: " + sel.getName());
            }
        });
        loadExcel.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File sel = fc.getSelectedFile();
                loadExcelFile(sel);
                try { FileDAO.insertOrUpdate(sel); } catch (Exception ignored) {}
                status.setText("Loaded: " + sel.getName());
            }
        });

        // top-level toolbar import/save actions
        importBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                loadCsvFile(fc.getSelectedFile());
                status.setText("Imported CSV");
            }
        });
        importXls.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                loadExcelFile(fc.getSelectedFile());
                status.setText("Imported Excel");
            }
        });
        saveBtn.addActionListener(e -> {
            try {
                String csv = tableModelToCsv(tableModel);
                int id = SheetDAO.saveSheet(ACTIVE_SHEET_NAME, csv);
                JOptionPane.showMessageDialog(this, "Saved sheet (id=" + id + ")", "Saved", JOptionPane.INFORMATION_MESSAGE);
                status.setText("Saved");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed saving sheet to DB: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
                status.setText("Save failed");
            }
        });

        right.add(card, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, right);
        split.setResizeWeight(0.28);
        split.setOneTouchExpandable(true);
        split.setContinuousLayout(true);
        split.setDividerSize(8);
        split.setBorder(null);
        split.setDividerLocation(leftScroll.getPreferredSize().width);
        pageContent.add(split, BorderLayout.CENTER);

        // load last saved sheet
        try {
            SheetDAO.SheetRecord last = SheetDAO.getLastSavedSheet();
            if (last != null && last.csv != null && !last.csv.isEmpty()) {
                List<String[]> parsed = new ArrayList<>();
                try (BufferedReader br = new BufferedReader(new java.io.StringReader(last.csv))) {
                    String line;
                    while ((line = br.readLine()) != null) parsed.add(parseCsvLine(line));
                }
                int maxCols = 0;
                for (String[] r : parsed) if (r.length > maxCols) maxCols = r.length;
                if (maxCols == 0) maxCols = 1;
                tableModel.clear();
                for (int c = 0; c < maxCols; c++) tableModel.addColumn("C" + (c + 1));
                for (String[] r : parsed) tableModel.addRow(r);
            } else {
                tableModel.addColumn("Col 1");
                tableModel.addRow();
            }
        } catch (Exception ex) {
            tableModel.clear();
            tableModel.addColumn("Col 1");
            tableModel.addRow();
        }

        // autosave debounce
        autosaveTimer = new Timer(1500, e -> {
            autosaveTimer.stop();
            try {
                String csv = tableModelToCsv(tableModel);
                SheetDAO.saveSheet(ACTIVE_SHEET_NAME, csv);
                status.setText("Autosaved");
            } catch (Exception ignored) {}
        });
        autosaveTimer.setRepeats(false);

        tableModel.addTableModelListener(new TableModelListener() {
            @Override public void tableChanged(TableModelEvent e) {
                if (autosaveTimer.isRunning()) autosaveTimer.restart();
                else autosaveTimer.start();
            }
        });

        // header double-click rename
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) return;
                int col = table.columnAtPoint(e.getPoint());
                if (col < 0) return;
                String current = tableModel.getColumnName(col);
                String s = (String) JOptionPane.showInputDialog(
                        SwingUtilities.getWindowAncestor(MailOrganize.this),
                        "Column name:",
                        "Rename column",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        current);
                if (s != null && !s.trim().isEmpty()) tableModel.setColumnName(col, s.trim());
            }
        });

        // wrap and add navigation bar
        add(NavigationBar.wrap(pageContent), BorderLayout.CENTER);
     }

    // scroll to bottom after adding row
    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            int r = tableModel.getRowCount() - 1;
            if (r >= 0) table.scrollRectToVisible(table.getCellRect(r, 0, true));
        });
    }

    private void loadCsvFile(File f) {
        if (f == null || !f.exists()) return;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            List<String[]> rows = new ArrayList<>();
            while ((line = br.readLine()) != null) rows.add(parseCsvLine(line));
            if (rows.isEmpty()) return;
            int maxCols = 0;
            for (String[] r : rows) if (r.length > maxCols) maxCols = r.length;

            tableModel.clear();
            for (int c = 0; c < maxCols; c++) tableModel.addColumn("C" + (c+1));
            for (String[] r : rows) {
                String[] row = new String[maxCols];
                for (int i = 0; i < maxCols; i++) row[i] = (i < r.length) ? r[i] : "";
                tableModel.addRow(row);
            }

            try { FileDAO.insertOrUpdate(f); } catch (Exception ignored) {}
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load CSV: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // simple CSV parser (handles quoted commas)
    private String[] parseCsvLine(String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuote = false;
        for (int i=0;i<line.length();i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuote && i+1 < line.length() && line.charAt(i+1) == '"') {
                    cur.append('"'); i++;
                } else inQuote = !inQuote;
            } else if (ch == ',' && !inQuote) {
                cells.add(cur.toString());
                cur.setLength(0);
            } else cur.append(ch);
        }
        cells.add(cur.toString());
        return cells.toArray(new String[0]);
    }

    private void populateChildren(DefaultMutableTreeNode node) {
        node.removeAllChildren();
        FileNode fn = (FileNode) node.getUserObject();
        File f = fn.file;
        File[] files = f.listFiles();
        if (files == null) return;
        for (File child : files) {
            DefaultMutableTreeNode cnode = new DefaultMutableTreeNode(new FileNode(child));
            if (child.isDirectory()) cnode.add(new DefaultMutableTreeNode(Boolean.TRUE)); // placeholder
            node.add(cnode);
        }
    }

    private class DirExpandListener implements TreeWillExpandListener {
        @Override public void treeWillExpand(TreeExpansionEvent event) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
            populateChildren(node);
            ((DefaultTreeModel) MailOrganize.this.tree.getModel()).reload(node);
        }
        @Override public void treeWillCollapse(TreeExpansionEvent event) {}
    }

    private class TreeMouseListener extends MouseAdapter {
        @Override public void mouseClicked(MouseEvent e) {
            TreePath tp = MailOrganize.this.tree.getPathForLocation(e.getX(), e.getY());
            if (tp == null) return;
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent();
            FileNode fn = (FileNode) node.getUserObject();
            if (e.getClickCount() == 2 && fn.file.isFile()) {
                // double-click: if csv, load; else show basic info
                if (fn.file.getName().toLowerCase().endsWith(".csv")) loadCsvFile(fn.file);
                else JOptionPane.showMessageDialog(MailOrganize.this, fn.file.getAbsolutePath(), "File", JOptionPane.INFORMATION_MESSAGE);

                // persist opened file
                try { FileDAO.insertOrUpdate(fn.file); } catch (Exception ignored) {}
            }
        }
    }

    // ------- Renderers & helpers for modern look -------

    private static class ModernFileTreeRenderer extends DefaultTreeCellRenderer {
        private final Icon folderIcon = UIManager.getIcon("FileView.directoryIcon");
        private final Icon fileIcon = UIManager.getIcon("FileView.fileIcon");
        @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object u = node.getUserObject();
            if (u instanceof FileNode) {
                FileNode fn = (FileNode) u;
                setText(fn.toString());
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                setForeground(sel ? Color.WHITE : new Color(32,32,32));
                setOpaque(sel);
                setBackgroundNonSelectionColor(new Color(0,0,0,2));
                if (fn.file.isDirectory()) setIcon(folderIcon);
                else setIcon(fileIcon);
            }
            return this;
        }
    }

    private static class JTableHeaderRenderer extends DefaultTableCellRenderer {
        JTableHeaderRenderer() {
            setHorizontalAlignment(CENTER);
            setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setBackground(new Color(245,247,250));
            setForeground(new Color(50,50,50));
        }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            return this;
        }
    }

    private static class AlternatingRowRenderer extends DefaultTableCellRenderer {
        private final Color EVEN = new Color(255,255,255);
        private final Color ODD = new Color(250,251,253);
        private final Color GRID = new Color(235,237,240);
        AlternatingRowRenderer() {
            setBorder(new EmptyBorder(6,8,6,8));
        }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(TABLE_FONT);
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground((row % 2 == 0) ? EVEN : ODD);
                setForeground(new Color(30,30,30));
            }
            return this;
        }
    }

    private static class FileNode {
        final File file;
        FileNode(File f) { file = f; }
        @Override public String toString() { return file.getName().isEmpty() ? file.getAbsolutePath() : file.getName(); }
    }

    // DynamicTableModel unchanged (keeps existing behavior)
    private static class DynamicTableModel extends AbstractTableModel {
        private final List<String> cols = new ArrayList<>();
        private final List<List<String>> rows = new ArrayList<>();

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.size(); }
        @Override public String getColumnName(int column) { return cols.get(column); }
        @Override public Object getValueAt(int rowIndex, int columnIndex) {
            List<String> r = rows.get(rowIndex);
            if (columnIndex < r.size()) return r.get(columnIndex);
            return "";
        }
        @Override public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            List<String> r = rows.get(rowIndex);
            while (columnIndex >= r.size()) r.add("");
            r.set(columnIndex, aValue == null ? "" : aValue.toString());
            fireTableCellUpdated(rowIndex, columnIndex);
        }
        @Override public boolean isCellEditable(int rowIndex, int columnIndex) { return true; }

        void setColumnName(int idx, String name) {
            if (idx < 0 || idx >= cols.size()) return;
            cols.set(idx, name);
            fireTableStructureChanged();
        }

        void addColumn(String name) {
            cols.add(name);
            for (List<String> r : rows) r.add("");
            fireTableStructureChanged();
        }

        void removeColumn(int idx) {
            if (idx < 0 || idx >= cols.size()) return;
            cols.remove(idx);
            for (List<String> r : rows) if (idx < r.size()) r.remove(idx);
            fireTableStructureChanged();
        }

        void addRow() { addRow(new String[cols.size()]); }

        void addRow(String[] values) {
            List<String> r = new ArrayList<>();
            for (int i = 0; i < cols.size(); i++) r.add(i < values.length ? values[i] : "");
            rows.add(r);
            fireTableRowsInserted(rows.size()-1, rows.size()-1);
        }

        void removeRow(int idx) {
            if (idx < 0 || idx >= rows.size()) return;
            rows.remove(idx);
            fireTableRowsDeleted(idx, idx);
        }

        void clear() {
            cols.clear();
            rows.clear();
            fireTableStructureChanged();
        }
    }

    // Save table to CSV
    private void saveTableToCsv(File out) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out), StandardCharsets.UTF_8))) {
            int cols = tableModel.getColumnCount();
            for (int c = 0; c < cols; c++) {
                if (c > 0) bw.write(',');
                bw.write(escapeCsv(tableModel.getColumnName(c)));
            }
            bw.write('\n');
            for (int r = 0; r < tableModel.getRowCount(); r++) {
                for (int c = 0; c < cols; c++) {
                    if (c > 0) bw.write(',');
                    Object v = tableModel.getValueAt(r, c);
                    bw.write(escapeCsv(v == null ? "" : v.toString()));
                }
                bw.write('\n');
            }
        }
    }

    private String escapeCsv(String v) {
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    private String tableModelToCsv(AbstractTableModel m) {
        StringBuilder sb = new StringBuilder();
        int cols = m.getColumnCount();
        for (int c = 0; c < cols; c++) {
            if (c > 0) sb.append(',');
            sb.append(escapeCsv(m.getColumnName(c)));
        }
        sb.append('\n');
        for (int r = 0; r < m.getRowCount(); r++) {
            for (int c = 0; c < cols; c++) {
                if (c > 0) sb.append(',');
                Object v = m.getValueAt(r, c);
                sb.append(escapeCsv(v == null ? "" : v.toString()));
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    // modern small toolbar button
    private JButton smallToolBtn(String tip, String iconPath, Color bg) {
        Icon ic = loadIcon(iconPath, 16, 16);
        JButton b = new JButton();
        b.setToolTipText(tip);
        b.setPreferredSize(new Dimension(84, 34));
        b.setFocusPainted(false);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(BTN_FONT);
        b.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (ic != null) b.setIcon(ic);
        else b.setText(tip);
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(bg.darker()); }
            @Override public void mouseExited(MouseEvent e) { b.setBackground(bg); }
        });
        return b;
    }

    // top toolbar button with label
    private JButton createToolbarBtn(String label, String iconPath, Color bg) {
        Icon ic = loadIcon(iconPath, 18, 18);
        JButton b = new JButton(label, ic);
        b.setFont(BTN_FONT);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0,0,0,20),1,true),
                new EmptyBorder(6,10,6,10)
        ));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(bg.darker()); }
            @Override public void mouseExited(MouseEvent e) { b.setBackground(bg); }
        });
        return b;
    }

    // Try to load .xls/.xlsx using Apache POI if available; otherwise prompt
    private void loadExcelFile(File f) {
        if (f == null || !f.exists()) return;
        try (InputStream is = new FileInputStream(f)) {
            Class<?> wf = Class.forName("org.apache.poi.ss.usermodel.WorkbookFactory");
            Object wb = wf.getMethod("create", InputStream.class).invoke(null, is);
            Class<?> wbClass = wb.getClass();
            Object sheet = wbClass.getMethod("getSheetAt", int.class).invoke(wb, 0);
            Class<?> sheetClass = sheet.getClass();
            java.util.List<String[]> rows = new ArrayList<>();
            java.lang.reflect.Method rowIterM = sheetClass.getMethod("iterator");
            java.util.Iterator<?> rowIt = (java.util.Iterator<?>) rowIterM.invoke(sheet);
            Class<?> dataFormatterClass = Class.forName("org.apache.poi.ss.usermodel.DataFormatter");
            Object df = dataFormatterClass.getDeclaredConstructor().newInstance();
            while (rowIt.hasNext()) {
                Object row = rowIt.next();
                Class<?> rowClass = row.getClass();
                java.lang.reflect.Method cellIterM = rowClass.getMethod("cellIterator");
                java.util.Iterator<?> cellIt = (java.util.Iterator<?>) cellIterM.invoke(row);
                java.util.List<String> cols = new ArrayList<>();
                while (cellIt.hasNext()) {
                    Object cell = cellIt.next();
                    String val = (String) dataFormatterClass.getMethod("formatCellValue", Class.forName("org.apache.poi.ss.usermodel.Cell")).invoke(df, cell);
                    cols.add(val);
                }
                rows.add(cols.toArray(new String[0]));
            }
            tableModel.clear();
            int maxCols = 0;
            for (String[] r : rows) if (r.length > maxCols) maxCols = r.length;
            if (maxCols == 0) maxCols = 1;
            for (int c = 0; c < maxCols; c++) tableModel.addColumn("C" + (c+1));
            for (String[] r : rows) tableModel.addRow(r);
            try { wbClass.getMethod("close").invoke(wb); } catch (Exception ignore) {}
            try { FileDAO.insertOrUpdate(f); } catch (Exception ignored) {}
        } catch (ClassNotFoundException cnf) {
            JOptionPane.showMessageDialog(this, "Apache POI not found. Add org.apache.poi:poi and poi-ooxml to load Excel files.", "Dependency required", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load Excel: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // load icon helper (classpath or resources)
    private Icon loadIcon(String resourcePath, int w, int h) {
        try {
            URL url = MailOrganize.class.getResource(resourcePath);
            if (url != null) {
                try {
                    BufferedImage img = ImageIO.read(url);
                    if (img != null) return new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
                } catch (Exception ignored) {}
                ImageIcon raw = new ImageIcon(url);
                if (raw.getIconWidth() > 0) return new ImageIcon(raw.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
            }
            File f = new File(System.getProperty("user.dir") + "/src/main/resources" + resourcePath);
            if (f.exists()) {
                try {
                    BufferedImage img = ImageIO.read(f);
                    if (img != null) return new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
                } catch (Exception ignored) {}
                ImageIcon rawFile = new ImageIcon(f.toURI().toURL());
                if (rawFile.getIconWidth() > 0) return new ImageIcon(rawFile.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
            }
        } catch (Exception ignored) {}
        return null;
    }

    // quick test frame
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Mail Organize - Modern");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(1100, 700);
            f.setLocationRelativeTo(null);
            f.setContentPane(new MailOrganize());
            f.setVisible(true);
        });
    }
}
