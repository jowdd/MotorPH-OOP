package com.group.motorph.service;

import java.util.List;

import com.group.motorph.dao.EmployeeDAO;
import com.group.motorph.dao.impl.EmployeeDAOImpl;
import com.group.motorph.model.Employee;

/**
 * Service class for employee management operations.
 */
public class EmployeeService {
    private final EmployeeDAO employeeDAO;

    public EmployeeService() {
        this.employeeDAO = new EmployeeDAOImpl();
    }

    /** Get all employees */
    public List<Employee> getAllEmployees() {
        return employeeDAO.getAllEmployees();
    }

    /** Get a single employee by ID */
    public Employee getEmployeeById(String employeeId) {
        return employeeDAO.getEmployeeById(employeeId);
    }

    /**
     * Get the next available Employee ID as a numeric string (e.g., "10035").
     * Scans all existing IDs for the highest numeric value then adds 1.
     */
    public String getNextEmployeeId() {
        List<Employee> employees = getAllEmployees();
        int max = 10000; // start from 10001 if no employees exist
        for (Employee emp : employees) {
            String id = emp.getEmployeeId();
            if (id != null && id.trim().matches("\\d{4,6}")) {
                int num = Integer.parseInt(id.trim());
                if (num > max) max = num;
            }
        }
        return String.valueOf(max + 1);
    }

    /**
     * Check if an employee ID already exists.
     * Used to prevent duplicate IDs.
     */
    public boolean employeeIdExists(String employeeId) {
        return employeeDAO.getEmployeeById(employeeId) != null;
    }

    /**
     * Add a new employee with validation.
     * Returns an error message, or null if successful.
     */
    public String addEmployee(Employee employee) {
        if (!com.group.motorph.util.InputValidationUtil.isNotBlank(employee.getEmployeeId())) {
            return "Employee ID is required.";
        }
        if (employeeIdExists(employee.getEmployeeId())) {
            return "Employee ID " + employee.getEmployeeId() + " already exists.";
        }
        if (!com.group.motorph.util.InputValidationUtil.isNotBlank(employee.getFirstName())) {
            return "First name is required.";
        }
        if (!com.group.motorph.util.InputValidationUtil.isNotBlank(employee.getLastName())) {
            return "Last name is required.";
        }
        boolean ok = employeeDAO.addEmployee(employee);
        return ok ? null : "Failed to save employee. Please try again.";
    }

    /**
     * Update an existing employee.
     * Returns an error message, or null if successful.
     */
    public String updateEmployee(Employee employee) {
        if (!com.group.motorph.util.InputValidationUtil.isNotBlank(employee.getEmployeeId())) {
            return "Employee ID is required.";
        }
        boolean ok = employeeDAO.updateEmployee(employee);
        return ok ? null : "Failed to update employee. Please try again.";
    }

    /**
     * Delete employee by ID.
     * Returns an error message, or null if successful.
     */
    public String deleteEmployee(String employeeId) {
        boolean ok = employeeDAO.deleteEmployee(employeeId);
        return ok ? null : "Failed to delete employee.";
    }
}

