package com.group.motorph.dao;

import java.util.List;

import com.group.motorph.model.PayrollRecord;

/**
 * Interface for Payroll data access operations
 * Demonstrates ABSTRACTION
 */
public interface PayrollDAO {
    List<PayrollRecord> getAllPayrollRecords();
    List<PayrollRecord> getPayrollByEmployeeId(String employeeId);
    PayrollRecord getPayrollByEmployeeAndPeriod(String employeeId, int month, int year);
    boolean addPayrollRecord(PayrollRecord record);
    boolean updatePayrollRecord(PayrollRecord record);
}
