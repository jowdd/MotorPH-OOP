package com.group.motorph.dao;

import java.util.List;

import com.group.motorph.model.AttendanceRecord;

public interface ApprovedAttendanceDAO {
    List<AttendanceRecord> getAllApproved();
    List<AttendanceRecord> getApprovedByMonth(int month, int year);
    void overwrite(List<AttendanceRecord> records);
    void clear();
}
