package com.group.motorph.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;

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

import com.group.motorph.filereader.EmployeeWriter;
import com.group.motorph.model.Employee;
import com.group.motorph.model.PayrollSystem;

/**
 * GUI form for adding a new employee to the payroll system.
 * 
 * This form allows the user to enter core employee information and payroll details,
 * validate required fields, save the data to both the in-memory PayrollSystem and 
 * a csv/tsv file using EmployeeWriter, and refresh the parent EmployeeListView.
 * 
 */
public class NewEmployeeForm extends JFrame {

    /** Reference to the parent EmployeeListView to refresh after adding a new employee */
    private final EmployeeListView parentFrame;

    /** Reference to the payroll system for in-memory employee management */
    private final PayrollSystem payrollSystem;

    // Form input fields
    private JTextField employeeNumberField;
    private JTextField lastNameField;
    private JTextField firstNameField;
    private JTextField positionField;
    private JTextField basicSalaryField;
    private JTextField sssNumberField;
    private JTextField philhealthNumberField;
    private JTextField tinNumberField;
    private JTextField pagibigNumberField;
    private JTextField riceSubsidyField;
    private JTextField phoneAllowanceField;
    private JTextField clothingAllowanceField;

    /**
     * Constructs a new NewEmployeeForm.
     *
     * @param parentFrame The parent EmployeeListView frame used for refreshing the table
     * @param payrollSystem The PayrollSystem instance where the employee will be added
     */
    public NewEmployeeForm(EmployeeListView parentFrame, PayrollSystem payrollSystem) {
        this.parentFrame = parentFrame;
        this.payrollSystem = payrollSystem;

        setTitle("Add New Employee");
        setMinimumSize(new Dimension(580, 640));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parentFrame);

        // Build the main content of the form
        setContentPane(buildContent());
    }

    /**
     * Builds the main content panel of the form including header, form fields, and footer.
     *
     * @return The fully constructed JPanel representing the form
     */
    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(Theme.BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Header section with title and subtitle
        root.add(buildHeader(), BorderLayout.NORTH);
        // Scrollable form section with input fields
        root.add(buildFormCard(), BorderLayout.CENTER);
        // Footer section with Save and Cancel buttons
        root.add(buildFooter(), BorderLayout.SOUTH);

        return root;
    }

    /**
     * Builds the header panel containing the form title and subtitle.
     *
     * @return A JComponent containing the header UI elements
     */
    private JComponent buildHeader() {
        JPanel header = Theme.cardPanel(new BorderLayout());
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new javax.swing.border.EmptyBorder(14, 16, 14, 16)));


        // Title label
        JLabel title = new JLabel("Add New Employee");
        title.setFont(Theme.TITLE_FONT);
        title.setForeground(Theme.TEXT_PRIMARY);
        
        // Subtitle label
        JLabel subtitle = new JLabel("Capture core personal and payroll details");
        subtitle.setFont(Theme.SUBTITLE_FONT);
        subtitle.setForeground(Theme.TEXT_MUTED);

        
        // Stack labels vertically
        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setBackground(Theme.SURFACE);
        stack.add(title);
        stack.add(Box.createVerticalStrut(4));
        stack.add(subtitle);

        
        header.add(stack, BorderLayout.WEST);
        return header;
    }

    /**
     * Builds the main scrollable form panel containing all input fields.
     *
     * @return A JComponent containing the form UI elements
     */
    private JComponent buildFormCard() {
        JPanel card = Theme.cardPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new javax.swing.border.EmptyBorder(14, 14, 14, 14)));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row = 0;

        // Add all labeled text fields
        positionField = addField(form, gbc, row++, "Position");
        employeeNumberField = addField(form, gbc, row++, "Employee Number");
        lastNameField = addField(form, gbc, row++, "Last Name");
        firstNameField = addField(form, gbc, row++, "First Name");
        basicSalaryField = addField(form, gbc, row++, "Basic Salary");
        sssNumberField = addField(form, gbc, row++, "SSS Number");
        philhealthNumberField = addField(form, gbc, row++, "PhilHealth Number");
        tinNumberField = addField(form, gbc, row++, "TIN");
        pagibigNumberField = addField(form, gbc, row++, "Pag-IBIG Number");
        riceSubsidyField = addField(form, gbc, row++, "Rice Subsidy");
        phoneAllowanceField = addField(form, gbc, row++, "Phone Allowance");
        clothingAllowanceField = addField(form, gbc, row++, "Clothing Allowance");

        // Make form scrollable for smaller screens
        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Theme.SURFACE);
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    /**
     * Helper method to add a labeled text field to a GridBagLayout panel.
     *
     * @param panel The parent panel to add the field to
     * @param gbc The GridBagConstraints used for layout positioning
     * @param row The row index for the field
     * @param labelText The label text describing the field
     * @return The created JTextField for input
     */
    private JTextField addField(JPanel panel, GridBagConstraints gbc, int row, String labelText) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.BODY_FONT);
        label.setForeground(Theme.TEXT_MUTED);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        JTextField field = new JTextField();
        field.setFont(Theme.BODY_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new javax.swing.border.EmptyBorder(8, 10, 8, 10)));
        panel.add(field, gbc);
        return field;
    }

    /**
     * Builds the footer panel containing Save and Cancel buttons.
     *
     * @return A JComponent containing footer buttons
     */
    private JComponent buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        // Save button triggers saveEmployee()
        JButton saveButton = Theme.createButton("Save Employee", Theme.ACCENT);
        saveButton.addActionListener(e -> saveEmployee());

        // Cancel button closes the form
        JButton cancelButton = Theme.createButton("Cancel", Theme.DANGER);
        cancelButton.addActionListener(e -> dispose());

        footer.add(saveButton);
        footer.add(cancelButton);
        return footer;
    }

    /**
     * Saves a new employee after validating required fields and numeric inputs.
     */
    private void saveEmployee() {
        try {
            // Validate input
            if (employeeNumberField.getText().isEmpty() || lastNameField.getText().isEmpty() ||
                    firstNameField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Employee Number, Last Name, and First Name are required fields.");
                return;
            }

            // Parse numeric values
            double basicSalary = Double.parseDouble(basicSalaryField.getText());
            double riceSubsidy = Double.parseDouble(riceSubsidyField.getText());
            double phoneAllowance = Double.parseDouble(phoneAllowanceField.getText());
            double clothingAllowance = Double.parseDouble(clothingAllowanceField.getText());

            // Create new Employee object
            Employee newEmployee = new Employee(
                    employeeNumberField.getText(),          // String employeeNumber
                    positionField.getText(),                // String position
                    lastNameField.getText(),                // String lastName
                    firstNameField.getText(),               // String firstName
                    basicSalary,                            // double basicSalary
                    sssNumberField.getText(),               // String sssNumber
                    philhealthNumberField.getText(),        // String philhealthNumber
                    pagibigNumberField.getText(),           // String pagibigNumber
                    tinNumberField.getText(),               // String tinNumber - NOTE THIS POSITION
                    riceSubsidy,                            // double riceSubsidy
                    phoneAllowance,                         // double phoneAllowance
                    clothingAllowance                       // double clothingAllowance
            );

            // Add to payroll system
            payrollSystem.addEmployee(newEmployee);

            // Save to CSV file
            EmployeeWriter writer = new EmployeeWriter();
            writer.appendEmployeeToFile(newEmployee);

            // Refresh employee list
            parentFrame.refreshEmployeeTable();

            // Show success message and close form
            JOptionPane.showMessageDialog(this, "Employee added successfully!");
            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values for salary and allowances.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving employee to file: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving employee: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
