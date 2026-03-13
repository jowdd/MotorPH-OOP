package com.group.motorph.dao.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.group.motorph.dao.EmployeeDAO;
import com.group.motorph.model.AdminEmployee;
import com.group.motorph.model.Employee;
import com.group.motorph.model.FinanceEmployee;
import com.group.motorph.model.HREmployee;
import com.group.motorph.model.ITEmployee;
import com.group.motorph.model.RegularEmployee;
import com.group.motorph.util.CSVHandler;

/**
 * TSV-based implementation of EmployeeDAO. Reads and writes employee-data.tsv
 * directly
 */
public class EmployeeDAOImpl implements EmployeeDAO {

    private static final String EMPLOYEE_FILE = CSVHandler.getDataDirectory() + "employee-data.tsv";
    private static final String USER_FILE = CSVHandler.getDataDirectory() + "users.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy");

    @Override
    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();

        // Load roles once up front
        Map<String, String> roleMap = loadRolesByEmployeeId();

        try (BufferedReader reader = new BufferedReader(new FileReader(EMPLOYEE_FILE))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // skip header row
                }
                if (line.isBlank()) {
                    continue;
                }

                String[] row = line.split("\\t", -1);
                if (row.length < 19) {
                    continue;
                }

                Employee employee = buildEmployee(row, roleMap);
                if (employee != null) {
                    employees.add(employee);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading employee file: " + e.getMessage());
        }

        return employees;
    }

    @Override
    public Employee getEmployeeById(String employeeId) {
        for (Employee employee : getAllEmployees()) {
            if (employee.getEmployeeId().equals(employeeId)) {
                return employee;
            }
        }
        return null;
    }

    @Override
    public boolean addEmployee(Employee employee) {
        List<Employee> employees = getAllEmployees();
        employees.add(employee);
        return writeEmployees(employees);
    }

    @Override
    public boolean updateEmployee(Employee employee) {
        List<Employee> employees = getAllEmployees();
        boolean updated = false;

        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getEmployeeId().equals(employee.getEmployeeId())) {
                employees.set(i, employee);
                updated = true;
                break;
            }
        }

        return updated && writeEmployees(employees);
    }

    @Override
    public boolean deleteEmployee(String employeeId) {
        List<Employee> employees = getAllEmployees();
        boolean removed = employees.removeIf(e -> e.getEmployeeId().equals(employeeId));
        return removed && writeEmployees(employees);
    }

    /**
        * Maps a TSV row to an Employee object. The subclass is chosen from the
        * employee's role in users.csv.
        *
        * The employee master file does not store the role-specific Java type, so
        * the DAO reconstructs it by joining employee-data.tsv with users.csv using
        * employee ID as the link key.
     */
    private Employee buildEmployee(String[] row, Map<String, String> roleMap) {
        try {
            String employeeId = row[0].trim();
            String position = row[11].trim();
            String role = roleMap.get(employeeId); // null if no user account yet
            Employee employee = createEmployeeByRole(role);
            employee.setEmployeeId(employeeId);
            employee.setLastName(row[1].trim());
            employee.setFirstName(row[2].trim());
            employee.setBirthday(parseDate(row[3]));
            employee.setAddress(row[4].trim());
            employee.setPhoneNumber(row[5].trim());
            employee.setSssNum(row[6].trim());
            employee.setPhilhealthNum(row[7].trim());
            employee.setTinNum(row[8].trim());
            employee.setPagibigNum(row[9].trim());
            employee.setStatus(row[10].trim());
            employee.setPosition(position);
            employee.setImmediateSupervisor(row[12].trim());
            employee.setBasicSalary(parseDouble(row[13]));
            employee.setRiceSubsidy(parseDouble(row[14]));
            employee.setPhoneAllowance(parseDouble(row[15]));
            employee.setClothingAllowance(parseDouble(row[16]));
            employee.setGrossSemiMonthly(parseDouble(row[17]));
            employee.setHourlyRate(parseDouble(row[18]));
            return employee;
        } catch (Exception e) {
            return null; // skip malformed rows silently
        }
    }

    /**
     * Picks the Employee subclass that matches the user's assigned role. Driven
     * by users.csv — the same source that controls UI access Employees who
     * don't have a user account yet default to RegularEmployee.
     */
    private Employee createEmployeeByRole(String role) {
        if (role == null) {
            return new RegularEmployee();
        }
        return switch (role.toUpperCase()) {
            case "HR" -> new HREmployee();
            case "IT" -> new ITEmployee();
            case "FINANCE" -> new FinanceEmployee();
            case "ADMIN" -> new AdminEmployee();
            default -> new RegularEmployee();
        };
    }

    /**
        * Reads users.csv and returns a map of employeeId - role. Column layout:
        * username, password, role, employeeId.
        *
        * This is a lightweight join table between authentication data
        * and the employee master record.
     */
    private Map<String, String> loadRolesByEmployeeId() {
        Map<String, String> roles = new HashMap<>();
        List<String[]> rows = CSVHandler.readCSV(USER_FILE);
        for (String[] row : rows) {
            if (row.length >= 4) {
                String empId = row[3].trim();
                String role = row[2].trim();
                if (!empId.isEmpty()) {
                    roles.put(empId, role);
                }
            }
        }
        return roles;
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // Writes all employees back to the TSV file.
    private boolean writeEmployees(List<Employee> employees) {
        String header = "Employee #\tLast Name\tFirst Name\tBirthday\tAddress\tPhone Number"
                + "\tSSS #\tPhilhealth #\tTIN #\tPag-ibig #\tStatus\tPosition"
                + "\tImmediate Supervisor\tBasic Salary\tRice Subsidy\tPhone Allowance"
                + "\tClothing Allowance\tGross Semi-monthly Rate\tHourly Rate";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(EMPLOYEE_FILE))) {
            writer.write(header);
            writer.newLine();

            for (Employee emp : employees) {
                String[] row = {
                    emp.getEmployeeId(),
                    emp.getLastName(),
                    emp.getFirstName(),
                    emp.getBirthday() == null ? "" : emp.getBirthday().format(DATE_FORMATTER),
                    emp.getAddress(),
                    emp.getPhoneNumber(),
                    emp.getSssNum(),
                    emp.getPhilhealthNum(),
                    emp.getTinNum(),
                    emp.getPagibigNum(),
                    emp.getStatus(),
                    emp.getPosition(),
                    emp.getImmediateSupervisor(),
                    String.format("%.2f", emp.getBasicSalary()),
                    String.format("%.2f", emp.getRiceSubsidy()),
                    String.format("%.2f", emp.getPhoneAllowance()),
                    String.format("%.2f", emp.getClothingAllowance()),
                    String.format("%.2f", emp.getGrossSemiMonthly()),
                    String.format("%.2f", emp.getHourlyRate())
                };
                writer.write(String.join("\t", row));
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error writing employee file: " + e.getMessage());
            return false;
        }
    }
}
