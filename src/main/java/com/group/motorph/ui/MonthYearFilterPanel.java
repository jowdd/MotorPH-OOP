package com.group.motorph.ui;

import com.group.motorph.model.AttendanceRecord;
import com.group.motorph.model.PayrollRecord;

import javax.swing.*;
import java.awt.*;
import java.time.YearMonth;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Reusable filter panel that shows Month and Year combo boxes.
 *
 * Only shows the months/years where actual data exists, so users
 * cannot select periods with no records.
 *
 * Use the static helper methods to build the available options list
 * from an actual data set (attendance, payroll, etc.).
 */
public class MonthYearFilterPanel extends JPanel {

    private final JComboBox<String> monthCombo;
    private final JComboBox<String> yearCombo;

    // Map from month name → month number (1-12)
    private static final String[] MONTH_NAMES = {
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    };

    /**
     * @param yearMonthPairs sorted list of YearMonth entries that exist in the data
     * @param onFilter       called with the selected (year, month) whenever the filter changes
     */
    public MonthYearFilterPanel(List<YearMonth> yearMonthPairs, Consumer<YearMonth> onFilter) {

        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 6));
        setBackground(UITheme.BACKGROUND);

        // Build unique sorted years
        TreeSet<Integer> years = new TreeSet<>();
        for (YearMonth ym : yearMonthPairs) years.add(ym.getYear());

        yearCombo  = new JComboBox<>(years.stream().map(String::valueOf).toArray(String[]::new));
        monthCombo = new JComboBox<>();

        updateMonthsForYear(yearMonthPairs);

        JLabel yearLbl  = new JLabel("Year:");  yearLbl.setFont(UITheme.FONT_BODY);
        JLabel monthLbl = new JLabel("Month:"); monthLbl.setFont(UITheme.FONT_BODY);
        yearCombo.setFont(UITheme.FONT_BODY);
        monthCombo.setFont(UITheme.FONT_BODY);

        add(yearLbl);
        add(yearCombo);
        add(monthLbl);
        add(monthCombo);

        // Re-populate months when year changes
        yearCombo.addActionListener(e -> {
            updateMonthsForYear(yearMonthPairs);
            if (onFilter != null) onFilter.accept(getSelected());
        });

        monthCombo.addActionListener(e -> {
            if (monthCombo.getSelectedIndex() >= 0 && onFilter != null) {
                onFilter.accept(getSelected());
            }
        });

        // If no data, show placeholder
        if (yearMonthPairs.isEmpty()) {
            monthCombo.addItem("N/A");
            yearCombo.addItem("N/A");
        }
    }

    /** Simple constructor for backward compatibility – static lists, no callback. */
    public MonthYearFilterPanel(List<String> months, List<String> years) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 6));
        setBackground(UITheme.BACKGROUND);
        monthCombo = new JComboBox<>(months.toArray(new String[0]));
        yearCombo  = new JComboBox<>(years.toArray(new String[0]));
        add(new JLabel("Year:"));  add(yearCombo);
        add(new JLabel("Month:")); add(monthCombo);
    }

    // ----------------------------------------------------------------

    /** Returns the currently selected YearMonth. */
    public YearMonth getSelected() {
        try {
            int year  = Integer.parseInt((String) yearCombo.getSelectedItem());
            String mn = (String) monthCombo.getSelectedItem();
            int month = monthNameToNumber(mn);
            return YearMonth.of(year, month);
        } catch (Exception e) {
            return YearMonth.now();
        }
    }

    public String getSelectedMonth() { return (String) monthCombo.getSelectedItem(); }
    public String getSelectedYear()  { return (String) yearCombo.getSelectedItem(); }

    // ----------------------------------------------------------------
    //  Internal helpers
    // ----------------------------------------------------------------

    private void updateMonthsForYear(List<YearMonth> allYearMonths) {
        String selYear = (String) yearCombo.getSelectedItem();
        if (selYear == null) return;
        int y;
        try { y = Integer.parseInt(selYear); } catch (Exception ex) { return; }

        TreeSet<Integer> months = new TreeSet<>();
        for (YearMonth ym : allYearMonths) {
            if (ym.getYear() == y) months.add(ym.getMonthValue());
        }

        String prevMonth = (String) monthCombo.getSelectedItem();
        monthCombo.removeAllItems();
        for (int m : months) monthCombo.addItem(MONTH_NAMES[m - 1]);

        // Try to restore previous selection
        if (prevMonth != null) {
            for (int i = 0; i < monthCombo.getItemCount(); i++) {
                if (monthCombo.getItemAt(i).equals(prevMonth)) {
                    monthCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private static int monthNameToNumber(String name) {
        for (int i = 0; i < MONTH_NAMES.length; i++) {
            if (MONTH_NAMES[i].equalsIgnoreCase(name)) return i + 1;
        }
        return 1;
    }

    // ----------------------------------------------------------------
    //  Static helper methods to extract YearMonth lists from data
    // ----------------------------------------------------------------

    /**
     * Extract sorted, distinct YearMonth values from a list of AttendanceRecords.
     */
    public static List<YearMonth> fromAttendance(List<AttendanceRecord> records) {
        TreeSet<YearMonth> set = new TreeSet<>();
        for (AttendanceRecord r : records) {
            if (r.getDate() != null) set.add(YearMonth.from(r.getDate()));
        }
        return new ArrayList<>(set);
    }

    /**
     * Extract sorted, distinct YearMonth values from a list of PayrollRecords.
     */
    public static List<YearMonth> fromPayroll(List<PayrollRecord> records) {
        TreeSet<YearMonth> set = new TreeSet<>();
        for (PayrollRecord r : records) {
            set.add(YearMonth.of(r.getYear(), r.getMonth()));
        }
        return new ArrayList<>(set);
    }
}

