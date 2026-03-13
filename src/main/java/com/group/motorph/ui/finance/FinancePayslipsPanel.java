package com.group.motorph.ui.finance;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.group.motorph.model.Employee;
import com.group.motorph.model.PayrollRecord;
import com.group.motorph.model.User;
import com.group.motorph.service.EmployeeService;
import com.group.motorph.service.PayrollService;
import com.group.motorph.ui.components.PayslipDialog;
import com.group.motorph.ui.components.RoundedPanel;
import com.group.motorph.ui.components.RoundedSearchField;
import com.group.motorph.ui.components.UITheme;

/**
 * Employees Payslips panel — lets Finance staff browse all processed payroll
 * records and view individual payslips.
 */
public class FinancePayslipsPanel extends JPanel {

    private static final String[] MONTH_NAMES = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    private final PayrollService payrollService = new PayrollService();
    private final EmployeeService employeeService = new EmployeeService();

    public FinancePayslipsPanel(User user) {
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        build();
    }

    private void build() {
        List<PayrollRecord> allRecords = payrollService.getAllPayrollRecords();

        // Build combo options from data
        TreeSet<Integer> monthsSet = new TreeSet<>();
        TreeSet<Integer> yearsSet = new TreeSet<>();
        for (PayrollRecord r : allRecords) {
            monthsSet.add(r.getMonth());
            yearsSet.add(r.getYear());
        }

        List<String> mOpts = new ArrayList<>();
        mOpts.add("All");
        for (int m : monthsSet) {
            mOpts.add(MONTH_NAMES[m - 1]);
        }
        List<String> yOpts = new ArrayList<>();
        yOpts.add("All");
        for (int y : yearsSet) {
            yOpts.add(String.valueOf(y));
        }

        JComboBox<String> monthCombo = new JComboBox<>(mOpts.toArray(String[]::new));
        JComboBox<String> yearCombo = new JComboBox<>(yOpts.toArray(String[]::new));
        monthCombo.setFont(UITheme.FONT_BODY);
        yearCombo.setFont(UITheme.FONT_BODY);

        JLabel filterLbl = new JLabel("Pay Cycle Month & Year:");
        filterLbl.setFont(UITheme.FONT_BODY);

        JPanel leftFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        leftFilter.setBackground(UITheme.PANEL_BG);
        leftFilter.add(filterLbl);
        leftFilter.add(monthCombo);
        leftFilter.add(yearCombo);

        RoundedSearchField searchField = new RoundedSearchField("Search by Employee ID or Name");

        JPanel filterRow = new JPanel(new BorderLayout(12, 0));
        filterRow.setBackground(UITheme.PANEL_BG);
        filterRow.add(leftFilter, BorderLayout.WEST);
        filterRow.add(searchField, BorderLayout.CENTER);

        // Table: Payslip ID | Employee ID | Employee Name | Month | Year | Action
        final int ACT_COL = 5;
        DefaultTableModel tblModel = new DefaultTableModel(
                new String[]{"Payslip ID", "Employee ID", "Employee Name", "Month", "Year", "Action"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == ACT_COL;
            }
        };

        JTable tbl = new JTable(tblModel);
        UITheme.styleTable(tbl);
        tbl.setRowHeight(38);
        tbl.getColumnModel().getColumn(0).setPreferredWidth(170);
        tbl.getColumnModel().getColumn(1).setPreferredWidth(90);
        tbl.getColumnModel().getColumn(2).setPreferredWidth(170);
        tbl.getColumnModel().getColumn(3).setPreferredWidth(70);
        tbl.getColumnModel().getColumn(4).setPreferredWidth(70);
        tbl.getColumnModel().getColumn(ACT_COL).setPreferredWidth(150);

        JButton vdRenderBtn = UITheme.tableActionButton("View Details");
        vdRenderBtn.setOpaque(false);
        JPanel vdCell = new JPanel(new java.awt.GridBagLayout());
        vdCell.add(vdRenderBtn);
        TableCellRenderer btnRenderer = (t2, v, sel, foc, row, col) -> {
            vdCell.setBackground(UITheme.rowBackground(t2, row, sel));
            return vdCell;
        };
        tbl.getColumnModel().getColumn(ACT_COL).setCellRenderer(btnRenderer);
        tbl.getColumnModel().getColumn(ACT_COL).setHeaderRenderer(UITheme.centeredHeaderRenderer());
        UITheme.setActionColumns(tbl, ACT_COL);
        tbl.getColumnModel().getColumn(ACT_COL).setCellEditor(
                new javax.swing.DefaultCellEditor(new JCheckBox()) {
            private final JButton btn = UITheme.tableActionButton("View Details");

            {
                btn.addActionListener(e -> fireEditingStopped());
            }

            @Override
            public Component getTableCellEditorComponent(JTable t2, Object v, boolean sel, int r, int c) {
                return btn;
            }

            @Override
            public Object getCellEditorValue() {
                return "View Details";
            }
        });

        List<PayrollRecord>[] filteredRef = new List[]{new ArrayList<PayrollRecord>()};

        tbl.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = tbl.columnAtPoint(e.getPoint());
                int row = tbl.rowAtPoint(e.getPoint());
                if (col == ACT_COL && row >= 0 && row < filteredRef[0].size()) {
                    PayrollRecord rec = filteredRef[0].get(row);
                    Employee emp = employeeService.getEmployeeById(rec.getEmployeeId());
                    PayslipDialog.show(rec, emp);
                }
            }
        });

        Runnable populate = () -> {
            tblModel.setRowCount(0);
            filteredRef[0] = new ArrayList<>();
            String selM = (String) monthCombo.getSelectedItem();
            String selY = (String) yearCombo.getSelectedItem();
            String q = searchField.getText().trim().toLowerCase();
            boolean allM = "All".equals(selM), allY = "All".equals(selY);
            int fm = allM ? -1 : monthNameToNum(selM);
            int fy = -1;
            if (!allY) try {
                fy = Integer.parseInt(selY);
            } catch (NumberFormatException ignored) {
            }
            final int fmf = fm, fyf = fy;

            Comparator<PayrollRecord> order;
            if (allM && allY) {
                order = Comparator.<PayrollRecord, Integer>comparing(PayrollRecord::getYear).reversed()
                        .thenComparing(Comparator.comparingInt(PayrollRecord::getMonth).reversed())
                        .thenComparing(r -> empIdKey(r.getEmployeeId()));
            } else {
                order = Comparator.comparing(r -> empIdKey(r.getEmployeeId()));
            }

            allRecords.stream()
                    .filter(r -> allM || r.getMonth() == fmf)
                    .filter(r -> allY || r.getYear() == fyf)
                    .sorted(order)
                    .filter(r -> {
                        if (q.isEmpty()) {
                            return true;
                        }
                        String eid = r.getEmployeeId() != null ? r.getEmployeeId().toLowerCase() : "";
                        Employee ef = employeeService.getEmployeeById(r.getEmployeeId() != null ? r.getEmployeeId() : "");
                        String name = ef != null ? ef.getFullName().toLowerCase() : "";
                        return eid.contains(q) || name.contains(q);
                    })
                    .forEach(r -> {
                        filteredRef[0].add(r);
                        String empId = r.getEmployeeId() != null ? r.getEmployeeId() : "";
                        Employee emp = employeeService.getEmployeeById(empId);
                        String empName = emp != null ? emp.getFullName() : "—";
                        String mName = (r.getMonth() >= 1 && r.getMonth() <= 12)
                                ? MONTH_NAMES[r.getMonth() - 1] : "";
                        String payslipId = r.getPayslipId() != null ? r.getPayslipId()
                                : String.format("PS-%d-%02d-%s", r.getYear(), r.getMonth(), empId);
                        tblModel.addRow(new Object[]{payslipId, empId, empName, mName, r.getYear(), "View Details"});
                    });
        };

        monthCombo.addActionListener(e -> populate.run());
        yearCombo.addActionListener(e -> populate.run());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                populate.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                populate.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                populate.run();
            }
        });
        populate.run();

        RoundedPanel card = new RoundedPanel(new BorderLayout(0, 12), 12, UITheme.PANEL_BORDER);
        card.setBackground(UITheme.PANEL_BG);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.add(filterRow, BorderLayout.NORTH);
        card.add(UITheme.scrollPane(tbl), BorderLayout.CENTER);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UITheme.BACKGROUND);
        wrap.setBorder(new EmptyBorder(0, 16, 16, 16));
        wrap.add(card, BorderLayout.CENTER);

        add(UITheme.sectionHeader("Employees Payslips"), BorderLayout.NORTH);
        add(wrap, BorderLayout.CENTER);
    }

    private static int monthNameToNum(String name) {
        for (int i = 0; i < MONTH_NAMES.length; i++) {
            if (MONTH_NAMES[i].equalsIgnoreCase(name)) {
                return i + 1;
            }
        }
        return -1;
    }

    // Sort key for employee IDs — numeric value if parseable, MAX_VALUE otherwise.
    private static int empIdKey(String id) {
        if (id == null || id.isBlank()) {
            return Integer.MAX_VALUE;
        }
        try {
            return Integer.parseInt(id.trim());
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }
}
