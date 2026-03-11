package com.group.motorph.dao.impl;

import com.group.motorph.dao.EmployeeDAO;
import com.group.motorph.model.Employee;
import com.group.motorph.model.FinanceEmployee;
import com.group.motorph.model.HREmployee;
import com.group.motorph.model.ITEmployee;
import com.group.motorph.model.RegularEmployee;
import com.group.motorph.util.CSVHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EmployeeDAOImpl implements EmployeeDAO {

    private static final String EMPLOYEE_FILE = CSVHandler.getDataDirectory() + "employee-data.tsv";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy");

    @Override
    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(EMPLOYEE_FILE))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                if (line.isBlank()) {
                    continue;
                }

                String[] row = line.split("\\t", -1);
                if (row.length < 19) {
                    continue;
                }

                Employee employee = buildEmployee(row);
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
       boolean removed = employees.removeIf(employee -> employee.getEmployeeId().equals(employeeId));
       return removed && writeEmployees(employees);
   }
   private Employee buildEmployee(String[] row) {
       try {
           String position = row[11].trim();
           Employee employee = createEmployeeType(position);
           employee.setEmployeeId(row[0].trim());
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
           return null;
       }
   }
   private Employee createEmployeeType(String position) {
       String lower = position.toLowerCase(Locale.ENGLISH);
       if (lower.contains("hr") || lower.contains("human resource")) {
           return new HREmployee();
       }
       if (lower.contains("it") || lower.contains("system") || lower.contains("developer")) {
           return new ITEmployee();
       }
       if (lower.contains("finance") || lower.contains("account") || lower.contains("payroll")) {
           return new FinanceEmployee();
       }
       return new RegularEmployee();
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
   private boolean writeEmployees(List<Employee> employees) {
       String header = "Employee #\tLast Name\tFirst Name\tBirthday\tAddress\tPhone Number\tSSS #\tPhilhealth #\tTIN #\tPag-ibig #\tStatus\tPosition\tImmediate Supervisor\tBasic Salary\tRice Subsidy\tPhone Allowance\tClothing Allowance\tGross Semi-monthly Rate\tHourly Rate";
       try (BufferedWriter writer = new BufferedWriter(new FileWriter(EMPLOYEE_FILE))) {
           writer.write(header);
           writer.newLine();
           for (Employee employee : employees) {
               String[] row = {
                   employee.getEmployeeId(),
                   employee.getLastName(),
                   employee.getFirstName(),
                   employee.getBirthday() == null ? "" : employee.getBirthday().format(DATE_FORMATTER),
                   employee.getAddress(),
                   employee.getPhoneNumber(),
                   employee.getSssNum(),
                   employee.getPhilhealthNum(),
                   employee.getTinNum(),
                   employee.getPagibigNum(),
                   employee.getStatus(),
                   employee.getPosition(),
                   employee.getImmediateSupervisor(),
                   String.format("%.2f", employee.getBasicSalary()),
                   String.format("%.2f", employee.getRiceSubsidy()),
                   String.format("%.2f", employee.getPhoneAllowance()),
                   String.format("%.2f", employee.getClothingAllowance()),
                   String.format("%.2f", employee.getGrossSemiMonthly()),
                   String.format("%.2f", employee.getHourlyRate())
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
