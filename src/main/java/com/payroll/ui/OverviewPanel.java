package com.payroll.ui;

import com.payroll.model.Payroll;
import com.payroll.model.PayrollStatus;
import com.payroll.service.DepartmentService;
import com.payroll.service.EmployeeService;
import com.payroll.service.PayrollService;
import com.payroll.util.Money;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.time.YearMonth;
import java.util.List;
import java.util.function.Consumer;

/** Landing tab: executive KPI cards and quick action hub. */
class OverviewPanel extends JPanel implements Refreshable {
    private final EmployeeService employees = new EmployeeService();
    private final DepartmentService departments = new DepartmentService();
    private final PayrollService payroll = new PayrollService();
    private final JLabel employeesValue = value();
    private final JLabel departmentsValue = value();
    private final JLabel netValue = value();
    private final JLabel pendingValue = value();
    private final JLabel monthLabel = new JLabel(" ");
    private final Consumer<Integer> tabSwitcher;

    OverviewPanel() {
        this(null);
    }

    OverviewPanel(Consumer<Integer> tabSwitcher) {
        super(new BorderLayout(0, 16));
        this.tabSwitcher = tabSwitcher;
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        setBackground(new Color(0xF8FAFC));

        // Top Header
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        monthLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        monthLabel.setForeground(new Color(0x0F172A));
        JLabel subtitle = new JLabel("Executive summary of workforce, payroll commitments, and pending actions.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(new Color(0x64748B));
        top.add(monthLabel, BorderLayout.NORTH);
        top.add(subtitle, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // Center: 4 Metric Cards + Quick Actions
        JPanel center = new JPanel(new BorderLayout(0, 18));
        center.setOpaque(false);

        JPanel cards = new JPanel(new GridLayout(2, 2, 16, 16));
        cards.setOpaque(false);
        cards.add(metricCard("👥 Active Employees", employeesValue, "Active personnel on payroll", new Color(0x4F46E5)));
        cards.add(metricCard("🏢 Departments", departmentsValue, "Organizational business units", new Color(0x10B981)));
        cards.add(metricCard("💰 Net Payroll (Last Month)", netValue, "Total processed compensation", new Color(0x8B5CF6)));
        cards.add(metricCard("⏳ Awaiting Verification", pendingValue, "Records requiring HR review", new Color(0xF59E0B)));
        center.add(cards, BorderLayout.CENTER);

        // Bottom Action Hub
        JPanel hub = new JPanel(new BorderLayout(16, 0));
        hub.setBackground(Color.WHITE);
        hub.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0), 1, true),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));

        JPanel hubLeft = new JPanel();
        hubLeft.setLayout(new BoxLayout(hubLeft, BoxLayout.Y_AXIS));
        hubLeft.setOpaque(false);

        JLabel hubTitle = new JLabel("⚡ Quick Navigation & Actions");
        hubTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        hubTitle.setForeground(new Color(0x1E293B));

        JLabel hubDesc = new JLabel("Fast shortcuts to core monthly administrative tasks.");
        hubDesc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        hubDesc.setForeground(new Color(0x64748B));

        hubLeft.add(hubTitle);
        hubLeft.add(Box.createVerticalStrut(4));
        hubLeft.add(hubDesc);

        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        actionButtons.setOpaque(false);

        JButton runBtn = Ui.primaryButton("⚡ Run Payroll", () -> jump(5));
        JButton empBtn = Ui.button("➕ Add Employee", () -> jump(1));
        JButton attBtn = Ui.button("📅 Mark Attendance", () -> jump(4));
        JButton repBtn = Ui.button("📊 View Reports", () -> jump(6));

        actionButtons.add(runBtn);
        actionButtons.add(empBtn);
        actionButtons.add(attBtn);
        actionButtons.add(repBtn);

        hub.add(hubLeft, BorderLayout.WEST);
        hub.add(actionButtons, BorderLayout.EAST);

        center.add(hub, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
    }

    private void jump(int tabIndex) {
        if (tabSwitcher != null) {
            tabSwitcher.accept(tabIndex);
        }
    }

    private static JLabel value() {
        JLabel l = new JLabel("-");
        l.setFont(new Font("SansSerif", Font.BOLD, 30));
        l.setForeground(new Color(0x0F172A));
        return l;
    }

    private static JPanel metricCard(String title, JLabel value, String hint, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Top accent strip
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
                g2.dispose();
            }
        };
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0), 1, true),
                BorderFactory.createEmptyBorder(18, 22, 18, 22)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        titleLabel.setForeground(new Color(0x475569));

        JLabel hintLabel = new JLabel(hint);
        hintLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hintLabel.setForeground(new Color(0x94A3B8));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        card.add(hintLabel, BorderLayout.SOUTH);
        return card;
    }

    @Override
    public void refresh() {
        YearMonth last = YearMonth.now().minusMonths(1);
        Ui.async(this, () -> {
            List<Payroll> payrolls = payroll.getPayrolls(last);
            long pending = payrolls.stream().filter(p -> p.status() == PayrollStatus.GENERATED).count();
            return new Object[]{employees.search(null, false, null).size(), departments.getAll().size(),
                    payroll.getSummary(last), pending};
        }, r -> {
            employeesValue.setText(String.valueOf(r[0]));
            departmentsValue.setText(String.valueOf(r[1]));
            netValue.setText(Money.fmt(((PayrollService.Summary) r[2]).net()));
            pendingValue.setText(String.valueOf(r[3]));
            monthLabel.setText("Monthly Overview — " + last);
        });
    }
}