package main.ui;

import main.db.OpportunityDAO;
import main.db.ClientDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SalesPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;

    public SalesPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JLabel title = new JLabel("Sales & Opportunities");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        String[] cols = new String[]{"ID", "Client", "Title", "Value", "Status", "Stage"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);
        add(sp, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        model.setRowCount(0);
        List<OpportunityDAO.Opp> opps = OpportunityDAO.listAll();
        for (OpportunityDAO.Opp o : opps) {
            String clientName = "-";
            try {
                for (ClientDAO.Client c : ClientDAO.listAll()) {
                    if (c.id == o.clientId) { clientName = c.name; break; }
                }
            } catch (Exception ignored) {}
            model.addRow(new Object[]{o.id, clientName, o.title, o.value, o.status, o.stage});
        }
    }

    public static JPanel createEmbeddedPanel() {
        return new SalesPanel();
    }
}
