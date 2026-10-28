package com.payroll.ui;

import com.payroll.model.Person;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

/** Main window for the Admin / HR role. */
public class AdminDashboard extends JFrame {
    public AdminDashboard(Person admin, LoginFrame loginFrame) {
        super("Payroll Management System - HR Admin");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        OverviewPanel overview = new OverviewPanel(tabs::setSelectedIndex);
        tabs.addTab("📊 Overview", overview);
        tabs.addTab("👥 Employees", new EmployeePanel());
        tabs.addTab("🏢 Departments", new DepartmentPanel());
        tabs.addTab("💰 Salary Structure", new SalaryStructurePanel());
        tabs.addTab("📅 Attendance", new AttendancePanel());
        tabs.addTab("⚡ Run Payroll", new PayrollPanel(admin.getUsername()));
        tabs.addTab("📈 Reports", new ReportsPanel());
        tabs.addTab("📜 Audit Log", new AuditPanel());

        tabs.addChangeListener(e -> {
            Component c = tabs.getSelectedComponent();
            if (c instanceof Refreshable r) {
                r.refresh();
            }
        });

        // Top Modern Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE2E8F0)),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel logo = new JLabel("💳");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        JLabel brandTitle = new JLabel("PAYROLL SYSTEM");
        brandTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        brandTitle.setForeground(new Color(0x1E1B4B));

        JLabel roleBadge = new JLabel(" HR ADMIN ");
        roleBadge.setFont(new Font("SansSerif", Font.BOLD, 10));
        roleBadge.setForeground(new Color(0x4F46E5));
        roleBadge.setBackground(new Color(0xEEF2FF));
        roleBadge.setOpaque(true);
        roleBadge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

        brandPanel.add(logo);
        brandPanel.add(brandTitle);
        brandPanel.add(roleBadge);
        header.add(brandPanel, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JLabel userLabel = new JLabel("👤 " + admin.getName());
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        userLabel.setForeground(new Color(0x334155));

        JButton changePass = Ui.button("Change Password", () -> ChangePasswordDialog.show(this, admin));
        changePass.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JButton logout = Ui.dangerButton("Logout", () -> {
            dispose();
            loginFrame.clear();
            loginFrame.setVisible(true);
        });
        logout.setFont(new Font("SansSerif", Font.BOLD, 12));

        actions.add(userLabel);
        actions.add(Box.createHorizontalStrut(8));
        actions.add(changePass);
        actions.add(logout);
        header.add(actions, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        setPreferredSize(new Dimension(1220, 760));
        pack();
        setLocationRelativeTo(null);
        overview.refresh();
    }
}