package main.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;


/**
 * A modern, dark-themed dashboard UI replicated from screenshots.
 * This class creates a window with several custom-styled buttons, each with a unique icon.
 * The application is built to be efficient, error-free, and aesthetically pleasing.
 */
public class DashboardUI extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContainer;
    private JPanel dashboardPanel;
    private FinancesPanel financesPanel;
    // The WorkAndCareerPanel has been removed.

    public DashboardUI() {
        initFrame();
        createMainContainer();
        createDashboardPanel();
        createFinancesPanel();
        
        mainContainer.add(dashboardPanel, "DASHBOARD");
        mainContainer.add(financesPanel, "FINANCES");
        // The WorkAndCareerPanel is no longer added to the main container.

        add(mainContainer);
        addEventListeners();
    }

    private void initFrame() {
        setTitle("Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Make the frame full screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // Apply a consistent dark theme to components used in custom dialogs
        UIManager.put("Panel.background", Color.BLACK);
        UIManager.put("Label.foreground", Color.WHITE);
        UIManager.put("TextField.background", new Color(0x4B5563));
        UIManager.put("TextField.foreground", Color.WHITE);
        UIManager.put("TextField.caretForeground", Color.WHITE);
        UIManager.put("Button.background", new Color(0x3B82F6));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.focus", new Color(0x3B82F6));
        UIManager.put("TextArea.background", new Color(0x4B5563));
        UIManager.put("TextArea.foreground", Color.WHITE);
        UIManager.put("TextArea.caretForeground", Color.WHITE);
        
        // Force dark theme for tables, which can be tricky with Nimbus L&F
        UIManager.put("Table.background", new Color(0x374151));
        UIManager.put("Table.foreground", Color.WHITE);
        UIManager.put("Table.gridColor", new Color(0x4B5563));
        UIManager.put("TableHeader.background", new Color(0x1F2937));
        UIManager.put("TableHeader.foreground", Color.WHITE);
        UIManager.put("Table.selectionBackground", new Color(0x3B82F6));
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("ScrollPane.background", new Color(0x1F2937));
        UIManager.put("Viewport.background", new Color(0x374151));
    }

    private void createMainContainer() {
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
    }

    private void createDashboardPanel() {
        dashboardPanel = new JPanel(new GridBagLayout());
        dashboardPanel.setBackground(new Color(0x1F2937));
        dashboardPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.weightx = 1.0;

        dashboardPanel.add(new RoundedButton("Finances", new FinanceIcon()), gbc);
        dashboardPanel.add(new RoundedButton("Work & Career", new BriefcaseIcon()), gbc);
        dashboardPanel.add(new RoundedButton("Fitness & Workouts", new DumbbellIcon()), gbc);
        dashboardPanel.add(new RoundedButton("Personal CRM", new CrmIcon()), gbc);
        dashboardPanel.add(new RoundedButton("Spots & Memberships", new MapPinIcon()), gbc);
        dashboardPanel.add(new RoundedButton("Knowledge & Content", new KnowledgeIcon()), gbc);
        dashboardPanel.add(new RoundedButton("Wishlist & Shopping List", new ShoppingIcon()), gbc);
        dashboardPanel.add(new RoundedButton("Travel", new TravelIcon()), gbc);
    }

    private void createFinancesPanel() {
        financesPanel = new FinancesPanel();
    }
    
    // createWorkAndCareerPanel method has been removed.

    private void addEventListeners() {
        // This listener will be on the "Finances" button on the main dashboard
        ((RoundedButton) dashboardPanel.getComponent(0)).addActionListener(e -> cardLayout.show(mainContainer, "FINANCES"));
        
        // Listener for Work & Career button is removed. It is now inactive.

        // Add listener to the back button inside the finances panel
        financesPanel.getBackButton().addActionListener(e -> cardLayout.show(mainContainer, "DASHBOARD"));
        
        // Add a window listener to save data when the application is closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                financesPanel.saveAllData();
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DashboardUI().setVisible(true));
    }
}

/**
 * A panel dedicated to displaying the detailed finance dashboard.
 */
class FinancesPanel extends JPanel {
    private JButton backButton;
    private TransactionTablePanel incomeTablePanel;
    private TransactionTablePanel expenseTablePanel;
    private TransactionTablePanel budgetTablePanel;
    private SpendingAreasPanel spendingAreasPanel;
    private CategoryDetailPanel categoryDetailPanel;


    private CardLayout mainFinanceLayout;
    private CardLayout contentCardLayout;
    private JPanel contentPanel;
    private JButton incomeViewButton, expenseViewButton, budgetViewButton, spendingAreasButton;


    public FinancesPanel() {
        mainFinanceLayout = new CardLayout();
        setLayout(mainFinanceLayout);
        setBackground(new Color(0x111827)); // Even darker background
        
        JPanel mainViewsPanel = new JPanel(new BorderLayout(10, 10));
        mainViewsPanel.setOpaque(false);
        mainViewsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        mainViewsPanel.add(createHeader(), BorderLayout.NORTH);
        mainViewsPanel.add(createContentCards(), BorderLayout.CENTER);
        
        categoryDetailPanel = new CategoryDetailPanel(budgetTablePanel.getModel());
        
        add(mainViewsPanel, "VIEWS");
        add(categoryDetailPanel, "DETAIL");
        
        loadAllData(); // Load data when the panel is created
        addEventListeners();
        updateActiveViewButton(incomeViewButton); // Set initial active button
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        leftPanel.setOpaque(false);
        backButton = new JButton("<- Back to Dashboard");
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(new Color(0x3B82F6));
        backButton.setFocusPainted(false);
        leftPanel.add(backButton);

        JLabel title = new JLabel("Finances");
        title.setFont(new Font("Inter", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        leftPanel.add(title);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        centerPanel.setOpaque(false);
        incomeViewButton = new JButton("Income Log");
        expenseViewButton = new JButton("Expense Log");
        budgetViewButton = new JButton("Spending by Category");
        spendingAreasButton = new JButton("Top Spending Areas");


        styleViewButton(incomeViewButton);
        styleViewButton(expenseViewButton);
        styleViewButton(budgetViewButton);
        styleViewButton(spendingAreasButton);

        centerPanel.add(incomeViewButton);
        centerPanel.add(expenseViewButton);
        centerPanel.add(budgetViewButton);
        centerPanel.add(spendingAreasButton);

        header.add(leftPanel, BorderLayout.WEST);
        header.add(centerPanel, BorderLayout.CENTER);

        return header;
    }

    private void styleViewButton(JButton button) {
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(0x374151));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(5, 15, 5, 15));
    }
    
    private void styleDialogButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(5, 15, 5, 15));
    }

    private JPanel createContentCards() {
        contentCardLayout = new CardLayout();
        contentPanel = new JPanel(contentCardLayout);
        contentPanel.setOpaque(false);
        
        Runnable dataChangedCallback = () -> {
            spendingAreasPanel.updateData(expenseTablePanel.getModel());
        };
        
        Consumer<String> onCardClickCallback = categoryName -> {
            categoryDetailPanel.updateView(categoryName, expenseTablePanel.getModel());
            mainFinanceLayout.show(this, "DETAIL");
        };

        contentPanel.add(createIncomePanel(dataChangedCallback), "INCOME");
        contentPanel.add(createExpensePanel(dataChangedCallback), "EXPENSES");
        contentPanel.add(createBudgetTrackerPanel(dataChangedCallback), "BUDGET");
        contentPanel.add(createSpendingAreasPanel(dataChangedCallback, onCardClickCallback), "SPENDING_AREAS");


        return contentPanel;
    }

    private JPanel createIncomePanel(Runnable onDataChanged) {
        String[] headers = {"Transaction", "From", "Amount", "Category", "Transaction Date", "Attachment"};
        incomeTablePanel = new TransactionTablePanel("Income Log", headers, 2, onDataChanged);
        return incomeTablePanel;
    }

    private JPanel createExpensePanel(Runnable onDataChanged) {
        String[] headers = {"Transaction", "Amount", "Category", "Sentiment", "Vendor", "Transaction Date", "Image Path"};
        expenseTablePanel = new TransactionTablePanel("Expense Log", headers, 1, onDataChanged);
        return expenseTablePanel;
    }

    private JPanel createBudgetTrackerPanel(Runnable onDataChanged) {
        String[] headers = {"Category", "Budget", "Monthly Avg", "Status", "Image Path", "Notes", "Manual Spend"};
        budgetTablePanel = new TransactionTablePanel("Spending by Category", headers, 1, onDataChanged); // Budget is at col 1
        return budgetTablePanel;
    }
    
    private JPanel createSpendingAreasPanel(Runnable onDataChanged, Consumer<String> onCardClick) {
        spendingAreasPanel = new SpendingAreasPanel(onCardClick, budgetTablePanel.getModel());
        return spendingAreasPanel;
    }

    private void addEventListeners() {
        // View switcher listeners
        incomeViewButton.addActionListener(e -> {
            contentCardLayout.show(contentPanel, "INCOME");
            updateActiveViewButton(incomeViewButton);
        });
        expenseViewButton.addActionListener(e -> {
            contentCardLayout.show(contentPanel, "EXPENSES");
            updateActiveViewButton(expenseViewButton);
        });
        budgetViewButton.addActionListener(e -> {
            contentCardLayout.show(contentPanel, "BUDGET");
            updateActiveViewButton(budgetViewButton);
        });
        spendingAreasButton.addActionListener(e -> {
            contentCardLayout.show(contentPanel, "SPENDING_AREAS");
            updateActiveViewButton(spendingAreasButton);
        });
        
        // Back from detail view listener
        categoryDetailPanel.getBackButton().addActionListener(e -> mainFinanceLayout.show(this, "VIEWS"));


        // Add new income
        incomeTablePanel.getNewButton().addActionListener(e -> showIncomeDialog());

        // Add new expense
        expenseTablePanel.getNewButton().addActionListener(e -> showExpenseDialog());

        // Add new budget category (shared by budget table and spending areas)
        budgetTablePanel.getNewButton().addActionListener(e -> showBudgetDialog());
        spendingAreasPanel.getNewButton().addActionListener(e -> showBudgetDialog());


        // Delete listeners
        incomeTablePanel.getDeleteButton().addActionListener(e -> deleteSelectedRows(incomeTablePanel.getTable(), "income"));
        expenseTablePanel.getDeleteButton().addActionListener(e -> deleteSelectedRows(expenseTablePanel.getTable(), "expense"));
        budgetTablePanel.getDeleteButton().addActionListener(e -> deleteSelectedRows(budgetTablePanel.getTable(), "budget category"));
    }

    private void showIncomeDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Income", true);
        dialog.getContentPane().setBackground(new Color(0x374151));
        dialog.setLayout(new BorderLayout(10, 10));
    
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField transactionField = new JTextField();
        JTextField fromField = new JTextField();
        JTextField amountField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField dateField = new JTextField();
        JTextField attachmentField = new JTextField();

        panel.add(new JLabel("Transaction:")); panel.add(transactionField);
        panel.add(new JLabel("From:")); panel.add(fromField);
        panel.add(new JLabel("Amount:")); panel.add(amountField);
        panel.add(new JLabel("Category:")); panel.add(categoryField);
        panel.add(new JLabel("Date:")); panel.add(dateField);
        panel.add(new JLabel("Attachment:")); panel.add(attachmentField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);
        JButton okButton = new JButton("OK");
        styleDialogButton(okButton, new Color(0x3B82F6));
        JButton cancelButton = new JButton("Cancel");
        styleDialogButton(cancelButton, new Color(0x4B5563));

        okButton.addActionListener(e -> {
            incomeTablePanel.getModel().addRow(new Object[]{
                    transactionField.getText(), fromField.getText(), "$" + amountField.getText(),
                    categoryField.getText(), dateField.getText(), attachmentField.getText()
            });
            dialog.dispose();
        });
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showExpenseDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Expense", true);
        dialog.getContentPane().setBackground(new Color(0x374151));
        dialog.setLayout(new BorderLayout(10, 10));
        
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JTextField transactionField = new JTextField();
        JTextField amountField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField sentimentField = new JTextField();
        JTextField vendorField = new JTextField();
        JTextField dateField = new JTextField();
        JTextField imagePathField = new JTextField();

        panel.add(new JLabel("Transaction:")); panel.add(transactionField);
        panel.add(new JLabel("Amount:")); panel.add(amountField);
        panel.add(new JLabel("Category:")); panel.add(categoryField);
        panel.add(new JLabel("Sentiment:")); panel.add(sentimentField);
        panel.add(new JLabel("Vendor:")); panel.add(vendorField);
        panel.add(new JLabel("Date:")); panel.add(dateField);
        panel.add(new JLabel("Image Path (Optional):")); panel.add(imagePathField);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);
        JButton okButton = new JButton("OK");
        styleDialogButton(okButton, new Color(0x3B82F6));
        JButton cancelButton = new JButton("Cancel");
        styleDialogButton(cancelButton, new Color(0x4B5563));

        okButton.addActionListener(e -> {
            expenseTablePanel.getModel().addRow(new Object[]{
                    transactionField.getText(), "$" + amountField.getText(), categoryField.getText(),
                    sentimentField.getText(), vendorField.getText(), dateField.getText(), imagePathField.getText()
            });
            dialog.dispose();
        });
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void showBudgetDialog(){
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Budget Category", true);
        dialog.getContentPane().setBackground(new Color(0x374151));
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JTextField categoryField = new JTextField();
        JTextField budgetField = new JTextField();
        JTextField avgField = new JTextField();
        JTextField statusField = new JTextField();
        JTextField imagePathField = new JTextField();

        panel.add(new JLabel("Category:")); panel.add(categoryField);
        panel.add(new JLabel("Budget:")); panel.add(budgetField);
        panel.add(new JLabel("Monthly Avg:")); panel.add(avgField);
        panel.add(new JLabel("Status:")); panel.add(statusField);
        panel.add(new JLabel("Image Path (Optional):")); panel.add(imagePathField);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);
        JButton okButton = new JButton("OK");
        styleDialogButton(okButton, new Color(0x3B82F6));
        JButton cancelButton = new JButton("Cancel");
        styleDialogButton(cancelButton, new Color(0x4B5563));

        okButton.addActionListener(e -> {
            budgetTablePanel.getModel().addRow(new Object[] {
                categoryField.getText(), "$" + budgetField.getText(),
                "$" + avgField.getText(), statusField.getText(),
                imagePathField.getText(), "", "" // Empty strings for Notes and Manual Spend
            });
            dialog.dispose();
        });
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void deleteSelectedRows(JTable table, String itemType) {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length == 0) {
            showCustomErrorDialog("Please select one or more rows to delete.", "No Rows Selected");
            return;
        }

        boolean confirmed = showCustomConfirmDialog("Are you sure you want to delete the selected " + selectedRows.length + " " + itemType + "(s)?", "Confirm Deletion");

        if (confirmed) {
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            for (int i = selectedRows.length - 1; i >= 0; i--) {
                model.removeRow(table.convertRowIndexToModel(selectedRows[i]));
            }
        }
    }
    
    private boolean showCustomConfirmDialog(String message, String title) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(new Color(0x374151));

        JLabel messageLabel = new JLabel(message);
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setBorder(new EmptyBorder(20, 20, 10, 20));
        dialog.add(messageLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);

        JButton yesButton = new JButton("Yes");
        styleDialogButton(yesButton, new Color(0xEF4444)); // Red for confirmation of deletion
        
        final boolean[] confirmed = {false};

        yesButton.addActionListener(e -> {
            confirmed[0] = true;
            dialog.dispose();
        });

        JButton noButton = new JButton("No");
        styleDialogButton(noButton, new Color(0x4B5563));
        noButton.addActionListener(e -> {
            dialog.dispose();
        });

        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        
        return confirmed[0];
    }
    
    private void showCustomErrorDialog(String message, String title) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(new Color(0x374151));

        JLabel messageLabel = new JLabel(message);
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setBorder(new EmptyBorder(20, 20, 10, 20));
        dialog.add(messageLabel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);
        JButton okButton = new JButton("OK");
        styleDialogButton(okButton, new Color(0x3B82F6));
        okButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(okButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void updateActiveViewButton(JButton activeButton) {
        // Reset all buttons to default style
        styleViewButton(incomeViewButton);
        styleViewButton(expenseViewButton);
        styleViewButton(budgetViewButton);
        styleViewButton(spendingAreasButton);

        
        // Highlight the active button
        activeButton.setBackground(new Color(0x3B82F6));
    }
    
    // --- Data Persistence Methods ---
    
    public void saveAllData() {
        saveTableData(incomeTablePanel.getModel(), "income.csv");
        saveTableData(expenseTablePanel.getModel(), "expenses.csv");
        saveTableData(budgetTablePanel.getModel(), "budget.csv");
    }
    
    private void saveTableData(DefaultTableModel model, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (int row = 0; row < model.getRowCount(); row++) {
                for (int col = 0; col < model.getColumnCount(); col++) {
                    // Enclose values with commas in quotes
                    String value = String.valueOf(model.getValueAt(row, col));
                    if (value.contains(",")) {
                        writer.print("\"" + value.replace("\"", "\"\"") + "\"");
                    } else {
                        writer.print(value);
                    }
                    if (col < model.getColumnCount() - 1) {
                        writer.print(",");
                    }
                }
                writer.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showCustomErrorDialog("Error saving data to " + filename, "Save Error");
        }
    }
    
    public void loadAllData() {
        boolean incomeLoaded = loadTableData(incomeTablePanel.getModel(), "income.csv");
        if (!incomeLoaded) {
            addSampleIncomeData();
        }

        boolean expensesLoaded = loadTableData(expenseTablePanel.getModel(), "expenses.csv");
        if (!expensesLoaded) {
            addSampleExpenseData();
        }
        
        boolean budgetLoaded = loadTableData(budgetTablePanel.getModel(), "budget.csv");
        if (!budgetLoaded) {
            addSampleBudgetData();
        }

        // Initial update for the spending areas panel
        spendingAreasPanel.updateData(expenseTablePanel.getModel());
    }

    private boolean loadTableData(DefaultTableModel model, String filename) {
        File file = new File(filename);
        if (!file.exists() || file.length() == 0) {
            return false; // Indicate that data was not loaded
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            model.setRowCount(0); // Clear existing data before loading
            while ((line = reader.readLine()) != null) {
                // Split by comma, but not if it's inside quotes
                String[] rowData = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                // Ensure the rowData length matches the model's column count, add empty strings if shorter
                if (rowData.length < model.getColumnCount()) {
                    String[] newRowData = new String[model.getColumnCount()];
                    System.arraycopy(rowData, 0, newRowData, 0, rowData.length);
                    for (int i = rowData.length; i < model.getColumnCount(); i++) {
                        newRowData[i] = ""; // Fill with empty string
                    }
                    model.addRow(newRowData);
                } else {
                    model.addRow(rowData);
                }
            }
            return true; // Indicate successful load
        } catch (IOException e) {
            e.printStackTrace();
            showCustomErrorDialog("Error loading data from " + filename, "Load Error");
            return false;
        }
    }
    
    private void addSampleIncomeData() {
        DefaultTableModel model = incomeTablePanel.getModel();
        model.addRow(new Object[]{"Paycheck", "Acme Inc.", "$2,500.00", "Salary", "Jan 19, 2023", ""});
        model.addRow(new Object[]{"Illustration Work", "James W.", "$500.00", "Side Hustle", "Jan 19, 2023", ""});
    }
    
    private void addSampleExpenseData() {
        DefaultTableModel model = expenseTablePanel.getModel();
        model.addRow(new Object[]{"Monthly Transit Pass", "$137.00", "Transportation", "Neutral", "MTA", "Jan 19, 2023", ""});
        model.addRow(new Object[]{"Monthly Rent", "$2,500.00", "Housing", "Neutral", "Casey T.", "Jan 19, 2023", ""});
        model.addRow(new Object[]{"Concert Tickets", "$318.00", "Entertainment", "Good", "Various", "Jan 20, 2023", ""});
        model.addRow(new Object[]{"New Shoes", "$248.40", "Clothing", "Neutral", "Nike", "Jan 15, 2023", ""});
        model.addRow(new Object[]{"Gift Card for Kevin", "$25.00", "Gifting", "Good", "Starbucks", "Jan 19, 2023", ""});
        model.addRow(new Object[]{"Groceries", "$80.00", "Food & Drink", "Neutral", "Whole Foods", "Jan 21, 2023", ""});
    }
    
    private void addSampleBudgetData() {
        DefaultTableModel model = budgetTablePanel.getModel();
        model.addRow(new Object[]{"Housing", "$2,500.00", "$2,500.00", "Exact", "", "Rent is due on the 1st.", ""});
        model.addRow(new Object[]{"Entertainment", "$250.00", "$318.00", "Needs Improvement", "", "", "$300.00"});
        model.addRow(new Object[]{"Gifting", "$40.00", "$25.00", "Doing Great", "", "", ""});
        model.addRow(new Object[]{"Transportation", "$150.00", "$137.00", "Doing Great", "", "", ""});
        model.addRow(new Object[]{"Food & Drink", "$300.00", "$80.00", "Doing Great", "", "Trying to eat out less.", ""});
    }


    public JButton getBackButton() {
        return backButton;
    }
}

/**
 * A panel for displaying spending categories in a card-based gallery view.
 */
class SpendingAreasPanel extends JPanel {
    private JPanel galleryPanel;
    private JButton newButton;
    private Consumer<String> onCardClickCallback;
    private DefaultTableModel budgetModel; // Added to get image path

    public SpendingAreasPanel(Consumer<String> onCardClickCallback, DefaultTableModel budgetModel) {
        this.onCardClickCallback = onCardClickCallback;
        this.budgetModel = budgetModel;
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(0x1F2937));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(createHeader(), BorderLayout.NORTH);
        
        galleryPanel = new JPanel(new GridLayout(0, 4, 15, 15)); // 4 columns, adjusts rows automatically
        galleryPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(galleryPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        // Top Header: Title and Buttons
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);
        topHeader.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel title = new JLabel("Top Spending Areas");
        title.setFont(new Font("Inter", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        topHeader.add(title, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        newButton = new JButton("New");
        newButton.setForeground(Color.WHITE);
        newButton.setBackground(new Color(0x3B82F6));
        newButton.setFocusPainted(false);
        buttonPanel.add(newButton);
        topHeader.add(buttonPanel, BorderLayout.EAST);

        return topHeader;
    }

    public void updateData(DefaultTableModel expenseLogModel) {
        galleryPanel.removeAll();
        
        Map<String, Double> categoryTotals = new HashMap<>();
        Map<String, String> categoryImagePaths = new HashMap<>();

        // Assuming Amount is at index 1 and Category is at index 2, Image Path is at index 6 in expense log
        // The image path for categories is now pulled from the budgetModel directly.
        for (int row = 0; row < expenseLogModel.getRowCount(); row++) {
            try {
                String category = (String) expenseLogModel.getValueAt(row, 2);
                String amountStr = ((String) expenseLogModel.getValueAt(row, 1)).replaceAll("[^\\d.]", "");
                if (!amountStr.isEmpty()) {
                    double amount = Double.parseDouble(amountStr);
                    categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
                }
            } catch (Exception e) {
                System.err.println("Could not parse row " + row + " in expense log.");
                // continue to next row
            }
        }
        
        // Populate image paths from the budget model (Category is col 0, Image Path is col 4)
        for (int row = 0; row < budgetModel.getRowCount(); row++) {
            try {
                String category = (String) budgetModel.getValueAt(row, 0);
                String imagePath = (String) budgetModel.getValueAt(row, 4);
                if (imagePath != null && !imagePath.isEmpty()) {
                    categoryImagePaths.put(category, imagePath);
                }
            } catch (Exception e) {
                System.err.println("Could not get image path from budget model row " + row);
            }
        }

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            String category = entry.getKey();
            double totalAmount = entry.getValue();
            String imagePath = categoryImagePaths.getOrDefault(category, ""); // Get stored image path
            
            galleryPanel.add(new CategoryCardPanel(category, totalAmount, imagePath, onCardClickCallback, budgetModel));
        }

        galleryPanel.revalidate();
        galleryPanel.repaint();
    }
    
    public JButton getNewButton() {
        return newButton;
    }
}

/**
 * A card component for the spending areas gallery.
 */
class CategoryCardPanel extends JPanel {
    private JLabel imageLabel;
    private String categoryName;
    @SuppressWarnings("unused")
    private String currentImagePath;
    private DefaultTableModel budgetModel; // To update the image path in the budget table

    public CategoryCardPanel(String category, double amount, String imagePath, Consumer<String> onClickCallback, DefaultTableModel budgetModel) {
        this.categoryName = category;
        this.currentImagePath = imagePath;
        this.budgetModel = budgetModel;

        setLayout(new BorderLayout());
        setBackground(new Color(0x374151));
        setBorder(new EmptyBorder(10,10,10,10));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        imageLabel = new JLabel();
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(0x4B5563)); // Placeholder color
        imageLabel.setPreferredSize(new Dimension(150, 100)); // Larger image area
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        loadImage(imagePath); // Load initial image
        add(imageLabel, BorderLayout.CENTER);
        
        // Add listener to imageLabel for image selection
        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Select Image for " + categoryName);
                    fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "gif"));
                    
                    int userSelection = fileChooser.showOpenDialog(CategoryCardPanel.this);
                    
                    if (userSelection == JFileChooser.APPROVE_OPTION) {
                        File fileToLoad = fileChooser.getSelectedFile();
                        String newPath = fileToLoad.getAbsolutePath();
                        loadImage(newPath);
                        updateCategoryImagePath(categoryName, newPath); // Update the budget model
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    // Option to remove image
                    int confirm = JOptionPane.showConfirmDialog(CategoryCardPanel.this, 
                                "Remove image for " + categoryName + "?", "Remove Image", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        loadImage(null); // Clear the image
                        updateCategoryImagePath(categoryName, ""); // Clear path in model
                    }
                }
            }
        });


        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(new EmptyBorder(10,0,0,0));
        
        JLabel categoryLabel = new JLabel(category);
        categoryLabel.setForeground(Color.WHITE);
        categoryLabel.setFont(new Font("Inter", Font.BOLD, 16));
        
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        JLabel amountLabel = new JLabel(currencyFormat.format(amount));
        amountLabel.setForeground(new Color(0x9CA3AF)); // Lighter grey for amount
        amountLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        
        infoPanel.add(categoryLabel);
        infoPanel.add(amountLabel);
        
        add(infoPanel, BorderLayout.SOUTH);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getSource() == CategoryCardPanel.this) { // Only trigger if click is on the card itself, not the image label
                    onClickCallback.accept(category);
                }
            }
        });
    }

    private void loadImage(String imagePath) {
        currentImagePath = imagePath;
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                BufferedImage originalImage = ImageIO.read(new File(imagePath));
                if (originalImage != null) {
                    // Scale image to fit the label, maintaining aspect ratio
                    int labelWidth = imageLabel.getPreferredSize().width;
                    int labelHeight = imageLabel.getPreferredSize().height;

                    Image scaledImage = originalImage.getScaledInstance(labelWidth, labelHeight, Image.SCALE_SMOOTH);
                    ImageIcon imageIcon = new ImageIcon(scaledImage);
                    imageLabel.setIcon(imageIcon);
                    imageLabel.setText("");
                } else {
                    imageLabel.setIcon(null);
                    imageLabel.setText("No Image");
                }
            } catch (IOException e) {
                imageLabel.setIcon(null);
                imageLabel.setText("Error loading image");
                e.printStackTrace();
            }
        } else {
            imageLabel.setIcon(null);
            imageLabel.setText("Add Image"); // Placeholder text when no image
            imageLabel.setForeground(new Color(0x9CA3AF));
        }
    }
    
    private void updateCategoryImagePath(String category, String path) {
        for (int i = 0; i < budgetModel.getRowCount(); i++) {
            if (category.equals(budgetModel.getValueAt(i, 0))) { // Category name is at column 0
                budgetModel.setValueAt(path, i, 4); // Image Path is at column 4
                break;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
        g2.dispose();
        // Let the superclass paint the children
        super.paintComponent(g);
    }
}

/**
 * A panel that shows the detailed breakdown for a single spending category.
 */
class CategoryDetailPanel extends JPanel {
    private JLabel categoryTitleLabel;
    private JButton backButton;
    
    // Labels to hold the data
    private JLabel allTimeSpendValue;
    private JLabel budgetValue, monthlyAvgValue, statusValue;
    private JPanel transactionsPanel;
    private JTextArea notesArea;
    private DefaultTableModel budgetModel; // Reference to the budget table model

    public CategoryDetailPanel(DefaultTableModel budgetModel) {
        this.budgetModel = budgetModel; // Initialize the budgetModel
        setLayout(new BorderLayout(10, 20));
        setBackground(new Color(0x1F2937)); // Match dashboard background
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        backButton = new JButton("<- Back");
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(new Color(0x3B82F6));
        backButton.setFocusPainted(false);
        headerPanel.add(backButton, BorderLayout.WEST);

        categoryTitleLabel = new JLabel("Category Details");
        categoryTitleLabel.setFont(new Font("Inter", Font.BOLD, 48));
        categoryTitleLabel.setForeground(Color.WHITE);
        categoryTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(categoryTitleLabel, BorderLayout.CENTER);
        
        // This panel will wrap the main content and center it
        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setOpaque(false);
        
        // Main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setPreferredSize(new Dimension(800, 600)); // Give it a preferred size

        // Properties Panel
        JPanel propertiesPanel = new JPanel();
        propertiesPanel.setOpaque(false);
        propertiesPanel.setLayout(new GridLayout(0, 1, 10, 20)); // Single column layout
        propertiesPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        propertiesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        propertiesPanel.setMaximumSize(new Dimension(800, 400)); // Constrain width

        allTimeSpendValue = createValueLabel();
        budgetValue = createValueLabel();
        monthlyAvgValue = createValueLabel();
        statusValue = createValueLabel();
        
        propertiesPanel.add(createEditableProperty("All-Time Spend", allTimeSpendValue, 6)); // Manual Spend is at col 6
        propertiesPanel.add(createEditableProperty("Budget", budgetValue, 1));
        propertiesPanel.add(createEditableProperty("Monthly Avg", monthlyAvgValue, 2));
        propertiesPanel.add(createEditableProperty("Status", statusValue, 3));
        
        // Transactions Panel
        transactionsPanel = new JPanel();
        transactionsPanel.setOpaque(false);
        transactionsPanel.setLayout(new BoxLayout(transactionsPanel, BoxLayout.Y_AXIS));
        transactionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Notes Area
        notesArea = new JTextArea();
        notesArea.setFont(new Font("Inter", Font.PLAIN, 18));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { saveNote(); }
            @Override public void removeUpdate(DocumentEvent e) { saveNote(); }
            @Override public void changedUpdate(DocumentEvent e) { saveNote(); }
        });


        contentPanel.add(propertiesPanel);
        contentPanel.add(createSectionTitle("Transactions"));
        contentPanel.add(transactionsPanel);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(createSectionTitle("Notes"));
        contentPanel.add(new JScrollPane(notesArea));
        
        wrapperPanel.add(contentPanel);
        add(headerPanel, BorderLayout.NORTH); // Add header separately
        add(wrapperPanel, BorderLayout.CENTER);
    }
    
    private JLabel createValueLabel() {
        JLabel label = new JLabel("$0.00");
        label.setFont(new Font("Inter", Font.PLAIN, 22));
        label.setForeground(Color.WHITE);
        return label;
    }
    
    private JLabel createPropertyLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Inter", Font.BOLD, 22));
        label.setForeground(new Color(0x9CA3AF));
        return label;
    }
    
    private JPanel createEditableProperty(String labelText, JLabel valueLabel, int budgetModelColIndex) {
        JPanel panel = new JPanel(new BorderLayout(15,0));
        panel.setOpaque(false);
        
        JLabel propertyLabel = createPropertyLabel(labelText);
        propertyLabel.setPreferredSize(new Dimension(200, 30)); // Fixed width for alignment
        panel.add(propertyLabel, BorderLayout.WEST);

        JPanel valueEditPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        valueEditPanel.setOpaque(false);
        valueEditPanel.add(valueLabel);

        JButton editButton = new JButton("Edit");
        editButton.setBackground(new Color(0x1F2937));
        editButton.setForeground(Color.WHITE);
        editButton.setFocusPainted(false);
        editButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x3B82F6), 1),
            new EmptyBorder(3, 10, 3, 10)
        ));
        editButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        editButton.addActionListener(e -> {
            String currentCategory = categoryTitleLabel.getText();
            String currentValue = valueLabel.getText();
            String newValue = showCustomInputDialog("Enter new value for " + labelText + ":", currentValue);
            if (newValue != null && !newValue.trim().isEmpty()) {
                valueLabel.setText(newValue.trim());
                updateBudgetModel(currentCategory, budgetModelColIndex, newValue.trim());
            }
        });
        valueEditPanel.add(editButton);
        panel.add(valueEditPanel, BorderLayout.CENTER);
        return panel;
    }

    private JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Inter", Font.BOLD, 28));
        label.setForeground(Color.WHITE);
        label.setBorder(new EmptyBorder(20,0,15,0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    private void updateBudgetModel(String categoryName, int columnIndex, String newValue) {
        for (int i = 0; i < budgetModel.getRowCount(); i++) {
            if (categoryName.equals(budgetModel.getValueAt(i, 0))) { // Category name is at column 0
                budgetModel.setValueAt(newValue, i, columnIndex);
                break;
            }
        }
    }
    
    private void saveNote() {
        String currentCategory = categoryTitleLabel.getText();
        String noteText = notesArea.getText();
        updateBudgetModel(currentCategory, 5, noteText); // Notes are at column 5
    }

    private String showCustomInputDialog(String message, String initialValue) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Value", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(Color.BLACK);

        JLabel messageLabel = new JLabel(message);
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setBorder(new EmptyBorder(20, 20, 10, 20));
        dialog.add(messageLabel, BorderLayout.NORTH);

        JTextField inputField = new JTextField(initialValue);
        inputField.setBackground(new Color(0x374151));
        inputField.setForeground(Color.WHITE);
        inputField.setCaretColor(Color.WHITE);
        inputField.setBorder(new EmptyBorder(5,5,5,5));
        dialog.add(inputField, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);
        JButton okButton = new JButton("OK");
        okButton.setBackground(new Color(0x3B82F6));
        okButton.setForeground(Color.WHITE);
        
        final String[] result = {null};
        okButton.addActionListener(e -> {
            result[0] = inputField.getText();
            dialog.dispose();
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(0x4B5563));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setSize(400, dialog.getHeight());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return result[0];
    }


    public void updateView(String categoryName, DefaultTableModel expenseModel) {
        categoryTitleLabel.setText(categoryName);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        // Calculate All-Time Spend and list transactions
        double calculatedTotalSpend = 0;
        transactionsPanel.removeAll();
        
        ArrayList<String> transactionNames = new ArrayList<>();

        for (int i = 0; i < expenseModel.getRowCount(); i++) {
            if (categoryName.equals(expenseModel.getValueAt(i, 2))) { // Category is at index 2
                try {
                    String transactionName = (String) expenseModel.getValueAt(i, 0); // Transaction name is at index 0
                    String amountStr = ((String) expenseModel.getValueAt(i, 1)).replaceAll("[^\\d.]", ""); // Amount is at index 1
                    calculatedTotalSpend += Double.parseDouble(amountStr);
                    transactionNames.add(transactionName);
                } catch (Exception e) { /* ignore */ }
            }
        }
        
        if (transactionNames.isEmpty()) {
            JLabel noTxLabel = new JLabel("No transactions found.");
            noTxLabel.setForeground(new Color(0x9CA3AF));
            transactionsPanel.add(noTxLabel);
        } else {
            for(String tx : transactionNames) {
                JLabel txLabel = new JLabel("• " + tx); // Added bullet point for style
                txLabel.setForeground(Color.WHITE);
                txLabel.setFont(new Font("Inter", Font.PLAIN, 18));
                transactionsPanel.add(txLabel);
            }
        }
        
        // Find budget info from the budgetModel
        budgetValue.setText("N/A");
        monthlyAvgValue.setText("N/A");
        statusValue.setText("N/A");
        notesArea.setText("");
        allTimeSpendValue.setText(currencyFormat.format(calculatedTotalSpend)); // Default to calculated
        
        for (int i = 0; i < budgetModel.getRowCount(); i++) {
             if (categoryName.equals(budgetModel.getValueAt(i, 0))) { // Category is col 0
                 budgetValue.setText((String) budgetModel.getValueAt(i, 1)); // Budget is col 1
                 monthlyAvgValue.setText((String) budgetModel.getValueAt(i, 2)); // Monthly Avg is col 2
                 statusValue.setText((String) budgetModel.getValueAt(i, 3)); // Status is col 3
                 notesArea.setText((String) budgetModel.getValueAt(i, 5)); // Notes is col 5
                 
                 // Check for manual spend override
                 String manualSpend = (String) budgetModel.getValueAt(i, 6); // Manual Spend is col 6
                 if (manualSpend != null && !manualSpend.isEmpty()) {
                     allTimeSpendValue.setText(manualSpend);
                 }
                 break;
             }
        }
        
        transactionsPanel.revalidate();
        transactionsPanel.repaint();
    }

    public JButton getBackButton() {
        return backButton;
    }
}


/**
 * A self-contained panel for displaying a transaction table with a header, footer, and interactive elements.
 */
class TransactionTablePanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JLabel sumLabel;
    private JButton newButton;
    private JButton deleteButton;
    private int sumColumn;
    private Runnable onDataChanged;

    /**
     * A custom renderer for JTableHeader that uses borders to create visible separation lines.
     */
    private static class DarkHeaderRenderer extends DefaultTableCellRenderer {
        public DarkHeaderRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBackground(Color.BLACK);
            setForeground(Color.WHITE);
            setFont(new Font("Inter", Font.BOLD, 18));
            // Add padding and a white border to the right and a thicker one on the bottom for separation
            setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(5, 10, 5, 10), // Padding
                BorderFactory.createMatteBorder(0, 0, 2, 1, Color.WHITE) // Bottom and right lines
            ));
            setHorizontalAlignment(SwingConstants.LEFT);
            return this;
        }
    }
    
    /**
     * A custom renderer for table data cells that uses borders to create a visible grid.
     */
    private static class DarkDataCellRenderer extends DefaultTableCellRenderer {
         public DarkDataCellRenderer() {
            setOpaque(true);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Set colors for non-selected state
            if (!isSelected) {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            } else {
                 setBackground(table.getSelectionBackground());
                 setForeground(table.getSelectionForeground());
            }
            
            // Add padding and a white border to the right and bottom for separation
            setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(5, 10, 5, 10), // Same padding as header
                BorderFactory.createMatteBorder(0, 0, 1, 1, Color.WHITE) // Bottom and right lines
            ));
            
            return this;
        }
    }


    public TransactionTablePanel(String title, String[] headers, int sumColumn, Runnable onDataChanged) {
        this.sumColumn = sumColumn;
        this.onDataChanged = onDataChanged;
        setLayout(new BorderLayout(0, 10));
        setBackground(new Color(0x1F2937));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(createHeader(title), BorderLayout.NORTH);
        add(createTable(headers), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        // Update sum and notify listeners whenever the table data changes
        model.addTableModelListener(e -> {
            updateSum();
            if (this.onDataChanged != null) {
                this.onDataChanged.run();
            }
        });
    }

    private JPanel createHeader(String title) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setFont(new Font("Inter", Font.BOLD, 20));
        label.setForeground(Color.WHITE);
        headerPanel.add(label, BorderLayout.WEST);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        newButton = new JButton("New");
        newButton.setForeground(Color.WHITE);
        newButton.setBackground(new Color(0x3B82F6));
        newButton.setFocusPainted(false);
        buttonPanel.add(newButton);

        deleteButton = new JButton("Delete");
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setBackground(new Color(0xEF4444)); // Red color for delete
        deleteButton.setFocusPainted(false);
        buttonPanel.add(deleteButton);
        
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        return headerPanel;
    }

    private JScrollPane createTable(String[] headers) {
        model = new DefaultTableModel(headers, 0);
        table = new JTable(model);
        
        // Set table colors
        table.setBackground(new Color(0x374151)); // Dark grey-blue for rows
        table.setForeground(Color.WHITE);
        table.setShowGrid(false); // We will paint our own grid
        table.setIntercellSpacing(new Dimension(0, 0)); // No space between cells
        table.setRowHeight(40); // Increased row height
        table.setFont(new Font("Inter", Font.PLAIN, 18)); // Increased font size
        
        // Set selection colors for better readability
        table.setSelectionBackground(new Color(0x3B82F6));
        table.setSelectionForeground(Color.WHITE);

        // This is important to allow horizontal scrolling if columns are too wide
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Style the table header using the custom renderer
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setDefaultRenderer(new DarkHeaderRenderer());
        tableHeader.setPreferredSize(new Dimension(100, 40)); // Ensure header is tall enough

        // Apply custom renderer to data cells to create an aligned grid
        DarkDataCellRenderer cellRenderer = new DarkDataCellRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        // Set preferred column widths safely
        int columnCount = table.getColumnCount();
        if (columnCount > 0) table.getColumnModel().getColumn(0).setPreferredWidth(200);
        if (columnCount > 1) table.getColumnModel().getColumn(1).setPreferredWidth(100); // Amount
        if (columnCount > 2) table.getColumnModel().getColumn(2).setPreferredWidth(150);
        if (columnCount > 3) table.getColumnModel().getColumn(3).setPreferredWidth(150);
        if (columnCount > 4) table.getColumnModel().getColumn(4).setPreferredWidth(150);
        if (columnCount > 5) table.getColumnModel().getColumn(5).setPreferredWidth(150);
        if (columnCount > 6) table.getColumnModel().getColumn(6).setPreferredWidth(200); // Image Path


        // Create the scroll pane and ensure its viewport matches the dark theme
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(0x374151));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0x4B5563)));
        return scrollPane;
    }

    private JPanel createFooter() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setOpaque(false);
        sumLabel = new JLabel("SUM: $0.00");
        sumLabel.setFont(new Font("Inter", Font.BOLD, 16));
        sumLabel.setForeground(Color.WHITE);
        footerPanel.add(sumLabel);
        return footerPanel;
    }

    public void updateSum() {
        if (sumColumn == -1) return;
        
        double total = 0.0;
        for (int i = 0; i < model.getRowCount(); i++) {
            try {
                Object value = model.getValueAt(i, sumColumn);
                if (value != null) {
                    String amountStr = value.toString();
                    amountStr = amountStr.replaceAll("[^\\d.]", "");
                    if (!amountStr.isEmpty()) {
                        total += Double.parseDouble(amountStr);
                    }
                }
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        sumLabel.setText("SUM: " + currencyFormat.format(total));
    }
    
    public JTable getTable() {
        return table;
    }

    public DefaultTableModel getModel() {
        return model;
    }

    public JButton getNewButton() {
        return newButton;
    }

    public JButton getDeleteButton() {
        return deleteButton;
    }
}


/**
 * A custom JButton subclass that paints itself with rounded corners.
 */
class RoundedButton extends JButton {
    private Color hoverBackgroundColor = new Color(0x4B5563);
    private Color pressedBackgroundColor = new Color(0x374151);
    private Color defaultBackgroundColor = new Color(0x374151);

    public RoundedButton(String text, Icon icon) {
        super(text, icon);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(Color.WHITE);
        setFont(new Font("Inter", Font.BOLD, 16));
        setHorizontalAlignment(SwingConstants.LEFT);
        setIconTextGap(15);
        setPreferredSize(new Dimension(200, 60));
        setBorder(new EmptyBorder(0, 20, 0, 0));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                defaultBackgroundColor = hoverBackgroundColor;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                defaultBackgroundColor = new Color(0x374151);
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                defaultBackgroundColor = pressedBackgroundColor;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (contains(e.getPoint())) {
                    defaultBackgroundColor = hoverBackgroundColor;
                } else {
                    defaultBackgroundColor = new Color(0x374151);
                }
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(defaultBackgroundColor);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 25, 25));
        g2.dispose();
        super.paintComponent(g);
    }
}

// --- Custom Icon Classes ---
abstract class BaseIcon implements Icon {
    protected int width = 24;
    protected int height = 24;
    protected Color iconColor = new Color(0x3B82F6);

    @Override
    public int getIconWidth() { return width; }
    @Override
    public int getIconHeight() { return height; }

    protected void setupGraphics(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(iconColor);
        g2.setStroke(new BasicStroke(2));
    }
}

class FinanceIcon extends BaseIcon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        setupGraphics(g2);
        g2.drawRect(x + 2, y + 5, width - 4, height - 10);
        g2.drawOval(x + 8, y + 8, width - 16, height - 16);
        g2.dispose();
    }
}

class BriefcaseIcon extends BaseIcon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        setupGraphics(g2);
        g2.drawRoundRect(x + 2, y + 6, width - 4, height - 9, 5, 5);
        g2.drawRect(x + 8, y + 2, width - 16, 4);
        g2.dispose();
    }
}

class DumbbellIcon extends BaseIcon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        setupGraphics(g2);
        g2.fillRect(x + 7, y + 10, width - 14, 4); // Handle
        g2.fillRect(x + 2, y + 8, 5, 8); // Left weight
        g2.fillRect(x + width - 7, y + 8, 5, 8); // Right weight
        g2.dispose();
    }
}

class CrmIcon extends BaseIcon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        setupGraphics(g2);
        g2.drawOval(x + 8, y + 4, 8, 8); // Head
        g2.drawRect(x + 4, y + 13, 16, 6); // Body
        g2.dispose();
    }
}

class MapPinIcon extends BaseIcon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        setupGraphics(g2);
        g2.fillOval(x + 7, y + 2, 10, 10);
        int[] xPoints = {x + 12, x + 8, x + 16};
        int[] yPoints = {y + 22, y + 12, y + 12};
        g2.fillPolygon(xPoints, yPoints, 3);
        g2.dispose();
    }
}

class KnowledgeIcon extends BaseIcon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        setupGraphics(g2);
        g2.drawRect(x + 2, y + 4, width - 4, height - 8);
        g2.drawLine(x + 12, y + 4, x + 12, y + 16);
        g2.drawLine(x + 2, y + 10, x + 22, y + 10);
        g2.dispose();
    }
}

class ShoppingIcon extends BaseIcon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        setupGraphics(g2);
        g2.drawRect(x + 3, y + 7, width - 6, height - 10);
        g2.drawArc(x + 8, y + 2, 8, 10, 0, 180);
        g2.dispose();
    }
}

class TravelIcon extends BaseIcon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        setupGraphics(g2);
        g2.drawRoundRect(x + 2, y + 4, width - 4, height - 8, 8, 8);
        g2.drawLine(x + 8, y + 4, x + 8, y + 16);
        g2.drawLine(x + 16, y + 4, x + 16, y + 16);
        g2.dispose();
    }
}

