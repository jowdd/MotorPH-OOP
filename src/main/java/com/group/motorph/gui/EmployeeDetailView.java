package com.group.motorph.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.util.Locale;

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
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;


import com.group.motorph.datamanager.EmployeeDataManager;
import com.group.motorph.model.Employee;
import com.group.motorph.model.PayrollSystem;

/**
 * GUI window displaying detailed information for a single employee.
 *
 * Show all employee details,including personal information, payroll identifiers (SSS, PhilHealth, Pag-IBIG, TIN),
 * and allowances.
 *
 */
public class EmployeeDetailView extends JFrame {

    private Employee employee;
    private final PayrollSystem payrollSystem;
    private final EmployeeDataManager dataManager = new EmployeeDataManager();
    private final Runnable onUpdated;

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField positionField;   
    private JTextField basicSalaryField;
    private JTextField sssField;
    private JTextField philHealthField;
    private JTextField pagibigField;
    private JTextField tinField;
    private JTextField riceField;
    private JTextField phoneField;
    private JTextField clothingField;

    /** Button toggling between Edit and Update modes. */
    private JButton editUpdateButton;

     /** Tracks whether the fields are currently in edit mode. */
    private boolean editing = false;

    /** Formats numeric values as Philippine Peso currency. */
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("en", "PH"));

    /**
     * Constructs an employee detail view.
     *
     * @param employee Employee whose details will be displayed.
     * @param payrollSystem Payroll system used for updating the employee.
     * @param onUpdated Callback executed after a successful update, can be null.
     */
    public EmployeeDetailView(Employee employee, PayrollSystem payrollSystem, Runnable onUpdated) {
        this.employee = employee;
        this.payrollSystem = payrollSystem;
        this.onUpdated = onUpdated;

        setTitle("Employee Details");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(640, 580));
        setLocationRelativeTo(null);
        setContentPane(buildContent());
        fillFields();
    }

    /**
     * Builds the main panel containing header, form, and footer.
     */
    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(Theme.BACKGROUND);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildForm(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        return root;
    }

    /** Builds the header panel displaying employee name and number. */
    private JComponent buildHeader() {
        JPanel header = Theme.cardPanel(new BorderLayout());
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel title = new JLabel(employee.getFirstName() + " " + employee.getLastName());
        title.setFont(Theme.TITLE_FONT);
        title.setForeground(Theme.TEXT_PRIMARY);
        JLabel subtitle = new JLabel("Employee #" + employee.getEmployeeNumber());
        subtitle.setFont(Theme.SUBTITLE_FONT);
        subtitle.setForeground(Theme.TEXT_MUTED);

        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setBackground(Theme.SURFACE);
        stack.add(title);
        stack.add(Box.createVerticalStrut(4));
        stack.add(subtitle);

        header.add(stack, BorderLayout.WEST);
        return header;
    }

    /** Builds the scrollable form for editing employee details. */
    private JComponent buildForm() {
        JPanel card = Theme.cardPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new EmptyBorder(16, 16, 16, 16)));

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row = 0;
        addFieldRow(grid, gbc, row++, "Position", positionField = new JTextField());
        addFieldRow(grid, gbc, row++, "First Name", firstNameField = new JTextField());
        addFieldRow(grid, gbc, row++, "Last Name", lastNameField = new JTextField());
        addFieldRow(grid, gbc, row++, "Basic Salary", basicSalaryField = new JTextField());
        addFieldRow(grid, gbc, row++, "SSS Number", sssField = new JTextField());
        addFieldRow(grid, gbc, row++, "PhilHealth Number", philHealthField = new JTextField());
        addFieldRow(grid, gbc, row++, "Pag-IBIG Number", pagibigField = new JTextField());
        addFieldRow(grid, gbc, row++, "TIN Number", tinField = new JTextField());
        addFieldRow(grid, gbc, row++, "Rice Subsidy", riceField = new JTextField());
        addFieldRow(grid, gbc, row++, "Phone Allowance", phoneField = new JTextField());
        addFieldRow(grid, gbc, row++, "Clothing Allowance", clothingField = new JTextField());

        card.add(grid, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(card);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Theme.BACKGROUND);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scrollPane;
    }

    /** Builds the footer panel containing Edit/Update and Cancel buttons. */
    private JComponent buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        editUpdateButton = Theme.createButton("Edit", Theme.ACCENT);
        editUpdateButton.addActionListener(e -> toggleEdit());
        JButton cancelButton = Theme.createButton("Cancel", Theme.DANGER);
        cancelButton.addActionListener(e -> dispose());

        footer.add(editUpdateButton);
        footer.add(cancelButton);
        return footer;
    }

    /** Adds a labeled field row to the form grid. */
    private void addFieldRow(JPanel grid, GridBagConstraints gbc, int row, String labelText, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.BODY_FONT);
        label.setForeground(Theme.TEXT_MUTED);
        grid.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        field.setFont(Theme.BODY_FONT);
        field.setEditable(false);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new EmptyBorder(8, 10, 8, 10)));
        grid.add(field, gbc);
    }

    /** Fills all fields with current employee values, formatting currency where needed. */
    private void fillFields() {
        firstNameField.setText(employee.getFirstName());
        lastNameField.setText(employee.getLastName());
        positionField.setText(employee.getPosition());
        basicSalaryField.setText(currencyFormat.format(employee.getBasicSalary()));
        sssField.setText(employee.getSssNumber());
        philHealthField.setText(employee.getPhilhealthNumber());
        pagibigField.setText(employee.getPagibigNumber());
        tinField.setText(employee.getTinNumber());
        riceField.setText(currencyFormat.format(employee.getRiceSubsidy()));
        phoneField.setText(currencyFormat.format(employee.getPhoneAllowance()));
        clothingField.setText(currencyFormat.format(employee.getClothingAllowance()));
    }

    /** Toggles between editing mode and update mode. */
    private void toggleEdit() {
        if (!editing) {
            setFieldsEditable(true);
            editUpdateButton.setText("Update");
            editing = true;
        } else {
            updateEmployee();
        }
    }

    /** Enables or disables field editing. */
    private void setFieldsEditable(boolean editable) {
        firstNameField.setEditable(editable);
        lastNameField.setEditable(editable);
        positionField.setEditable(editable);
        basicSalaryField.setEditable(editable);
        sssField.setEditable(editable);
        philHealthField.setEditable(editable);
        pagibigField.setEditable(editable);
        tinField.setEditable(editable);
        riceField.setEditable(editable);
        phoneField.setEditable(editable);
        clothingField.setEditable(editable);
    }

    /**
     * Reads field values, validates numeric inputs, updates the employee object,
     * refreshes payroll data, and notifies the user of success or errors.
     */
    private void updateEmployee() {
        try {
            double basicSalary = parseCurrency(basicSalaryField.getText());
            double rice = parseCurrency(riceField.getText());
            double phone = parseCurrency(phoneField.getText());
            double clothing = parseCurrency(clothingField.getText());

            Employee updated = new Employee(
                    employee.getEmployeeNumber(),
                    positionField.getText(),
                    lastNameField.getText(),
                    firstNameField.getText(),
                    basicSalary,
                    sssField.getText(),
                    philHealthField.getText(),
                    pagibigField.getText(),
                    tinField.getText(),
                    rice,
                    phone,
                    clothing
            );

            payrollSystem.updateEmployee(employee.getEmployeeNumber(), updated);
            dataManager.updateEmployee(updated);
            payrollSystem.setEmployees(dataManager.getRefreshedEmployees());

            employee = updated;
            fillFields();
            setFieldsEditable(false);
            editUpdateButton.setText("Edit");
            editing = false;

            if (onUpdated != null) {
                onUpdated.run();
            }

            JOptionPane.showMessageDialog(this, "Employee updated successfully.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values for salary and allowances.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to update employee: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Parses a formatted currency string into a numeric value. */
    private double parseCurrency(String value) {
        String normalized = value.replace(",", "").replace("₱", "").trim();
        return Double.parseDouble(normalized);
    }
}