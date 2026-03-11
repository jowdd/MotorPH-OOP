package com.group.motorph.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.group.motorph.model.Employee;
import com.group.motorph.model.PayrollRecord;
import com.group.motorph.model.User;
import com.group.motorph.service.EmployeeService;
import com.group.motorph.service.PayrollService;

/**
 * Payslips panel – shows the logged-in user's payslips.
 * Columns: Payslip ID | Month | Year | Actions ("View Details" button per row).
 */
public class EmployeePayslipsPanel extends JPanel {

    private static final int ACT_COL = 3;   // "Actions" column index
    private static final String[] MONTH_NAMES = {
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    };

    private final User            currentUser;
    private final PayrollService  payrollService  = new PayrollService();
    private final EmployeeService employeeService = new EmployeeService();

    private List<PayrollRecord>   allRecords;
    private List<PayrollRecord>   filteredRecords = new ArrayList<>();
    private Employee              employee;
    private DefaultTableModel     tableModel;
    private JTable                table;
    private JComboBox<String>     monthCombo;
    private JComboBox<String>     yearCombo;

    public EmployeePayslipsPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        build();
    }

    private void build() {
        String empId = currentUser.getEmployeeId() != null ? currentUser.getEmployeeId().trim() : "";
        employee   = employeeService.getEmployeeById(empId);
        allRecords = payrollService.getPayrollByEmployee(empId);

        // Month / Year combos
        TreeSet<Integer> yearsSet  = new TreeSet<>();
        TreeSet<Integer> monthsSet = new TreeSet<>();
        for (PayrollRecord r : allRecords) {
            yearsSet .add(r.getYear());
            monthsSet.add(r.getMonth());
        }

        List<String> mOpts = new ArrayList<>();
        mOpts.add("All");
        for (int m : monthsSet) mOpts.add(MONTH_NAMES[m - 1]);
        monthCombo = new JComboBox<>(mOpts.toArray(new String[0]));
        monthCombo.setFont(UITheme.FONT_BODY);

        List<String> yOpts = new ArrayList<>();
        yOpts.add("All");
        for (int y : yearsSet) yOpts.add(String.valueOf(y));
        yearCombo = new JComboBox<>(yOpts.toArray(new String[0]));
        yearCombo.setFont(UITheme.FONT_BODY);

        JLabel cycleLbl = new JLabel("Pay Cycle Month:");
        cycleLbl.setFont(UITheme.FONT_BODY);

        JLabel note = new JLabel("NOTE: Pay Cycle Month should be \"All\" for both Month and Year as default");
        note.setFont(UITheme.FONT_SMALL);
        note.setForeground(UITheme.DANGER);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filterRow.setBackground(UITheme.PANEL_BG);
        filterRow.add(cycleLbl);
        filterRow.add(monthCombo);
        filterRow.add(yearCombo);
        filterRow.add(note);

        // Table  (Date col hidden – Payslip ID | Month | Year | Actions)
        tableModel = new DefaultTableModel(
            new String[]{"Payslip ID", "Month", "Year", "Actions"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == ACT_COL; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setRowHeight(36);
        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);

        // "View Details" button renderer + editor
        table.getColumnModel().getColumn(ACT_COL).setCellRenderer(new ButtonCellRenderer("View Details"));
        table.getColumnModel().getColumn(ACT_COL).setCellEditor(new ButtonCellEditor("View Details"));

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                if (col == ACT_COL && row >= 0 && row < filteredRecords.size()) {
                    PayrollRecord rec = filteredRecords.get(row);
                    FinancePanel.showPayslipDialog(rec, employee);
                }
            }
        });

        // Card assembly
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(UITheme.PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.PANEL_BORDER),
                new EmptyBorder(16, 16, 16, 16)));
        card.add(filterRow,               BorderLayout.NORTH);
        card.add(UITheme.scrollPane(table), BorderLayout.CENTER);

        JPanel cardWrap = new JPanel(new BorderLayout());
        cardWrap.setBackground(UITheme.BACKGROUND);
        cardWrap.setBorder(new EmptyBorder(0, 16, 16, 16));
        cardWrap.add(card, BorderLayout.CENTER);

        add(UITheme.sectionHeader("Payslips"), BorderLayout.NORTH);
        add(cardWrap, BorderLayout.CENTER);

        monthCombo.addActionListener(e -> refreshTable());
        yearCombo .addActionListener(e -> refreshTable());
        refreshTable();
    }

    private void refreshTable() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);
        filteredRecords.clear();

        String selMonth = (String) monthCombo.getSelectedItem();
        String selYear  = (String) yearCombo .getSelectedItem();
        boolean allMonths = "All".equals(selMonth);
        boolean allYears  = "All".equals(selYear);

        final int fm = allMonths ? -1 : monthNameToNumber(selMonth);
        final int fy;
        if (allYears) {
            fy = -1;
        } else {
            int p = -1;
            try { p = Integer.parseInt(selYear); } catch (NumberFormatException ignored) {}
            fy = p;
        }

        filteredRecords = allRecords.stream()
            .filter(r -> allMonths || r.getMonth() == fm)
            .filter(r -> allYears  || r.getYear()  == fy)
            .sorted(Comparator.<PayrollRecord, Integer>comparing(PayrollRecord::getYear)
                    .thenComparing(PayrollRecord::getMonth))
            .collect(Collectors.toList());

        String empId = currentUser.getEmployeeId() != null ? currentUser.getEmployeeId().trim() : "";
        for (PayrollRecord rec : filteredRecords) {
            String payslipId = String.format("PS-%d-%02d-%s", rec.getYear(), rec.getMonth(), empId);
            String month     = rec.getMonth() >= 1 && rec.getMonth() <= 12
                               ? MONTH_NAMES[rec.getMonth() - 1] : String.valueOf(rec.getMonth());
            tableModel.addRow(new Object[]{ payslipId, month, rec.getYear(), "View Details" });
        }
    }

    // Button cell helpers
    private static class ButtonCellRenderer implements TableCellRenderer {
        private final JButton btn;
        ButtonCellRenderer(String label) {
            btn = UITheme.primaryButton(label);
            btn.setFont(UITheme.FONT_SMALL);
        }
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            return btn;
        }
    }

    private static class ButtonCellEditor extends DefaultCellEditor {
        private final JButton btn;
        ButtonCellEditor(String label) {
            super(new JCheckBox());
            btn = UITheme.primaryButton(label);
            btn.setFont(UITheme.FONT_SMALL);
            btn.addActionListener(e -> fireEditingStopped());
        }
        @Override public Component getTableCellEditorComponent(
                JTable t, Object v, boolean sel, int row, int col) { return btn; }
        @Override public Object getCellEditorValue() { return btn.getText(); }
    }

    // Helpers
    private static int monthNameToNumber(String name) {
        if (name == null) return 1;
        for (int i = 0; i < MONTH_NAMES.length; i++) {
            if (MONTH_NAMES[i].equalsIgnoreCase(name)) return i + 1;
        }
        return 1;
    }
}
