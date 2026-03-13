package com.group.motorph.service;

import java.util.List;

import com.group.motorph.dao.AttendanceDAO;
import com.group.motorph.dao.impl.AttendanceDAOImpl;
import com.group.motorph.model.AttendanceRecord;

/**
 * Service for HR staff to review and approve pending attendance records.
 * Approved records are then eligible for payroll processing.
 */
public class AttendanceApprovalService {

    private final AttendanceDAO attendanceDAO;

    public AttendanceApprovalService() {
        this.attendanceDAO = new AttendanceDAOImpl();
    }

    /**
     * Marks all Pending attendance records for the given month/year as Approved
     * directly in attendance-record.csv.
     */
    public int approveMonth(int month, int year) {
        attendanceDAO.ensureStatusColumn();
        List<AttendanceRecord> pending = attendanceDAO.getPendingAttendanceByMonth(month, year);
        if (pending.isEmpty()) {
            return 0;
        }
        attendanceDAO.markAttendanceApproved(month, year);
        return pending.size();
    }
}
