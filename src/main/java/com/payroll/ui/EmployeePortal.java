package com.payroll.ui;

import com.payroll.model.Attendance;
import com.payroll.model.Payroll;
import com.payroll.model.Payslip;
import com.payroll.model.Person;
import com.payroll.service.AttendanceService;
import com.payroll.service.EmployeeService;
import com.payroll.service.PayslipService;
import com.payroll.util.Money;
import com.payroll.util.Validators;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.time.YearMonth;
import java.util.List;

/** Self-service window for the Employee role: profile, own attendance, own payslips. */
public class EmployeePortal extends JFrame {
    private final Person user;
    private final EmployeeService employees = new EmployeeService();
    private final AttendanceService attendance = new AttendanceService();
    private final PayslipService payslips = new PayslipService();

    public EmployeePortal(Person user, LoginFrame loginFrame) {
        super("Payroll Management System — Employee Self-Service");
        this.user = user;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        ProfileTab profile = new ProfileTab();
        AttendanceTab att = new AttendanceTab();
        PayslipTab slips = new PayslipTab();
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("👤 My Profile", profile);
        tabs.addTab("📅 My Attendance", att);
        tabs.addTab("📄 My Payslips", slips);
        tabs.addChangeListener(e -> ((Refreshable) tabs.getSelectedComponent()).refresh());

        // Modern Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE2E8F0)),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brand.setOpaque(false);

        JLabel logo = new JLabel("💳");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        JLabel brandTitle = new JLabel("PAYROLL SYSTEM");
        brandTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        brandTitle.setForeground(new Color(0x1E1B4B));

        JLabel roleBadge = new JLabel(" EMPLOYEE PORTAL ");
        roleBadge.setFont(new Font("SansSerif", Font.BOLD, 10));
        roleBadge.setForeground(new Color(0x059669));
        roleBadge.setBackground(new Color(0xECFDF5));
        roleBadge.setOpaque(true);
        roleBadge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

        brand.add(logo);
        brand.add(brandTitle);
        brand.add(roleBadge);
        header.add(brand, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JLabel userLabel = new JLabel("👤 " + user.getName());
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        userLabel.setForeground(new Color(0x334155));

        JButton changePass = Ui.button("Change Password", () -> ChangePasswordDialog.show(this, user));
        JButton logout = Ui.dangerButton("Logout", () -> {
            dispose();
            loginFrame.clear();
            loginFrame.setVisible(true);
        });

        actions.add(userLabel);
        actions.add(Box.createHorizontalStrut(8));
        actions.add(changePass);
        actions.add(logout);
        header.add(actions, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        setPreferredSize(new Dimension(960, 640));
        pack();
        setLocationRelativeTo(null);
        profile.refresh();
    }

    private class ProfileTab extends JPanel implements Refreshable {
        private final JLabel[] values = new JLabel[7];

        ProfileTab() {
            super(new BorderLayout(0, 16));
            setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
            setBackground(new Color(0xF8FAFC));

            JPanel card = new JPanel(new BorderLayout(0, 16));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xE2E8F0), 1, true),
                    BorderFactory.createEmptyBorder(24, 28, 24, 28)
            ));

            JLabel title = new JLabel("Employee Profile Details");
            title.setFont(new Font("SansSerif", Font.BOLD, 16));
            title.setForeground(new Color(0x0F172A));
            card.add(title, BorderLayout.NORTH);

            String[] labels = {"Full Name", "Email Address", "System Username", "Designation", "Department", "Joining Date", "Base Salary"};
            JPanel grid = new JPanel(new GridLayout(labels.length, 2, 12, 14));
            grid.setOpaque(false);
            for (int i = 0; i < labels.length; i++) {
                JLabel l = new JLabel(labels[i] + ":");
                l.setFont(new Font("SansSerif", Font.BOLD, 12));
                l.setForeground(new Color(0x64748B));
                grid.add(l);

                values[i] = new JLabel("-");
                values[i].setFont(new Font("SansSerif", Font.PLAIN, 13));
                values[i].setForeground(new Color(0x0F172A));
                grid.add(values[i]);
            }
            card.add(grid, BorderLayout.CENTER);

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            btnRow.setOpaque(false);
            JButton editBtn = Ui.primaryButton("Edit Name / Email", this::edit);
            btnRow.add(editBtn);
            card.add(btnRow, BorderLayout.SOUTH);

            add(card, BorderLayout.NORTH);
        }

        @Override
        public void refresh() {
            Ui.async(this, () -> employees.get(user.getId()), e -> {
                values[0].setText(e.getName());
                values[1].setText(e.getEmail());
                values[2].setText(e.getUsername());
                values[3].setText(String.valueOf(e.getDesignation()));
                values[4].setText(e.getDeptName() == null ? "-" : e.getDeptName());
                values[5].setText(String.valueOf(e.getJoiningDate()));
                values[6].setText(Money.fmt(e.getBasicSalary()));
            });
        }

        private void edit() {
            JTextField name = new JTextField(user.getName(), 20);
            JTextField email = new JTextField(user.getEmail());
            while (Ui.form(this, "Edit profile", new String[]{"Name", "Email"}, new JComponent[]{name, email})) {
                try {
                    employees.updateProfile(user, name.getText(), email.getText());
                    refresh();
                    return;
                } catch (RuntimeException e) {
                    Ui.showError(this, e);
                }
            }
        }
    }

    private class AttendanceTab extends JPanel implements Refreshable {
        private final DefaultTableModel model = Ui.model("Date", "Status");
        private final JTextField monthField = new JTextField(YearMonth.now().toString(), 8);
        private final JLabel totals = new JLabel(" ");

        AttendanceTab() {
            super(new BorderLayout(0, 10));
            setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
            monthField.addActionListener(e -> refresh());

            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
            JLabel mLabel = new JLabel("Month (yyyy-MM):");
            mLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            top.add(mLabel);
            top.add(monthField);
            top.add(Ui.primaryButton("Show Attendance", this::refresh));
            add(top, BorderLayout.NORTH);

            add(new JScrollPane(Ui.table(model)), BorderLayout.CENTER);

            totals.setFont(new Font("SansSerif", Font.PLAIN, 12));
            totals.setForeground(new Color(0x475569));
            totals.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            add(totals, BorderLayout.SOUTH);
        }

        @Override
        public void refresh() {
            YearMonth m;
            try {
                m = Validators.month(monthField.getText());
            } catch (RuntimeException e) {
                Ui.showError(this, e);
                return;
            }
            Ui.async(this, () -> new Object[]{attendance.getAttendance(user.getId(), m),
                    attendance.countAbsences(user.getId(), m), attendance.count(user.getId(), m, Attendance.LEAVE)}, r -> {
                @SuppressWarnings("unchecked") List<Attendance> rows = (List<Attendance>) r[0];
                model.setRowCount(0);
                rows.forEach(a -> model.addRow(new Object[]{a.date(), a.status()}));
                totals.setText("✓ Marked days: " + rows.size() + "   |   ❌ Absent: " + r[1] + "   |   🏖️ Leave: " + r[2]
                        + "   (Unmarked days count as present)");
            });
        }
    }

    private class PayslipTab extends JPanel implements Refreshable {
        private final DefaultListModel<Payroll> list = new DefaultListModel<>();
        private final JList<Payroll> months = new JList<>(list);
        private final JTextArea view = new JTextArea();
        private Payslip shown;

        PayslipTab() {
            super(new BorderLayout(0, 10));
            setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

            months.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            months.setFixedCellHeight(38);
            months.setCellRenderer((l, p, i, sel, focus) -> {
                JLabel c = new JLabel(" 📅 " + p.payMonth() + "  —  Net: " + Money.fmt(p.netSalary()) + "  [" + p.status() + "]");
                c.setFont(new Font("SansSerif", Font.PLAIN, 12));
                c.setOpaque(true);
                c.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                c.setBackground(sel ? new Color(0xEEF2FF) : Color.WHITE);
                c.setForeground(sel ? new Color(0x3730A3) : new Color(0x1E293B));
                return c;
            });
            months.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && months.getSelectedValue() != null) {
                    show(months.getSelectedValue());
                }
            });

            view.setEditable(false);
            view.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            view.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
            view.setBackground(new Color(0xF8FAFC));

            JScrollPane left = new JScrollPane(months);
            left.setPreferredSize(new Dimension(320, 100));

            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, new JScrollPane(view));
            split.setDividerLocation(320);
            add(split, BorderLayout.CENTER);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
            JButton downloadBtn = Ui.successButton("📥 Download PDF Payslip", this::download);
            downloadBtn.setPreferredSize(new Dimension(200, 36));
            bottom.add(downloadBtn);
            add(bottom, BorderLayout.SOUTH);
        }

        @Override
        public void refresh() {
            Ui.async(this, () -> payslips.availablePayslips(user.getId()), rows -> {
                list.clear();
                rows.forEach(list::addElement);
                shown = null;
                view.setText(rows.isEmpty() ? "No payslips are available yet.\nPayslips appear here once HR verifies the month's payroll."
                        : "Select a month on the left.");
                if (!rows.isEmpty()) {
                    months.setSelectedIndex(0);
                }
            });
        }

        private void show(Payroll p) {
            Ui.async(this, () -> {
                Payslip s = payslips.getPayslip(user.getId(), YearMonth.parse(p.payMonth()));
                return new Object[]{s, payslips.toText(s)};
            }, r -> {
                shown = (Payslip) r[0];
                view.setText((String) r[1]);
                view.setCaretPosition(0);
            });
        }

        private void download() {
            if (shown == null) {
                Ui.info(this, "Select a payslip first");
                return;
            }
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("payslip-" + shown.payroll().payMonth() + ".pdf"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                Payslip s = shown;
                File f = fc.getSelectedFile();
                Ui.async(this, () -> payslips.generatePdf(s, f.toPath()), () -> Ui.info(this, "Saved " + f.getName()));
            }
        }
    }
}