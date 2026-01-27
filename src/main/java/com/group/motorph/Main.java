/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.group.motorph;


import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.group.motorph.gui.EmployeeListView;
import com.group.motorph.gui.EmployeeManagementPanel;
import com.group.motorph.model.PayrollSystem;

/**
 * Main entry point for the MotorPH Payroll Management System.
 * Initializes the application UI and loads payroll data.
 */
public class Main {
    
    /**
     * Main method - starts the application.
     * Initializes the PayrollSystem and displays the employee list view.
     */
    public static void main(String[] args) {
        // Configure system look and feel
        initializeLookAndFeel();

        // Create PayrollSystem which loads data from files
        PayrollSystem payrollSystem = new PayrollSystem();

        // Log loaded data
        logLoadedData(payrollSystem);

        // Initialize and show the GUI using SwingUtilities for thread safety
        SwingUtilities.invokeLater(() -> {
            EmployeeListView employeeListView = new EmployeeListView();
            employeeListView.setPayrollSystem(payrollSystem);
            employeeListView.setVisible(true);
        });
    }

    /**
     * Initializes the system look and feel.
     * Private method to encapsulate UI configuration.
     */
    private static void initializeLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Failed to set system look and feel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Logs the loaded data from the payroll system.
     * Private method to encapsulate logging logic.
     */
    private static void logLoadedData(PayrollSystem payrollSystem) {
        System.out.println("Loaded " + payrollSystem.getAllEmployees().size() + " employees");
        System.out.println("Loaded " + payrollSystem.getTimeLogs().size() + " time logs");
    }

    /**
     * Opens the employee management panel in a new window.
     * Called from EmployeeListView.
     */
    public static void showEmployeeManagementPanel(PayrollSystem payrollSystem) {
        EmployeeManagementPanel panel = new EmployeeManagementPanel();
        panel.setPayrollSystem(payrollSystem);

        // Create a new frame for the panel
        JFrame frame = new JFrame("Employee Management");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.add(panel);
        frame.setLocationRelativeTo(null); // Center on screen
        frame.setVisible(true);
    }
}