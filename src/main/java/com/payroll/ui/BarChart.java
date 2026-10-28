package com.payroll.ui;

import com.payroll.util.Money;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/** Vertical bar chart for "net salary by department" with modern gradient styling. */
class BarChart extends JPanel {
    private Map<String, BigDecimal> data = new LinkedHashMap<>();
    private String title = "";

    BarChart() {
        setPreferredSize(new Dimension(450, 240));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0), 1, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
    }

    void setData(String title, Map<String, BigDecimal> data) {
        this.title = title;
        this.data = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Title
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.setColor(new Color(0x1E293B));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title.isEmpty() ? "Net Salary by Department" : title, 16, fm.getAscent() + 10);

        if (data == null || data.isEmpty()) {
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g.setColor(new Color(0x94A3B8));
            g.drawString("No payroll data available for this month", 16, getHeight() / 2);
            g.dispose();
            return;
        }

        BigDecimal max = data.values().stream().max(BigDecimal::compareTo).orElse(BigDecimal.ONE);
        if (max.signum() == 0) {
            max = BigDecimal.ONE;
        }

        int left = 16;
        int top = fm.getHeight() + 32;
        int bottom = getHeight() - 36;
        int chartH = Math.max(30, bottom - top);

        // Baseline
        g.setColor(new Color(0xE2E8F0));
        g.drawLine(left, bottom, getWidth() - left, bottom);

        int slot = Math.max(50, (getWidth() - 2 * left) / data.size());
        int barW = Math.min(64, slot - 18);
        int i = 0;

        Color gradStart = new Color(0x6366F1);
        Color gradEnd = new Color(0x4338CA);

        for (Map.Entry<String, BigDecimal> e : data.entrySet()) {
            int h = (int) (chartH * e.getValue().doubleValue() / max.doubleValue());
            int x = left + i * slot + (slot - barW) / 2;

            // Bar gradient
            GradientPaint gp = new GradientPaint(x, bottom - h, gradStart, x, bottom, gradEnd);
            g.setPaint(gp);
            g.fillRoundRect(x, bottom - h, barW, h, 8, 8);

            // Value label
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            FontMetrics vfm = g.getFontMetrics();
            String value = Money.fmt(e.getValue());
            g.setColor(new Color(0x334155));
            g.drawString(value, x + (barW - vfm.stringWidth(value)) / 2, bottom - h - 5);

            // Category label below
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            FontMetrics lfm = g.getFontMetrics();
            g.setColor(new Color(0x64748B));
            String label = e.getKey();
            while (lfm.stringWidth(label) > slot - 6 && label.length() > 3) {
                label = label.substring(0, label.length() - 2);
            }
            g.drawString(label, left + i * slot + (slot - lfm.stringWidth(label)) / 2, bottom + lfm.getAscent() + 6);
            i++;
        }
        g.dispose();
    }
}