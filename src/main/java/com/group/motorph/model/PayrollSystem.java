package com.group.motorph.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.group.motorph.datamanager.EmployeeDataManager;

/**
 * Core payroll system managing employees and their time logs.
 * Interacts with {@link EmployeeDataManager}
 * Responsibilities include:
 * Loading employees and time logs from storage
 * Adding, updating, and deleting employees
 * Searching employees by employee number
 * Fetching time logs for employees over specific date ranges
 */
public class PayrollSystem {
    private List<Employee> employees;           // List of employees
    private List<TimeLog> timeLogs;             // List of time logs
    private EmployeeDataManager dataManager;    // Data manager handling file

    /**
     * Constructs a {@code PayrollSystem} instance and initializes employee and time log lists from csv/tsv.
     */
    public PayrollSystem() {
        this.employees = new ArrayList<>();
        this.timeLogs = new ArrayList<>();
        this.dataManager = new EmployeeDataManager();

        // Initialize with data from files
        loadDataFromFiles();
    }

    /**
     * Loads employee and time log data from csv/tsv file into memory.
     */
    private void loadDataFromFiles() {
        this.employees = dataManager.getEmployees();
        this.timeLogs = dataManager.getTimeLogs();
    }

     /**
     * Adds a new employee to the system.
     * Updates both in-memory list and csv/tsv file.
     * @param employee Employee object to add
     * @return true if successfully added, false otherwise
     */
    public boolean addEmployee(Employee employee) {
        // Add to memory
        employees.add(employee);

        // Add to file
        return dataManager.updateEmployee(employee);
    }

    /**
     * Updates an existing employee or adds as new if not found.
     * Updates both in-memory list and csv/tsv file.
     * @param employeeNumber Employee number of the employee to update
     * @param updatedEmployee Employee object with updated information
     */
    public void updateEmployee(String employeeNumber, Employee updatedEmployee) {
        // First, see if the employee exists
        boolean found = false;
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getEmployeeNumber().equals(employeeNumber)) {
                employees.set(i, updatedEmployee);
                found = true;
                break;
            }
        }

        // If not found, add as new
        if (!found) {
            employees.add(updatedEmployee);
        }

        // Update in file
        dataManager.updateEmployee(updatedEmployee);
    }

    /**
     * Deletes an employee from the system.
     * Removes employee and associated time logs from memory and csv/tsv file.
     * @param employeeNumber Employee number of the employee to delete
     */
    public void deleteEmployee(String employeeNumber) {
        employees.removeIf(emp -> emp.getEmployeeNumber().equals(employeeNumber));

        // Also remove associated time logs
        timeLogs.removeIf(log -> log.getEmployeeNumber().equals(employeeNumber));

        // Delete from file
        dataManager.deleteEmployee(employeeNumber);
    }

    /**
     * Finds an employee by their employee number.
     * @param employeeNumber Employee number to search for
     * @return Employee object if found, or null if not found
     */
    public Employee findEmployee(String employeeNumber) {
        for (Employee employee : employees) {
            if (employee.getEmployeeNumber().equals(employeeNumber)) {
                return employee;
            }
        }
        return null;
    }

    /**
     * Getters
     */
    public List<Employee> getAllEmployees() {
        return employees;
    }

    public List<TimeLog> getTimeLogs() {
        return timeLogs;
    }

    /**
     * Setters
     */

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    public void setTimeLogs(List<TimeLog> timeLogs) {
        this.timeLogs = timeLogs;
    }

    /**
     * Refreshes in-memory employee and time log data from csv/tsv file.
     */
    public void refreshData() {
        loadDataFromFiles();
    }

    /**
     * Gets time logs for a specific employee within a date range
     * @param employeeNumber The employee number to find time logs for
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return A list of time logs for the employee within the date range
     */
    public List<TimeLog> getEmployeeTimeLogs(String employeeNumber, LocalDate startDate, LocalDate endDate) {
        List<TimeLog> employeeTimeLogs = new ArrayList<>();

        for (TimeLog log : timeLogs) {
            if (log.getEmployeeNumber().equals(employeeNumber)) {
                LocalDate logDate = log.getDate();
                if ((logDate.isEqual(startDate) || logDate.isAfter(startDate)) &&
                        (logDate.isEqual(endDate) || logDate.isBefore(endDate))) {
                    employeeTimeLogs.add(log);
                }
            }
        }

        return employeeTimeLogs;
    }
}