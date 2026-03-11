package com.group.motorph.service;

import java.time.LocalDate;
import java.util.List;

import com.group.motorph.dao.AttendanceDAO;
import com.group.motorph.dao.impl.AttendanceDAOImpl;
import com.group.motorph.model.AttendanceRecord;

public class AttendanceService {

    private final AttendanceDAO attendanceDAO;

    public AttendanceService() {
        this.attendanceDAO = new AttendanceDAOImpl();
    }

    public List<AttendanceRecord> getAllAttendance() { return attendanceDAO.getAllAttendance(); }

    public List<AttendanceRecord> getAttendanceByEmployee(String employeeId) { return attendanceDAO.getAttendanceByEmployeeId(employeeId); }

    public List<AttendanceRecord> getAttendanceByEmployeeAndMonth(String employeeId, int month, int year) { return attendanceDAO.getAttendanceByEmployeeAndMonth(employeeId, month, year); }

    public List<AttendanceRecord> getPendingAttendanceByMonth(int month, int year) { return attendanceDAO.getPendingAttendanceByMonth(month, year); }

    public boolean addAttendanceRecord(AttendanceRecord record) { return attendanceDAO.addAttendanceRecord(record); }

    public boolean updateAttendanceRecord(String employeeId, LocalDate oldDate, AttendanceRecord updated) { return attendanceDAO.updateAttendanceRecord(employeeId, oldDate, updated); }

    public boolean deleteAttendanceRecord(String employeeId, LocalDate date) { return attendanceDAO.deleteAttendanceRecord(employeeId, date); }
}
