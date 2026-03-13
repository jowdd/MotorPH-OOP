package com.group.motorph.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.group.motorph.dao.AttendanceDAO;
import com.group.motorph.dao.EmployeeDAO;
import com.group.motorph.dao.PayrollDAO;
import com.group.motorph.dao.impl.AttendanceDAOImpl;
import com.group.motorph.dao.impl.EmployeeDAOImpl;
import com.group.motorph.dao.impl.PayrollDAOImpl;
import com.group.motorph.model.AttendanceRecord;
import com.group.motorph.model.Employee;
import com.group.motorph.model.PayrollRecord;

/**
 * Handles payroll calculation and retrieval. Contains all business logic for
 * hours worked, overtime, and statutory deductions.
 */
public class PayrollService {

    private final PayrollDAO payrollDAO;
    private final AttendanceDAO attendanceDAO;
    private final EmployeeDAO employeeDAO;

    // Standard MotorPH business hours
    private static final LocalTime BUSINESS_START = LocalTime.of(8, 0);   // 8:00 AM
    private static final LocalTime BUSINESS_END = LocalTime.of(17, 0);  // 5:00 PM
    private static final int GRACE_PERIOD_MINS = 10;

    public PayrollService() {
        this.payrollDAO = new PayrollDAOImpl();
        this.attendanceDAO = new AttendanceDAOImpl();
        this.employeeDAO = new EmployeeDAOImpl();
    }

    // Check if employee is late (more than 10 minutes after 8:00 AM)
    private boolean isLate(LocalTime clockIn) {
        return clockIn.isAfter(BUSINESS_START.plusMinutes(GRACE_PERIOD_MINS));
    }

    /**
     * Calculates payable regular hours for one attendance record.
     *
     * Business rule: regular time is always capped to the standard work window
     * of 8:00 AM to 5:00 PM. Employees who arrive within the grace period are
     * still credited from {@code BUSINESS_START}; otherwise counting begins at
     * their actual clock-in time.
     */
    private double calculateHoursWorked(AttendanceRecord record, boolean isLate) {
        LocalTime start = isLate ? record.getClockIn() : BUSINESS_START;
        LocalTime end = record.getClockOut().isBefore(BUSINESS_END) ? record.getClockOut() : BUSINESS_END;
        return Math.max(0, ChronoUnit.MINUTES.between(start, end) / 60.0);
    }

    /**
     * Calculate overtime hours for an attendance record. Overtime is always
     * counted even if the employee was late.
     */
    private double calculateOvertimeHours(AttendanceRecord record) {
        if (!record.getClockOut().isAfter(BUSINESS_END)) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(BUSINESS_END, record.getClockOut()) / 60.0;
    }

    // Calculate SSS contribution
    private double calculateSSS(double grossPay) {
        if (grossPay < 3250) {
            return 135.00;
        } else if (grossPay < 3750) {
            return 157.50;
        } else if (grossPay < 4250) {
            return 180.00;
        } else if (grossPay < 4750) {
            return 202.50;
        } else if (grossPay < 5250) {
            return 225.00;
        } else if (grossPay < 5750) {
            return 247.50;
        } else if (grossPay < 6250) {
            return 270.00;
        } else if (grossPay < 6750) {
            return 292.50;
        } else if (grossPay < 7250) {
            return 315.00;
        } else if (grossPay < 7750) {
            return 337.50;
        } else if (grossPay < 8250) {
            return 360.00;
        } else if (grossPay < 8750) {
            return 382.50;
        } else if (grossPay < 9250) {
            return 405.00;
        } else if (grossPay < 9750) {
            return 427.50;
        } else if (grossPay < 10250) {
            return 450.00;
        } else if (grossPay < 10750) {
            return 472.50;
        } else if (grossPay < 11250) {
            return 495.00;
        } else if (grossPay < 11750) {
            return 517.50;
        } else if (grossPay < 12250) {
            return 540.00;
        } else if (grossPay < 12750) {
            return 562.50;
        } else if (grossPay < 13250) {
            return 585.00;
        } else if (grossPay < 13750) {
            return 607.50;
        } else if (grossPay < 14250) {
            return 630.00;
        } else if (grossPay < 14750) {
            return 652.50;
        } else if (grossPay < 15250) {
            return 675.00;
        } else if (grossPay < 15750) {
            return 697.50;
        } else if (grossPay < 16250) {
            return 720.00;
        } else if (grossPay < 16750) {
            return 742.50;
        } else if (grossPay < 17250) {
            return 765.00;
        } else if (grossPay < 17750) {
            return 787.50;
        } else if (grossPay < 18250) {
            return 810.00;
        } else if (grossPay < 18750) {
            return 832.50;
        } else if (grossPay < 19250) {
            return 855.00;
        } else if (grossPay < 19750) {
            return 877.50;
        } else if (grossPay < 20250) {
            return 900.00;
        } else if (grossPay < 20750) {
            return 922.50;
        } else if (grossPay < 21250) {
            return 945.00;
        } else if (grossPay < 21750) {
            return 967.50;
        } else if (grossPay < 22250) {
            return 990.00;
        } else if (grossPay < 22750) {
            return 1012.50;
        } else if (grossPay < 23250) {
            return 1035.00;
        } else if (grossPay < 23750) {
            return 1057.50;
        } else if (grossPay < 24250) {
            return 1080.00;
        } else if (grossPay < 24750) {
            return 1102.50;
        } else {
            return 1125.00;
        }
    }

    // Employee share: 50% of 3% premium, capped at 1,800 monthly premium
    private double calculatePhilHealth(double grossPay) {
        double salaryBase = Math.max(grossPay, 10000.00); // minimum salary bracket
        double premium = salaryBase * 0.03;               // 3% premium
        premium = Math.min(premium, 1800.00);             // cap at 1,800

        return premium / 2; // employee share (50%)
    }

    // 1% for monthly wages <= PHP 1,500; 2% above that.
    private double calculatePagIbig(double grossPay) {
        if (grossPay >= 1000 && grossPay <= 1500) {
            return grossPay * 0.01; // 1%
        } else if (grossPay > 1500) {
            return grossPay * 0.02; // 2%
        }
        return 0;
    }

    /**
     * Calculates withholding tax from taxable income using the project's
     * hard-coded bracket table.
     *
     * Taxable income here means gross pay less SSS, PhilHealth, and Pag-IBIG.
     * The thresholds are encoded directly in code so the service can compute a
     * monthly payslip without depending on an external tax table file.
     */
    private double calculateWithholdingTax(double grossPay, double sss, double philHealth, double pagIbig) {
        
        double taxable = grossPay - sss - philHealth - pagIbig;

        if (taxable <= 20832) {
            return 0;
        } 
        else if (taxable < 33333) {
            return (taxable - 20833) * 0.20;
        } 
        else if (taxable < 66667) {
            return 2500 + (taxable - 33333) * 0.25;
        } 
        else if (taxable < 166667) {
            return 10833 + (taxable - 66667) * 0.30;
        } 
        else if (taxable < 666667) {
            return 40833.33 + (taxable - 166667) * 0.32;
        } 
        else {
            return 200833.33 + (taxable - 666667) * 0.35;
        }
    }

    /**
     * Processes payroll for one attendance period.
     *
     * Workflow summary:
     * 1. Load existing payroll records and remember which employees were
     *    already processed for the same month/year.
     * 2. Load only attendance rows whose status is {@code Approved}.
     * 3. Group those rows per employee, compute one payroll record per group,
     *    and persist only the missing ones.
     * 4. Promote attendance rows from {@code Approved} to {@code Processed}
     *    only when at least one payroll record was actually generated.
     *
     * This makes the method safe to re-run for the same period without
     * duplicating payslips.
     *
     * @return number of payroll records generated
     */
    public int processPayrollForMonth(int month, int year) {
        Set<String> processed = new HashSet<>();
        for (PayrollRecord r : payrollDAO.getAllPayrollRecords()) {
            if (r.getMonth() == month && r.getYear() == year) {
                processed.add(r.getEmployeeId());
            }
        }

        Map<String, List<AttendanceRecord>> byEmp = new HashMap<>();
        for (AttendanceRecord r : attendanceDAO.getApprovedByMonth(month, year)) {
            byEmp.computeIfAbsent(r.getEmployeeId(), k -> new ArrayList<>()).add(r);
        }

        int count = 0;
        for (Map.Entry<String, List<AttendanceRecord>> entry : byEmp.entrySet()) {
            String empId = entry.getKey();
            if (processed.contains(empId)) {
                continue;
            }
            PayrollRecord rec = calculatePayrollFromRecords(empId, month, year, entry.getValue());
            if (rec != null) {
                payrollDAO.addPayrollRecord(rec);
                count++;
            }
        }

        if (count > 0) {
            attendanceDAO.markAttendanceProcessed(month, year);
        }

        return count;
    }

    // Get all payslips for one employee.
    public List<PayrollRecord> getPayrollByEmployee(String employeeId) {
        return payrollDAO.getPayrollByEmployeeId(employeeId);
    }

    // Get every payroll record across all employees and periods.
    public List<PayrollRecord> getAllPayrollRecords() {
        return payrollDAO.getAllPayrollRecords();
    }

    /**
     * Builds a single payroll record from a pre-filtered list of approved
     * attendance rows for one employee and one payroll period.
     *
     * The method intentionally centralizes all payroll math in one place:
     * paid regular hours, overtime premium, statutory deductions, generated
     * payslip ID, and final net pay.
     */
    private PayrollRecord calculatePayrollFromRecords(
            String employeeId, int month, int year,
            List<AttendanceRecord> attendanceRecords) {

        Employee employee = employeeDAO.getEmployeeById(employeeId);
        if (employee == null) {
            return null;
        }

        double totalHours = 0;
        double overtimeHours = 0;

        for (AttendanceRecord record : attendanceRecords) {
            boolean late = isLate(record.getClockIn());
            totalHours += calculateHoursWorked(record, late);
            overtimeHours += calculateOvertimeHours(record);
        }

        LocalDate today = LocalDate.now();
        PayrollRecord payroll = new PayrollRecord();
        payroll.setPayslipId(String.format("%02d-%d-%02d-%02d-%s",
                month, year, today.getMonthValue(), today.getDayOfMonth(), employeeId));
        payroll.setEmployeeId(employeeId);
        payroll.setMonth(month);
        payroll.setYear(year);
        payroll.setHoursWorked(totalHours);
        payroll.setOvertimeHours(overtimeHours);

        double regularPay = totalHours * employee.getHourlyRate();
        double overtimePay = overtimeHours * employee.getHourlyRate() * 1.25; // 25% OT premium
        double grossPay = regularPay + overtimePay;
        payroll.setGrossPay(grossPay);

        double sss = calculateSSS(grossPay);
        double philHealth = calculatePhilHealth(grossPay);
        double pagIbig = calculatePagIbig(grossPay);
        double withholdingTax = calculateWithholdingTax(grossPay, sss, philHealth, pagIbig);

        payroll.setSss(sss);
        payroll.setPhilHealth(philHealth);
        payroll.setPagIbig(pagIbig);
        payroll.setWithholdingTax(withholdingTax);

        double totalDeductions = sss + philHealth + pagIbig + withholdingTax;
        payroll.setTotalDeductions(totalDeductions);
        payroll.setNetPay(grossPay - totalDeductions);
        payroll.setGeneratedDate(today);

        return payroll;
    }
}
