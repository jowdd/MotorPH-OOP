package com.group.motorph.ui;

import com.group.motorph.model.Employee;
import com.group.motorph.model.LeaveRequest;
import com.group.motorph.model.RegularEmployee;
import com.group.motorph.model.User;
import com.group.motorph.service.EmployeeService;
import com.group.motorph.service.LeaveService;
import com.group.motorph.util.InputValidationUtil;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * HR panel – two views controlled by the sidebar:
 *   "employees" – Employee Management (add / edit / delete employees)
 *   "leaves"    – Leave Management   (approve / decline leave requests)
 */
public class HRPanel extends JPanel {

    private static final DateTimeFormatter DT_MM    = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final Color             CYAN_DATE = new Color(0x0099CC);

    // Employee table action column indices
    private static final int EMP_EDIT_COL   = 4;
    private static final int EMP_DELETE_COL = 5;

    // Leave table action column indices
    private static final int LV_APP_COL = 5;
    private static final int LV_DEC_COL = 6;

    private final User            user;
    private final String          view;
    private final EmployeeService employeeService = new EmployeeService();
    private final LeaveService    leaveService    = new LeaveService();

    // Employee-management state
    private DefaultTableModel empModel;
    private JTable            empTable;
    private List<Employee>    empList;

    // Leave-management state
    private DefaultTableModel  leaveModel;
    private JTable             leaveTable;
    private List<LeaveRequest> leaveList;

    public HRPanel(User user, String view) {
        this.user = user;
        this.view = view;
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        if ("employees".equals(view)) buildEmployeeManagement();
        else                          buildLeaveManagement();
    }

    // ================================================================
    //  PAGE: EMPLOYEE MANAGEMENT
    // ================================================================

    private void buildEmployeeManagement() {
        JButton addBtn = UITheme.primaryButton("Add Employee");
        addBtn.addActionListener(e -> openEmployeeDialog(null));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topBar.setBackground(UITheme.PANEL_BG);
        topBar.add(addBtn);

        empModel = new DefaultTableModel(
                new String[]{"Employee ID", "Employee Name", "Position", "Status", "Edit", "Delete"}, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return c == EMP_EDIT_COL || c == EMP_DELETE_COL;
            }
        };
        empTable = new JTable(empModel);
        UITheme.styleTable(empTable);
        empTable.setRowHeight(38);
        empTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        empTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        empTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        empTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        empTable.getColumnModel().getColumn(EMP_EDIT_COL)  .setPreferredWidth(80);
        empTable.getColumnModel().getColumn(EMP_DELETE_COL).setPreferredWidth(80);

        empTable.getColumnModel().getColumn(EMP_EDIT_COL)  .setCellRenderer(new BtnRenderer(UITheme.primaryButton("Edit")));
        empTable.getColumnModel().getColumn(EMP_DELETE_COL).setCellRenderer(new BtnRenderer(UITheme.dangerButton("Delete")));
        empTable.getColumnModel().getColumn(EMP_EDIT_COL)  .setCellEditor(new BtnEditor(UITheme.primaryButton("Edit")));
        empTable.getColumnModel().getColumn(EMP_DELETE_COL).setCellEditor(new BtnEditor(UITheme.dangerButton("Delete")));

        empTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = empTable.columnAtPoint(e.getPoint());
                int row = empTable.rowAtPoint(e.getPoint());
                if (row < 0 || empList == null || row >= empList.size()) return;
                if (col == EMP_EDIT_COL)   openEmployeeDialog(empList.get(row));
                if (col == EMP_DELETE_COL) confirmDeleteEmployee(row);
            }
        });

        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(UITheme.PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.PANEL_BORDER),
                new EmptyBorder(16, 16, 16, 16)));
        card.add(topBar,                       BorderLayout.NORTH);
        card.add(UITheme.scrollPane(empTable), BorderLayout.CENTER);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UITheme.BACKGROUND);
        wrap.setBorder(new EmptyBorder(0, 16, 16, 16));
        wrap.add(card, BorderLayout.CENTER);

        add(UITheme.sectionHeader("Employee Management"), BorderLayout.NORTH);
        add(wrap, BorderLayout.CENTER);
        refreshEmployees();
    }

    private void refreshEmployees() {
        empModel.setRowCount(0);
        empList = employeeService.getAllEmployees();
        for (Employee e : empList) {
            empModel.addRow(new Object[]{
                e.getEmployeeId(),
                e.getFullName(),
                e.getPosition() != null ? e.getPosition() : "",
                e.getStatus()   != null ? e.getStatus()   : "",
                "Edit", "Delete"
            });
        }
    }

    private void confirmDeleteEmployee(int row) {
        boolean yes = showConfirmDialog(SwingUtilities.getWindowAncestor(this),
                "Are you sure you want to delete this employee?");
        if (!yes) return;
        String err = employeeService.deleteEmployee(empList.get(row).getEmployeeId());
        if (err == null) refreshEmployees();
        else JOptionPane.showMessageDialog(this, "Error: " + err, "Delete Failed", JOptionPane.ERROR_MESSAGE);
    }

    // ================================================================
    //  Add / Edit Employee Dialog
    // ================================================================

    private void openEmployeeDialog(Employee existing) {
        boolean isEdit = (existing != null);
        Window parent = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(parent, isEdit ? "Edit Employee Profile" : "Add Employee",
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(640, 700);
        dlg.setLocationRelativeTo(parent);
        dlg.setLayout(new BorderLayout());

        // Dialog title
        JLabel titleLbl = new JLabel("Edit Employee Profile");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLbl.setForeground(UITheme.PRIMARY);
        titleLbl.setBorder(new EmptyBorder(20, 24, 10, 24));
        dlg.add(titleLbl, BorderLayout.NORTH);

        // Form panel (scrollable)
        JPanel form = new JPanel();
        form.setBackground(UITheme.PANEL_BG);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(0, 24, 10, 24));

        // Personal Information fields
        JTextField nameField   = field(isEdit ? (existing.getFirstName() + " " + existing.getLastName()) : "");
        JTextField birthdayFld = field(isEdit && existing.getBirthday() != null
                                        ? existing.getBirthday().format(DT_MM) : "");
        JButton bdayBtn = new JButton("...");
        bdayBtn.setFont(UITheme.FONT_BODY);
        bdayBtn.addActionListener(ev -> {
            LocalDate init = null;
            try { init = LocalDate.parse(birthdayFld.getText().trim(), DT_MM); } catch (Exception ignore) {}
            LocalDate d = DatePickerDialog.show(dlg, "Select Birthday",
                    init != null ? init : LocalDate.of(1990, 1, 1));
            if (d != null) birthdayFld.setText(d.format(DT_MM));
        });
        JTextField sexField    = field("");
        JTextField addrField   = field(isEdit ? existing.getAddress()     : "");
        JTextField provAddrFld = field("");
        JTextField phoneField  = field(isEdit ? existing.getPhoneNumber() : "");

        // Employee Information fields
        JTextField empIdFld = field(isEdit ? existing.getEmployeeId() : employeeService.getNextEmployeeId());
        if (isEdit) empIdFld.setEditable(false);
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Regular", "Probationary"});
        if (isEdit && existing.getStatus() != null) statusCombo.setSelectedItem(existing.getStatus());
        JTextField posFld  = field(isEdit ? existing.getPosition()            : "");
        JTextField deptFld = field("");
        JTextField supFld  = field(isEdit ? existing.getImmediateSupervisor() : "");

        // Government ID fields
        JTextField sssFld  = field(isEdit ? existing.getSssNum()        : "");
        JTextField philFld = field(isEdit ? existing.getPhilhealthNum() : "");
        JTextField tinFld  = field(isEdit ? existing.getTinNum()        : "");
        JTextField pagFld  = field(isEdit ? existing.getPagibigNum()    : "");

        // Compensation fields
        JTextField salFld    = field(isEdit ? fmt(existing.getBasicSalary())       : "0.00");
        JTextField riceFld   = field(isEdit ? fmt(existing.getRiceSubsidy())       : "1500.00");
        JTextField phAlwFld  = field(isEdit ? fmt(existing.getPhoneAllowance())    : "1000.00");
        JTextField clAlwFld  = field(isEdit ? fmt(existing.getClothingAllowance()) : "1000.00");
        JTextField grossFld  = field(isEdit ? fmt(existing.getGrossSemiMonthly())  : "0.00");
        JTextField hourFld   = field(isEdit ? fmt(existing.getHourlyRate())        : "0.00");

        JLabel msgLbl = new JLabel(" ");
        msgLbl.setFont(UITheme.FONT_SMALL);
        msgLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Build form sections
        form.add(sectionTitle("Personal Information"));
        form.add(Box.createVerticalStrut(6));
        form.add(formRow("Full Name:",          nameField));
        form.add(formRowWithBtn("Birthday:",    birthdayFld, bdayBtn));
        form.add(formRow("Sex:",                sexField));
        form.add(formRow("Current Address:",    addrField));
        form.add(formRow("Provincial Address:", provAddrFld));
        form.add(formRow("Phone Number:",       phoneField));
        form.add(hSeparator());

        // Two-column: Employee Info | Government IDs
        JPanel twoCol = new JPanel(new GridLayout(1, 2, 24, 0));
        twoCol.setBackground(UITheme.PANEL_BG);
        twoCol.setAlignmentX(Component.LEFT_ALIGNMENT);
        twoCol.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        JPanel left = col();
        left.add(sectionTitle("Employee Information"));
        left.add(Box.createVerticalStrut(6));
        left.add(formRow("Employee ID:", empIdFld));
        left.add(formRowCombo("Status:", statusCombo));
        left.add(formRow("Position:",    posFld));
        left.add(formRow("Department:",  deptFld));
        left.add(formRow("Immediate Supervisor:", supFld));

        JPanel right = col();
        right.add(sectionTitle("Government ID Numbers"));
        right.add(Box.createVerticalStrut(6));
        right.add(formRow("SSS:",        sssFld));
        right.add(formRow("PhilHealth:", philFld));
        right.add(formRow("TIN:",        tinFld));
        right.add(formRow("Pag-IBIG:",   pagFld));

        twoCol.add(left);
        twoCol.add(right);
        form.add(twoCol);
        form.add(hSeparator());

        form.add(sectionTitle("Compensation"));
        form.add(Box.createVerticalStrut(4));
        form.add(formRow("Basic Salary:",       salFld));
        form.add(formRow("Rice Subsidy:",       riceFld));
        form.add(formRow("Phone Allowance:",    phAlwFld));
        form.add(formRow("Clothing Allowance:", clAlwFld));
        form.add(formRow("Gross Semi-monthly:", grossFld));
        form.add(formRow("Hourly Rate:",        hourFld));
        form.add(Box.createVerticalStrut(8));
        form.add(msgLbl);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        dlg.add(scroll, BorderLayout.CENTER);

        // Action buttons
        JButton saveBtn   = UITheme.primaryButton(isEdit ? "Save Changes"  : "Add Employee");
        JButton cancelBtn = UITheme.dangerButton (isEdit ? "Close"         : "Cancel");
        cancelBtn.addActionListener(e -> dlg.dispose());

        saveBtn.addActionListener(e -> {
            String fullName = nameField.getText().trim();
            if (fullName.isEmpty()) {
                msgLbl.setText("Full Name is required."); msgLbl.setForeground(UITheme.DANGER); return;
            }
            String[] parts    = fullName.split("\\s+");
            String lastName   = parts.length >= 2 ? parts[parts.length - 1] : "";
            String firstName  = parts.length >= 2
                    ? fullName.substring(0, fullName.lastIndexOf(lastName)).trim() : fullName;

            if (!isEdit && employeeService.employeeIdExists(empIdFld.getText().trim())) {
                msgLbl.setText("Employee ID already exists."); msgLbl.setForeground(UITheme.DANGER); return;
            }

            String sss     = InputValidationUtil.formatSSS(sssFld.getText());
            String pagibig = InputValidationUtil.formatPagIbig(pagFld.getText());
            String phil    = InputValidationUtil.formatPhilHealth(philFld.getText());
            String tin     = InputValidationUtil.formatTIN(tinFld.getText());

            if (!sss.isEmpty()    && !InputValidationUtil.isValidSSS(sss))          { msgLbl.setText("Invalid SSS.");        msgLbl.setForeground(UITheme.DANGER); return; }
            if (!pagibig.isEmpty() && !InputValidationUtil.isValidPagIbig(pagibig)) { msgLbl.setText("Invalid Pag-IBIG.");   msgLbl.setForeground(UITheme.DANGER); return; }
            if (!phil.isEmpty()   && !InputValidationUtil.isValidPhilHealth(phil))  { msgLbl.setText("Invalid PhilHealth."); msgLbl.setForeground(UITheme.DANGER); return; }
            if (!tin.isEmpty()    && !InputValidationUtil.isValidTIN(tin))           { msgLbl.setText("Invalid TIN.");        msgLbl.setForeground(UITheme.DANGER); return; }

            String confirmMsg = isEdit ? "Are you sure you want to save changes?"
                                       : "Are you sure you want to add this employee?";
            if (!showConfirmDialog(dlg, confirmMsg)) return;

            Employee emp = isEdit ? existing : new RegularEmployee();
            emp.setEmployeeId(empIdFld.getText().trim());
            emp.setLastName(lastName);
            emp.setFirstName(firstName);
            try { emp.setBirthday(LocalDate.parse(birthdayFld.getText().trim(), DT_MM)); } catch (Exception ignore) {}
            emp.setAddress(addrField.getText().trim());
            emp.setPhoneNumber(phoneField.getText().trim());
            emp.setSssNum(sss);
            emp.setPagibigNum(pagibig);
            emp.setPhilhealthNum(phil);
            emp.setTinNum(tin);
            emp.setStatus((String) statusCombo.getSelectedItem());
            emp.setPosition(posFld.getText().trim());
            emp.setImmediateSupervisor(supFld.getText().trim());
            emp.setBasicSalary(parseDouble(salFld.getText()));
            emp.setRiceSubsidy(parseDouble(riceFld.getText()));
            emp.setPhoneAllowance(parseDouble(phAlwFld.getText()));
            emp.setClothingAllowance(parseDouble(clAlwFld.getText()));
            emp.setGrossSemiMonthly(parseDouble(grossFld.getText()));
            emp.setHourlyRate(parseDouble(hourFld.getText()));

            String err = isEdit ? employeeService.updateEmployee(emp) : employeeService.addEmployee(emp);
            if (err == null) { dlg.dispose(); refreshEmployees(); }
            else             { msgLbl.setText(err); msgLbl.setForeground(UITheme.DANGER); }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        btnRow.setBackground(UITheme.PANEL_BG);
        btnRow.add(saveBtn);
        btnRow.add(cancelBtn);
        dlg.add(btnRow, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ================================================================
    //  PAGE: LEAVE MANAGEMENT
    // ================================================================

    private void buildLeaveManagement() {
        // Columns: Employee Name | Type | Start | End | req_id(hidden) | Approve | Decline
        leaveModel = new DefaultTableModel(
                new String[]{"Employee Name", "Type of Request", "Leave Start Date", "Leave End Date",
                             "req_id", "Approve", "Decline"}, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return c == LV_APP_COL || c == LV_DEC_COL;
            }
        };
        leaveTable = new JTable(leaveModel);
        UITheme.styleTable(leaveTable);
        leaveTable.setRowHeight(38);

        // Hide req_id column
        leaveTable.getColumnModel().getColumn(4).setMaxWidth(0);
        leaveTable.getColumnModel().getColumn(4).setMinWidth(0);
        leaveTable.getColumnModel().getColumn(4).setPreferredWidth(0);

        leaveTable.getColumnModel().getColumn(0).setPreferredWidth(190);
        leaveTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        leaveTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        leaveTable.getColumnModel().getColumn(3).setPreferredWidth(140);
        leaveTable.getColumnModel().getColumn(LV_APP_COL).setPreferredWidth(90);
        leaveTable.getColumnModel().getColumn(LV_DEC_COL).setPreferredWidth(90);

        // Teal date renderer for Start/End columns
        DefaultTableCellRenderer teal = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setForeground(CYAN_DATE);
                return this;
            }
        };
        leaveTable.getColumnModel().getColumn(2).setCellRenderer(teal);
        leaveTable.getColumnModel().getColumn(3).setCellRenderer(teal);

        leaveTable.getColumnModel().getColumn(LV_APP_COL).setCellRenderer(new BtnRenderer(UITheme.successButton("Approve")));
        leaveTable.getColumnModel().getColumn(LV_DEC_COL).setCellRenderer(new BtnRenderer(UITheme.dangerButton("Decline")));
        leaveTable.getColumnModel().getColumn(LV_APP_COL).setCellEditor(new BtnEditor(UITheme.successButton("Approve")));
        leaveTable.getColumnModel().getColumn(LV_DEC_COL).setCellEditor(new BtnEditor(UITheme.dangerButton("Decline")));

        leaveTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = leaveTable.columnAtPoint(e.getPoint());
                int row = leaveTable.rowAtPoint(e.getPoint());
                if (row < 0 || leaveList == null || row >= leaveList.size()) return;
                if (col == LV_APP_COL) confirmLeaveAction(row, true);
                if (col == LV_DEC_COL) confirmLeaveAction(row, false);
            }
        });

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UITheme.PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.PANEL_BORDER),
                new EmptyBorder(16, 16, 16, 16)));
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
        leaveModel.setRowCount(0);
        leaveList = leaveService.getAllLeaveRequests();
        for (LeaveRequest req : leaveList) {
            String empName = "\u2014";
            Employee emp = employeeService.getEmployeeById(req.getEmployeeId());
            if (emp != null) empName = emp.getFullName();
            String start = req.getStartDate() != null ? req.getStartDate().format(DT_MM) : "";
            String end   = req.getEndDate()   != null ? req.getEndDate()  .format(DT_MM) : "";
            leaveModel.addRow(new Object[]{
                empName,
                req.getLeaveType() != null ? req.getLeaveType() : "",
                start, end,
                req.getRequestId(),
                "Approve", "Decline"
            });
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
        if (!showConfirmDialog(SwingUtilities.getWindowAncestor(this), msg)) return;
        boolean ok = approve ? leaveService.approveLeaveRequest(req.getRequestId())
                              : leaveService.declineLeaveRequest(req.getRequestId());
        if (ok) refreshLeaves();
        else JOptionPane.showMessageDialog(this, "Operation failed.", "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ================================================================
    //  Shared: custom blue-Yes / red-No confirm dialog
    // ================================================================

    static boolean showConfirmDialog(Window parent, String message) {
        JDialog dlg = new JDialog(parent, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(420, 200);
        dlg.setLocationRelativeTo(parent);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);

        JLabel lbl = new JLabel(
                "<html><div style='text-align:center;'>" + message + "</div></html>",
                SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        lbl.setForeground(UITheme.PRIMARY);
        lbl.setBorder(new EmptyBorder(30, 20, 10, 20));
        dlg.add(lbl, BorderLayout.CENTER);

        final boolean[] result = {false};
        JButton yesBtn = UITheme.primaryButton("Yes");
        JButton noBtn  = UITheme.dangerButton("No");
        yesBtn.setPreferredSize(new Dimension(100, 36));
        noBtn .setPreferredSize(new Dimension(100, 36));
        yesBtn.addActionListener(e -> { result[0] = true;  dlg.dispose(); });
        noBtn .addActionListener(e -> { result[0] = false; dlg.dispose(); });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
        btnRow.setBackground(Color.WHITE);
        btnRow.add(yesBtn);
        btnRow.add(noBtn);
        dlg.add(btnRow, BorderLayout.SOUTH);
        dlg.setVisible(true);
        return result[0];
    }

    // ================================================================
    //  Form helpers
    // ================================================================

    private static JTextField field(String v) {
        JTextField f = new JTextField(v, 22);
        f.setFont(UITheme.FONT_BODY);
        return f;
    }

    private static JPanel formRow(String label, JTextField f) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(UITheme.PANEL_BG);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setBorder(new EmptyBorder(3, 0, 3, 0));
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setPreferredSize(new Dimension(165, 24));
        row.add(lbl, BorderLayout.WEST);
        row.add(f,   BorderLayout.CENTER);
        return row;
    }

    private static JPanel formRowWithBtn(String label, JTextField f, JButton btn) {
        JPanel row = formRow(label, f);
        row.add(btn, BorderLayout.EAST);
        return row;
    }

    private static JPanel formRowCombo(String label, JComboBox<?> combo) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(UITheme.PANEL_BG);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setBorder(new EmptyBorder(3, 0, 3, 0));
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setPreferredSize(new Dimension(165, 24));
        row.add(lbl,   BorderLayout.WEST);
        row.add(combo, BorderLayout.CENTER);
        return row;
    }

    private static JLabel sectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(UITheme.PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(10, 0, 4, 0));
        return lbl;
    }

    private static JPanel col() {
        JPanel p = new JPanel();
        p.setBackground(UITheme.PANEL_BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    private static JSeparator hSeparator() {
        JSeparator sep = new JSeparator();
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(0xDDDDDD));
        return sep;
    }

    private static String fmt(double d) { return String.format("%.2f", d); }

    private static double parseDouble(String s) {
        try { return Double.parseDouble(s.replace(",", "").trim()); } catch (Exception e) { return 0.0; }
    }

    // ================================================================
    //  Generic in-table button renderer / editor
    // ================================================================

    private static class BtnRenderer implements TableCellRenderer {
        private final JButton proto;
        BtnRenderer(JButton proto) { this.proto = proto; }
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) { return proto; }
    }

    private static class BtnEditor extends DefaultCellEditor {
        private final JButton btn;
        BtnEditor(JButton src) {
            super(new JCheckBox());
            btn = src;
            btn.addActionListener(e -> fireEditingStopped());
        }
        @Override public Component getTableCellEditorComponent(
                JTable t, Object v, boolean sel, int r, int c) { return btn; }
        @Override public Object getCellEditorValue() { return btn.getText(); }
    }
}
