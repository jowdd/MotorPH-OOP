package com.group.motorph.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * A simple calendar-style date picker dialog.
 *
 * Usage:
 *   LocalDate chosen = DatePickerDialog.show(parentComponent, "Select Date", LocalDate.now());
 *   if (chosen != null) { ... }
 */
public class DatePickerDialog extends JDialog {

    private LocalDate selectedDate;
    private int displayYear;
    private int displayMonth; // 1-12

    private final JLabel monthYearLabel = new JLabel("", SwingConstants.CENTER);
    private final JPanel calendarGrid = new JPanel(new GridLayout(0, 7, 4, 4));

    private DatePickerDialog(Frame owner, String title, LocalDate initial) {
        super(owner, title, true);
        selectedDate = null;
        displayYear =  initial.getYear();
        displayMonth = initial.getMonthValue();
        buildUI();
        setSize(320, 300);
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    // ---------------------------------------------------------------

    /**
     * Show the dialog and return the chosen date, or null if cancelled.
     */
    public static LocalDate show(Component parent, String title, LocalDate initial) {
        Frame frame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, parent);
        DatePickerDialog dlg = new DatePickerDialog(frame, title, initial == null ? LocalDate.now() : initial);
        dlg.setVisible(true);
        return dlg.selectedDate;
    }

    /** Convenience – uses today as default. */
    public static LocalDate show(Component parent, String title) {
        return show(parent, title, LocalDate.now());
    }

    // ---------------------------------------------------------------

    private void buildUI() {
        setLayout(new BorderLayout(8, 8));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Navigation row
        JButton prev = new JButton("<");
        JButton next = new JButton(">");
        prev.addActionListener(e -> navigate(-1));
        next.addActionListener(e -> navigate(1));

        JPanel nav = new JPanel(new BorderLayout());
        nav.add(prev, BorderLayout.WEST);
        nav.add(monthYearLabel, BorderLayout.CENTER);
        nav.add(next, BorderLayout.EAST);
        add(nav, BorderLayout.NORTH);

        add(calendarGrid, BorderLayout.CENTER);

        // Cancel button
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(cancel);
        add(south, BorderLayout.SOUTH);

        refreshCalendar();
    }

    private void navigate(int delta) {
        displayMonth += delta;
        if (displayMonth > 12) { displayMonth = 1;  displayYear++; }
        if (displayMonth < 1)  { displayMonth = 12; displayYear--; }
        refreshCalendar();
    }

    private void refreshCalendar() {
        String monthName = java.time.Month.of(displayMonth)
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        monthYearLabel.setText(monthName + " " + displayYear);
        monthYearLabel.setFont(UITheme.FONT_HEADER);

        calendarGrid.removeAll();

        // Day-of-week headers
        String[] days = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
        for (String d : days) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(UITheme.FONT_SMALL);
            lbl.setForeground(UITheme.PRIMARY);
            calendarGrid.add(lbl);
        }

        // First day offset (SUNDAY=1)
        LocalDate first = LocalDate.of(displayYear, displayMonth, 1);
        int offset = first.getDayOfWeek().getValue() % 7; // Sunday=0
        for (int i = 0; i < offset; i++) calendarGrid.add(new JLabel());

        // Day buttons
        int daysInMonth = YearMonth.of(displayYear, displayMonth).lengthOfMonth();
        LocalDate today = LocalDate.now();
        for (int day = 1; day <= daysInMonth; day++) {
            final LocalDate date = LocalDate.of(displayYear, displayMonth, day);
            JButton btn = new JButton(String.valueOf(day));
            btn.setFont(UITheme.FONT_BODY);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setOpaque(true);
            btn.setBackground(date.equals(today) ? UITheme.PRIMARY : Color.WHITE);
            btn.setForeground(date.equals(today) ? Color.WHITE : UITheme.TEXT_DARK);
            btn.addActionListener(e -> {
                selectedDate = date;
                dispose();
            });
            calendarGrid.add(btn);
        }

        calendarGrid.revalidate();
        calendarGrid.repaint();
    }
}
