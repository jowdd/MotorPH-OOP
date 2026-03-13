package com.group.motorph.ui.hr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import com.group.motorph.model.Employee;
import com.group.motorph.model.RegularEmployee;
import com.group.motorph.model.User;
import com.group.motorph.service.EmployeeService;
import com.group.motorph.ui.components.DatePickerDialog;
import com.group.motorph.ui.components.DialogUtil;
import com.group.motorph.ui.components.RoundedPanel;
import com.group.motorph.ui.components.RoundedSearchField;
import com.group.motorph.ui.components.UITheme;
import com.group.motorph.util.InputValidationUtil;

/**
 * Employee Management panel — lists all employees with Add, Edit, Delete, and
 * View Details actions.
 */
public class HREmployeePanel extends JPanel {

    private static final DateTimeFormatter DT_MM = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private final EmployeeService employeeService = new EmployeeService();

    private DefaultTableModel empModel;
    private JTable empTable;
    private List<Employee> empList;
    private JButton editBtn;
    private JButton deleteBtn;
    private RoundedSearchField empSearchField;

    public HREmployeePanel(User user) {
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        build();
    }

    private void build() {
        JButton addBtn = UITheme.successButton("Add Employee");
        addBtn.addActionListener(e -> openEmployeeDialog(null, false));

        editBtn = UITheme.primaryButton("Edit");
        deleteBtn = UITheme.dangerButton("Delete");
        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        editBtn.addActionListener(e -> {
            int row = empTable.getSelectedRow();
            if (row >= 0 && row < empList.size()) {
                openEmployeeDialog(empList.get(row), false);
            }
        });
        deleteBtn.addActionListener(e -> {
            int row = empTable.getSelectedRow();
            if (row >= 0 && row < empList.size()) {
                confirmDeleteEmployee(row);
            }
        });

        JPanel leftBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftBar.setBackground(UITheme.PANEL_BG);
        leftBar.add(addBtn);
        leftBar.add(editBtn);
        leftBar.add(deleteBtn);

        empSearchField = new RoundedSearchField("Search by Employee ID or Name");
        empSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                refreshEmployees();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                refreshEmployees();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                refreshEmployees();
            }
        });

        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setBackground(UITheme.PANEL_BG);
        topBar.add(leftBar, BorderLayout.WEST);
        topBar.add(empSearchField, BorderLayout.CENTER);

        empModel = new DefaultTableModel(
                new String[]{"Employee ID", "Employee Name", "Position",
                    "Status", "Immediate Supervisor", "Action"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 5;
            }
        };

        empTable = new JTable(empModel);
        UITheme.styleTable(empTable);
        empTable.setRowHeight(38);
        empTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        empTable.getColumnModel().getColumn(0).setPreferredWidth(85);
        empTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        empTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        empTable.getColumnModel().getColumn(3).setPreferredWidth(87);
        empTable.getColumnModel().getColumn(4).setPreferredWidth(147);
        empTable.getColumnModel().getColumn(5).setPreferredWidth(110);

        // "View Details" column renderer
        JButton vdRenderBtn = UITheme.tableActionButton("View Details");
        vdRenderBtn.setOpaque(false);
        JPanel vdCell = new JPanel(new java.awt.GridBagLayout());
        vdCell.add(vdRenderBtn);
        empTable.getColumnModel().getColumn(5).setCellRenderer((t, v, sel, foc, r, c) -> {
            vdCell.setBackground(UITheme.rowBackground(t, r, sel));
            return vdCell;
        });

        empTable.getColumnModel().getColumn(5).setHeaderRenderer(UITheme.centeredHeaderRenderer());
        UITheme.setActionColumns(empTable, 5);
        empTable.getColumnModel().getColumn(5).setCellEditor(new BtnEditor(new JButton("View Details")));

        empTable.getSelectionModel().addListSelectionListener(e -> {
            boolean sel = empTable.getSelectedRow() >= 0;
            editBtn.setEnabled(sel);
            deleteBtn.setEnabled(sel);
        });

        empTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = empTable.columnAtPoint(e.getPoint());
                int row = empTable.rowAtPoint(e.getPoint());
                if (row < 0 || empList == null || row >= empList.size()) {
                    return;
                }
                if (col == 5) {
                    openEmployeeDialog(empList.get(row), true);
                }
            }
        });

        RoundedPanel card = new RoundedPanel(new BorderLayout(0, 12), 12, UITheme.PANEL_BORDER);
        card.setBackground(UITheme.PANEL_BG);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.add(topBar, BorderLayout.NORTH);
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
        List<Employee> all = employeeService.getAllEmployees();
        String q = empSearchField != null ? empSearchField.getText().trim().toLowerCase() : "";
        empList = q.isEmpty() ? all : all.stream()
                .filter(e
                        -> (e.getEmployeeId() != null && e.getEmployeeId().toLowerCase().contains(q))
                || (e.getFullName() != null && e.getFullName().toLowerCase().contains(q)))
                .collect(java.util.stream.Collectors.toList());
        for (Employee e : empList) {
            empModel.addRow(new Object[]{
                e.getEmployeeId(),
                e.getFullName(),
                e.getPosition() != null ? e.getPosition() : "",
                e.getStatus() != null ? e.getStatus() : "",
                e.getImmediateSupervisor() != null ? e.getImmediateSupervisor() : "",
                "View Details"
            });
        }
        if (editBtn != null) {
            editBtn.setEnabled(false);
        }
        if (deleteBtn != null) {
            deleteBtn.setEnabled(false);
        }
    }

    private void confirmDeleteEmployee(int row) {
        boolean yes = DialogUtil.showConfirmDialog(SwingUtilities.getWindowAncestor(this),
                "Are you sure you want to delete this employee?");
        if (!yes) {
            return;
        }
        String err = employeeService.deleteEmployee(empList.get(row).getEmployeeId());
        if (err == null) {
            refreshEmployees(); 
        }else {
            JOptionPane.showMessageDialog(this, "Error: " + err, "Delete Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Opens the employee dialog in add, edit, or read-only mode.
     *
     * This method intentionally centralizes all employee-form concerns in one
     * place: field prefill, UI mode switching, input normalization, government
     * ID formatting, numeric validation, and the final branch between creating
     * a new {@code RegularEmployee} versus updating an existing model instance.
     */
    private void openEmployeeDialog(Employee existing, boolean viewOnly) {
        boolean isEdit = (existing != null) && !viewOnly;
        boolean isAdd = (existing == null);

        String dialogTitle = viewOnly ? "Employee Details"
                : isEdit ? "Edit Employee Profile"
                        : "Add Employee Profile";

        Window parent = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(parent, dialogTitle, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(580, 680);
        dlg.setLocationRelativeTo(parent);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(UITheme.PANEL_BG);

        JLabel titleLbl = new JLabel(dialogTitle);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLbl.setForeground(UITheme.PRIMARY);
        titleLbl.setBorder(new EmptyBorder(20, 24, 6, 24));
        dlg.add(titleLbl, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setBackground(UITheme.PANEL_BG);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(4, 24, 10, 24));

        // Personal Information fields
        JTextField lastNameFld = field(existing != null ? nvl(existing.getLastName()) : "");
        JTextField firstNameFld = field(existing != null ? nvl(existing.getFirstName()) : "");
        LocalDate[] bdayVal = {existing != null ? existing.getBirthday() : null};
        JTextField addrFld = field(existing != null ? nvl(existing.getAddress()) : "");
        JTextField phoneFld = field(existing != null ? nvl(existing.getPhoneNumber()) : "");

        JButton bdayBtn = new JButton(bdayVal[0] != null
                ? "\uD83D\uDCC5 " + bdayVal[0].format(DT_MM) : "\uD83D\uDCC5 Select Birthdate");
        bdayBtn.setFont(UITheme.FONT_BODY);
        bdayBtn.setBackground(Color.WHITE);
        bdayBtn.setForeground(bdayVal[0] != null ? UITheme.TEXT_DARK : UITheme.TEXT_MUTED);
        bdayBtn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        bdayBtn.addActionListener(ev -> {
            LocalDate d = DatePickerDialog.show(dlg, "Select Birthday",
                    bdayVal[0] != null ? bdayVal[0] : LocalDate.of(1990, 1, 1));
            if (d != null) {
                bdayVal[0] = d;
                bdayBtn.setText("\uD83D\uDCC5 " + d.format(DT_MM));
                bdayBtn.setForeground(UITheme.TEXT_DARK);
            }
        });

        // Employee Information fields
        JTextField empIdFld = field(existing != null
                ? nvl(existing.getEmployeeId()) : employeeService.getNextEmployeeId());
        if (!isAdd) {
            empIdFld.setEditable(false);
        }
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Regular", "Probationary"});
        if (existing != null && existing.getStatus() != null) {
            statusCombo.setSelectedItem(existing.getStatus());
        }
        JTextField posFld = field(existing != null ? nvl(existing.getPosition()) : "");
        JTextField supFld = field(existing != null ? nvl(existing.getImmediateSupervisor()) : "");

        // Government ID fields
        JTextField sssFld = field(existing != null ? nvl(existing.getSssNum()) : "");
        JTextField philFld = field(existing != null ? nvl(existing.getPhilhealthNum()) : "");
        JTextField tinFld = field(existing != null ? nvl(existing.getTinNum()) : "");
        JTextField pagFld = field(existing != null ? nvl(existing.getPagibigNum()) : "");

        // Compensation fields
        JTextField salFld = field(existing != null ? fmt(existing.getBasicSalary()) : "0.00");
        JTextField riceFld = field(existing != null ? fmt(existing.getRiceSubsidy()) : "1500.00");
        JTextField phAlwFld = field(existing != null ? fmt(existing.getPhoneAllowance()) : "1000.00");
        JTextField clAlwFld = field(existing != null ? fmt(existing.getClothingAllowance()) : "1000.00");
        JTextField grossFld = field(existing != null ? fmt(existing.getGrossSemiMonthly()) : "0.00");
        JTextField hourFld = field(existing != null ? fmt(existing.getHourlyRate()) : "0.00");

        setIdFilter(sssFld);
        setIdFilter(philFld);
        setIdFilter(tinFld);
        setIdFilter(pagFld);
        setDecimalFilter(salFld);
        setDecimalFilter(riceFld);
        setDecimalFilter(phAlwFld);
        setDecimalFilter(clAlwFld);
        setDecimalFilter(grossFld);
        setDecimalFilter(hourFld);

        if (viewOnly) {
            for (JTextField tf : new JTextField[]{lastNameFld, firstNameFld,
                addrFld, phoneFld, empIdFld, posFld, supFld,
                sssFld, philFld, tinFld, pagFld,
                salFld, riceFld, phAlwFld, clAlwFld, grossFld, hourFld}) {
                tf.setEditable(false);
                tf.setBackground(new Color(0xF5F5F5));
            }
            statusCombo.setEnabled(false);
            bdayBtn.setEnabled(false);
        }

        JLabel msgLbl = new JLabel(" ");
        msgLbl.setFont(UITheme.FONT_SMALL);
        msgLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(sectionTitle("Personal Information"));
        form.add(Box.createVerticalStrut(4));
        form.add(formRow("Last Name:", lastNameFld));
        form.add(formRow("First Name:", firstNameFld));
        JPanel bdayWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bdayWrap.setBackground(UITheme.PANEL_BG);
        bdayWrap.add(bdayBtn);
        form.add(formRowPanel("Birthday:", bdayWrap));
        form.add(formRow("Address:", addrFld));
        form.add(formRow("Phone Number:", phoneFld));
        form.add(hSeparator());

        form.add(sectionTitle("Employee Information"));
        form.add(Box.createVerticalStrut(4));
        form.add(formRow("Employee ID:", empIdFld));
        form.add(formRowCombo("Status:", statusCombo));
        form.add(formRow("Position:", posFld));
        form.add(formRow("Immediate Supervisor:", supFld));
        form.add(hSeparator());

        form.add(sectionTitle("Government ID Numbers"));
        form.add(Box.createVerticalStrut(4));
        form.add(formRow("SSS:", sssFld));
        form.add(formRow("PhilHealth:", philFld));
        form.add(formRow("TIN:", tinFld));
        form.add(formRow("Pag-IBIG:", pagFld));
        form.add(hSeparator());

        form.add(sectionTitle("Compensation"));
        form.add(Box.createVerticalStrut(4));
        form.add(formRow("Basic Salary:", salFld));
        form.add(formRow("Rice Subsidy:", riceFld));
        form.add(formRow("Phone Allowance:", phAlwFld));
        form.add(formRow("Clothing Allowance:", clAlwFld));
        form.add(formRow("Gross Semi-monthly:", grossFld));
        form.add(formRow("Hourly Rate:", hourFld));
        form.add(Box.createVerticalStrut(8));
        form.add(msgLbl);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        dlg.add(scroll, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        btnRow.setBackground(UITheme.PANEL_BG);

        if (viewOnly) {
            JButton closeBtn = UITheme.dangerButton("Close");
            closeBtn.addActionListener(e -> dlg.dispose());
            btnRow.add(closeBtn);
        } else {
            JButton cancelBtn = UITheme.dangerButton("Cancel");
            cancelBtn.addActionListener(e -> dlg.dispose());

            String saveLabel = isEdit ? "Save" : "Add Employee";
            JButton saveBtn = isEdit ? UITheme.primaryButton(saveLabel) : UITheme.successButton(saveLabel);
            saveBtn.addActionListener(e -> {
                // Normalize user input before validation so CSV output stays
                // consistent even when the form is filled with mixed casing.
                String lastName = InputValidationUtil.toTitleCase(lastNameFld.getText().trim());
                String firstName = InputValidationUtil.toTitleCase(firstNameFld.getText().trim());
                String address = InputValidationUtil.toTitleCase(addrFld.getText().trim());
                String phone = phoneFld.getText().trim();
                if (lastName.isEmpty()) {
                    msg(msgLbl, "Last Name is required.");
                    return;
                }
                if (firstName.isEmpty()) {
                    msg(msgLbl, "First Name is required.");
                    return;
                }
                if (bdayVal[0] == null) {
                    msg(msgLbl, "Birthday is required.");
                    return;
                }
                if (address.isEmpty()) {
                    msg(msgLbl, "Address is required.");
                    return;
                }
                if (phone.isEmpty()) {
                    msg(msgLbl, "Phone Number is required.");
                    return;
                }

                String position = InputValidationUtil.toTitleCase(posFld.getText().trim());
                String supervisor = InputValidationUtil.toTitleCase(supFld.getText().trim());
                if (isAdd && employeeService.employeeIdExists(empIdFld.getText().trim())) {
                    msg(msgLbl, "Employee ID already exists.");
                    return;
                }
                if (position.isEmpty()) {
                    msg(msgLbl, "Position is required.");
                    return;
                }
                if (supervisor.isEmpty()) {
                    msg(msgLbl, "Immediate Supervisor is required.");
                    return;
                }

                // Government IDs accept loosely formatted input, but they are
                // normalized here before validation and persistence so the DAO
                // always writes a predictable representation.
                String sss = InputValidationUtil.formatSSS(sssFld.getText());
                String pagibig = InputValidationUtil.formatPagIbig(pagFld.getText());
                String phil = InputValidationUtil.formatPhilHealth(philFld.getText());
                String tin = InputValidationUtil.formatTIN(tinFld.getText());
                if (sss.isEmpty() || !InputValidationUtil.isValidSSS(sss)) {
                    msg(msgLbl, "SSS number is required (10 digits).");
                    return;
                }
                if (pagibig.isEmpty() || !InputValidationUtil.isValidPagIbig(pagibig)) {
                    msg(msgLbl, "Pag-IBIG number is required (12 digits).");
                    return;
                }
                if (phil.isEmpty() || !InputValidationUtil.isValidPhilHealth(phil)) {
                    msg(msgLbl, "PhilHealth number is required (12 digits).");
                    return;
                }
                if (tin.isEmpty() || !InputValidationUtil.isValidTIN(tin)) {
                    msg(msgLbl, "TIN is required (12 digits).");
                    return;
                }

                double basicSal = parseDouble(salFld.getText());
                double rice = parseDouble(riceFld.getText());
                double phoneAlw = parseDouble(phAlwFld.getText());
                double clothAlw = parseDouble(clAlwFld.getText());
                double gross = parseDouble(grossFld.getText());
                double hourly = parseDouble(hourFld.getText());
                if (basicSal <= 0) {
                    msg(msgLbl, "Basic Salary must be greater than 0.");
                    return;
                }
                if (rice <= 0) {
                    msg(msgLbl, "Rice Subsidy must be greater than 0.");
                    return;
                }
                if (phoneAlw <= 0) {
                    msg(msgLbl, "Phone Allowance must be greater than 0.");
                    return;
                }
                if (clothAlw <= 0) {
                    msg(msgLbl, "Clothing Allowance must be greater than 0.");
                    return;
                }
                if (gross <= 0) {
                    msg(msgLbl, "Gross Semi-monthly must be greater than 0.");
                    return;
                }
                if (hourly <= 0) {
                    msg(msgLbl, "Hourly Rate must be greater than 0.");
                    return;
                }

                String confirmMsg = isEdit ? "Are you sure you want to save changes?"
                        : "Are you sure you want to add this employee?";
                if (!DialogUtil.showConfirmDialog(dlg, confirmMsg)) {
                    return;
                }

                Employee emp = (isEdit && existing != null) ? existing : new RegularEmployee();
                emp.setEmployeeId(empIdFld.getText().trim());
                emp.setLastName(lastName);
                emp.setFirstName(firstName);
                emp.setBirthday(bdayVal[0]);
                emp.setAddress(address);
                emp.setPhoneNumber(phone);
                emp.setSssNum(sss);
                emp.setPagibigNum(pagibig);
                emp.setPhilhealthNum(phil);
                emp.setTinNum(tin);
                emp.setStatus((String) statusCombo.getSelectedItem());
                emp.setPosition(position);
                emp.setImmediateSupervisor(supervisor);
                emp.setBasicSalary(basicSal);
                emp.setRiceSubsidy(rice);
                emp.setPhoneAllowance(phoneAlw);
                emp.setClothingAllowance(clothAlw);
                emp.setGrossSemiMonthly(gross);
                emp.setHourlyRate(hourly);

                String err = isEdit ? employeeService.updateEmployee(emp)
                        : employeeService.addEmployee(emp);
                if (err == null) {
                    dlg.dispose();
                    refreshEmployees();
                } else {
                    msg(msgLbl, err);
                }
            });

            btnRow.add(cancelBtn);
            btnRow.add(saveBtn);
        }

        dlg.add(btnRow, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // Form helper methods
    private static void msg(JLabel lbl, String text) {
        lbl.setText(text);
        lbl.setForeground(UITheme.DANGER);
    }

    private static JTextField field(String v) {
        JTextField f = new JTextField(v, 22);
        f.setFont(UITheme.FONT_BODY);
        return f;
    }

    private static String nvl(String s) {
        return s != null ? s : "";
    }

    private static String fmt(double d) {
        return String.format("%.2f", d);
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
        row.add(f, BorderLayout.CENTER);
        return row;
    }

    private static JPanel formRowPanel(String label, JPanel right) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(UITheme.PANEL_BG);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        row.setBorder(new EmptyBorder(3, 0, 3, 0));
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setPreferredSize(new Dimension(165, 24));
        row.add(lbl, BorderLayout.WEST);
        row.add(right, BorderLayout.CENTER);
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
        row.add(lbl, BorderLayout.WEST);
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

    private static JSeparator hSeparator() {
        JSeparator sep = new JSeparator();
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(0xDDDDDD));
        return sep;
    }

    /** Restricts text fields to numeric government-ID style input plus dashes. */
    private static void setIdFilter(JTextField f) {
        ((AbstractDocument) f.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int off, String str, AttributeSet a)
                    throws BadLocationException {
                if (str != null && str.matches("[0-9\\-]*")) {
                    super.insertString(fb, off, str, a);
                }
            }

            @Override
            public void replace(FilterBypass fb, int off, int len, String str, AttributeSet a)
                    throws BadLocationException {
                if (str != null && str.matches("[0-9\\-]*")) {
                    super.replace(fb, off, len, str, a);
                }
            }
        });
    }

    /** Restricts monetary fields to digits and at most one decimal point. */
    private static void setDecimalFilter(JTextField f) {
        ((AbstractDocument) f.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int off, String str, AttributeSet a)
                    throws BadLocationException {
                if (str == null || !str.matches("[0-9.]*")) {
                    return;
                }
                String cur = fb.getDocument().getText(0, fb.getDocument().getLength());
                String result = cur.substring(0, off) + str + cur.substring(off);
                if (result.chars().filter(c -> c == '.').count() <= 1) {
                    super.insertString(fb, off, str, a);
                }
            }

            @Override
            public void replace(FilterBypass fb, int off, int len, String str, AttributeSet a)
                    throws BadLocationException {
                if (str == null || !str.matches("[0-9.]*")) {
                    return;
                }
                String cur = fb.getDocument().getText(0, fb.getDocument().getLength());
                String result = cur.substring(0, off) + str + cur.substring(off + len);
                if (result.chars().filter(c -> c == '.').count() <= 1) {
                    super.replace(fb, off, len, str, a);
                }
            }
        });
    }

    private static double parseDouble(String s) {
        try {
            return Double.parseDouble(s.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // In-table button editor
    private static class BtnEditor extends DefaultCellEditor {

        private final JButton btn;

        BtnEditor(JButton src) {
            super(new JCheckBox());
            btn = src;
            btn.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable t, Object v, boolean sel, int r, int c) {
            return btn;
        }

        @Override
        public Object getCellEditorValue() {
            return btn.getText();
        }
    }
}
