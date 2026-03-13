package com.group.motorph.dao;

import java.time.LocalDate;
import java.util.List;

import com.group.motorph.model.AttendanceRecord;

/**
 * Interface for Attendance data access operations.
 */
public interface AttendanceDAO {

    List<AttendanceRecord> getAllAttendance();

    List<AttendanceRecord> getAttendanceByEmployeeId(String employeeId);

    List<AttendanceRecord> getAttendanceByDateRange(LocalDate startDate, LocalDate endDate);

    List<AttendanceRecord> getAttendanceByEmployeeAndMonth(String employeeId, int month, int year);

    List<AttendanceRecord> getPendingAttendanceByMonth(int month, int year);

    boolean addAttendanceRecord(AttendanceRecord record);

    boolean updateAttendanceRecord(String employeeId, LocalDate oldDate, AttendanceRecord updated);

    boolean deleteAttendanceRecord(String employeeId, LocalDate date);

    boolean markAttendanceApproved(int month, int year);

    List<AttendanceRecord> getApprovedByMonth(int month, int year);

    boolean markAttendanceProcessed(int month, int year);

    void ensureStatusColumn();
}
