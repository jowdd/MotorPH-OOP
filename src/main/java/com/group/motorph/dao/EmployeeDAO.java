package com.group.motorph.dao;

import java.util.List;

import com.group.motorph.model.Employee;

/**
 * Interface for Employee data access operations
 * Demonstrates ABSTRACTION
 */
public interface EmployeeDAO {
    List<Employee> getAllEmployees();
    Employee getEmployeeById(String employeeId);
    boolean addEmployee(Employee employee);
    boolean updateEmployee(Employee employee);
    boolean deleteEmployee(String employeeId);
}
