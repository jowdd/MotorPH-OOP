package com.group.motorph.ui.hr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.group.motorph.model.Employee;
import com.group.motorph.model.LeaveRequest;
import com.group.motorph.model.User;
import com.group.motorph.service.EmployeeService;
import com.group.motorph.service.LeaveService;
import com.group.motorph.ui.components.DialogUtil;
import com.group.motorph.ui.components.RoundedPanel;
import com.group.motorph.ui.components.RoundedSearchField;
import com.group.motorph.ui.components.UITheme;

/**
 * Leave Management panel — lists all leave requests with filters and Approve /
 * Decline actions.
 */
public class HRLeavePanel extends JPanel {

    private static final DateTimeFormatter DT_MM = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final Color CYAN_DATE = new Color(0x0099CC);
    private static final int LV_STATUS_COL = 5;
    private static final int LV_REQID_COL = 6;

    private static final String[] LV_MONTH_NAMES = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    private final EmployeeService employeeService = new EmployeeService();
    private final LeaveService leaveService = new LeaveService();

    private DefaultTableModel leaveModel;
    private JTable leaveTable;
    private List<LeaveRequest> leaveList = new ArrayList<>();
    private List<LeaveRequest> lvAllLeaves = new ArrayList<>();
    private JButton approveBtn;
    private JButton declineBtn;
    private JComboBox<String> lvMonthCombo;
    private JComboBox<String> lvYearCombo;
    private JComboBox<String> lvStatusCombo;
    private RoundedSearchField leaveSearchField;

    public HRLeavePanel(User user) {
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        build();
    }

    private void build() {
        lvMonthCombo = new JComboBox<>(new String[]{"All"});
        lvMonthCombo.setFont(UITheme.FONT_BODY);
        lvYearCombo = new JComboBox<>(new String[]{"All"});
        lvYearCombo.setFont(UITheme.FONT_BODY);
        lvStatusCombo = new JComboBox<>(new String[]{"All", "Pending", "Approved", "Declined"});
        lvStatusCombo.setFont(UITheme.FONT_BODY);
        lvStatusCombo.setSelectedItem("Pending");

        JLabel filterLbl = new JLabel("Filter by Date Submitted:");
        filterLbl.setFont(UITheme.FONT_BODY);
        JLabel statusLbl = new JLabel("Status:");
        statusLbl.setFont(UITheme.FONT_BODY);

        JPanel leftBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        leftBar.setBackground(UITheme.PANEL_BG);
        leftBar.add(filterLbl);
        leftBar.add(lvMonthCombo);
        leftBar.add(lvYearCombo);
        leftBar.add(Box.createHorizontalStrut(8));
        leftBar.add(statusLbl);
        leftBar.add(lvStatusCombo);

        approveBtn = UITheme.successButton("Approve");
        declineBtn = UITheme.dangerButton("Decline");
        approveBtn.setEnabled(false);
        declineBtn.setEnabled(false);
        approveBtn.addActionListener(e -> {
            int row = leaveTable.getSelectedRow();
            if (row >= 0 && leaveList != null && row < leaveList.size()) {
                confirmLeaveAction(row, true);
            }
        });
        declineBtn.addActionListener(e -> {
            int row = leaveTable.getSelectedRow();
            if (row >= 0 && leaveList != null && row < leaveList.size()) {
                confirmLeaveAction(row, false);
            }
        });

        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBar.setBackground(UITheme.PANEL_BG);
        rightBar.add(approveBtn);
        rightBar.add(declineBtn);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.PANEL_BG);
        topBar.setBorder(new EmptyBorder(0, 0, 10, 0));
        topBar.add(leftBar, BorderLayout.WEST);
        topBar.add(rightBar, BorderLayout.EAST);

        leaveModel = new DefaultTableModel(
                new String[]{"Date Submitted", "Employee Name", "Type of Request",
                    "Leave Start Date", "Leave End Date", "Status", "req_id"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        leaveTable = new JTable(leaveModel);
        UITheme.styleTable(leaveTable);
        leaveTable.setRowHeight(38);
        leaveTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        leaveTable.getColumnModel().getColumn(LV_REQID_COL).setMaxWidth(0);
        leaveTable.getColumnModel().getColumn(LV_REQID_COL).setMinWidth(0);
        leaveTable.getColumnModel().getColumn(LV_REQID_COL).setPreferredWidth(0);
        leaveTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        leaveTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        leaveTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        leaveTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        leaveTable.getColumnModel().getColumn(4).setPreferredWidth(130);
        leaveTable.getColumnModel().getColumn(LV_STATUS_COL).setPreferredWidth(100);

        // Teal renderer for date columns
        DefaultTableCellRenderer teal = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(UITheme.rowBackground(t, r, sel));
                setForeground(CYAN_DATE);
                setBorder(BorderFactory.createEmptyBorder(2, 12, 2, 12));
                return this;
            }
        };
        leaveTable.getColumnModel().getColumn(0).setCellRenderer(teal);
        leaveTable.getColumnModel().getColumn(3).setCellRenderer(teal);
        leaveTable.getColumnModel().getColumn(4).setCellRenderer(teal);

        // Status column
        leaveTable.getColumnModel().getColumn(LV_STATUS_COL).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(UITheme.rowBackground(t, r, sel));
                String status = v != null ? v.toString() : "";
                if ("Approved".equalsIgnoreCase(status)) {
                    setForeground(UITheme.SUCCESS);
                } else if ("Declined".equalsIgnoreCase(status)) {
                    setForeground(UITheme.DANGER);
                } else {
                    setForeground(new Color(0xE07A00));
                }
                setFont(getFont().deriveFont(Font.BOLD));
                setBorder(BorderFactory.createEmptyBorder(2, 12, 2, 12));
                return this;
            }
        });

        leaveTable.getSelectionModel().addListSelectionListener(e -> {
            int row = leaveTable.getSelectedRow();
            boolean sel = row >= 0;
            boolean pending = sel && leaveList != null && row < leaveList.size()
                    && "Pending".equalsIgnoreCase(leaveList.get(row).getStatus());
            approveBtn.setEnabled(pending);
            declineBtn.setEnabled(pending);
        });

        lvMonthCombo.addActionListener(e -> filterLeaves());
        lvYearCombo.addActionListener(e -> filterLeaves());
        lvStatusCombo.addActionListener(e -> filterLeaves());

        leaveSearchField = new RoundedSearchField("Search by Employee Name");
        leaveSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterLeaves();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterLeaves();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterLeaves();
            }
        });

        JPanel searchRow = new JPanel(new BorderLayout());
        searchRow.setBackground(UITheme.PANEL_BG);
        searchRow.setBorder(new EmptyBorder(0, 0, 8, 0));
        searchRow.add(leaveSearchField, BorderLayout.CENTER);

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.setBackground(UITheme.PANEL_BG);
        northPanel.add(searchRow);
        northPanel.add(topBar);

        RoundedPanel card = new RoundedPanel(new BorderLayout(0, 4), 12, UITheme.PANEL_BORDER);
        card.setBackground(UITheme.PANEL_BG);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.add(northPanel, BorderLayout.NORTH);
        card.add(UITheme.scrollPane(leaveTable), BorderLayout.CENTER);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UITheme.BACKGROUND);
        wrap.setBorder(new EmptyBorder(0, 16, 16, 16));
        wrap.add(card, BorderLayout.CENTER);

        add(UITheme.sectionHeader("Leave Management"), BorderLayout.NORTH);
        add(wrap, BorderLayout.CENTER);
        refreshLeaves();
    }

    private void refreshLeaves() {
        lvAllLeaves = leaveService.getAllLeaveRequests();

        TreeSet<Integer> months = new TreeSet<>();
        TreeSet<Integer> years = new TreeSet<>();
        for (LeaveRequest r : lvAllLeaves) {
            if (r.getRequestDate() != null) {
                months.add(r.getRequestDate().getMonthValue());
                years.add(r.getRequestDate().getYear());
            }
        }
        // Preserve the current combo selections while rebuilding the available
        // month/year choices from fresh data. This avoids resetting the HR
        // user's filters every time a request is approved or declined.
        String selM = (String) lvMonthCombo.getSelectedItem();
        String selY = (String) lvYearCombo.getSelectedItem();
        String selS = (String) lvStatusCombo.getSelectedItem();

        lvMonthCombo.removeAllItems();
        lvMonthCombo.addItem("All");
        for (int m : months) {
            lvMonthCombo.addItem(LV_MONTH_NAMES[m - 1]);
        }
        lvYearCombo.removeAllItems();
        lvYearCombo.addItem("All");
        for (int y : years) {
            lvYearCombo.addItem(String.valueOf(y));
        }

        if (selM != null) {
            lvMonthCombo.setSelectedItem(selM);
        }
        if (selY != null) {
            lvYearCombo.setSelectedItem(selY);
        }
        if (selS != null) {
            lvStatusCombo.setSelectedItem(selS);
        } else {
            lvStatusCombo.setSelectedItem("Pending");
        }

        filterLeaves();
    }

    /**
     * Applies the leave-management table filters in stages.
     *
     * The method first narrows data by submitted month/year and request status,
     * then sorts the remaining rows so Pending requests surface first, and only
     * then applies the employee-name search. That ordering keeps HR's most
     * actionable requests at the top of the table.
     */
    private void filterLeaves() {
        leaveModel.setRowCount(0);
        leaveList.clear();

        String selM = (String) lvMonthCombo.getSelectedItem();
        String selY = (String) lvYearCombo.getSelectedItem();
        String selS = (String) lvStatusCombo.getSelectedItem();

        boolean allMonths = selM == null || "All".equals(selM);
        boolean allYears = selY == null || "All".equals(selY);
        boolean allStatus = selS == null || "All".equals(selS);

        int fm = -1;
        if (!allMonths) {
            for (int i = 0; i < LV_MONTH_NAMES.length; i++) {
                if (LV_MONTH_NAMES[i].equals(selM)) {
                    fm = i + 1;
                    break;
                }
            }
        }
        int fy = -1;
        if (!allYears) {
            try {
                fy = Integer.parseInt(selY);
            } catch (NumberFormatException ignored) {
            }
        }

        for (LeaveRequest req : lvAllLeaves) {

            var requestDate = req.getRequestDate();
            String status = req.getStatus();

            if (!allMonths && (requestDate == null || requestDate.getMonthValue() != fm)) {
                continue;
            }

            if (!allYears && (requestDate == null || requestDate.getYear() != fy)) {
                continue;
            }

            if (!allStatus && (status == null || !status.equalsIgnoreCase(selS))) {
                continue;
            }

            leaveList.add(req);
        }

        // Pending first, then Approved, then Declined; within each group newest first
        leaveList.sort(Comparator
                .<LeaveRequest, Integer>comparing(r -> statusOrder(r.getStatus()))
                .thenComparing(Comparator.comparing(
                        (LeaveRequest r) -> r.getRequestDate() != null
                        ? r.getRequestDate() : java.time.LocalDate.MIN,
                        Comparator.reverseOrder())));

        // Apply employee name search filter
        if (leaveSearchField != null) {
            String q = leaveSearchField.getText().trim().toLowerCase();
            if (!q.isEmpty()) {
                leaveList = leaveList.stream()
                        .filter(r -> {
                            Employee emp = employeeService.getEmployeeById(r.getEmployeeId());
                            return emp != null && emp.getFullName().toLowerCase().contains(q);
                        })
                        .collect(java.util.stream.Collectors.toList());
            }
        }

        for (LeaveRequest req : leaveList) {
            Employee emp = employeeService.getEmployeeById(req.getEmployeeId());
            String empName = emp != null ? emp.getFullName() : "\u2014";
            String start = req.getStartDate() != null ? req.getStartDate().format(DT_MM) : "";
            String end = req.getEndDate() != null ? req.getEndDate().format(DT_MM) : "";
            String submitted = req.getRequestDate() != null ? req.getRequestDate().format(DT_MM) : "";
            leaveModel.addRow(new Object[]{
                submitted, empName,
                req.getLeaveType() != null ? req.getLeaveType() : "",
                start, end,
                req.getStatus() != null ? req.getStatus() : "",
                req.getRequestId()
            });
        }
        if (approveBtn != null) {
            approveBtn.setEnabled(false);
        }
        if (declineBtn != null) {
            declineBtn.setEnabled(false);
        }
    }

    private void confirmLeaveAction(int row, boolean approve) {
        LeaveRequest req = leaveList.get(row);
        if (!"Pending".equalsIgnoreCase(req.getStatus())) {
            JOptionPane.showMessageDialog(this, "Only Pending requests can be updated.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String msg = approve
                ? "Are you sure you want to approve this leave request?"
                : "Are you sure you want to decline this leave request?";
        if (!DialogUtil.showConfirmDialog(SwingUtilities.getWindowAncestor(this), msg)) {
            return;
        }
        boolean ok = approve
                ? leaveService.approveLeaveRequest(req.getRequestId())
                : leaveService.declineLeaveRequest(req.getRequestId());
        if (ok) {
            refreshLeaves();
        } else {
            JOptionPane.showMessageDialog(this, "Operation failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Pending=0, Approved=1, Declined=2, anything else=3
    private static int statusOrder(String status) {
        if (status == null) {
            return 3;
        }
        if ("Pending".equalsIgnoreCase(status)) {
            return 0;
        }
        if ("Approved".equalsIgnoreCase(status)) {
            return 1;
        }
        if ("Declined".equalsIgnoreCase(status)) {
            return 2;
        }
        return 3;
    }
}
