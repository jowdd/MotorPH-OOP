package com.group.motorph.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;

import com.group.motorph.util.InputValidationUtil;

/**
 * Employee Add/Edit panel with validation and calendar pop-up.
 */
public class EmployeeAddEditPanel extends JPanel {
    private final JTextField employeeIdField = new JTextField(10);
    private final JTextField sssField = new JTextField(15);
    private final JTextField pagibigField = new JTextField(15);
    private final JTextField philhealthField = new JTextField(15);
    private final JTextField tinField = new JTextField(15);
    private final JTextField dobField = new JTextField(10);
    private final JButton dobCalendarBtn = new JButton("📅");
    private final JComboBox<String> roleCombo = new JComboBox<>(new String[]{"REGULAR", "HR", "FINANCE", "IT", "ADMIN"});
    private final JButton saveButton = new JButton("Save");
    private final JLabel messageLabel = new JLabel(" ");

    public EmployeeAddEditPanel(String nextEmployeeId) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; add(new JLabel("Employee ID:"), gbc);
        gbc.gridx = 1; employeeIdField.setText(nextEmployeeId); employeeIdField.setEditable(false); add(employeeIdField, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; add(new JLabel("SSS Number:"), gbc);
        gbc.gridx = 1; add(sssField, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; add(new JLabel("PAG-IBIG Number:"), gbc);
        gbc.gridx = 1; add(pagibigField, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; add(new JLabel("PHILHEALTH Number:"), gbc);
        gbc.gridx = 1; add(philhealthField, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; add(new JLabel("TIN Number:"), gbc);
        gbc.gridx = 1; add(tinField, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; add(new JLabel("Date of Birth:"), gbc);
        gbc.gridx = 1; add(dobField, gbc); gbc.gridx = 2; add(dobCalendarBtn, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; add(new JLabel("Role:"), gbc);
        gbc.gridx = 1; add(roleCombo, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3; add(saveButton, gbc);
        row++;
        gbc.gridy = row; add(messageLabel, gbc);

        dobCalendarBtn.addActionListener(e -> showCalendarDialog());
        saveButton.addActionListener(e -> validateAndSave());
    }

    private void showCalendarDialog() {
        JDatePicker picker = new JDatePicker();
        int result = JOptionPane.showConfirmDialog(this, picker, "Select Date", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Date selected = picker.getSelectedDate();
            if (selected != null) {
                dobField.setText(new SimpleDateFormat("MM/dd/yyyy").format(selected));
            }
        }
    }

    private void validateAndSave() {
        String sss = InputValidationUtil.formatSSS(sssField.getText());
        String pagibig = InputValidationUtil.formatPagIbig(pagibigField.getText());
        String philhealth = InputValidationUtil.formatPhilHealth(philhealthField.getText());
        String tin = InputValidationUtil.formatTIN(tinField.getText());
        boolean valid = true;
        if (!InputValidationUtil.isValidSSS(sss)) { showMessage("Invalid SSS number.", Color.RED); valid = false; }
        else if (!InputValidationUtil.isValidPagIbig(pagibig)) { showMessage("Invalid PAG-IBIG number.", Color.RED); valid = false; }
        else if (!InputValidationUtil.isValidPhilHealth(philhealth)) { showMessage("Invalid PHILHEALTH number.", Color.RED); valid = false; }
        else if (!InputValidationUtil.isValidTIN(tin)) { showMessage("Invalid TIN number.", Color.RED); valid = false; }
        if (valid) {
            sssField.setText(sss);
            pagibigField.setText(pagibig);
            philhealthField.setText(philhealth);
            tinField.setText(tin);
            showMessage("Saved! (Stub, connect to service)", new Color(0,128,0));
        }
    }

    private void showMessage(String msg, Color color) {
        messageLabel.setText(msg);
        messageLabel.setForeground(color);
    }

    // Simple date picker (inner class for demonstration)
    private static class JDatePicker extends JPanel {
        private final JSpinner spinner;
        public JDatePicker() {
            setLayout(new BorderLayout());
            SpinnerDateModel model = new SpinnerDateModel();
            spinner = new JSpinner(model);
            spinner.setEditor(new JSpinner.DateEditor(spinner, "MM/dd/yyyy"));
            add(spinner, BorderLayout.CENTER);
        }
        public Date getSelectedDate() {
            return (Date) spinner.getValue();
        }
    }
}
