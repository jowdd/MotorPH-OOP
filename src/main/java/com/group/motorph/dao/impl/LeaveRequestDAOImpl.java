package com.group.motorph.dao.impl;

import com.group.motorph.dao.LeaveRequestDAO;
import com.group.motorph.model.LeaveRequest;
import com.group.motorph.util.CSVHandler;
import com.group.motorph.util.DateTimeUtil;

// import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CSV-based implementation of LeaveRequestDAO
 * Demonstrates POLYMORPHISM
 */
public class LeaveRequestDAOImpl implements LeaveRequestDAO {
    
    private static final String LEAVE_FILE = CSVHandler.getDataDirectory() + "leave_requests.csv";
    private static final String[] HEADERS = {
        "RequestID", "EmployeeID", "StartDate", "EndDate", 
        "LeaveType", "Reason", "Status", "RequestDate"
    };
    
    public LeaveRequestDAOImpl() {
        CSVHandler.ensureDataDirectory();
    }
    
    @Override
    public List<LeaveRequest> getAllLeaveRequests() {
        List<LeaveRequest> requests = new ArrayList<>();
        List<String[]> data = CSVHandler.readCSV(LEAVE_FILE);
        
        for (String[] row : data) {
            if (row.length >= 8) {
                try {
                    LeaveRequest request = new LeaveRequest();
                    request.setRequestId(row[0].trim());
                    request.setEmployeeId(row[1].trim());
                    request.setStartDate(DateTimeUtil.parseDate(row[2].trim()));
                    request.setEndDate(DateTimeUtil.parseDate(row[3].trim()));
                    request.setLeaveType(row[4].trim());
                    request.setReason(row[5].trim());
                    request.setStatus(row[6].trim());
                    request.setRequestDate(DateTimeUtil.parseDate(row[7].trim()));
                    requests.add(request);
                } catch (Exception e) {
                    System.err.println("Error parsing leave request: " + e.getMessage());
                }
            }
        }
        
        return requests;
    }
    
    @Override
    public List<LeaveRequest> getPendingLeaveRequests() {
        return getAllLeaveRequests().stream()
            .filter(request -> "Pending".equals(request.getStatus()))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<LeaveRequest> getLeaveRequestsByEmployeeId(String employeeId) {
        return getAllLeaveRequests().stream()
            .filter(request -> request.getEmployeeId().equals(employeeId))
            .collect(Collectors.toList());
    }
    
    @Override
    public LeaveRequest getLeaveRequestById(String requestId) {
        return getAllLeaveRequests().stream()
            .filter(request -> request.getRequestId().equals(requestId))
            .findFirst()
            .orElse(null);
    }
    
    @Override
    public boolean addLeaveRequest(LeaveRequest request) {
        try {
            String[] data = {
                request.getRequestId(),
                request.getEmployeeId(),
                DateTimeUtil.formatDate(request.getStartDate()),
                DateTimeUtil.formatDate(request.getEndDate()),
                request.getLeaveType(),
                request.getReason(),
                request.getStatus(),
                DateTimeUtil.formatDate(request.getRequestDate())
            };
            CSVHandler.appendToCSV(LEAVE_FILE, data);
            return true;
        } catch (Exception e) {
            System.err.println("Error adding leave request: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean updateLeaveRequest(LeaveRequest request) {
        List<LeaveRequest> requests = getAllLeaveRequests();
        List<String[]> data = new ArrayList<>();
        
        boolean updated = false;
        for (LeaveRequest req : requests) {
            if (req.getRequestId().equals(request.getRequestId())) {
                data.add(new String[]{
                    request.getRequestId(),
                    request.getEmployeeId(),
                    DateTimeUtil.formatDate(request.getStartDate()),
                    DateTimeUtil.formatDate(request.getEndDate()),
                    request.getLeaveType(),
                    request.getReason(),
                    request.getStatus(),
                    DateTimeUtil.formatDate(request.getRequestDate())
                });
                updated = true;
            } else {
                data.add(new String[]{
                    req.getRequestId(),
                    req.getEmployeeId(),
                    DateTimeUtil.formatDate(req.getStartDate()),
                    DateTimeUtil.formatDate(req.getEndDate()),
                    req.getLeaveType(),
                    req.getReason(),
                    req.getStatus(),
                    DateTimeUtil.formatDate(req.getRequestDate())
                });
            }
        }
        
        if (updated) {
            CSVHandler.writeCSV(LEAVE_FILE, HEADERS, data);
        }
        
        return updated;
    }
    
    @Override
    public boolean approveLeaveRequest(String requestId) {
        LeaveRequest request = getLeaveRequestById(requestId);
        if (request != null) {
            request.setStatus("Approved");
            return updateLeaveRequest(request);
        }
        return false;
    }
    
    @Override
    public boolean declineLeaveRequest(String requestId) {
        LeaveRequest request = getLeaveRequestById(requestId);
        if (request != null) {
            request.setStatus("Declined");
            return updateLeaveRequest(request);
        }
        return false;
    }
}
