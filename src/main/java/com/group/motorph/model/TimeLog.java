package com.group.motorph.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents a single time log entry for an employee.
 * Each instance captures an employee's clock-in (timeIn) and clock-out (timeOut) for a specific date.
 * Provides computed properties such as hours worked and completeness of the time log.
 * Designed for payroll and attendance calculations
 */
public class TimeLog {
    private final String employeeNumber;
    private final LocalDate date;                 // Date of the time log entry
    private final LocalTime timeIn;               // Clock-in time
    private final LocalTime timeOut;              // Clock-out time

    /**
     * Constructs a TimeLog instance with validation.
     * Ensures that employeeNumber and date are not null.
     * @param employeeNumber Unique employee number (cannot be null or empty)
     * @param date Date of the time log (cannot be null)
     * @param timeIn Clock-in time (can be null if not yet recorded)
     * @param timeOut Clock-out time (can be null if not yet recorded)  
     */
    public TimeLog(String employeeNumber, LocalDate date, LocalTime timeIn, LocalTime timeOut) {
        this.employeeNumber = validateRequired(employeeNumber, "Employee Number");
        this.date = validateRequired(date, "Date");
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }

    /**
     * Validates that a required String is not null or empty.
     *
     * @param value String value to validate
     * @param fieldName Name of the field for exception message
     * @return Trimmed string if valid
     * @throws IllegalArgumentException if null or empty
     */
    private String validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        return value.trim();
    }

    /**
     * Validates that a required object is not null.
     *
     * @param value Object to validate
     * @param fieldName Name of the field for exception message
     * @param <T> Type of the object
     * @return Object if valid
     * @throws IllegalArgumentException if null
     */
    private <T> T validateRequired(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
        return value;
    }

    /**
     * Calculates the total number of hours worked in this time log.
     * Handles cases where the employee works past midnight (timeOut before timeIn).
     * Returns 0 if timeIn or timeOut is null.
     * @return Number of hours worked (decimal)
     */
    public double getHoursWorked() {
        if (timeIn == null || timeOut == null) {
            return 0.0;
        }

        LocalDateTime start = LocalDateTime.of(date, timeIn);
        LocalDateTime end;

        // Handle case when time-out is on the next day
        if (timeOut.isBefore(timeIn)) {
            end = LocalDateTime.of(date.plusDays(1), timeOut);
        } else {
            end = LocalDateTime.of(date, timeOut);
        }

        Duration duration = Duration.between(start, end);
        return duration.toMinutes() / 60.0;
    }

    /**
     * Checks whether this time log entry represents a complete workday.
     * @return true if both timeIn and timeOut are recorded; false otherwise
     */
    public boolean isComplete() {
        return timeIn != null && timeOut != null;
    }

    // Getters
    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTimeIn() {
        return timeIn;
    }

    public LocalTime getTimeOut() {
        return timeOut;
    }

    /**
     * Returns a string representation of this time log entry.
     * Includes employee number, date, timeIn, timeOut, and hours worked.
     *
     * @return Formatted string
     */
    @Override
    public String toString() {
        return String.format("TimeLog{employee=%s, date=%s, in=%s, out=%s, hours=%.2f}",
                           employeeNumber, date, timeIn, timeOut, getHoursWorked());
    }
}
