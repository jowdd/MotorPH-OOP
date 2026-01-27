package com.group.motorph.filereader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import com.group.motorph.model.Employee;


/**
 * Utility class responsible for appending new employee records to the employee TSV data file.
 * This class focuses on writing operations only and assumes the employee data file exists
 * and is formatted as a tab-separated values (TSV) file. Each line corresponds to an
 * {@link Employee} object with specific fields.
 */
public class EmployeeWriter {
    /** Base directory for the project files */
    private static final String USER_DIR = System.getProperty("user.dir");

    /** Path to the employee TSV file */
    private static final String EMPLOYEE_FILE_PATH = USER_DIR + "/src/main/java/com/group/motorph/Resources/employee-data.tsv";

    /**
     * Appends a new employee record to the employee TSV data file.
     *
     * @param employee The {@link Employee} object to append to the file
     * @throws IOException if the file cannot be found, accessed, or written
     */
    public void appendEmployeeToFile(Employee employee) throws IOException {
        // Attempt to locate the employee data file using the class resource
        URL resourceUrl = getClass().getResource(EMPLOYEE_FILE_PATH);
        if (resourceUrl == null) {
            throw new IOException("Cannot find resource file: " + EMPLOYEE_FILE_PATH);
        }

        // Convert the resource URL to a File object
        String filePath = resourceUrl.getPath();
        File file = new File(filePath);

        // Verify that the file physically exists before writing
        if (!file.exists()) {
            throw new IOException("Employee data file not found: " + filePath);
        }

        // Use try-with-resources to ensure the PrintWriter is properly closed
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
            // Format employee data as TSV line
            // Note: This simplified version only includes the fields we collect
            // Add placeholders for birth date, address, phone, etc.
            String line = String.format("%s\t%s\t%s\t\t\t\t%s\t%s\t%s\t%s\tRegular\t%s\tN/A\t%.2f\t%.2f\t%.2f\t%.2f",
                    employee.getEmployeeNumber(),
                    employee.getLastName(),
                    employee.getFirstName(),
                    employee.getSssNumber(),
                    employee.getPhilhealthNumber(),
                    employee.getTinNumber(),
                    employee.getPagibigNumber(),
                    employee.getPosition(),
                    employee.getBasicSalary(),
                    employee.getRiceSubsidy(),
                    employee.getPhoneAllowance(),
                    employee.getClothingAllowance());

            // Append the formatted line to the file
            writer.println(line);
        }
    }
}