package com.group.motorph.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * Calendar-style date picker dialog. Returns the selected date or null if
 * cancelled.
 */
public class DatePickerDialog extends JDialog {

    private LocalDate selectedDate;
    private int displayYear;
    private int displayMonth; // 1-12

    private final JLabel monthYearLabel = new JLabel("", SwingConstants.CENTER);
    private final JPanel calendarGrid = new JPanel(new GridLayout(0, 7, 2, 2));

    private DatePickerDialog(Frame owner, String title, LocalDate initial) {
        super(owner, title, true);
        selectedDate = null;
        displayYear = initial.getYear();
        displayMonth = initial.getMonthValue();
        buildUI();
        pack();
        setMinimumSize(new Dimension(310, 300));
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    // Public API
    public static LocalDate show(Component parent, String title, LocalDate initial) {
        Frame frame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, parent);
        if (frame == null && parent instanceof Frame f) {
            frame = f;
        }
        DatePickerDialog dlg = new DatePickerDialog(frame, title, initial != null ? initial : LocalDate.now());
        dlg.setVisible(true);
        return dlg.selectedDate;
    }

    public static LocalDate show(Component parent, String title) {
        return show(parent, title, LocalDate.now());
    }

    // UI Creation
    private void buildUI() {
        setLayout(new BorderLayout(4, 4));
        getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));
        getContentPane().setBackground(UITheme.PANEL_BG);

        // Navigation row
        JButton prev = navBtn("\u2039"); // ‹
        JButton next = navBtn("\u203A"); // ›
        prev.addActionListener(e -> navigate(-1));
        next.addActionListener(e -> navigate(1));

        monthYearLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        monthYearLabel.setForeground(UITheme.TEXT_DARK);

        JPanel nav = new JPanel(new BorderLayout(4, 0));
        nav.setBackground(UITheme.PANEL_BG);
        nav.add(prev, BorderLayout.WEST);
        nav.add(monthYearLabel, BorderLayout.CENTER);
        nav.add(next, BorderLayout.EAST);
        add(nav, BorderLayout.NORTH);

        calendarGrid.setBackground(UITheme.PANEL_BG);
        add(calendarGrid, BorderLayout.CENTER);

        JButton cancel = UITheme.dangerButton("Cancel");
        cancel.addActionListener(e -> dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        south.setBackground(UITheme.PANEL_BG);
        south.add(cancel);
        add(south, BorderLayout.SOUTH);

        refreshCalendar();
    }

    private JButton navBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 16));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setForeground(UITheme.PRIMARY);
        return b;
    }

    // Calendar refresh
    private void navigate(int delta) {
        displayMonth += delta;
        if (displayMonth > 12) {
            displayMonth = 1;
            displayYear++;
        }
        if (displayMonth < 1) {
            displayMonth = 12;
            displayYear--;
        }
        refreshCalendar();
    }

    private void refreshCalendar() {
        String monthName = java.time.Month.of(displayMonth)
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        monthYearLabel.setText(monthName + " " + displayYear);

        calendarGrid.removeAll();

        // Day-of-week headers
        for (String d : new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"}) {
            JLabel h = new JLabel(d, SwingConstants.CENTER);
            h.setFont(new Font("SansSerif", Font.BOLD, 11));
            h.setForeground(UITheme.PRIMARY);
            h.setPreferredSize(new Dimension(38, 24));
            calendarGrid.add(h);
        }

        // Empty cells before the 1st
        LocalDate first = LocalDate.of(displayYear, displayMonth, 1);
        int offset = first.getDayOfWeek().getValue() % 7; // Sunday = 0
        for (int i = 0; i < offset; i++) {
            JLabel empty = new JLabel();
            empty.setPreferredSize(new Dimension(38, 32));
            calendarGrid.add(empty);
        }

        // Day cells
        LocalDate today = LocalDate.now();
        int daysInMonth = YearMonth.of(displayYear, displayMonth).lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            calendarGrid.add(new DayCell(LocalDate.of(displayYear, displayMonth, day), today));
        }

        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    // Custom day cell
    // A plain JPanel that paints itself as a rounded day cell.
    private class DayCell extends JPanel {

        private final LocalDate date;
        private final boolean isToday;
        private boolean hovered = false;

        DayCell(LocalDate date, LocalDate today) {
            this.date = date;
            this.isToday = date.equals(today);

            setPreferredSize(new Dimension(38, 32));
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedDate = date;
                    dispose();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg = isToday ? UITheme.PRIMARY : (hovered ? UITheme.TABLE_ROW_HOVER : UITheme.PANEL_BG);
            g2.setColor(bg);
            g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);

            String text = String.valueOf(date.getDayOfMonth());
            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g2.setColor(isToday ? Color.WHITE : UITheme.TEXT_DARK);
            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth() - fm.stringWidth(text)) / 2;
            int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(text, tx, ty);
            g2.dispose();
        }
    }
}
