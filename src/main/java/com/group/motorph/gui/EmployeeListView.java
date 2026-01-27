package com.group.motorph.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.group.motorph.datamanager.EmployeeDataManager;
import com.group.motorph.model.Employee;
import com.group.motorph.model.PayrollSystem;

/**
 * Main GUI window showing a centralized list of all employees.
 *
 * This window allows administrators to:
 * - View employee details in a table with sortable columns.
 * - Add new employees using a dedicated form.
 * - Delete employees with confirmation.
 * - Access payroll calculation for selected employees.
 * 
 */
public class EmployeeListView extends JFrame {

    /** Table displaying employee records. */
    private JTable employeeTable;

    /** Table model holding employee data. */
    private DefaultTableModel tableModel;
    
    /** Reference to payroll system for employee management and payroll calculations. */
    private PayrollSystem payrollSystem;

    /** Data manager for persistent employee data storage and retrieval. */
    private final EmployeeDataManager dataManager = new EmployeeDataManager();

    /** Action buttons for table operations. */
    private JButton viewEmployeeButton;
    private JButton newEmployeeButton;
    private JButton deleteEmployeeButton;
    private JButton calculateSalaryButton;

    /**
     * Constructs the employee list view window.
     */
    public EmployeeListView() {
        setTitle("MotorPH Payroll Workspace");
        setMinimumSize(new Dimension(1040, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setContentPane(buildContent());
    }

    /**
     * Builds the root panel containing header and table card.
     */
    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(Theme.BACKGROUND);
        root.setBorder(new EmptyBorder(18, 18, 18, 18));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTableCard(), BorderLayout.CENTER);

        return root;
    }

    /** Builds the header panel with title and subtitle. */
    private JComponent buildHeader() {
        JPanel header = Theme.cardPanel(new BorderLayout());
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new EmptyBorder(18, 20, 18, 20)));

        JLabel title = new JLabel("MotorPH Employee Records");
        title.setFont(Theme.TITLE_FONT);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Centralized view with quick actions for employee data and payroll");
        subtitle.setFont(Theme.SUBTITLE_FONT);
        subtitle.setForeground(Theme.TEXT_MUTED);

        JPanel textStack = new JPanel();
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
        textStack.setBackground(Theme.SURFACE);
        textStack.add(title);
        textStack.add(Box.createVerticalStrut(4));
        textStack.add(subtitle);

        header.add(textStack, BorderLayout.WEST);
        return header;
    }

    /** Builds the card panel containing toolbar and employee table. */
    private JComponent buildTableCard() {
        JPanel card = Theme.cardPanel(new BorderLayout(12, 12));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new EmptyBorder(16, 16, 16, 16)));

        card.add(buildToolbar(), BorderLayout.NORTH);
        card.add(buildTable(), BorderLayout.CENTER);
        return card;
    }

    /** Builds toolbar with buttons for common employee actions. */
    private JComponent buildToolbar() {
        JPanel actions = new JPanel(new BorderLayout(10, 0));
        actions.setOpaque(false);

        viewEmployeeButton = Theme.createButton("View Details", Theme.ACCENT);
        newEmployeeButton = Theme.createButton("Add New Employee", Theme.ACCENT);
        deleteEmployeeButton = Theme.createButton("Delete Employee", Theme.DANGER);
        calculateSalaryButton = Theme.createButton("Calculate Salary", Theme.ACCENT);

        viewEmployeeButton.addActionListener(e -> viewSelectedEmployee());
        newEmployeeButton.addActionListener(e -> openNewEmployeeForm());
        deleteEmployeeButton.addActionListener(e -> deleteSelectedEmployee());
        calculateSalaryButton.addActionListener(e -> openSalaryCalculator());

        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftButtons.setOpaque(false);
        leftButtons.add(viewEmployeeButton);
        leftButtons.add(newEmployeeButton);
        leftButtons.add(deleteEmployeeButton);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightButtons.setOpaque(false);
        rightButtons.add(calculateSalaryButton);

        actions.add(leftButtons, BorderLayout.WEST);
        actions.add(rightButtons, BorderLayout.EAST);

        return actions;
    }

    /** Builds toolbar with buttons for common employee actions. */
    private JComponent buildTable() {
        String[] columnNames = {"Employee Number", "Last Name", "First Name",
                "SSS Number", "PhilHealth Number", "TIN", "Pag-IBIG Number"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        employeeTable = new JTable(tableModel);
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Theme.styleTable(employeeTable);
        employeeTable.getSelectionModel().addListSelectionListener(e -> updateActionStates());

        JScrollPane scrollPane = new JScrollPane(employeeTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scrollPane.getViewport().setBackground(Theme.SURFACE);

        return scrollPane;
    }

    /**
     * Sets the payroll system instance to be used and refreshes the employee table.
     *
     * @param payrollSystem PayrollSystem instance managing employees.
     */
    public void setPayrollSystem(PayrollSystem payrollSystem) {
        this.payrollSystem = payrollSystem;
        refreshEmployeeTable();
    }

    /** Refreshes the employee table from payroll system data. */
    public void refreshEmployeeTable() {
        // Clear existing data
        tableModel.setRowCount(0); // clear existing rows

        // Add employee data to the table
        List<Employee> employees = payrollSystem.getAllEmployees();

        for (Employee employee : employees) {
            Object[] rowData = {
                    employee.getEmployeeNumber(),
                    employee.getLastName(),
                    employee.getFirstName(),
                    employee.getSssNumber(),
                    employee.getPhilhealthNumber(),
                    employee.getTinNumber(),
                    employee.getPagibigNumber()
            };
            tableModel.addRow(rowData);
        }

        // Sort by employee number
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        employeeTable.setRowSorter(sorter);

        updateActionStates();
    }

    /** Opens the detail view for the currently selected employee. */
    private void viewSelectedEmployee() {
        Employee selectedEmployee = getSelectedEmployee();
        if (selectedEmployee == null) {
            JOptionPane.showMessageDialog(this, "Please select an employee from the table.");
            return;
        }

        EmployeeDetailView detailView = new EmployeeDetailView(selectedEmployee, payrollSystem, this::refreshEmployeeTable);
        detailView.setVisible(true);
    }

    /** Opens the form to create a new employee. */
    private void openNewEmployeeForm() {
        NewEmployeeForm newEmployeeForm = new NewEmployeeForm(this, payrollSystem);
        newEmployeeForm.setVisible(true);
    }

    /** Deletes the selected employee after confirmation. */
    private void deleteSelectedEmployee() {
        Employee employee = getSelectedEmployee();
        if (employee == null) {
            JOptionPane.showMessageDialog(this, "Select an employee to delete.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete " + employee.getFirstName() + " " + employee.getLastName() + "? This removes records and time logs.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        boolean deleted = dataManager.deleteEmployee(employee.getEmployeeNumber());
        if (deleted) {
            payrollSystem.setEmployees(dataManager.getRefreshedEmployees());
            payrollSystem.setTimeLogs(dataManager.getRefreshedTimeLogs());
            refreshEmployeeTable();
            JOptionPane.showMessageDialog(this, "Employee deleted successfully.");
        } else {
            JOptionPane.showMessageDialog(this, "Unable to delete employee. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Opens a dialog to calculate the salary for the selected employee. */
    private void openSalaryCalculator() {
        Employee employee = getSelectedEmployee();
        if (employee == null) {
            JOptionPane.showMessageDialog(this, "Select an employee to calculate salary.");
            return;
        }
        SalaryCalculationDialog dialog = new SalaryCalculationDialog(this, payrollSystem, employee);
        dialog.setVisible(true);
    }

    /**
     * Returns the currently selected employee in the table, or null if none.
     */
    private Employee getSelectedEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }
        selectedRow = employeeTable.convertRowIndexToModel(selectedRow);
        String employeeNumber = (String) tableModel.getValueAt(selectedRow, 0);
        return findEmployeeByNumber(employeeNumber);
    }

    /** Updates the enabled state of action buttons based on table selection. */
    private void updateActionStates() {
        boolean hasSelection = employeeTable != null && employeeTable.getSelectedRow() >= 0;
        if (viewEmployeeButton != null) {
            viewEmployeeButton.setEnabled(hasSelection);
            viewEmployeeButton.setOpaque(true);
        }
        if (deleteEmployeeButton != null) {
            deleteEmployeeButton.setEnabled(hasSelection);
            deleteEmployeeButton.setOpaque(true);
        }
        if (calculateSalaryButton != null) {
            calculateSalaryButton.setEnabled(hasSelection);
            calculateSalaryButton.setOpaque(true);
        }
    }

    /** Finds an employee by their employee number. Returns null if not found. */
    private Employee findEmployeeByNumber(String employeeNumber) {
        for (Employee emp : payrollSystem.getAllEmployees()) {
            if (emp.getEmployeeNumber().equals(employeeNumber)) {
                return emp;
            }
        }
        return null;
    }
}