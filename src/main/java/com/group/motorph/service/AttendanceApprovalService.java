package com.group.motorph.service;

import java.util.ArrayList;
import java.util.List;

import com.group.motorph.dao.ApprovedAttendanceDAO;
import com.group.motorph.dao.AttendanceDAO;
import com.group.motorph.dao.impl.ApprovedAttendanceDAOImpl;
import com.group.motorph.dao.impl.AttendanceDAOImpl;
import com.group.motorph.model.AttendanceRecord;

public class AttendanceApprovalService {

    private final AttendanceDAO attendanceDAO;
    private final ApprovedAttendanceDAO approvedAttendanceDAO;

    public AttendanceApprovalService() {
        this.attendanceDAO = new AttendanceDAOImpl();
        this.approvedAttendanceDAO = new ApprovedAttendanceDAOImpl();
    }

    public int approveMonth(int month, int year) {
        attendanceDAO.ensureStatusColumn();
        List<AttendanceRecord> pending = attendanceDAO.getPendingAttendanceByMonth(month, year);
        if (pending.isEmpty()) return 0;

        List<AttendanceRecord> approvedToWrite = new ArrayList<>();
        for (AttendanceRecord r : pending) {
            r.setStatus("Approved");
            approvedToWrite.add(r);
        }

        approvedAttendanceDAO.overwrite(approvedToWrite);
        attendanceDAO.markAttendanceApproved(month, year);
        return approvedToWrite.size();
    }
}
