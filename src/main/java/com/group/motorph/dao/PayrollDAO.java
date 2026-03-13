package com.group.motorph.dao;

import java.util.List;

import com.group.motorph.model.PayrollRecord;

/**
 * Data access contract for payroll records. Implementations handle reading and
 * writing payslip data to the payroll data store.
 */
public interface PayrollDAO {

    List<PayrollRecord> getAllPayrollRecords();

    List<PayrollRecord> getPayrollByEmployeeId(String employeeId);

    PayrollRecord getPayrollByEmployeeAndPeriod(String employeeId, int month, int year);

    boolean addPayrollRecord(PayrollRecord record);

    boolean updatePayrollRecord(PayrollRecord record);
}
