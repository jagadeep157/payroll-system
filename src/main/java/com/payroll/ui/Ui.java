package com.payroll.ui;

import com.payroll.db.DataAccessException;
import com.payroll.service.ServiceException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Shared Swing helpers so panels stay short. */
public final class Ui {
    private Ui() {
    }

    public static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static JTable table(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setAutoCreateRowSorter(true);
        t.setRowHeight(34);
        t.setFillsViewportHeight(true);
        t.setShowHorizontalLines(true);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(0xE2E8F0));
        t.setSelectionBackground(new Color(0xEEF2FF));
        t.setSelectionForeground(new Color(0x1E1B4B));
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        t.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 36));

        // Default cell renderer with padding and status pill badges
        t.setDefaultRenderer(Object.class, new StatusCellRenderer());
        return t;
    }

    public static JButton button(String text, Runnable action) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> action.run());
        return b;
    }

    public static JButton primaryButton(String text, Runnable action) {
        JButton b = button(text, action);
        b.setBackground(new Color(0x4F46E5));
        b.setForeground(Color.WHITE);
        b.setFont(b.getFont().deriveFont(Font.BOLD));
        b.putClientProperty("FlatLaf.style", "hoverBackground: #4338CA; pressedBackground: #3730A3");
        return b;
    }

    public static JButton successButton(String text, Runnable action) {
        JButton b = button(text, action);
        b.setBackground(new Color(0x10B981));
        b.setForeground(Color.WHITE);
        b.setFont(b.getFont().deriveFont(Font.BOLD));
        b.putClientProperty("FlatLaf.style", "hoverBackground: #059669; pressedBackground: #047857");
        return b;
    }

    public static JButton dangerButton(String text, Runnable action) {
        JButton b = button(text, action);
        b.setBackground(new Color(0xEF4444));
        b.setForeground(Color.WHITE);
        b.setFont(b.getFont().deriveFont(Font.BOLD));
        b.putClientProperty("FlatLaf.style", "hoverBackground: #DC2626; pressedBackground: #B91C1C");
        return b;
    }

    public static JPanel cardPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0), 1, true),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));
        return p;
    }

    public static JPanel flow(Component... parts) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        for (Component c : parts) {
            p.add(c);
        }
        return p;
    }

    /** Selected rows converted to model indexes (the table may be sorted). */
    public static int[] selectedRows(JTable t) {
        int[] rows = t.getSelectedRows();
        for (int i = 0; i < rows.length; i++) {
            rows[i] = t.convertRowIndexToModel(rows[i]);
        }
        return rows;
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Payroll", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Please confirm", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static void showError(Component parent, Throwable t) {
        String msg;
        if (t instanceof ServiceException) {
            msg = t.getMessage();
        } else if (t instanceof DataAccessException) {
            t.printStackTrace();
            msg = "Database problem: " + t.getMessage();
        } else {
            t.printStackTrace();
            msg = "Unexpected error: " + t.getMessage();
        }
        JOptionPane.showMessageDialog(parent, msg, "Payroll", JOptionPane.ERROR_MESSAGE);
    }

    /** Runs {@code work} off the Swing thread, then {@code done} (or the error dialog) back on it. */
    public static <T> void async(Component owner, Supplier<T> work, Consumer<T> done) {
        async(owner, work, done, () -> { });
    }

    /** Same, plus {@code always} which runs after success or failure (e.g. to re-enable a button). */
    public static <T> void async(Component owner, Supplier<T> work, Consumer<T> done, Runnable always) {
        Window w = owner == null ? null : SwingUtilities.getWindowAncestor(owner);
        if (w != null) {
            w.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() {
                return work.get();
            }

            @Override
            protected void done() {
                if (w != null) {
                    w.setCursor(Cursor.getDefaultCursor());
                }
                try {
                    done.accept(get());
                } catch (ExecutionException e) {
                    showError(owner, e.getCause());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (RuntimeException e) {
                    showError(owner, e);
                } finally {
                    always.run();
                }
            }
        }.execute();
    }

    public static void async(Component owner, Runnable work, Runnable done) {
        async(owner, () -> {
            work.run();
            return null;
        }, r -> done.run());
    }

    /** Shows a label/field form in a modal OK/Cancel dialog. Returns true when OK was pressed. */
    public static boolean form(Component parent, String title, String[] labels, JComponent[] fields) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < labels.length; i++) {
            c.gridx = 0;
            c.gridy = i;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
            p.add(new JLabel(labels[i]), c);
            c.gridx = 1;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            p.add(fields[i], c);
        }
        return JOptionPane.showConfirmDialog(parent, p, title, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        private static final Color BG_GREEN = new Color(0xDCFCE7);
        private static final Color FG_GREEN = new Color(0x166534);
        private static final Color BG_BLUE = new Color(0xE0E7FF);
        private static final Color FG_BLUE = new Color(0x3730A3);
        private static final Color BG_AMBER = new Color(0xFEF3C7);
        private static final Color FG_AMBER = new Color(0x92400E);
        private static final Color BG_RED = new Color(0xFEE2E2);
        private static final Color FG_RED = new Color(0x991B1B);
        private static final Color BG_GRAY = new Color(0xF1F5F9);
        private static final Color FG_GRAY = new Color(0x475569);

        private boolean isBadge = false;
        private Color badgeBg = null;
        private Color badgeFg = null;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

            isBadge = false;
            String text = value == null ? "" : value.toString().trim();

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF8FAFC));
                setForeground(new Color(0x1E293B));
            }

            switch (text) {
                case "PAID", "VERIFIED", "PRESENT", "Active" -> {
                    isBadge = !isSelected;
                    badgeBg = BG_GREEN;
                    badgeFg = FG_GREEN;
                    if (!isSelected) setForeground(badgeFg);
                }
                case "GENERATED", "REPROCESSED", "MANUAL", "AUTO" -> {
                    isBadge = !isSelected;
                    badgeBg = BG_BLUE;
                    badgeFg = FG_BLUE;
                    if (!isSelected) setForeground(badgeFg);
                }
                case "LEAVE", "RECALCULATED" -> {
                    isBadge = !isSelected;
                    badgeBg = BG_AMBER;
                    badgeFg = FG_AMBER;
                    if (!isSelected) setForeground(badgeFg);
                }
                case "ABSENT", "CANCELLED", "Inactive" -> {
                    isBadge = !isSelected;
                    badgeBg = BG_RED;
                    badgeFg = FG_RED;
                    if (!isSelected) setForeground(badgeFg);
                }
                default -> {
                    // Normal text
                }
            }

            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (isBadge && badgeBg != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(badgeBg);
                int h = Math.min(24, getHeight() - 8);
                int y = (getHeight() - h) / 2;
                int textW = getFontMetrics(getFont()).stringWidth(getText());
                int w = Math.min(getWidth() - 12, textW + 16);
                g2.fillRoundRect(8, y, w, h, h, h);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
