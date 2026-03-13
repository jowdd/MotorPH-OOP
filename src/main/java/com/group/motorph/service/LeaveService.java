package com.group.motorph.service;

import java.time.LocalDate;
import java.util.List;

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

    // Get all leave requests
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestDAO.getAllLeaveRequests();
    }

    // Get pending leave requests (for HR approval)
    public List<LeaveRequest> getPendingLeaveRequests() {
        return leaveRequestDAO.getPendingLeaveRequests();
    }

    // Get leave requests by employee ID
    public List<LeaveRequest> getLeaveRequestsByEmployee(String employeeId) {
        return leaveRequestDAO.getLeaveRequestsByEmployeeId(employeeId);
    }

    /**
     * Creates and persists a new leave request.
     *
     * Rules enforced here:
     * - start date cannot be after end date
     * - start date cannot be in the past
     * - request IDs are generated from the current date, employee ID, and a short timestamp suffix so the CSV layer has a unique key
     * - all new requests start in Pending status and use today's date as the request submission date
     */
    public boolean submitLeaveRequest(String employeeId, LocalDate startDate,
            LocalDate endDate, String leaveType, String reason) {
        // Validate dates
        if (startDate.isAfter(endDate)) {
            return false;
        }
        if (startDate.isBefore(LocalDate.now())) {
            return false;
        }

        // Create leave request
        LocalDate today = LocalDate.now();
        LeaveRequest request = new LeaveRequest();
        request.setRequestId(String.format("%02d-%02d-%d-%s-%d",
                today.getMonthValue(), today.getDayOfMonth(), today.getYear(),
                employeeId, System.currentTimeMillis() % 1_000_000));
        request.setEmployeeId(employeeId);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setLeaveType(leaveType);
        request.setReason(reason);
        request.setStatus("Pending");
        request.setRequestDate(today);

        return leaveRequestDAO.addLeaveRequest(request);
    }

    // Approve leave request (HR only)
    public boolean approveLeaveRequest(String requestId) {
        return leaveRequestDAO.approveLeaveRequest(requestId);
    }

    // Decline leave request (HR only)
    public boolean declineLeaveRequest(String requestId) {
        return leaveRequestDAO.declineLeaveRequest(requestId);
    }

    // Delete (remove) a pending leave request (employee action)
    public boolean deleteLeaveRequest(String requestId) {
        return leaveRequestDAO.deleteLeaveRequest(requestId);
    }

    // Get leave request by ID
    public LeaveRequest getLeaveRequestById(String requestId) {
        return leaveRequestDAO.getLeaveRequestById(requestId);
    }
}
