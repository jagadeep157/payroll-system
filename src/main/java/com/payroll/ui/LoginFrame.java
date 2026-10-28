package com.payroll.ui;

import com.payroll.model.Person;
import com.payroll.model.Role;
import com.payroll.service.AuthService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;

public class LoginFrame extends JFrame {
    private final AuthService auth = new AuthService();
    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JLabel messageLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JButton loginButton = Ui.primaryButton("Sign In  ➔", this::doLogin);

    public LoginFrame() {
        super("Payroll Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setPreferredSize(new Dimension(800, 520));

        // Left Hero / Branding Panel
        JPanel hero = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x1E1B4B), getWidth(), getHeight(), new Color(0x3730A3));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Subtle glowing circles in background
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillOval(-60, -60, 240, 240);
                g2.fillOval(getWidth() - 140, getHeight() - 140, 260, 260);
                g2.dispose();
            }
        };
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setPreferredSize(new Dimension(340, 520));
        hero.setBorder(BorderFactory.createEmptyBorder(40, 36, 40, 36));

        // Brand badge / icon
        JLabel icon = new JLabel("💳");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 44));
        icon.setAlignmentX(LEFT_ALIGNMENT);

        JLabel brand = new JLabel("PAYROLL");
        brand.setFont(new Font("SansSerif", Font.BOLD, 26));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subBrand = new JLabel("Management System");
        subBrand.setFont(new Font("SansSerif", Font.PLAIN, 15));
        subBrand.setForeground(new Color(0xC7D2FE));
        subBrand.setAlignmentX(LEFT_ALIGNMENT);

        JLabel desc = new JLabel("<html>Enterprise-grade compensation, automated deductions, attendance & instant payslips.</html>");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 13));
        desc.setForeground(new Color(0xE0E7FF));
        desc.setAlignmentX(LEFT_ALIGNMENT);
        desc.setMaximumSize(new Dimension(270, 60));

        hero.add(icon);
        hero.add(Box.createVerticalStrut(12));
        hero.add(brand);
        hero.add(subBrand);
        hero.add(Box.createVerticalStrut(18));
        hero.add(desc);
        hero.add(Box.createVerticalStrut(28));

        // Highlights list
        String[] perks = {
                "⚡ One-Click Automated Payroll",
                "📊 Real-time Attendance & Leave",
                "📄 Downloadable PDF Payslips",
                "🔒 Role-Based BCrypt Security"
        };
        for (String perk : perks) {
            JLabel item = new JLabel(perk);
            item.setFont(new Font("SansSerif", Font.PLAIN, 12));
            item.setForeground(new Color(0xEEF2FF));
            item.setAlignmentX(LEFT_ALIGNMENT);
            hero.add(item);
            hero.add(Box.createVerticalStrut(10));
        }

        hero.add(Box.createVerticalGlue());

        // System status badge
        JLabel status = new JLabel("● Database: MySQL 8 (Connected)");
        status.setFont(new Font("SansSerif", Font.BOLD, 11));
        status.setForeground(new Color(0x86EFAC));
        status.setAlignmentX(LEFT_ALIGNMENT);
        hero.add(status);

        // Right Sign-in Form Panel
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createEmptyBorder(36, 40, 36, 40));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 4, 5, 4);

        JLabel title = new JLabel("Welcome Back");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(0x0F172A));
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        form.add(title, c);

        JLabel sub = new JLabel("Please enter your account credentials to sign in");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(new Color(0x64748B));
        c.gridy = 1;
        c.insets = new Insets(0, 4, 18, 4);
        form.add(sub, c);

        c.insets = new Insets(4, 4, 4, 4);
        c.gridwidth = 2;
        c.gridy = 2;
        JLabel userLabel = new JLabel("Username or Email");
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        userLabel.setForeground(new Color(0x334155));
        form.add(userLabel, c);

        usernameField.putClientProperty("JTextField.placeholderText", "e.g. admin or asha");
        usernameField.setPreferredSize(new Dimension(240, 36));
        c.gridy = 3;
        form.add(usernameField, c);

        c.gridy = 4;
        c.insets = new Insets(10, 4, 4, 4);
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        passLabel.setForeground(new Color(0x334155));
        form.add(passLabel, c);

        passwordField.putClientProperty("JTextField.placeholderText", "••••••••");
        passwordField.setPreferredSize(new Dimension(240, 36));
        c.insets = new Insets(4, 4, 4, 4);
        c.gridy = 5;
        form.add(passwordField, c);

        loginButton.setPreferredSize(new Dimension(240, 40));
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        c.gridy = 6;
        c.insets = new Insets(16, 4, 6, 4);
        form.add(loginButton, c);

        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        messageLabel.setForeground(new Color(0xDC2626));
        c.gridy = 7;
        c.insets = new Insets(2, 4, 10, 4);
        form.add(messageLabel, c);

        // Quick demo fills section
        JPanel demoBox = new JPanel();
        demoBox.setLayout(new BoxLayout(demoBox, BoxLayout.Y_AXIS));
        demoBox.setOpaque(false);
        demoBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xE2E8F0)),
                BorderFactory.createEmptyBorder(12, 0, 0, 0)
        ));

        JLabel demoHeader = new JLabel("⚡ Quick Demo Fill (click to autofill):");
        demoHeader.setFont(new Font("SansSerif", Font.BOLD, 11));
        demoHeader.setForeground(new Color(0x64748B));
        demoHeader.setAlignmentX(LEFT_ALIGNMENT);
        demoBox.add(demoHeader);
        demoBox.add(Box.createVerticalStrut(8));

        JPanel demoButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        demoButtons.setOpaque(false);
        demoButtons.setAlignmentX(LEFT_ALIGNMENT);

        demoButtons.add(quickFillChip("👑 Admin (HR)", "admin", "admin123"));
        demoButtons.add(quickFillChip("💼 Asha (Emp)", "asha", "emp123"));
        demoButtons.add(quickFillChip("💼 Rohan (Emp)", "rohan", "emp123"));

        demoBox.add(demoButtons);

        c.gridy = 8;
        c.insets = new Insets(8, 4, 0, 4);
        form.add(demoBox, c);

        formContainer.add(form, BorderLayout.CENTER);

        root.add(hero, BorderLayout.WEST);
        root.add(formContainer, BorderLayout.CENTER);

        passwordField.addActionListener(e -> doLogin());
        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());
        getRootPane().setDefaultButton(loginButton);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
    }

    private JButton quickFillChip(String label, String user, String pass) {
        JButton b = new JButton(label);
        b.setFont(new Font("SansSerif", Font.PLAIN, 11));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBackground(new Color(0xF1F5F9));
        b.setForeground(new Color(0x334155));
        b.addActionListener(e -> {
            usernameField.setText(user);
            passwordField.setText(pass);
            messageLabel.setText(" ");
            loginButton.requestFocusInWindow();
        });
        return b;
    }

    public void clear() {
        usernameField.setText("");
        passwordField.setText("");
        messageLabel.setText(" ");
        usernameField.requestFocusInWindow();
    }

    private void doLogin() {
        String user = usernameField.getText();
        String pass = new String(passwordField.getPassword());
        loginButton.setEnabled(false);
        messageLabel.setForeground(new Color(0x4F46E5));
        messageLabel.setText("Signing in...");
        Ui.async(this, () -> auth.login(user, pass), person -> {
            passwordField.setText("");
            messageLabel.setText(" ");
            open(person);
        }, () -> {
            loginButton.setEnabled(true);
            if ("Signing in...".equals(messageLabel.getText())) {
                messageLabel.setText(" ");
            }
        });
    }

    private void open(Person person) {
        setVisible(false);
        if (Role.ADMIN.equals(person.getRole())) {
            new AdminDashboard(person, this).setVisible(true);
        } else {
            new EmployeePortal(person, this).setVisible(true);
        }
    }
}