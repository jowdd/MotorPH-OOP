package com.group.motorph.dao;

import java.util.List;

import com.group.motorph.model.LeaveRequest;

/**
 * Data access contract for leave requests. Implementations handle reading,
 * writing, and status changes in the leave data store.
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

    boolean deleteLeaveRequest(String requestId);
}
