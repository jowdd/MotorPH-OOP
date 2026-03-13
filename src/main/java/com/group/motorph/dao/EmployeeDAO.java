package com.group.motorph.dao;

import java.util.List;

import com.group.motorph.model.Employee;

/**
 * Data access contract for employee records. Implementations handle reading and
 * writing to the employee data store.
 */
public interface EmployeeDAO {

    List<Employee> getAllEmployees();

    Employee getEmployeeById(String employeeId);

    boolean addEmployee(Employee employee);

    boolean updateEmployee(Employee employee);

    boolean deleteEmployee(String employeeId);
}
