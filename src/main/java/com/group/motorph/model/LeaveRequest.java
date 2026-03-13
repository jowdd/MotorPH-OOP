package com.group.motorph.model;

import java.time.LocalDate;

/**
 * Represents a leave request from an employee
 */
public class LeaveRequest {

    private String requestId;
    private String employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String leaveType; // Sick, Vacation, Emergency, etc.
    private String reason;
    private String status; // Pending, Approved, Declined
    private LocalDate requestDate;

    // Constructors
    public LeaveRequest() {
        this.status = "Pending";
        this.requestDate = LocalDate.now();
    }

    public LeaveRequest(String requestId, String employeeId, LocalDate startDate,
            LocalDate endDate, String leaveType, String reason) {
        this.requestId = requestId;
        this.employeeId = employeeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.leaveType = leaveType;
        this.reason = reason;
        this.status = "Pending";
        this.requestDate = LocalDate.now();
    }

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    @Override
    public String toString() {
        return "LeaveRequest{"
                + "requestId='" + requestId + '\''
                + ", employeeId='" + employeeId + '\''
                + ", startDate=" + startDate
                + ", endDate=" + endDate
                + ", status='" + status + '\''
                + '}';
    }
}
