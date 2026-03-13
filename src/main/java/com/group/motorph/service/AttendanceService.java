package com.group.motorph.service;

import java.time.LocalDate;
import java.util.List;

import com.group.motorph.dao.AttendanceDAO;
import com.group.motorph.dao.impl.AttendanceDAOImpl;
import com.group.motorph.model.AttendanceRecord;

/**
 * Service for attendance record operations. Delegates directly to
 * AttendanceDAO; business logic lives in PayrollService and
 * AttendanceApprovalService.
 */
public class AttendanceService {

    private final AttendanceDAO attendanceDAO;

    public AttendanceService() {
        this.attendanceDAO = new AttendanceDAOImpl();
    }

    /**
     * Returns every attendance record across all employees.
     */
    public List<AttendanceRecord> getAllAttendance() {
        return attendanceDAO.getAllAttendance();
    }

    /**
     * Returns all attendance records for a specific employee.
     */
    public List<AttendanceRecord> getAttendanceByEmployee(String employeeId) {
        return attendanceDAO.getAttendanceByEmployeeId(employeeId);
    }

    /**
     * Returns attendance records for an employee filtered to a specific month
     * and year.
     */
    public List<AttendanceRecord> getAttendanceByEmployeeAndMonth(String employeeId, int month, int year) {
        return attendanceDAO.getAttendanceByEmployeeAndMonth(employeeId, month, year);
    }

    /**
     * Returns all non-approved records for a given month — used by Finance
     * before approval.
     */
    public List<AttendanceRecord> getPendingAttendanceByMonth(int month, int year) {
        return attendanceDAO.getPendingAttendanceByMonth(month, year);
    }

    /**
     * Adds a new attendance record to the CSV.
     */
    public boolean addAttendanceRecord(AttendanceRecord record) {
        return attendanceDAO.addAttendanceRecord(record);
    }

    /**
     * Updates a specific attendance record identified by employee ID and
     * original date. oldDate is needed because the date itself may be changed
     * in the edit.
     */
    public boolean updateAttendanceRecord(String employeeId, LocalDate oldDate, AttendanceRecord updated) {
        return attendanceDAO.updateAttendanceRecord(employeeId, oldDate, updated);
    }

    /**
     * Removes the attendance record for the given employee on the given date.
     */
    public boolean deleteAttendanceRecord(String employeeId, LocalDate date) {
        return attendanceDAO.deleteAttendanceRecord(employeeId, date);
    }

    /**
     * Returns only Approved records for a month — used during payroll
     * processing.
     */
    public List<AttendanceRecord> getApprovedByMonth(int month, int year) {
        return attendanceDAO.getApprovedByMonth(month, year);
    }

    /**
     * Stamps Approved records for the month as Processed after payroll
     * generation.
     */
    public boolean markAttendanceProcessed(int month, int year) {
        return attendanceDAO.markAttendanceProcessed(month, year);
    }
}
