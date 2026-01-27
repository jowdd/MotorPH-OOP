package com.group.motorph.filereader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.group.motorph.model.Employee;
import com.group.motorph.model.TimeLog;

/**
 * Utility class responsible for loading, saving, and updating employee and attendance data files.
 * This class handles both reading from resource files (TSV for employees, CSV for time logs) and writing updates.
 */
public class DataLoader {

    /** Base directory for project files */
    private static final String USER_DIR = System.getProperty("user.dir");

    /** Path to the employee TSV data file */
    private static final String EMPLOYEE_FILE_PATH = USER_DIR + "/src/main/java/com/group/motorph/Resources/employee-data.tsv";

    /** Path to the time log CSV file */
    private static final String TIME_LOG_FILE_PATH = USER_DIR + "/src/main/java/com/group/motorph/Resources/attendance-record.csv";

    /** Output path for writing employee data */
    private static final String EMPLOYEE_OUTPUT_PATH = EMPLOYEE_FILE_PATH;

    /** Output path for writing time log data */
    private static final String TIME_LOG_OUTPUT_PATH = TIME_LOG_FILE_PATH;

    /**
     * Loads all employee records from the employee TSV file.
     * Skips the header line and expects the following relevant fields:
     * employeeNumber, lastName, firstName, sssNumber, philhealthNumber, tinNumber, pagibigNumber,
     * position, basicSalary, riceSubsidy, phoneAllowance, clothingAllowance
     *
     * @return List of {@link Employee} objects, empty if file not found or error occurs
     */
    public List<Employee> loadEmployees() {
        List<Employee> employees = new ArrayList<>();

        try {
            InputStream is = getResourceStream(EMPLOYEE_FILE_PATH);
            if (is == null) {
                System.err.println("Error: Could not find resource " + EMPLOYEE_FILE_PATH);
                return employees;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            reader.readLine(); // Skip header

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\t"); // TSV

                // Map fields to Employee object
                Employee employee = new Employee(
                        fields[0],                  // employeeNumber
                        fields[11],                 // position
                        fields[1],                  // lastName
                        fields[2],                  // firstName
                        Double.parseDouble(fields[13].replace(",", "")), // basicSalary
                        fields[6],                  // sssNumber
                        fields[7],                  // philhealthNumber
                        fields[9],                  // pagibigNumber
                        fields[8],                  // tinNumber
                        Double.parseDouble(fields[14].replace(",", "")), // riceSubsidy
                        Double.parseDouble(fields[15].replace(",", "")), // phoneAllowance
                        Double.parseDouble(fields[16].replace(",", ""))  // clothingAllowance
                );

                employees.add(employee);
            }

            reader.close();

        } catch (IOException e) {
            System.err.println("Error loading employee data: " + e.getMessage());
            e.printStackTrace();
        }

        return employees;
    }

    /**
     * Loads all time log records from the attendance CSV file.
     * Skips the header line and expects the following relevant fields:
     * employeeNumber, lastName, firstName, date, timeIn, timeOut
     *
     * @return List of {@link TimeLog} objects, empty if file not found or error occurs
     */
    public List<TimeLog> loadTimeLogs() {
        List<TimeLog> timeLogs = new ArrayList<>();

        try {
            InputStream is = getResourceStream(TIME_LOG_FILE_PATH);
            if (is == null) {
                System.err.println("Error: Could not find resource " + TIME_LOG_FILE_PATH);
                return timeLogs;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            reader.readLine(); // Skip header

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(","); // CSV

                String employeeNumber = fields[0];
                LocalDate date = LocalDate.parse(fields[3], DateTimeFormatter.ofPattern("MM/dd/yyyy"));

                // Parse time in and time out
                LocalTime timeIn = null;
                if (fields.length > 4 && !fields[4].isEmpty()) {
                    timeIn = LocalTime.parse(fields[4], DateTimeFormatter.ofPattern("H:mm"));
                }

                LocalTime timeOut = null;
                if (fields.length > 5 && !fields[5].isEmpty()) {
                    timeOut = LocalTime.parse(fields[5], DateTimeFormatter.ofPattern("H:mm"));
                }

                // Create TimeLog with the correct parameter types
                TimeLog timeLog = new TimeLog(employeeNumber, date, timeIn, timeOut);
                timeLogs.add(timeLog);
            }

            reader.close();

        } catch (IOException e) {
            System.err.println("Error loading time log data: " + e.getMessage());
            e.printStackTrace();
        }

        return timeLogs;
    }

    /**
     * Saves a new employee record to the employee TSV file.
     *
     * @param employee The {@link Employee} to save
     * @return true if successfully saved, false otherwise
     */
    public boolean saveEmployee(Employee employee) {
        try {
            // Read existing lines
            List<String> lines = new ArrayList<>();
            String header;
            try (BufferedReader reader = new BufferedReader(new FileReader(EMPLOYEE_OUTPUT_PATH))) {
                header = reader.readLine();
                lines.add(header);

                String line;
                while ((line = reader.readLine()) != null) lines.add(line);
            }

            // Format new employee line (TSV)
            String newEmployeeData = String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%,.0f\t%,.0f\t%,.0f\t%,.0f\t%,.0f\t%.2f",
                    employee.getEmployeeNumber(),
                    employee.getLastName(),
                    employee.getFirstName(),
                    "", // birthday placeholder
                    "", // address
                    "", // phone
                    employee.getSssNumber(),
                    employee.getPhilhealthNumber(),
                    employee.getTinNumber(),
                    employee.getPagibigNumber(),
                    "Regular", // status
                    employee.getPosition(),
                    "", // supervisor
                    employee.getBasicSalary(),
                    employee.getRiceSubsidy(),
                    employee.getPhoneAllowance(),
                    employee.getClothingAllowance(),
                    employee.getBasicSalary() / 2,  // semi-monthly
                    employee.getBasicSalary() / 168 // hourly
            );

            lines.add(newEmployeeData);

            // Write all lines back
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(EMPLOYEE_OUTPUT_PATH))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            System.out.println("Employee record saved successfully.");
            return true;

        } catch (IOException e) {
            System.err.println("Error saving employee data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Saves a new time log record to the attendance CSV file.
     *
     * @param timeLog           The {@link TimeLog} to save
     * @param employeeLastName  Employee's last name
     * @param employeeFirstName Employee's first name
     * @return true if successfully saved, false otherwise
     */
    public boolean saveTimeLog(TimeLog timeLog, String employeeLastName, String employeeFirstName) {
        try {
            // First, read the entire file to get the header and existing data
            List<String> lines = new ArrayList<>();
            String header;
            try (BufferedReader reader = new BufferedReader(new FileReader(TIME_LOG_OUTPUT_PATH))) {
                header = reader.readLine(); 
                lines.add(header);

                String line;
                while ((line = reader.readLine()) != null) lines.add(line);
            }

            // Format date and times
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");

            String formattedDate = timeLog.getDate() != null ? timeLog.getDate().format(dateFormatter) : "";
            String formattedTimeIn = timeLog.getTimeIn() != null ? timeLog.getTimeIn().format(timeFormatter) : "";
            String formattedTimeOut = timeLog.getTimeOut() != null ? timeLog.getTimeOut().format(timeFormatter) : "";

            // Format the new time log data
            String newTimeLogData = String.format("%s,%s,%s,%s,%s,%s",
                    timeLog.getEmployeeNumber(),
                    employeeLastName,
                    employeeFirstName,
                    formattedDate,
                    formattedTimeIn,
                    formattedTimeOut
            );

            lines.add(newTimeLogData);

            // Write all lines back to the file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(TIME_LOG_OUTPUT_PATH))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            System.out.println("Time log record saved successfully.");
            return true;

        } catch (IOException e) {
            System.err.println("Error saving time log data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing employee record in the employee TSV file.
     *
     * @param updatedEmployee Updated {@link Employee} object
     * @return true if successfully updated, false if employee not found or error occurs
     */
    public boolean updateEmployee(Employee updatedEmployee) {
        try {
            // Read the entire file
            List<String> lines = new ArrayList<>();
            String header;
            boolean found = false;

            try (BufferedReader reader = new BufferedReader(new FileReader(EMPLOYEE_OUTPUT_PATH))) {
                header = reader.readLine();
                lines.add(header);

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split("\t");
                    if (fields[0].equals(updatedEmployee.getEmployeeNumber())) {
                        // Replace with updated employee line
                        String updatedData = String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%,.0f\t%,.0f\t%,.0f\t%,.0f\t%,.0f\t%.2f",
                                updatedEmployee.getEmployeeNumber(),
                                updatedEmployee.getLastName(),
                                updatedEmployee.getFirstName(),
                                fields[3], // preserve birthday
                                fields[4], // address
                                fields[5], // phone
                                updatedEmployee.getSssNumber(),
                                updatedEmployee.getPhilhealthNumber(),
                                updatedEmployee.getTinNumber(),
                                updatedEmployee.getPagibigNumber(),
                                fields[10], // status
                                updatedEmployee.getPosition(),
                                fields[12], // supervisor
                                updatedEmployee.getBasicSalary(),
                                updatedEmployee.getRiceSubsidy(),
                                updatedEmployee.getPhoneAllowance(),
                                updatedEmployee.getClothingAllowance(),
                                updatedEmployee.getBasicSalary() / 2,
                                updatedEmployee.getBasicSalary() / 168
                        );
                        lines.add(updatedData);
                        found = true;
                    } else {
                        lines.add(line);
                    }
                }
            }

            if (!found) {
                System.err.println("Employee not found. Cannot update.");
                return false;
            }

            // Write all lines back to the file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(EMPLOYEE_OUTPUT_PATH))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            System.out.println("Employee record updated successfully.");
            return true;

        } catch (IOException e) {
            System.err.println("Error updating employee data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Attempts to retrieve an InputStream for a given resource path using multiple strategies:
     * class resource, classloader resource, and project file path fallback.
     *
     * @param resourcePath The relative path of the resource
     * @return InputStream if found, null otherwise
     */
    private InputStream getResourceStream(String resourcePath) {
        // Try with original path
        InputStream is = getClass().getResourceAsStream(resourcePath);

        if (is == null) {
            String pathWithoutSlash = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
            is = getClass().getResourceAsStream(pathWithoutSlash);
        }

        if (is == null) {
            // Try with class loader
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
        }

        if (is == null) {
            String pathWithoutSlash = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(pathWithoutSlash);
        }

        if (is == null) {
            // Try as a file in the project directory
            try {
                // Try with src prefix and leading slash removed for file path
                String filePath = "src" + (resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath);
                File file = new File(filePath);
                if (file.exists()) {
                    return new FileInputStream(file);
                }
            } catch (FileNotFoundException e) {
                // Ignore
            }
        }

        return is;
    }
}