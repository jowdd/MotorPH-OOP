package com.group.motorph.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a single attendance record with clock-in and clock-out times.
 *
 * CSV-first model: fields map directly to attendance-record.csv columns.
 */
public class AttendanceRecord {

    private String employeeId;
    private String lastName;
    private String firstName;

    private LocalDate date;
    private LocalTime clockIn;
    private LocalTime clockOut;

    /**
     * Values: "Pending" (default) or "Approved".
     */
    private String status;

    public AttendanceRecord() {
    }

    public AttendanceRecord(String employeeId, String lastName, String firstName,
            LocalDate date, LocalTime clockIn, LocalTime clockOut, String status) {
        this.employeeId = employeeId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.date = date;
        this.clockIn = clockIn;
        this.clockOut = clockOut;
        this.status = status;
    }

    // Backward compatible constructor
    public AttendanceRecord(String employeeId, LocalDate date, LocalTime clockIn, LocalTime clockOut) {
        this(employeeId, "", "", date, clockIn, clockOut, "Pending");
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getClockIn() {
        return clockIn;
    }

    public void setClockIn(LocalTime clockIn) {
        this.clockIn = clockIn;
    }

    public LocalTime getClockOut() {
        return clockOut;
    }

    public void setClockOut(LocalTime clockOut) {
        this.clockOut = clockOut;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFullName() {
        String ln = lastName == null ? "" : lastName.trim();
        String fn = firstName == null ? "" : firstName.trim();
        return (ln + ", " + fn).replaceAll("^,\s*", "").trim();
    }
}
