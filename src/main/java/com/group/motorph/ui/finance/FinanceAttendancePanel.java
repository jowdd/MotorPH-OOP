package com.group.motorph.ui.finance;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.group.motorph.model.AttendanceRecord;
import com.group.motorph.model.User;
import com.group.motorph.service.AttendanceApprovalService;
import com.group.motorph.service.AttendanceService;
import com.group.motorph.service.PayrollService;
import com.group.motorph.ui.components.DatePickerDialog;
import com.group.motorph.ui.components.DialogUtil;
import com.group.motorph.ui.components.RoundedPanel;
import com.group.motorph.ui.components.UITheme;

/**
 * Payroll Management panel — lets Finance staff review pending attendance
 * records, approve a full month, and process payroll.
 */
public class FinanceAttendancePanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("H:mm");
    private static final int ATT_ACT_COL = 6;

    private static final String[] MONTH_NAMES = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    private final AttendanceService attendanceService = new AttendanceService();
    private final AttendanceApprovalService approvalService = new AttendanceApprovalService();
    private final PayrollService payrollService = new PayrollService();

    private JComboBox<String> attMonthCombo;
    private JComboBox<String> attYearCombo;
    private DefaultTableModel attModel;
    private JTable attTable;

    public FinanceAttendancePanel(User user) {
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        build();
    }

    private void build() {
        JPanel north = new JPanel(new BorderLayout());
        north.setBackground(UITheme.BACKGROUND);
        north.add(UITheme.sectionHeader("Payroll Management"), BorderLayout.NORTH);

        RoundedPanel card = new RoundedPanel(new BorderLayout(0, 8), 12, UITheme.PANEL_BORDER);
        card.setBackground(UITheme.PANEL_BG);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.add(buildFilterRow(), BorderLayout.NORTH);

        attModel = new DefaultTableModel(
                new String[]{"Employee ID", "Employee Name", "Date",
                    "Log In", "Log Out", "Status", "Action"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == ATT_ACT_COL;
            }
        };
        attTable = new JTable(attModel);
        UITheme.styleTable(attTable);
        attTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Color-coded Status column
        attTable.getColumnModel().getColumn(5).setCellRenderer((t, v, sel, foc, row, col) -> {
            JLabel lbl = new JLabel(v != null ? v.toString() : "", SwingConstants.CENTER);
            lbl.setFont(UITheme.FONT_BODY.deriveFont(Font.BOLD));
            String status = v != null ? v.toString() : "";
            if ("Approved".equalsIgnoreCase(status)) {
                lbl.setForeground(UITheme.SUCCESS);
            } else if ("Processed".equalsIgnoreCase(status)) {
                lbl.setForeground(UITheme.PRIMARY);
            } else if ("Declined".equalsIgnoreCase(status)) {
                lbl.setForeground(UITheme.DANGER);
            } else {
                lbl.setForeground(new Color(0xFF8C00));
            }
            lbl.setOpaque(true);
            lbl.setBackground(UITheme.rowBackground(t, row, sel));
            return lbl;
        });

        // "Edit" button renderer — hidden for Approved/Processed rows
        JButton editRenderBtn = UITheme.tableActionButton("Edit");
        editRenderBtn.setOpaque(false);
        JPanel editCell = new JPanel(new java.awt.GridBagLayout());
        editCell.add(editRenderBtn);
        JLabel emptyCell = new JLabel();
        emptyCell.setOpaque(true);
        attTable.getColumnModel().getColumn(ATT_ACT_COL).setCellRenderer((t, v, sel, foc, row, col) -> {
            String status = (String) attModel.getValueAt(row, 5);
            if ("Approved".equalsIgnoreCase(status) || "Processed".equalsIgnoreCase(status)) {
                emptyCell.setBackground(UITheme.rowBackground(t, row, sel));
                return emptyCell;
            }
            editCell.setBackground(UITheme.rowBackground(t, row, sel));
            return editCell;
        });
        attTable.getColumnModel().getColumn(5).setHeaderRenderer(UITheme.centeredHeaderRenderer());
        attTable.getColumnModel().getColumn(ATT_ACT_COL).setHeaderRenderer(UITheme.centeredHeaderRenderer());
        UITheme.setActionColumns(attTable, ATT_ACT_COL);

        attTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        attTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        attTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        attTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        attTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        attTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        attTable.getColumnModel().getColumn(6).setPreferredWidth(70);

        attTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = attTable.columnAtPoint(e.getPoint());
                int row = attTable.rowAtPoint(e.getPoint());
                if (col == ATT_ACT_COL && row >= 0) {
                    String status = (String) attModel.getValueAt(row, 5);
                    if (!"Approved".equalsIgnoreCase(status) && !"Processed".equalsIgnoreCase(status)) {
                        openEditDialog(row);
                    }
                }
            }
        });

        card.add(UITheme.scrollPane(attTable), BorderLayout.CENTER);

        JPanel cardWrap = new JPanel(new BorderLayout());
        cardWrap.setBackground(UITheme.BACKGROUND);
        cardWrap.setBorder(new EmptyBorder(0, 16, 16, 16));
        cardWrap.add(card, BorderLayout.CENTER);

        add(north, BorderLayout.NORTH);
        add(cardWrap, BorderLayout.CENTER);
        refreshAttendance();
    }

    private JPanel buildFilterRow() {
        List<AttendanceRecord> allAtt = attendanceService.getAllAttendance();
        TreeSet<Integer> yearsSet = new TreeSet<>();
        TreeSet<Integer> monthsSet = new TreeSet<>();
        for (AttendanceRecord r : allAtt) {
            if (r.getDate() == null) {
                continue;
            }
            yearsSet.add(r.getDate().getYear());
            monthsSet.add(r.getDate().getMonthValue());
        }

        String[] monthItems = monthsSet.stream().map(m -> MONTH_NAMES[m - 1]).toArray(String[]::new);
        String[] yearItems = yearsSet.stream().map(String::valueOf).toArray(String[]::new);

        attMonthCombo = new JComboBox<>(monthItems.length > 0 ? monthItems : new String[]{"January"});
        attYearCombo = new JComboBox<>(yearItems.length > 0 ? yearItems
                : new String[]{String.valueOf(LocalDate.now().getYear())});
        attMonthCombo.setFont(UITheme.FONT_BODY);
        attYearCombo.setFont(UITheme.FONT_BODY);

        setDefaultFilter(allAtt);
        attMonthCombo.addActionListener(e -> refreshAttendance());
        attYearCombo.addActionListener(e -> refreshAttendance());

        JLabel cycleLbl = new JLabel("Pay Cycle Month:");
        cycleLbl.setFont(UITheme.FONT_BODY);

        JPanel filterSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filterSection.setBackground(UITheme.PANEL_BG);
        filterSection.add(cycleLbl);
        filterSection.add(attMonthCombo);
        filterSection.add(attYearCombo);

        JButton approveBtn = UITheme.primaryButton("Approve Attendance");
        approveBtn.addActionListener(e -> approveMonth());
        JButton processBtn = UITheme.successButton("Process Payroll");
        processBtn.addActionListener(e -> processPayroll());

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBtns.setBackground(UITheme.PANEL_BG);
        rightBtns.add(approveBtn);
        rightBtns.add(processBtn);

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(UITheme.PANEL_BG);
        row.add(filterSection, BorderLayout.WEST);
        row.add(rightBtns, BorderLayout.EAST);
        return row;
    }

    /**
     * Defaults the visible payroll period to the month immediately after the
     * latest approved/processed attendance batch.
     *
     * This matches the operational flow: finance approves one attendance month
     * first, then the next actionable month becomes the natural default for
     * payroll review and processing.
     */
    private void setDefaultFilter(List<AttendanceRecord> allAtt) {
        YearMonth latest = null;
        for (AttendanceRecord r : allAtt) {
            if (r.getDate() == null) {
                continue;
            }
            String s = r.getStatus() != null ? r.getStatus().trim() : "";
            if ("Approved".equalsIgnoreCase(s) || "Processed".equalsIgnoreCase(s)) {
                YearMonth ym = YearMonth.of(r.getDate().getYear(), r.getDate().getMonthValue());
                if (latest == null || ym.isAfter(latest)) {
                    latest = ym;
                }
            }
        }
        if (latest == null) {
            return;
        }
        YearMonth next = latest.plusMonths(1);
        for (int i = 0; i < attYearCombo.getItemCount(); i++) {
            if (attYearCombo.getItemAt(i).equals(String.valueOf(next.getYear()))) {
                attYearCombo.setSelectedIndex(i);
                break;
            }
        }
        String targetMonth = MONTH_NAMES[next.getMonthValue() - 1];
        for (int i = 0; i < attMonthCombo.getItemCount(); i++) {
            if (attMonthCombo.getItemAt(i).equals(targetMonth)) {
                attMonthCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void refreshAttendance() {
        if (attModel == null || attMonthCombo == null || attYearCombo == null) {
            return;
        }
        attModel.setRowCount(0);
        int month, year;
        try {
            month = monthToNumber((String) attMonthCombo.getSelectedItem());
            year = Integer.parseInt((String) attYearCombo.getSelectedItem());
        } catch (NumberFormatException ex) {
            return;
        }

        for (AttendanceRecord r : attendanceService.getAllAttendance()) {
            if (r.getDate() == null) {
                continue;
            }
            if (r.getDate().getMonthValue() != month || r.getDate().getYear() != year) {
                continue;
            }
            String empId = r.getEmployeeId() != null ? r.getEmployeeId().trim() : "";
            String ln = r.getLastName() != null ? r.getLastName().trim() : "";
            String fn = r.getFirstName() != null ? r.getFirstName().trim() : "";
            String empName = ln.isEmpty() && fn.isEmpty() ? empId : ln + ", " + fn;
            String inStr = r.getClockIn() != null ? r.getClockIn().format(TIME_FMT) : "";
            String outStr = r.getClockOut() != null ? r.getClockOut().format(TIME_FMT) : "";
            String status = r.getStatus() != null ? r.getStatus().trim() : "Pending";
            attModel.addRow(new Object[]{empId, empName, r.getDate().format(DATE_FMT),
                inStr, outStr, status, "Edit"});
        }
    }

    private void approveMonth() {
        if (attMonthCombo == null || attYearCombo == null) {
            return;
        }
        int month, year;
        try {
            month = monthToNumber((String) attMonthCombo.getSelectedItem());
            year = Integer.parseInt((String) attYearCombo.getSelectedItem());
        } catch (NumberFormatException ex) {
            return;
        }

        String monthName = MONTH_NAMES[month - 1];
        if (!DialogUtil.showConfirmDialog(SwingUtilities.getWindowAncestor(this),
                "Approve all attendance records for " + monthName + " " + year + "?")) {
            return;
        }

        int count = approvalService.approveMonth(month, year);
        if (count > 0) {
            DialogUtil.showInfoDialog(SwingUtilities.getWindowAncestor(this),
                    count + " record(s) approved for " + monthName + " " + year + ".");
            refreshAttendance();
        } else {
            DialogUtil.showInfoDialog(SwingUtilities.getWindowAncestor(this),
                    "No pending records found for " + monthName + " " + year + ".");
        }
    }

    private void processPayroll() {
        if (attMonthCombo == null || attYearCombo == null) {
            return;
        }
        int month, year;
        try {
            month = monthToNumber((String) attMonthCombo.getSelectedItem());
            year = Integer.parseInt((String) attYearCombo.getSelectedItem());
        } catch (NumberFormatException ex) {
            return;
        }

        String monthName = MONTH_NAMES[month - 1];
        if (!DialogUtil.showConfirmDialog(SwingUtilities.getWindowAncestor(this),
                "Process payroll for " + monthName + " " + year
                + "?<br>Only Approved records will be processed.")) {
            return;
        }

        int processed = payrollService.processPayrollForMonth(month, year);
        if (processed > 0) {
            DialogUtil.showInfoDialog(SwingUtilities.getWindowAncestor(this),
                    processed + " payroll record(s) generated for " + monthName + " " + year + ".");
            refreshAttendance();
        } else {
            DialogUtil.showInfoDialog(SwingUtilities.getWindowAncestor(this),
                    "No approved records found for " + monthName + " " + year
                    + ",<br>or payroll already processed.");
        }
    }

    /**
     * Opens the edit/delete dialog for one attendance row selected from the
     * table.
     *
     * Any saved edit intentionally resets the row back to {@code Pending}. That
     * forces finance to re-review the changed record before it can be approved
     * again and eventually included in payroll.
     */
    private void openEditDialog(int row) {
        String empId = (String) attModel.getValueAt(row, 0);
        String dateStr = (String) attModel.getValueAt(row, 2);
        String inStr = (String) attModel.getValueAt(row, 3);
        String outStr = (String) attModel.getValueAt(row, 4);

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr, DATE_FMT);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Cannot parse date: " + dateStr);
            return;
        }

        LocalTime parsedIn = tryParseTime(inStr);
        LocalTime parsedOut = tryParseTime(outStr);

        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, "Edit Attendance", true);
        dlg.setSize(430, 360);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(UITheme.PANEL_BG);

        JLabel titleLbl = new JLabel("Edit Attendance");
        titleLbl.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 18));
        titleLbl.setForeground(UITheme.PRIMARY);
        titleLbl.setBorder(new EmptyBorder(22, 28, 0, 28));
        dlg.add(titleLbl, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.PANEL_BG);
        form.setBorder(new EmptyBorder(12, 28, 8, 28));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(10, 0, 10, 12);

        final LocalDate[] selectedDate = {date};
        DateTimeFormatter displayFmt = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
        JButton datePicker = new JButton("\uD83D\uDCC5  " + date.format(displayFmt));
        datePicker.setFont(UITheme.FONT_BODY);
        datePicker.setBackground(Color.WHITE);
        datePicker.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        datePicker.setHorizontalAlignment(SwingConstants.LEFT);
        datePicker.addActionListener(ev -> {
            LocalDate chosen = DatePickerDialog.show(dlg, "Select Date", selectedDate[0]);
            if (chosen != null) {
                selectedDate[0] = chosen;
                datePicker.setText("\uD83D\uDCC5  " + chosen.format(displayFmt));
            }
        });

        JPanel inPanel = buildTimePanel(parsedIn);
        JPanel outPanel = buildTimePanel(parsedOut);
        JLabel msgLbl = new JLabel(" ");
        msgLbl.setFont(UITheme.FONT_SMALL);

        g.gridx = 0;
        g.gridy = 0;
        g.weightx = 0.30;
        JLabel dateLbl = new JLabel("Date:");
        dateLbl.setFont(UITheme.FONT_BODY);
        form.add(dateLbl, g);
        g.gridx = 1;
        g.weightx = 0.70;
        form.add(datePicker, g);

        g.gridx = 0;
        g.gridy = 1;
        g.weightx = 0.30;
        JLabel inLbl = new JLabel("Log In:");
        inLbl.setFont(UITheme.FONT_BODY);
        form.add(inLbl, g);
        g.gridx = 1;
        g.weightx = 0.70;
        form.add(inPanel, g);

        g.gridx = 0;
        g.gridy = 2;
        g.weightx = 0.30;
        JLabel outLbl = new JLabel("Log Out:");
        outLbl.setFont(UITheme.FONT_BODY);
        form.add(outLbl, g);
        g.gridx = 1;
        g.weightx = 0.70;
        form.add(outPanel, g);

        g.gridx = 0;
        g.gridy = 3;
        g.gridwidth = 2;
        g.insets = new Insets(4, 0, 0, 0);
        form.add(msgLbl, g);

        dlg.add(form, BorderLayout.CENTER);

        JButton deleteBtn = UITheme.neutralButton("Delete Log");
        JButton cancelBtn = UITheme.dangerButton("Cancel");
        JButton saveBtn = UITheme.primaryButton("Save");

        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftBtns.setBackground(UITheme.PANEL_BG);
        leftBtns.add(deleteBtn);
        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBtns.setBackground(UITheme.PANEL_BG);
        rightBtns.add(cancelBtn);
        rightBtns.add(saveBtn);

        JPanel btnRow = new JPanel(new BorderLayout());
        btnRow.setBackground(UITheme.PANEL_BG);
        btnRow.setBorder(new EmptyBorder(0, 28, 20, 28));
        btnRow.add(leftBtns, BorderLayout.WEST);
        btnRow.add(rightBtns, BorderLayout.EAST);
        dlg.add(btnRow, BorderLayout.SOUTH);

        deleteBtn.addActionListener(ev -> {
            if (showDeleteConfirm(dlg)) {
                boolean ok = attendanceService.deleteAttendanceRecord(empId, selectedDate[0]);
                if (ok) {
                    dlg.dispose();
                    refreshAttendance();
                } else {
                    msgLbl.setText("Delete failed.");
                    msgLbl.setForeground(UITheme.DANGER);
                }
            }
        });
        cancelBtn.addActionListener(ev -> dlg.dispose());
        saveBtn.addActionListener(ev -> {
            LocalTime clockIn = readTimePanel(inPanel, msgLbl, "Log In");
            if (clockIn == null) {
                return;
            }
            LocalTime clockOut = readTimePanel(outPanel, msgLbl, "Log Out");
            if (clockOut == null) {
                return;
            }
            if (!clockOut.isAfter(clockIn)) {
                msgLbl.setText("Log Out must be after Log In.");
                msgLbl.setForeground(UITheme.DANGER);
                return;
            }
            AttendanceRecord updated = new AttendanceRecord();
            updated.setEmployeeId(empId);
            updated.setDate(selectedDate[0]);
            updated.setClockIn(clockIn);
            updated.setClockOut(clockOut);
            updated.setStatus("Pending");
            boolean ok = attendanceService.updateAttendanceRecord(empId, date, updated);
            if (ok) {
                dlg.dispose();
                refreshAttendance();
            } else {
                msgLbl.setText("Update failed.");
                msgLbl.setForeground(UITheme.DANGER);
            }
        });

        dlg.setVisible(true);
    }

    /**
     * Builds a two-part time input composed of a free-form H:MM text field and
     * an AM/PM selector.
     *
     * The UI stores attendance as 24-hour {@code LocalTime}, but the dialog
     * exposes a friendlier 12-hour form for manual editing.
     */
    private JPanel buildTimePanel(LocalTime t) {
        int h24 = t != null ? t.getHour() : 8;
        int min = t != null ? t.getMinute() : 0;
        String ampm;
        int h12;
        if (h24 == 0) {
            h12 = 12;
            ampm = "AM";
        } else if (h24 < 12) {
            h12 = h24;
            ampm = "AM";
        } else if (h24 == 12) {
            h12 = 12;
            ampm = "PM";
        } else {
            h12 = h24 - 12;
            ampm = "PM";
        }

        JTextField timeField = new JTextField(String.format("%d:%02d", h12, min), 6);
        timeField.setFont(UITheme.FONT_BODY);
        JComboBox<String> ampmCombo = new JComboBox<>(new String[]{"AM", "PM"});
        ampmCombo.setFont(UITheme.FONT_BODY);
        ampmCombo.setSelectedItem(ampm);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setBackground(UITheme.PANEL_BG);
        p.add(timeField);
        p.add(ampmCombo);
        return p;
    }

    /**
     * Converts the dialog's 12-hour time input back into a 24-hour
     * {@code LocalTime}.
     *
     * The helper validates the H:MM pattern, clamps the hour range to 1..12,
     * and handles the noon/midnight edge cases where 12 AM maps to 00:xx while
     * 12 PM remains 12:xx.
     */
    private static LocalTime readTimePanel(JPanel p, JLabel msgLbl, String fieldName) {
        JTextField tf = (JTextField) p.getComponent(0);
        JComboBox<?> cb = (JComboBox<?>) p.getComponent(1);
        String txt = tf.getText().trim();
        String ampm = (String) cb.getSelectedItem();
        try {
            String[] parts = txt.split(":");
            int h = Integer.parseInt(parts[0].trim());
            int m = Integer.parseInt(parts[1].trim());
            if (h < 1 || h > 12 || m < 0 || m > 59) {
                throw new Exception();
            }
            if ("AM".equals(ampm)) {
                if (h == 12) {
                    h = 0;

                }
            } else {
                if (h != 12) {
                    h += 12;

                }
            }
            return LocalTime.of(h, m);
        } catch (Exception e) {
            msgLbl.setText(fieldName + ": enter time as H:MM  (e.g. 8:00)");
            msgLbl.setForeground(UITheme.DANGER);
            return null;
        }
    }

    private static LocalTime tryParseTime(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(s.trim(), DateTimeFormatter.ofPattern("H:mm"));
        } catch (Exception e) {
            return null;
        }
    }

    private boolean showDeleteConfirm(java.awt.Component parent) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(parent);
        JDialog dlg = new JDialog(owner, "Confirm Delete", true);
        dlg.setSize(340, 180);
        dlg.setLocationRelativeTo(parent);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(UITheme.PANEL_BG);

        JLabel msg = new JLabel(
                "<html><div style='text-align:center'>Are you sure you want to<br>delete this record?</div></html>",
                SwingConstants.CENTER);
        msg.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 15));
        msg.setForeground(UITheme.PRIMARY);
        msg.setBorder(new EmptyBorder(24, 16, 16, 16));
        dlg.add(msg, BorderLayout.CENTER);

        final boolean[] result = {false};
        JButton yes = UITheme.primaryButton("Yes");
        JButton no = UITheme.dangerButton("No");
        yes.setPreferredSize(new Dimension(100, 38));
        no.setPreferredSize(new Dimension(100, 38));
        yes.addActionListener(e -> {
            result[0] = true;
            dlg.dispose();
        });
        no.addActionListener(e -> {
            result[0] = false;
            dlg.dispose();
        });

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
        btns.setBackground(UITheme.PANEL_BG);
        btns.add(no);
        btns.add(yes);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setVisible(true);
        return result[0];
    }

    private static int monthToNumber(String name) {
        if (name == null) {
            return 1;
        }
        for (int i = 0; i < MONTH_NAMES.length; i++) {
            if (MONTH_NAMES[i].equalsIgnoreCase(name)) {
                return i + 1;
            }
        }
        return 1;
    }
}
