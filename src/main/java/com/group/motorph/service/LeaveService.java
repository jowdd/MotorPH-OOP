package com.group.motorph.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.group.motorph.dao.LeaveRequestDAO;
import com.group.motorph.dao.impl.LeaveRequestDAOImpl;
import com.group.motorph.model.LeaveRequest;

/**
 * Service class for leave request management
 */
public class LeaveService {
    
    private final LeaveRequestDAO leaveRequestDAO;
    
    public LeaveService() {
        this.leaveRequestDAO = new LeaveRequestDAOImpl();
    }
    
    /**
     * Get all leave requests
     */
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestDAO.getAllLeaveRequests();
    }
    
    /**
     * Get pending leave requests (for HR approval)
     */
    public List<LeaveRequest> getPendingLeaveRequests() {
        return leaveRequestDAO.getPendingLeaveRequests();
    }
    
    /**
     * Get leave requests by employee ID
     */
    public List<LeaveRequest> getLeaveRequestsByEmployee(String employeeId) {
        return leaveRequestDAO.getLeaveRequestsByEmployeeId(employeeId);
    }
    
    /**
     * Submit a new leave request
     */
    public boolean submitLeaveRequest(String employeeId, LocalDate startDate, 
                                     LocalDate endDate, String leaveType, String reason) {
        // Validate dates
        if (startDate.isAfter(endDate)) {
            System.err.println("Start date must be before or equal to end date");
            return false;
        }
        
        if (startDate.isBefore(LocalDate.now())) {
            System.err.println("Cannot request leave for past dates");
            return false;
        }
        
        // Create leave request
        LeaveRequest request = new LeaveRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setEmployeeId(employeeId);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setLeaveType(leaveType);
        request.setReason(reason);
        request.setStatus("Pending");
        request.setRequestDate(LocalDate.now());
        
        return leaveRequestDAO.addLeaveRequest(request);
    }
    
    /**
     * Approve leave request (HR only)
     */
    public boolean approveLeaveRequest(String requestId) {
        return leaveRequestDAO.approveLeaveRequest(requestId);
    }
    
    /**
     * Decline leave request (HR only)
     */
    public boolean declineLeaveRequest(String requestId) {
        return leaveRequestDAO.declineLeaveRequest(requestId);
    }
    
    /**
     * Get leave request by ID
     */
    public LeaveRequest getLeaveRequestById(String requestId) {
        return leaveRequestDAO.getLeaveRequestById(requestId);
    }
}
