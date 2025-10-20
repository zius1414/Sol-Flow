package main;

import javax.swing.SwingUtilities;
import main.ui.NotionStyleUI;
import main.db.Database;

public class Main {
    public static void main(String[] args) {
        Database.init();
        SwingUtilities.invokeLater(() -> NotionStyleUI.show());
    }
}
