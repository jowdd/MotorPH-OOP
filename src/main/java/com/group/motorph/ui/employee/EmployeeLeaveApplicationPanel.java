package com.group.motorph.ui.employee;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.group.motorph.model.LeaveRequest;
import com.group.motorph.model.User;
import com.group.motorph.service.LeaveService;
import com.group.motorph.ui.components.DatePickerDialog;
import com.group.motorph.ui.components.DialogUtil;
import com.group.motorph.ui.components.RoundedPanel;
import com.group.motorph.ui.components.UITheme;

/**
 * Leave Application panel Columns: Type of Request | Leave Start Date | Leave | End Date | Remarks
 */
public class EmployeeLeaveApplicationPanel extends JPanel {

    // Dates shown as MM/dd/yyyy in table
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH);
    private static final Color CYAN_DATE = new Color(0x0099CC);
    private static final Color ORANGE = new Color(0xFF8C00);

    private final User currentUser;
    private final LeaveService leaveService = new LeaveService();

    private List<LeaveRequest> allLeaves;
    private List<LeaveRequest> displayedLeaves = new java.util.ArrayList<>();
    private DefaultTableModel tableModel;
    private JTable table;
    private JButton deleteBtn;
    private JComboBox<String> typeFilterCombo;
    private JComboBox<String> remarksFilterCombo;

    public EmployeeLeaveApplicationPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        build();
    }

    private void build() {
        String empId = currentUser.getEmployeeId() != null ? currentUser.getEmployeeId().trim() : "";
        allLeaves = leaveService.getLeaveRequestsByEmployee(empId);

        // “Request for Leave” button + filter dropdowns aligned on right
        JButton requestBtn = UITheme.primaryButton("Request for Leave");
        requestBtn.addActionListener(e -> openRequestDialog());

        deleteBtn = UITheme.dangerButton("Delete Request");
        deleteBtn.setEnabled(false);
        deleteBtn.addActionListener(e -> deleteSelectedRequest());

        JPanel leftTopBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftTopBar.setBackground(UITheme.PANEL_BG);
        leftTopBar.add(requestBtn);
        leftTopBar.add(deleteBtn);

        // Collect unique leave types for the Type filter
        java.util.LinkedHashSet<String> types = new java.util.LinkedHashSet<>();
        for (LeaveRequest lr : allLeaves) {
            if (lr.getLeaveType() != null && !lr.getLeaveType().isEmpty()) {
                types.add(lr.getLeaveType());
            }
        }
        typeFilterCombo = new JComboBox<>();
        typeFilterCombo.setFont(UITheme.FONT_BODY);
        typeFilterCombo.addItem("All");
        for (String t : types) {
            typeFilterCombo.addItem(t);
        }

        remarksFilterCombo = new JComboBox<>(new String[]{"All", "Pending", "Approved", "Declined"});
        remarksFilterCombo.setFont(UITheme.FONT_BODY);

        JLabel typeLbl = new JLabel("Type:");
        typeLbl.setFont(UITheme.FONT_BODY);
        JLabel remLbl = new JLabel("Remarks:");
        remLbl.setFont(UITheme.FONT_BODY);

        JPanel rightTopBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightTopBar.setBackground(UITheme.PANEL_BG);
        rightTopBar.add(typeLbl);
        rightTopBar.add(typeFilterCombo);
        rightTopBar.add(remLbl);
        rightTopBar.add(remarksFilterCombo);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.PANEL_BG);
        topBar.add(leftTopBar, BorderLayout.WEST);
        topBar.add(rightTopBar, BorderLayout.EAST);

        typeFilterCombo.addActionListener(e -> refreshTable());
        remarksFilterCombo.addActionListener(e -> refreshTable());

        // Table  (columns: Type | Start | End | Remarks)
        tableModel = new DefaultTableModel(
                new String[]{"Type of Request", "Leave Start Date", "Leave End Date", "Remarks"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setRowHeight(36);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0 && row < displayedLeaves.size()) {
                    LeaveRequest lr = displayedLeaves.get(row);
                    deleteBtn.setEnabled("Pending".equalsIgnoreCase(lr.getStatus()));
                } else {
                    deleteBtn.setEnabled(false);
                }
            }
        });
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);

        // Date columns
        DefaultTableCellRenderer teal = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(UITheme.rowBackground(t, r, sel));
                setForeground(CYAN_DATE);
                setBorder(new javax.swing.border.EmptyBorder(2, 12, 2, 12));
                return this;
            }
        };
        table.getColumnModel().getColumn(1).setCellRenderer(teal);
        table.getColumnModel().getColumn(2).setCellRenderer(teal);

        // Remarks column
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, value, sel, foc, row, col);
                setBackground(UITheme.rowBackground(tbl, row, sel));
                String v = value != null ? value.toString() : "";
                if ("Approved".equalsIgnoreCase(v)) {
                    setForeground(UITheme.SUCCESS);
                } else if ("Declined".equalsIgnoreCase(v)) {
                    setForeground(UITheme.DANGER);
                } else {
                    setForeground(ORANGE);
                }
                setFont(UITheme.FONT_BODY.deriveFont(Font.BOLD));
                setBorder(new javax.swing.border.EmptyBorder(2, 12, 2, 12));
                return this;
            }
        });

        // Card assembly
        RoundedPanel card = new RoundedPanel(new BorderLayout(0, 12), 12, UITheme.PANEL_BORDER);
        card.setBackground(UITheme.PANEL_BG);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.add(topBar, BorderLayout.NORTH);
        card.add(UITheme.scrollPane(table), BorderLayout.CENTER);

        JPanel cardWrap = new JPanel(new BorderLayout());
        cardWrap.setBackground(UITheme.BACKGROUND);
        cardWrap.setBorder(new EmptyBorder(0, 16, 16, 16));
        cardWrap.add(card, BorderLayout.CENTER);

        add(UITheme.sectionHeader("Leave Application"), BorderLayout.NORTH);
        add(cardWrap, BorderLayout.CENTER);

        refreshTable();
    }

    private void refreshTable() {
        if (tableModel == null) {
            return;
        }
        tableModel.setRowCount(0);
        displayedLeaves = new java.util.ArrayList<>();

        String selType = typeFilterCombo != null ? (String) typeFilterCombo.getSelectedItem() : "All";
        String selRemarks = remarksFilterCombo != null ? (String) remarksFilterCombo.getSelectedItem() : "All";
        boolean allTypes = selType == null || "All".equals(selType);
        boolean allRemarks = selRemarks == null || "All".equals(selRemarks);

        // Sort most-recent request first (by requestDate desc, then startDate desc)
        java.util.List<LeaveRequest> sorted = new java.util.ArrayList<>(allLeaves);
        sorted.sort(java.util.Comparator.comparing(
                (LeaveRequest r) -> r.getRequestDate() != null ? r.getRequestDate() : java.time.LocalDate.MIN,
                java.util.Comparator.reverseOrder()));

        for (LeaveRequest lr : sorted) {
            String type = lr.getLeaveType() != null ? lr.getLeaveType() : "";
            String status = lr.getStatus() != null ? lr.getStatus() : "Pending";
            if (!allTypes && !type.equalsIgnoreCase(selType)) {
                continue;
            }
            if (!allRemarks && !status.equalsIgnoreCase(selRemarks)) {
                continue;
            }
            String startStr = lr.getStartDate() != null ? lr.getStartDate().format(DATE_FMT) : "";
            String endStr = lr.getEndDate() != null ? lr.getEndDate().format(DATE_FMT) : "";
            tableModel.addRow(new Object[]{type, startStr, endStr, status});
            displayedLeaves.add(lr);
        }
        if (deleteBtn != null) {
            deleteBtn.setEnabled(false);
        }
    }

    /**
     * Opens the leave-request dialog and coordinates all front-end business
     * rules before data reaches {@code LeaveService}.
     *
     * In addition to basic date validation, the dialog blocks requests that
     * overlap any existing pending/approved leave for the same employee. That
     * keeps duplicate or conflicting leave periods from being submitted from
     * the UI even before HR reviews them.
     */
    private void openRequestDialog() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(parent, "Request for Leave",
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(400, 330);
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(parent);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(UITheme.PANEL_BG);

        // Dialog title
        JLabel titleLbl = new JLabel("Request for Leave");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLbl.setForeground(UITheme.PRIMARY);
        titleLbl.setBorder(new EmptyBorder(20, 24, 8, 24));
        dlg.add(titleLbl, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.PANEL_BG);
        form.setBorder(new EmptyBorder(4, 24, 8, 24));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(8, 4, 8, 4);
        g.weightx = 1.0;

        final LocalDate[] startDate = {LocalDate.now()};
        final LocalDate[] endDate = {LocalDate.now()};

        JButton startBtn = calBtn(startDate[0]);
        JButton endBtn = calBtn(endDate[0]);

        startBtn.addActionListener(e -> {
            LocalDate d = DatePickerDialog.show(dlg, "Select Leave Start Date", startDate[0]);
            if (d != null) {
                startDate[0] = d;
                startBtn.setText("\uD83D\uDCC5  " + formatDate(d));
            }
        });
        endBtn.addActionListener(e -> {
            LocalDate d = DatePickerDialog.show(dlg, "Select Leave End Date", endDate[0]);
            if (d != null) {
                endDate[0] = d;
                endBtn.setText("\uD83D\uDCC5  " + formatDate(d));
            }
        });

        String[] types = {"Vacation Leave", "Sick Leave", "Emergency Leave",
            "Maternity Leave", "Paternity Leave", "Others"};
        JComboBox<String> typeCombo = new JComboBox<>(types);
        typeCombo.setFont(UITheme.FONT_BODY);

        JLabel msgLabel = new JLabel(" ");
        msgLabel.setFont(UITheme.FONT_SMALL);

        // Field order: Leave Start Date, Leave End Date, Type of Leave
        addFormRow(form, g, 0, "Leave Start Date:", startBtn);
        addFormRow(form, g, 1, "Leave End Date:", endBtn);
        addFormRow(form, g, 2, "Type of Leave:", typeCombo);
        g.gridx = 0;
        g.gridy = 3;
        g.gridwidth = 2;
        form.add(msgLabel, g);

        dlg.add(form, BorderLayout.CENTER);

        // Cancel and Submit buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        btnRow.setBackground(UITheme.PANEL_BG);
        JButton submitBtn = UITheme.primaryButton("Submit");
        JButton cancelBtn = UITheme.dangerButton("Cancel");
        btnRow.add(cancelBtn);
        btnRow.add(submitBtn);
        dlg.add(btnRow, BorderLayout.SOUTH);

        cancelBtn.addActionListener(e -> dlg.dispose());
        submitBtn.addActionListener(e -> {
            LocalDate s = startDate[0];
            LocalDate en = endDate[0];
            if (s.isAfter(en)) {
                msgLabel.setText("Start date must be on or before end date.");
                msgLabel.setForeground(UITheme.DANGER);
                return;
            }
            if (s.isBefore(LocalDate.now())) {
                msgLabel.setText("Leave start date cannot be in the past.");
                msgLabel.setForeground(UITheme.DANGER);
                return;
            }
            
            // Declined requests are ignored here because they no longer reserve
            // any leave dates; Pending and Approved requests still do.
            for (LeaveRequest lr : allLeaves) {
                if ("Declined".equalsIgnoreCase(lr.getStatus())) {
                    continue;
                }
                LocalDate lrStart = lr.getStartDate();
                LocalDate lrEnd = lr.getEndDate();
                if (lrStart == null || lrEnd == null) {
                    continue;
                }
                if (!s.isAfter(lrEnd) && !en.isBefore(lrStart)) {
                    msgLabel.setText("Dates overlap with an existing "
                            + lr.getStatus().toLowerCase() + " leave.");
                    msgLabel.setForeground(UITheme.DANGER);
                    return;
                }
            }

            // Confirmation dialog before submitting
            if (!DialogUtil.showConfirmDialog(dlg,
                    "Submit leave request from " + formatDate(s)
                    + "<br>to " + formatDate(en) + "?")) {
                return;
            }
            String type = (String) typeCombo.getSelectedItem();
            boolean ok = leaveService.submitLeaveRequest(
                    currentUser.getEmployeeId().trim(), s, en, type, "");
            if (ok) {
                allLeaves = leaveService.getLeaveRequestsByEmployee(
                        currentUser.getEmployeeId().trim());
                dlg.dispose();
                refreshTable();
            } else {
                msgLabel.setText("Failed to submit leave request. Please try again.");
                msgLabel.setForeground(UITheme.DANGER);
            }
        });

        dlg.setVisible(true);
    }

    //  Delete selected Pending leave request
    private void deleteSelectedRequest() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= displayedLeaves.size()) {
            return;
        }
        LeaveRequest lr = displayedLeaves.get(row);
        if (!"Pending".equalsIgnoreCase(lr.getStatus())) {
            return;
        }
        if (!DialogUtil.showConfirmDialog(
                SwingUtilities.getWindowAncestor(this),
                "Delete this leave request?<br>This action cannot be undone.")) {
            return;
        }
        boolean ok = leaveService.deleteLeaveRequest(lr.getRequestId());
        if (ok) {
            allLeaves = leaveService.getLeaveRequestsByEmployee(
                    currentUser.getEmployeeId().trim());
            refreshTable();
        }
    }

    // Helpers
    private static JButton calBtn(LocalDate initial) {
        JButton btn = new JButton("\uD83D\uDCC5 " + formatDate(initial));
        btn.setFont(UITheme.FONT_BODY);
        btn.setBackground(Color.WHITE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static String formatDate(LocalDate d) {
        return d.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH));
    }

    private static void addFormRow(JPanel form, GridBagConstraints g,
            int row, String label, JComponent field) {
        g.gridwidth = 1;
        g.gridx = 0;
        g.gridy = row;
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_BODY);
        l.setPreferredSize(new Dimension(140, 24));
        form.add(l, g);
        g.gridx = 1;
        form.add(field, g);
    }
}
