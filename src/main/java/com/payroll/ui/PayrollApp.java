package com.payroll.ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.payroll.db.DBConnection;
import com.payroll.db.DatabaseInitializer;
import com.payroll.service.PayrollScheduler;
import com.payroll.service.PayrollService;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;

public class PayrollApp {
    private static PayrollScheduler scheduler;

    public static void main(String[] args) {
        initTheme();
        SwingUtilities.invokeLater(PayrollApp::launchLogin);
    }

    public static void initTheme() {
        try {
            FlatMacLightLaf.setup();
        } catch (Throwable t) {
            FlatLightLaf.setup();
        }
        UIManager.put("Component.accentColor", new Color(0x4F46E5));
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);

        UIManager.put("TabbedPane.tabType", "underlined");
        UIManager.put("TabbedPane.underlineColor", new Color(0x4F46E5));
        UIManager.put("TabbedPane.underlineHeight", 3);
        UIManager.put("TabbedPane.selectedForeground", new Color(0x4F46E5));
        UIManager.put("TabbedPane.tabInsets", new Insets(10, 16, 10, 16));
        UIManager.put("TabbedPane.font", new Font("SansSerif", Font.BOLD, 13));

        UIManager.put("Table.rowHeight", 34);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.gridColor", new Color(0xE2E8F0));
        UIManager.put("Table.selectionBackground", new Color(0xEEF2FF));
        UIManager.put("Table.selectionForeground", new Color(0x1E1B4B));
        UIManager.put("TableHeader.font", new Font("SansSerif", Font.BOLD, 12));
        UIManager.put("TableHeader.height", 36);
        UIManager.put("TableHeader.background", new Color(0xF8FAFC));
        UIManager.put("TableHeader.foreground", new Color(0x475569));
        UIManager.put("TableHeader.separatorColor", new Color(0xE2E8F0));

        UIManager.put("ProgressBar.arc", 10);
        UIManager.put("ProgressBar.foreground", new Color(0x4F46E5));
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 10);
    }

    /** Connects to the database (creating tables/demo data on first run), then shows the login window. */
    static void launchLogin() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                DBConnection.init();
                DatabaseInitializer.run();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(null,
                            "Could not connect to the database:\n" + cause.getMessage()
                                    + "\n\nCheck config/db.properties (see README) and that MySQL is running.",
                            "Payroll - startup error", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                    return;
                }
                scheduler = new PayrollScheduler(new PayrollService(),
                        r -> System.out.println("Scheduled payroll " + r.month() + ": " + r.created() + " created"));
                scheduler.start();
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    scheduler.stop();
                    DBConnection.close();
                }));
                new LoginFrame().setVisible(true);
            }
        }.execute();
    }
}
