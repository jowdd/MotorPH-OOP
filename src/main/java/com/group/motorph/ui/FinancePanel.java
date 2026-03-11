package com.group.motorph.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.group.motorph.dao.impl.ApprovedAttendanceDAOImpl;
import com.group.motorph.model.AttendanceRecord;
import com.group.motorph.model.Employee;
import com.group.motorph.model.PayrollRecord;
import com.group.motorph.model.User;
import com.group.motorph.service.AttendanceApprovalService;
import com.group.motorph.service.AttendanceService;
import com.group.motorph.service.EmployeeService;
import com.group.motorph.service.PayrollService;

/**
 * Finance panel with two views:
 *   "attendance"  – Attendance Logs: review and approve monthly attendance
 *   "payroll"     – Payroll Management: process payroll from approved attendance
 */
public class FinancePanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("H:mm");

    private static final String[] MONTH_NAMES = {
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    };

    private final User user;
    private final String view;

    private final AttendanceService         attendanceService = new AttendanceService();
    private final AttendanceApprovalService approvalService   = new AttendanceApprovalService();
    private final PayrollService            payrollService    = new PayrollService();
    private final EmployeeService           employeeService   = new EmployeeService();

    // Attendance Logs state
    private JComboBox<String> attMonthCombo;
    private JComboBox<String> attYearCombo;
    private DefaultTableModel attModel;
    private JTable attTable;
    private static final int ATT_ACT_COL = 5;

    // Payroll Management state
    private DefaultTableModel payModel;
    private JTable payTable;

    public FinancePanel(User user, String view) {
        this.user = user;
        this.view = view;
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);

        if ("attendance".equals(view)) buildAttendanceLogs();
        else                           buildPayrollManagement();
    }

    // ================================================================
    //  ATTENDANCE LOGS
    // ================================================================

    private void buildAttendanceLogs() {
        // Page title
        JPanel north = new JPanel(new BorderLayout());
        north.setBackground(UITheme.BACKGROUND);
        north.add(UITheme.sectionHeader("Attendance Logs"), BorderLayout.NORTH);

        // White card
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(UITheme.PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.PANEL_BORDER),
                new EmptyBorder(16, 16, 16, 16)));

        card.add(buildAttFilterRow(), BorderLayout.NORTH);

        // Table: Emp ID, Name, Date, Log In, Log Out, Actions
        attModel = new DefaultTableModel(
                new String[]{"Employee ID", "Employee Name", "Date", "Log In", "Log Out", "Actions"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == ATT_ACT_COL; }
        };
        attTable = new JTable(attModel);
        UITheme.styleTable(attTable);
        attTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Edit button per row via renderer
        JButton editRenderBtn = UITheme.primaryButton("Edit");
        editRenderBtn.setMargin(new Insets(4, 12, 4, 12));
        attTable.getColumnModel().getColumn(ATT_ACT_COL)
                .setCellRenderer((t, v, sel, foc, row, col) -> editRenderBtn);

        attTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        attTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        attTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        attTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        attTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        attTable.getColumnModel().getColumn(5).setPreferredWidth(80);

        // Handle click in Actions column
        attTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int col = attTable.columnAtPoint(e.getPoint());
                int row = attTable.rowAtPoint(e.getPoint());
                if (col == ATT_ACT_COL && row >= 0) openEditAttendanceDialog(row);
            }
        });

        card.add(UITheme.scrollPane(attTable), BorderLayout.CENTER);

        JLabel tableNote = new JLabel("NOTE: In this table, it shows all the attendance logs from different employees for the month selected");
        tableNote.setFont(UITheme.FONT_SMALL);
        tableNote.setForeground(UITheme.DANGER);
        tableNote.setBorder(new EmptyBorder(6, 2, 0, 0));
        card.add(tableNote, BorderLayout.SOUTH);

        JPanel cardWrap = new JPanel(new BorderLayout());
        cardWrap.setBackground(UITheme.BACKGROUND);
        cardWrap.setBorder(new EmptyBorder(0, 16, 16, 16));
        cardWrap.add(card, BorderLayout.CENTER);

        add(north, BorderLayout.NORTH);
        add(cardWrap, BorderLayout.CENTER);

        refreshAttendance();
    }

    private JPanel buildAttFilterRow() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setBackground(UITheme.PANEL_BG);

        JPanel topRow = new JPanel(new BorderLayout(8, 0));
        topRow.setBackground(UITheme.PANEL_BG);

        // Filter controls
        JPanel filterSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filterSection.setBackground(UITheme.PANEL_BG);

        JLabel cycleLbl = new JLabel("Pay Cycle Month:");
        cycleLbl.setFont(UITheme.FONT_BODY);

        List<AttendanceRecord> allAtt = attendanceService.getAllAttendance();
        TreeSet<Integer> yearsSet  = new TreeSet<>();
        TreeSet<Integer> monthsSet = new TreeSet<>();
        for (AttendanceRecord r : allAtt) {
            if (r.getDate() == null) continue;
            yearsSet.add(r.getDate().getYear());
            monthsSet.add(r.getDate().getMonthValue());
        }

        String[] monthItems = monthsSet.stream().map(m -> MONTH_NAMES[m - 1]).toArray(String[]::new);
        String[] yearItems  = yearsSet.stream().map(String::valueOf).toArray(String[]::new);

        attMonthCombo = new JComboBox<>(monthItems.length > 0 ? monthItems : new String[]{"January"});
        attYearCombo  = new JComboBox<>(yearItems.length  > 0 ? yearItems  : new String[]{String.valueOf(LocalDate.now().getYear())});
        attMonthCombo.setFont(UITheme.FONT_BODY);
        attYearCombo.setFont(UITheme.FONT_BODY);

        setDefaultAttFilter(allAtt, yearsSet, monthsSet);

        attMonthCombo.addActionListener(e -> refreshAttendance());
        attYearCombo.addActionListener(e  -> refreshAttendance());

        JLabel note = new JLabel("NOTE: Pay cycle month and year should be the next month after the approved attendance logs month as a default to show");
        note.setFont(UITheme.FONT_SMALL);
        note.setForeground(UITheme.DANGER);

        filterSection.add(cycleLbl);
        filterSection.add(attMonthCombo);
        filterSection.add(attYearCombo);
        filterSection.add(note);

        JButton approveBtn = UITheme.successButton("Approve Attendance Logs");
        approveBtn.addActionListener(e -> approveMonth());

        topRow.add(filterSection, BorderLayout.WEST);
        topRow.add(approveBtn,    BorderLayout.EAST);

        wrapper.add(topRow, BorderLayout.NORTH);
        return wrapper;
    }

    private void setDefaultAttFilter(List<AttendanceRecord> allAtt,
                                     TreeSet<Integer> yearsSet,
                                     TreeSet<Integer> monthsSet) {
        List<AttendanceRecord> approved = new ApprovedAttendanceDAOImpl().getAllApproved();
        if (approved.isEmpty()) return;
        YearMonth latest = null;
        for (AttendanceRecord r : approved) {
            if (r.getDate() == null) continue;
            YearMonth ym = YearMonth.of(r.getDate().getYear(), r.getDate().getMonthValue());
            if (latest == null || ym.isAfter(latest)) latest = ym;
        }
        if (latest == null) return;
        YearMonth nextMonth = latest.plusMonths(1);
        // Try to select that year
        for (int i = 0; i < attYearCombo.getItemCount(); i++) {
            if (attYearCombo.getItemAt(i).equals(String.valueOf(nextMonth.getYear()))) {
                attYearCombo.setSelectedIndex(i); break;
            }
        }
        // Try to select that month
        String targetMonthName = MONTH_NAMES[nextMonth.getMonthValue() - 1];
        for (int i = 0; i < attMonthCombo.getItemCount(); i++) {
            if (attMonthCombo.getItemAt(i).equals(targetMonthName)) {
                attMonthCombo.setSelectedIndex(i); break;
            }
        }
    }

    private void refreshAttendance() {
        if (attModel == null || attMonthCombo == null || attYearCombo == null) return;
        attModel.setRowCount(0);
        int selectedMonth;
        try { selectedMonth = monthNameToNumber((String) attMonthCombo.getSelectedItem()); }
        catch (Exception ex) { return; }
        int selectedYear;
        try { selectedYear = Integer.parseInt((String) attYearCombo.getSelectedItem()); }
        catch (Exception ex) { return; }

        for (AttendanceRecord r : attendanceService.getAllAttendance()) {
            if (r.getDate() == null) continue;
            if (r.getDate().getMonthValue() != selectedMonth || r.getDate().getYear() != selectedYear) continue;

            String empId   = r.getEmployeeId() != null ? r.getEmployeeId().trim() : "";
            String ln      = r.getLastName()   != null ? r.getLastName().trim()   : "";
            String fn      = r.getFirstName()  != null ? r.getFirstName().trim()  : "";
            String empName = ln.isEmpty() && fn.isEmpty() ? empId : ln + ", " + fn;
            String dateStr = r.getDate().format(DATE_FMT);
            String inStr   = r.getClockIn()  != null ? r.getClockIn().format(TIME_FMT)  : "";
            String outStr  = r.getClockOut() != null ? r.getClockOut().format(TIME_FMT) : "";
            attModel.addRow(new Object[]{ empId, empName, dateStr, inStr, outStr, "Edit" });
        }
    }

    private void approveMonth() {
        if (attMonthCombo == null || attYearCombo == null) return;
        int month;
        try { month = monthNameToNumber((String) attMonthCombo.getSelectedItem()); }
        catch (Exception ex) { return; }
        int year;
        try { year = Integer.parseInt((String) attYearCombo.getSelectedItem()); }
        catch (Exception ex) { return; }

        String monthName = MONTH_NAMES[month - 1];
        int conf = JOptionPane.showConfirmDialog(this,
                "Approve all attendance records for " + monthName + " " + year + "?\n"
                + "Approved records will be moved to Approved Attendance Logs.",
                "Confirm Approval", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (conf != JOptionPane.YES_OPTION) return;

        int count = approvalService.approveMonth(month, year);
        if (count > 0) {
            JOptionPane.showMessageDialog(this, count + " record(s) approved for " + monthName + " " + year + ".",
                    "Approval Complete", JOptionPane.INFORMATION_MESSAGE);
            refreshAttendance();
        } else {
            JOptionPane.showMessageDialog(this, "No pending records found for " + monthName + " " + year + ".",
                    "No Records", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ── Edit Attendance Dialog ───────────────────────────────────

    private void openEditAttendanceDialog(int row) {
        String empId   = (String) attModel.getValueAt(row, 0);
        String dateStr = (String) attModel.getValueAt(row, 2);
        String inStr   = (String) attModel.getValueAt(row, 3);
        String outStr  = (String) attModel.getValueAt(row, 4);

        LocalDate date;
        try { date = LocalDate.parse(dateStr, DATE_FMT); }
        catch (Exception e) { JOptionPane.showMessageDialog(this, "Cannot parse date: " + dateStr); return; }

        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, "Edit Attendance", true);
        dlg.setSize(380, 340);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(UITheme.PANEL_BG);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.PANEL_BG);
        form.setBorder(new EmptyBorder(20, 24, 12, 24));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(8, 4, 8, 4);

        // Dialog title
        JLabel titleLbl = new JLabel("Edit Attendance");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLbl.setForeground(UITheme.PRIMARY);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; g.insets = new Insets(0, 4, 16, 4);
        form.add(titleLbl, g);
        g.gridwidth = 1; g.insets = new Insets(8, 4, 8, 4);

        // Date picker
        final LocalDate[] selectedDate = {date};
        DateTimeFormatter displayFmt = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
        JButton datePicker = new JButton("\uD83D\uDCC5  " + date.format(displayFmt));
        datePicker.setFont(UITheme.FONT_BODY);
        datePicker.setBackground(Color.WHITE);
        datePicker.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        datePicker.addActionListener(ev -> {
            LocalDate chosen = DatePickerDialog.show(dlg, "Select Date", selectedDate[0]);
            if (chosen != null) {
                selectedDate[0] = chosen;
                datePicker.setText("\uD83D\uDCC5  " + chosen.format(displayFmt));
            }
        });

        JTextField inField  = new JTextField(inStr,  10);
        JTextField outField = new JTextField(outStr, 10);
        inField.setFont(UITheme.FONT_BODY);
        outField.setFont(UITheme.FONT_BODY);
        JLabel msgLbl = new JLabel(" ");
        msgLbl.setFont(UITheme.FONT_SMALL);

        g.gridx = 0; g.gridy = 1; g.weightx = 0.35; form.add(new JLabel("Date:"),   g);
        g.gridx = 1;              g.weightx = 0.65; form.add(datePicker,             g);
        g.gridx = 0; g.gridy = 2; g.weightx = 0.35; form.add(new JLabel("Log In"),  g);
        g.gridx = 1;              g.weightx = 0.65; form.add(inField,                g);
        g.gridx = 0; g.gridy = 3; g.weightx = 0.35; form.add(new JLabel("Log Out"), g);
        g.gridx = 1;              g.weightx = 0.65; form.add(outField,               g);
        g.gridx = 0; g.gridy = 4; g.gridwidth = 2;  form.add(msgLbl,                g);
        dlg.add(form, BorderLayout.CENTER);

        // Buttons
        JButton deleteBtn = UITheme.neutralButton("Delete Log");
        JButton saveBtn   = UITheme.primaryButton("Save");
        JButton cancelBtn = UITheme.dangerButton("Cancel");

        JPanel btnRow = new JPanel(new BorderLayout(8, 0));
        btnRow.setBackground(UITheme.PANEL_BG);
        btnRow.setBorder(new EmptyBorder(0, 24, 16, 24));
        JPanel leftBtns  = new JPanel(new FlowLayout(FlowLayout.LEFT,  4, 0));
        leftBtns.setBackground(UITheme.PANEL_BG);
        leftBtns.add(deleteBtn);
        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        rightBtns.setBackground(UITheme.PANEL_BG);
        rightBtns.add(saveBtn);
        rightBtns.add(cancelBtn);
        btnRow.add(leftBtns,  BorderLayout.WEST);
        btnRow.add(rightBtns, BorderLayout.EAST);
        dlg.add(btnRow, BorderLayout.SOUTH);

        deleteBtn.addActionListener(ev -> {
            if (showDeleteConfirmDialog(dlg)) {
                boolean ok = attendanceService.deleteAttendanceRecord(empId, selectedDate[0]);
                if (ok) { dlg.dispose(); refreshAttendance(); }
                else { msgLbl.setText("Delete failed."); msgLbl.setForeground(UITheme.DANGER); }
            }
        });
        cancelBtn.addActionListener(ev -> dlg.dispose());
        saveBtn.addActionListener(ev -> {
            LocalTime clockIn, clockOut;
            try { clockIn  = LocalTime.parse(inField.getText().trim(),  TIME_FMT); }
            catch (Exception ex) { msgLbl.setText("Invalid Log In time (H:mm)."); msgLbl.setForeground(UITheme.DANGER); return; }
            try { clockOut = LocalTime.parse(outField.getText().trim(), TIME_FMT); }
            catch (Exception ex) { msgLbl.setText("Invalid Log Out time (H:mm)."); msgLbl.setForeground(UITheme.DANGER); return; }
            if (!clockOut.isAfter(clockIn)) { msgLbl.setText("Log Out must be after Log In."); msgLbl.setForeground(UITheme.DANGER); return; }

            AttendanceRecord updated = new AttendanceRecord();
            updated.setEmployeeId(empId);
            updated.setDate(selectedDate[0]);
            updated.setClockIn(clockIn);
            updated.setClockOut(clockOut);
            updated.setStatus("Pending");
            boolean ok = attendanceService.updateAttendanceRecord(empId, date, updated);
            if (ok) { dlg.dispose(); refreshAttendance(); }
            else    { msgLbl.setText("Update failed."); msgLbl.setForeground(UITheme.DANGER); }
        });

        dlg.setVisible(true);
    }

    private boolean showDeleteConfirmDialog(Component parent) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(parent);
        JDialog dlg = new JDialog(owner, "Confirm Delete", true);
        dlg.setSize(340, 180);
        dlg.setLocationRelativeTo(parent);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(UITheme.PANEL_BG);

        JLabel msg = new JLabel("<html><div style='text-align:center'>Are you sure you want to<br>delete this record?</div></html>", SwingConstants.CENTER);
        msg.setFont(new Font("SansSerif", Font.BOLD, 15));
        msg.setForeground(UITheme.PRIMARY);
        msg.setBorder(new EmptyBorder(24, 16, 16, 16));
        dlg.add(msg, BorderLayout.CENTER);

        final boolean[] result = {false};
        JButton yes = UITheme.primaryButton("Yes");
        JButton no  = UITheme.primaryButton("No");
        yes.setPreferredSize(new Dimension(100, 38));
        no .setPreferredSize(new Dimension(100, 38));
        yes.addActionListener(e -> { result[0] = true;  dlg.dispose(); });
        no .addActionListener(e -> { result[0] = false; dlg.dispose(); });

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
        btns.setBackground(UITheme.PANEL_BG);
        btns.add(yes); btns.add(no);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setVisible(true);
        return result[0];
    }

    // ================================================================
    //  PAYROLL MANAGEMENT
    // ================================================================

    private void buildPayrollManagement() {
        // Page title
        JPanel north = new JPanel(new BorderLayout());
        north.setBackground(UITheme.BACKGROUND);
        north.add(UITheme.sectionHeader("Payroll Management"), BorderLayout.NORTH);
        add(north, BorderLayout.NORTH);

        // Load approved attendance logs – show latest approved month
        List<AttendanceRecord> allApproved = new ApprovedAttendanceDAOImpl().getAllApproved();
        YearMonth latestYM = findLatestYearMonth(allApproved);
        List<AttendanceRecord> display = new ArrayList<>();
        if (latestYM != null) {
            for (AttendanceRecord r : allApproved) {
                if (r.getDate() == null) continue;
                if (r.getDate().getYear() == latestYM.getYear()
                        && r.getDate().getMonthValue() == latestYM.getMonthValue()) {
                    display.add(r);
                }
            }
        }

        // White card
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(UITheme.PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.PANEL_BORDER),
                new EmptyBorder(16, 16, 16, 16)));

        // Card header: title + month/year labels + Process Payroll button
        JPanel cardHeader = new JPanel(new BorderLayout(8, 0));
        cardHeader.setBackground(UITheme.PANEL_BG);

        JPanel titleAndDate = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleAndDate.setBackground(UITheme.PANEL_BG);
        JLabel subTitle = new JLabel("Approved Attendance Logs");
        subTitle.setFont(UITheme.FONT_HEADER);
        subTitle.setForeground(UITheme.TEXT_DARK);
        titleAndDate.add(subTitle);

        if (latestYM != null) {
            String mName = MONTH_NAMES[latestYM.getMonthValue() - 1];
            JLabel ml = new JLabel("Month: "); ml.setFont(UITheme.FONT_BODY);
            JLabel mv = new JLabel(mName);     mv.setFont(UITheme.FONT_BODY); mv.setForeground(UITheme.SUCCESS);
            JLabel yl = new JLabel("  Year: "); yl.setFont(UITheme.FONT_BODY);
            JLabel yv = new JLabel(String.valueOf(latestYM.getYear())); yv.setFont(UITheme.FONT_BODY); yv.setForeground(UITheme.SUCCESS);
            titleAndDate.add(Box.createHorizontalStrut(12));
            titleAndDate.add(ml); titleAndDate.add(mv);
            titleAndDate.add(yl); titleAndDate.add(yv);
        }

        JButton processBtn = UITheme.successButton("Process Payroll");
        cardHeader.add(titleAndDate, BorderLayout.WEST);
        cardHeader.add(processBtn,   BorderLayout.EAST);
        card.add(cardHeader, BorderLayout.NORTH);

        if (display.isEmpty()) {
            JLabel emptyLbl = new JLabel("Currently, no approved attendance logs to process the payroll.");
            emptyLbl.setFont(UITheme.FONT_BODY);
            emptyLbl.setForeground(UITheme.DANGER);
            emptyLbl.setHorizontalAlignment(SwingConstants.CENTER);
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setBackground(UITheme.PANEL_BG);
            emptyPanel.add(emptyLbl);
            card.add(emptyPanel, BorderLayout.CENTER);
            processBtn.setEnabled(false);
            processBtn.setBackground(new Color(0xAAAAAA));
        } else {
            payModel = new DefaultTableModel(
                    new String[]{"Employee ID","Employee Name","Date","Log In","Log Out","Status"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            payTable = new JTable(payModel);
            UITheme.styleTable(payTable);
            for (AttendanceRecord r : display) {
                String empId   = r.getEmployeeId() != null ? r.getEmployeeId().trim() : "";
                String ln      = r.getLastName()   != null ? r.getLastName().trim()   : "";
                String fn      = r.getFirstName()  != null ? r.getFirstName().trim()  : "";
                String empName = ln.isEmpty() && fn.isEmpty() ? empId : ln + ", " + fn;
                String dateStr = r.getDate().format(DATE_FMT);
                String inStr   = r.getClockIn()  != null ? r.getClockIn().format(TIME_FMT)  : "";
                String outStr  = r.getClockOut() != null ? r.getClockOut().format(TIME_FMT) : "";
                String status  = r.getStatus()   != null ? r.getStatus() : "Approved";
                payModel.addRow(new Object[]{ empId, empName, dateStr, inStr, outStr, status });
            }
            card.add(UITheme.scrollPane(payTable), BorderLayout.CENTER);

            JLabel note = new JLabel("NOTE: In this table, it shows all the attendance logs from different employees for the approved month attendance");
            note.setFont(UITheme.FONT_SMALL);
            note.setForeground(UITheme.DANGER);
            note.setBorder(new EmptyBorder(6, 2, 0, 0));
            card.add(note, BorderLayout.SOUTH);
        }

        // Process Payroll confirmation
        final YearMonth ymFinal = latestYM;
        processBtn.addActionListener(e -> {
            if (ymFinal == null) return;
            if (!showProcessPayrollConfirmDialog()) return;
            int processed = payrollService.processPayrollForMonth(ymFinal.getMonthValue(), ymFinal.getYear());
            JOptionPane.showMessageDialog(this,
                    processed > 0
                        ? processed + " payroll record(s) generated for " + MONTH_NAMES[ymFinal.getMonthValue()-1] + " " + ymFinal.getYear() + "."
                        : "Payroll already processed or no approved records found for this period.",
                    "Payroll Processing", JOptionPane.INFORMATION_MESSAGE);
        });

        JPanel cardWrap = new JPanel(new BorderLayout());
        cardWrap.setBackground(UITheme.BACKGROUND);
        cardWrap.setBorder(new EmptyBorder(0, 16, 16, 16));
        cardWrap.add(card, BorderLayout.CENTER);
        add(cardWrap, BorderLayout.CENTER);
    }

    private boolean showProcessPayrollConfirmDialog() {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, "Confirm", true);
        dlg.setSize(340, 180);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(UITheme.PANEL_BG);

        JLabel msg = new JLabel("<html><div style='text-align:center'>Are you sure you want to<br>process this payroll?</div></html>", SwingConstants.CENTER);
        msg.setFont(new Font("SansSerif", Font.BOLD, 15));
        msg.setForeground(UITheme.PRIMARY);
        msg.setBorder(new EmptyBorder(24, 16, 16, 16));
        dlg.add(msg, BorderLayout.CENTER);

        final boolean[] result = {false};
        JButton yes = UITheme.primaryButton("Yes");
        JButton no  = UITheme.dangerButton("No");
        yes.setPreferredSize(new Dimension(100, 38));
        no .setPreferredSize(new Dimension(100, 38));
        yes.addActionListener(e -> { result[0] = true;  dlg.dispose(); });
        no .addActionListener(e -> { result[0] = false; dlg.dispose(); });

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
        btns.setBackground(UITheme.PANEL_BG);
        btns.add(yes); btns.add(no);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setVisible(true);
        return result[0];
    }

    // ================================================================
    //  Payslip Detail Dialog  (also called from EmployeePayslipsPanel)
    // ================================================================

    static void showPayslipDialog(PayrollRecord rec, Employee emp) {
        JDialog dlg = new JDialog();
        dlg.setTitle("Employee Payslip");
        dlg.setSize(500, 580);
        dlg.setLocationRelativeTo(null);
        dlg.setModal(true);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(UITheme.PANEL_BG);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(24, 32, 24, 32));

        // Title
        JLabel titleLbl = new JLabel("Employee Payslip");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLbl.setForeground(UITheme.SUCCESS);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(titleLbl);
        content.add(Box.createVerticalStrut(16));

        String monthName = (rec.getMonth() >= 1 && rec.getMonth() <= 12)
                ? Month.of(rec.getMonth()).getDisplayName(TextStyle.FULL, Locale.ENGLISH) : "";

        addPayslipInfoRow(content, "Payslip ID:", UITheme.PRIMARY, rec.getPayslipId() != null ? rec.getPayslipId() : "");
        addPayslipInfoPair(content, "Month:", monthName, "Year:", String.valueOf(rec.getYear()));
        content.add(Box.createVerticalStrut(4));
        if (emp != null) {
            addPayslipInfoRow(content, "Employe ID:", UITheme.PRIMARY, emp.getEmployeeId());
            addPayslipInfoPair(content, "Name:", emp.getFullName(), "Position:", emp.getPosition());
        }
        content.add(Box.createVerticalStrut(16));

        // Deductions section
        content.add(sectionBanner("Deductions"));
        content.add(Box.createVerticalStrut(8));
        addDetailRow(content, "SSS:",            String.format("₱%,.2f", rec.getSss()));
        addDetailRow(content, "PhilHealth:",      String.format("₱%,.2f", rec.getPhilHealth()));
        addDetailRow(content, "PAG-IBIG:",        String.format("₱%,.2f", rec.getPagIbig()));
        addDetailRow(content, "Withholding Tax:", String.format("₱%,.2f", rec.getWithholdingTax()));
        addDetailRow(content, "Total Deductions:",String.format("₱%,.2f", rec.getTotalDeductions()));
        content.add(Box.createVerticalStrut(16));

        // Summary section
        content.add(sectionBanner("Summary"));
        content.add(Box.createVerticalStrut(8));
        addDetailRow(content, "Hours Worked:",   String.format("%.2f hrs", rec.getHoursWorked()));
        addDetailRow(content, "Overtime Hours:", String.format("%.2f hrs", rec.getOvertimeHours()));
        addDetailRow(content, "Gross Pay:",      String.format("₱%,.2f",  rec.getGrossPay()));
        addDetailRow(content, "Net Pay:",        String.format("₱%,.2f",  rec.getNetPay()));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        dlg.add(scroll, BorderLayout.CENTER);

        // Buttons
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        south.setBackground(UITheme.PANEL_BG);
        JButton downloadBtn = UITheme.primaryButton("Download PDF");
        JButton closeBtn    = UITheme.dangerButton("Close");
        downloadBtn.addActionListener(e ->
            JOptionPane.showMessageDialog(dlg,
                "PDF export is not yet available in this version.",
                "Feature Unavailable", JOptionPane.INFORMATION_MESSAGE));
        closeBtn.addActionListener(e -> dlg.dispose());
        south.add(downloadBtn);
        south.add(closeBtn);
        dlg.add(south, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private static JPanel sectionBanner(String text) {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(new Color(0x2F3142));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel("  " + text);
        lbl.setFont(UITheme.FONT_HEADER);
        lbl.setForeground(Color.WHITE);
        banner.add(lbl, BorderLayout.WEST);
        return banner;
    }

    private static void addPayslipInfoRow(JPanel panel, String label, Color valueColor, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        row.setBackground(Color.WHITE);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = new JLabel(label); l.setFont(UITheme.FONT_BODY);
        JLabel v = new JLabel(value); v.setFont(UITheme.FONT_BODY); v.setForeground(valueColor);
        row.add(l); row.add(v);
        panel.add(row);
    }

    private static void addPayslipInfoPair(JPanel panel,
            String lbl1, String val1, String lbl2, String val2) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setBackground(Color.WHITE);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l1 = new JLabel(lbl1); l1.setFont(UITheme.FONT_BODY);
        JLabel v1 = new JLabel(val1); v1.setFont(UITheme.FONT_BODY); v1.setForeground(UITheme.PRIMARY);
        JLabel l2 = new JLabel("    " + lbl2); l2.setFont(UITheme.FONT_BODY);
        JLabel v2 = new JLabel(val2); v2.setFont(UITheme.FONT_BODY); v2.setForeground(UITheme.PRIMARY);
        row.add(l1); row.add(v1); row.add(l2); row.add(v2);
        panel.add(row);
    }

    static void addDetailRow(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = new JLabel(label); l.setFont(UITheme.FONT_BODY);
        JLabel v = new JLabel(value); v.setFont(UITheme.FONT_BODY);
        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        panel.add(row);
        panel.add(Box.createVerticalStrut(2));
    }

    // ================================================================
    //  Helpers
    // ================================================================

    private static int monthNameToNumber(String name) {
        if (name == null) return 1;
        for (int i = 0; i < MONTH_NAMES.length; i++) {
            if (MONTH_NAMES[i].equalsIgnoreCase(name)) return i + 1;
        }
        return 1;
    }

    private static YearMonth findLatestYearMonth(List<AttendanceRecord> records) {
        YearMonth latest = null;
        for (AttendanceRecord r : records) {
            if (r.getDate() == null) continue;
            YearMonth ym = YearMonth.of(r.getDate().getYear(), r.getDate().getMonthValue());
            if (latest == null || ym.isAfter(latest)) latest = ym;
        }
        return latest;
    }
}

