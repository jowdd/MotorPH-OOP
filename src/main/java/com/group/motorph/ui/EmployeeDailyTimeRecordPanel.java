package com.group.motorph.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.group.motorph.model.AttendanceRecord;
import com.group.motorph.model.User;
import com.group.motorph.service.AttendanceService;

/**
 * Displays the logged-in user's own Daily Time Record.
 * Columns: Date / Log In / Log Out.
 * Summary bar shows total Working Hours, Over Time and Late minutes for the selected period.
 */
public class EmployeeDailyTimeRecordPanel extends JPanel {

    // ── Business-rules constants
    private static final LocalTime BUSINESS_START = LocalTime.of(8, 0);
    private static final LocalTime BUSINESS_END   = LocalTime.of(17, 0);
    private static final int       GRACE_MINUTES  = 10;   // late if clock-in after 08:10

    // ── Formatters
    private static final DateTimeFormatter TIME_12H  = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_DISP = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);

    private static final String[] MONTH_NAMES = {
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    };

    // ── State
    private final User currentUser;
    private final AttendanceService attendanceService = new AttendanceService();

    private List<AttendanceRecord> allRecords;
    private DefaultTableModel      tableModel;
    private JComboBox<String>      monthCombo;
    private JComboBox<String>      yearCombo;
    private JLabel                 workingHrsLbl;
    private JLabel                 overtimeLbl;
    private JLabel                 lateLbl;

    public EmployeeDailyTimeRecordPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        build();
    }

    // ─────────────────────────────────────────────────────────────────
    private void build() {
        String empId = currentUser.getEmployeeId() != null ? currentUser.getEmployeeId().trim() : "";
        allRecords = attendanceService.getAttendanceByEmployee(empId);

        // Collect distinct months/years for the combos
        TreeSet<Integer> yearsSet  = new TreeSet<>();
        TreeSet<Integer> monthsSet = new TreeSet<>();
        for (AttendanceRecord r : allRecords) {
            if (r.getDate() == null) continue;
            yearsSet .add(r.getDate().getYear());
            monthsSet.add(r.getDate().getMonthValue());
        }

        // Month combo: "All" + month names present in data
        List<String> monthOpts = new ArrayList<>();
        monthOpts.add("All");
        for (int m : monthsSet) monthOpts.add(MONTH_NAMES[m - 1]);
        monthCombo = new JComboBox<>(monthOpts.toArray(new String[0]));
        monthCombo.setFont(UITheme.FONT_BODY);

        // Year combo: "All" + years present in data
        List<String> yearOpts = new ArrayList<>();
        yearOpts.add("All");
        for (int y : yearsSet) yearOpts.add(String.valueOf(y));
        yearCombo = new JComboBox<>(yearOpts.toArray(new String[0]));
        yearCombo.setFont(UITheme.FONT_BODY);

        // ── Filter row
        JLabel cycleLbl = new JLabel("Pay Cycle Month:");
        cycleLbl.setFont(UITheme.FONT_BODY);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filterRow.setBackground(UITheme.PANEL_BG);
        filterRow.add(cycleLbl);
        filterRow.add(monthCombo);
        filterRow.add(yearCombo);

        // ── Summary value labels
        workingHrsLbl = boldLabel("0 Hours");
        overtimeLbl = boldLabel("0 Hours");
        lateLbl = boldLabel("0 Minutes");
        workingHrsLbl.setForeground(UITheme.SUCCESS);
        overtimeLbl.setForeground(UITheme.SUCCESS);
        lateLbl.setForeground(UITheme.DANGER);

        JPanel summaryRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        summaryRow.setBackground(UITheme.PANEL_BG);
        summaryRow.add(summaryGroup("Working Hours:", workingHrsLbl));
        summaryRow.add(summaryGroup("Over Time:", overtimeLbl));
        summaryRow.add(summaryGroup("Late:", lateLbl));

        // ── Table
        tableModel = new DefaultTableModel(new String[]{"Date", "Log In", "Log Out"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);

        // ── Card assembly
        JPanel northInCard = new JPanel(new BorderLayout(0, 8));
        northInCard.setBackground(UITheme.PANEL_BG);
        northInCard.add(filterRow,  BorderLayout.NORTH);
        northInCard.add(summaryRow, BorderLayout.SOUTH);

        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(UITheme.PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.PANEL_BORDER),
                new EmptyBorder(16, 16, 16, 16)));
        card.add(northInCard,            BorderLayout.NORTH);
        card.add(UITheme.scrollPane(table), BorderLayout.CENTER);

        JPanel cardWrap = new JPanel(new BorderLayout());
        cardWrap.setBackground(UITheme.BACKGROUND);
        cardWrap.setBorder(new EmptyBorder(0, 16, 16, 16));
        cardWrap.add(card, BorderLayout.CENTER);

        add(UITheme.sectionHeader("Daily Time Record"), BorderLayout.NORTH);
        add(cardWrap, BorderLayout.CENTER);

        // Wire listeners
        monthCombo.addActionListener(e -> refreshTable());
        yearCombo .addActionListener(e -> refreshTable());

        refreshTable();
    }

    // ─────────────────────────────────────────────────────────────────
    private void refreshTable() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);

        String selMonth = (String) monthCombo.getSelectedItem();
        String selYear  = (String) yearCombo .getSelectedItem();

        boolean allMonths = "All".equals(selMonth);
        boolean allYears  = "All".equals(selYear);

        final int fm = allMonths ? -1 : monthNameToNumber(selMonth);
        final int fy;
        if (allYears) {
            fy = -1;
        } else {
            int parsed = -1;
            try { parsed = Integer.parseInt(selYear); } catch (NumberFormatException ignored) {}
            fy = parsed;
        }

        List<AttendanceRecord> rows = allRecords.stream()
            .filter(r -> r.getDate() != null)
            .filter(r -> allMonths || r.getDate().getMonthValue() == fm)
            .filter(r -> allYears  || r.getDate().getYear()       == fy)
            .sorted(Comparator.comparing(AttendanceRecord::getDate))
            .collect(Collectors.toList());

        long totalWorkMins = 0;
        long totalOtMins   = 0;
        long totalLateMins = 0;

        for (AttendanceRecord rec : rows) {
            String dateStr = rec.getDate().format(DATE_DISP);
            String inStr   = "--:--";
            String outStr  = "--:--";

            if (rec.getClockIn()  != null) inStr  = rec.getClockIn() .format(TIME_12H);
            if (rec.getClockOut() != null) outStr = rec.getClockOut().format(TIME_12H);

            if (rec.getClockIn() != null && rec.getClockOut() != null) {
                LocalTime ci = rec.getClockIn();
                LocalTime co = rec.getClockOut();

                LocalTime graceEnd = BUSINESS_START.plusMinutes(GRACE_MINUTES);
                boolean isLate = ci.isAfter(graceEnd);

                if (isLate) {
                    totalLateMins += Duration.between(BUSINESS_START, ci).toMinutes();
                }

                LocalTime effectiveStart = isLate ? ci : BUSINESS_START;
                LocalTime effectiveEnd   = co.isBefore(BUSINESS_END) ? co : BUSINESS_END;

                if (effectiveEnd.isAfter(effectiveStart)) {
                    totalWorkMins += Duration.between(effectiveStart, effectiveEnd).toMinutes();
                }
                if (co.isAfter(BUSINESS_END)) {
                    totalOtMins += Duration.between(BUSINESS_END, co).toMinutes();
                }
            }

            tableModel.addRow(new Object[]{ dateStr, inStr, outStr });
        }

        workingHrsLbl.setText((totalWorkMins / 60) + " Hours");
        overtimeLbl  .setText((totalOtMins   / 60) + " Hours");
        lateLbl      .setText(totalLateMins        + " Minutes");
    }

    // ── Helpers ────────────────────────────────────────────────────────
    private static JLabel boldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        return l;
    }

    private static JPanel summaryGroup(String caption, JLabel valueLabel) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setBackground(UITheme.PANEL_BG);
        JLabel cap = new JLabel(caption);
        cap.setFont(UITheme.FONT_BODY);
        cap.setForeground(UITheme.TEXT_DARK);
        p.add(cap);
        p.add(valueLabel);
        return p;
    }

    private static int monthNameToNumber(String name) {
        if (name == null) return 1;
        for (int i = 0; i < MONTH_NAMES.length; i++) {
            if (MONTH_NAMES[i].equalsIgnoreCase(name)) return i + 1;
        }
        return 1;
    }
}
