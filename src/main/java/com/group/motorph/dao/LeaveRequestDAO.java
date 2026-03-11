package com.group.motorph.dao;

import java.util.List;

import com.group.motorph.model.LeaveRequest;

/**
 * Interface for Leave Request data access operations
 * Demonstrates ABSTRACTION
 */
public interface LeaveRequestDAO {
    List<LeaveRequest> getAllLeaveRequests();
    List<LeaveRequest> getPendingLeaveRequests();
    List<LeaveRequest> getLeaveRequestsByEmployeeId(String employeeId);
    LeaveRequest getLeaveRequestById(String requestId);
    boolean addLeaveRequest(LeaveRequest request);
    boolean updateLeaveRequest(LeaveRequest request);
    boolean approveLeaveRequest(String requestId);
    boolean declineLeaveRequest(String requestId);
}
