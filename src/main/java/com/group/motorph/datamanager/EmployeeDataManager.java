package com.group.motorph.datamanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.group.motorph.model.Employee;
import com.group.motorph.model.TimeLog;

/**
 * EmployeeDataManager handles persistence of employee and time log data using file storage.
 * Provides methods for CRUD operations on employee and time log data.
 */
public class EmployeeDataManager {
    
    /** Base directory of the project, automatically resolved at run time */
    private static final String USER_DIR = System.getProperty("user.dir");

    /** Path to the employee data TSV file */
    private static final String EMPLOYEE_DATA_FILE = USER_DIR + "/src/main/java/com/group/motorph/Resources/employee-data.tsv";

    /** Path to the time log CSV file */
    private static final String TIME_LOG_FILE = USER_DIR + "/src/main/java/com/group/motorph/Resources/attendance-record.csv";

    /** Suffix to append to backup files */
    private static final String BACKUP_SUFFIX = ".bak";

    /**
     * Initializes the EmployeeDataManager and validates the presence of necessary files.
     * Logs warnings if files are missing.
     */
    public EmployeeDataManager() {
        validateFiles();
    }

    /**
     * Validates that employee and time log files exist and are writable.
     * Logs details to console.
     */
    private void validateFiles() {
        File employeeFile = new File(EMPLOYEE_DATA_FILE);
        File timeLogFile = new File(TIME_LOG_FILE);

        System.out.println("Checking file paths:");
        System.out.println("Employee data file path: " + employeeFile.getAbsolutePath());
        System.out.println("Time log file path: " + timeLogFile.getAbsolutePath());

        if (!employeeFile.exists()) {
            System.err.println("WARNING: Employee data file not found!");
        } else {
            System.out.println("Employee data file exists and is " +
                    (employeeFile.canWrite() ? "writable" : "not writable"));
        }

        if (!timeLogFile.exists()) {
            System.err.println("WARNING: Time log file not found!");
        } else {
            System.out.println("Time log file exists and is " +
                    (timeLogFile.canWrite() ? "writable" : "not writable"));
        }
    }

    /**
     * Creates a backup of a file by copying it with a .bak suffix
     *
     * @param filePath Absolute path of the file to back up
     */
    private void createBackup(String filePath) {
        try {
            File originalFile = new File(filePath);
            File backupFile = new File(filePath + BACKUP_SUFFIX);

            Files.copy(originalFile.toPath(), backupFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Backup created: " + backupFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to create backup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Parses monetary string values that may include commas.
     * @param value The string representing the monetary value
     * @return Parsed double value or 0.0 on failure
     */
    private double parseMoneyValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0.0;
        }
        String cleanValue = value.replace(",", "").trim();
        try {
            return Double.parseDouble(cleanValue);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing money value: " + value);
            return 0.0;
        }
    }

    /**
     * Validates that a required string field is non-null and non-empty.
     * @param value Field value
     * @param fieldName Name of the field for error reporting
     * @return Trimmed string
     * @throws IllegalArgumentException if value is null or empty
     */
    private String validateString(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        return value.trim();
    }

    /**
     * Loads all employees from the TSV data file.
     * @return List of Employee objects
     */
    public List<Employee> getEmployees() {
        List<Employee> employees = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(EMPLOYEE_DATA_FILE))) {
            // Skip header line
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\t");

                if (data.length >= 17) {
                    try {
                        String employeeNumber = validateString(data[0], "Employee Number");
                        String lastName = validateString(data[1], "Last Name");
                        String firstName = validateString(data[2], "First Name");
                        String sssNumber = validateString(data[6], "SSS Number");
                        String philhealthNumber = validateString(data[7], "PhilHealth Number");
                        String pagibigNumber = validateString(data[9], "Pag-IBIG Number");
                        String tinNumber = validateString(data[8], "TIN Number");
                        String position = validateString(data[11], "Position");

                        double basicSalary = parseMoneyValue(data[13]);
                        double riceSubsidy = parseMoneyValue(data[14]);
                        double phoneAllowance = parseMoneyValue(data[15]);
                        double clothingAllowance = parseMoneyValue(data[16]);

                        Employee employee = new Employee(
                                employeeNumber,
                                position,
                                lastName,
                                firstName,
                                basicSalary,
                                sssNumber,
                                philhealthNumber,
                                pagibigNumber,
                                tinNumber,
                                riceSubsidy,
                                phoneAllowance,
                                clothingAllowance
                        );
                        employees.add(employee);
                    } catch (Exception e) {
                        System.err.println("Error processing employee data row: " + e.getMessage());
                    }
                } else {
                    System.err.println("Row has insufficient data: " + data.length + " columns, expected at least 17");
                }
            }

            System.out.println("Successfully loaded " + employees.size() + " employees from file");
        } catch (IOException e) {
            System.err.println("Error reading employee data: " + e.getMessage());
            e.printStackTrace();
        }

        return employees;
    }

    /**
     * Returns a single employee by employee number.
     * @param employeeNumber Employee ID
     * @return Employee object or null if not found
     */
    public Employee getEmployee(String employeeNumber) {
        for (Employee employee : getEmployees()) {
            if (employee.getEmployeeNumber().equals(employeeNumber)) {
                return employee;
            }
        }
        return null;
    }

    /**
     * Loads all time logs from CSV file.
     * @return List of TimeLog objects
     */
    public List<TimeLog> getTimeLogs() {
        List<TimeLog> timeLogs = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");

        // First check if file exists
        File timeLogFile = new File(TIME_LOG_FILE);
        if (!timeLogFile.exists() || !timeLogFile.canRead()) {
            System.err.println("Time log file does not exist or cannot be read: " + TIME_LOG_FILE);
            return timeLogs;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(TIME_LOG_FILE))) {
            // Skip header line
            String line = reader.readLine();
            if (line == null) {
                System.out.println("Time log file is empty or has no header");
                return timeLogs;
            }

            // Read data lines
            while ((line = reader.readLine()) != null) {
                try {
                    String[] data = line.split(",");
                    if (data.length >= 6) {
                        String employeeNumber = data[0].trim();
                        String dateString = data[3].trim();
                        String timeInString = data[4].trim();
                        String timeOutString = data[5].trim();

                        // Parse time entries (some might be empty)
                        LocalDate date = LocalDate.parse(dateString, dateFormatter);
                        LocalTime timeIn = timeInString.isEmpty() ? null : LocalTime.parse(timeInString, timeFormatter);
                        LocalTime timeOut = timeOutString.isEmpty() ? null : LocalTime.parse(timeOutString, timeFormatter);

                        TimeLog timeLog = new TimeLog(employeeNumber, date, timeIn, timeOut);
                        timeLogs.add(timeLog);
                    } else {
                        System.err.println("Invalid time log format: " + line);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing time log line: " + line + " - " + e.getMessage());
                    // Continue processing other lines
                }
            }
            System.out.println("Successfully loaded " + timeLogs.size() + " time logs from file");
        } catch (IOException e) {
            System.err.println("Error reading time log data: " + e.getMessage());
            e.printStackTrace();
        }

        return timeLogs;
    }

    // Get refreshed employee list from file
    public List<Employee> getRefreshedEmployees() {
        return getEmployees();
    }

    // Get refreshed time log list from file
    public List<TimeLog> getRefreshedTimeLogs() {
        return getTimeLogs();
    }

    // Add a new employee
    public boolean addEmployee(Employee newEmployee) {
        try {
            System.out.println("Attempting to add employee: " + newEmployee.getEmployeeNumber());

            // First, create a backup
            createBackup(EMPLOYEE_DATA_FILE);

            // Read all employees
            List<Employee> allEmployees = getEmployees();

            // Check if employee already exists
            for (Employee employee : allEmployees) {
                if (employee.getEmployeeNumber().equals(newEmployee.getEmployeeNumber())) {
                    System.err.println("Employee already exists: " + newEmployee.getEmployeeNumber());
                    return false;
                }
            }

            // Add the new employee
            allEmployees.add(newEmployee);

            // Write all employees back to file
            return writeEmployeesToFile(allEmployees);

        } catch (Exception e) {
            System.err.println("Error adding employee: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing employee record in the file.
     * Creates a backup before modification.
     */
    public boolean updateEmployee(Employee updatedEmployee) {
        try {
            System.out.println("Attempting to update employee: " + updatedEmployee.getEmployeeNumber());

            // First, create a backup
            createBackup(EMPLOYEE_DATA_FILE);

            // Read all employees
            List<Employee> allEmployees = getEmployees();
            boolean found = false;

            // Replace the employee to update
            for (int i = 0; i < allEmployees.size(); i++) {
                if (allEmployees.get(i).getEmployeeNumber().equals(updatedEmployee.getEmployeeNumber())) {
                    allEmployees.set(i, updatedEmployee);
                    found = true;
                    break;
                }
            }

            if (!found) {
                // If employee not found, add them as new
                allEmployees.add(updatedEmployee);
                System.out.println("Employee not found for update, adding as new: " + updatedEmployee.getEmployeeNumber());
            }

            // Write all employees back to file
            return writeEmployeesToFile(allEmployees);

        } catch (Exception e) {
            System.err.println("Error updating employee: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes an employee record and associated time logs from files.
     */
    public boolean deleteEmployee(String employeeNumber) {
        try {
            System.out.println("Attempting to delete employee: " + employeeNumber);

            // First, create backups
            createBackup(EMPLOYEE_DATA_FILE);
            createBackup(TIME_LOG_FILE);

            // Read all employees
            List<Employee> allEmployees = getEmployees();
            boolean found = false;

            // Remove the employee
            for (int i = 0; i < allEmployees.size(); i++) {
                if (allEmployees.get(i).getEmployeeNumber().equals(employeeNumber)) {
                    allEmployees.remove(i);
                    found = true;
                    break;
                }
            }

            if (!found) {
                System.err.println("Employee not found for deletion: " + employeeNumber);
                return false;
            }

            // Also delete associated time logs
            boolean timeLogsDeleted = deleteEmployeeTimeLogs(employeeNumber);

            // Write all employees back to file
            boolean employeesUpdated = writeEmployeesToFile(allEmployees);

            return employeesUpdated && timeLogsDeleted;

        } catch (Exception e) {
            System.err.println("Error deleting employee: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes time logs for a specific employee.
     * Private method to encapsulate time log deletion logic.
     */
    private boolean deleteEmployeeTimeLogs(String employeeNumber) {
        try {
            // Read all time logs
            List<TimeLog> allTimeLogs = getTimeLogs();
            List<TimeLog> updatedTimeLogs = new ArrayList<>();

            // Filter out the employee's time logs
            for (TimeLog timeLog : allTimeLogs) {
                if (!timeLog.getEmployeeNumber().equals(employeeNumber)) {
                    updatedTimeLogs.add(timeLog);
                }
            }

            System.out.println("Removed " + (allTimeLogs.size() - updatedTimeLogs.size()) +
                    " time logs for employee " + employeeNumber);

            // Write updated time logs back to file
            return writeTimeLogsToFile(updatedTimeLogs);

        } catch (Exception e) {
            System.err.println("Error deleting employee time logs: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Writes employee data to file.
     * Private method to encapsulate file writing logic.
     */
    private boolean writeEmployeesToFile(List<Employee> employees) {
        try {
            // Create a temporary file
            Path tempFile = Files.createTempFile("employee-data", ".tmp");
            System.out.println("Created temporary file: " + tempFile.toString());

            // Write header and data to temp file
            try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
                // Write header
                writer.write("Employee #\tLast Name\tFirst Name\tPosition\tSSS #\tPhilhealth #\tPag-ibig #\tTIN #\tBasic Salary\tRice Subsidy\tPhone Allowance\tClothing Allowance");
                writer.newLine();

                // Write employee data
                for (Employee emp : employees) {
                    writer.write(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%.2f\t%.2f\t%.2f\t%.2f",
                            emp.getEmployeeNumber(),
                            emp.getLastName(),
                            emp.getFirstName(),
                            emp.getPosition(),
                            emp.getSssNumber(),
                            emp.getPhilhealthNumber(),
                            emp.getPagibigNumber(),
                            emp.getTinNumber(),
                            emp.getBasicSalary(),
                            emp.getRiceSubsidy(),
                            emp.getPhoneAllowance(),
                            emp.getClothingAllowance()));
                    writer.newLine();
                }
            }

            // Replace the original file with the temp file
            Path targetPath = Paths.get(EMPLOYEE_DATA_FILE);
            Files.move(tempFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Successfully wrote " + employees.size() + " employees to file");

            return true;
        } catch (IOException e) {
            System.err.println("Error writing employees to file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Writes time log data to file.
     * Private method to encapsulate time log file writing logic.
     */
    private boolean writeTimeLogsToFile(List<TimeLog> timeLogs) {
        try {
            // Create a temporary file
            Path tempFile = Files.createTempFile("time-logs", ".tmp");
            System.out.println("Created temporary file: " + tempFile.toString());

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

            // Write header and data to temp file
            try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
                // Write header
                writer.write("Employee Number,Date,Time-in,Time-out");
                writer.newLine();

                // Write time log data
                for (TimeLog log : timeLogs) {
                    String timeIn = log.getTimeIn() != null ? log.getTimeIn().toString() : "";
                    String timeOut = log.getTimeOut() != null ? log.getTimeOut().toString() : "";

                    writer.write(String.format("%s,%s,%s,%s",
                            log.getEmployeeNumber(),
                            log.getDate().format(dateFormatter),
                            timeIn,
                            timeOut));
                    writer.newLine();
                }
            }

            // Replace the original file with the temp file
            Path targetPath = Paths.get(TIME_LOG_FILE);
            Files.move(tempFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Successfully wrote " + timeLogs.size() + " time logs to file");

            return true;
        } catch (IOException e) {
            System.err.println("Error writing time logs to file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}