package com.group.motorph.service;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import com.group.motorph.dao.ApprovedAttendanceDAO;
import com.group.motorph.dao.AttendanceDAO;
import com.group.motorph.dao.EmployeeDAO;
import com.group.motorph.dao.PayrollDAO;
import com.group.motorph.dao.impl.ApprovedAttendanceDAOImpl;
import com.group.motorph.dao.impl.AttendanceDAOImpl;
import com.group.motorph.dao.impl.EmployeeDAOImpl;
import com.group.motorph.dao.impl.PayrollDAOImpl;
import com.group.motorph.model.AttendanceRecord;
import com.group.motorph.model.Employee;
import com.group.motorph.model.PayrollRecord;

/**
 * Service class for payroll calculations and management
 * Contains business logic for calculating salaries, deductions, and taxes
 */
public class PayrollService {
    
    private final PayrollDAO payrollDAO;
    private final AttendanceDAO attendanceDAO;
    private final ApprovedAttendanceDAO approvedAttendanceDAO;
    private final EmployeeDAO employeeDAO;
    
    // Business hours constants
    private static final LocalTime BUSINESS_START = LocalTime.of(8, 0);  // 8:00 AM
    private static final LocalTime BUSINESS_END = LocalTime.of(17, 0);   // 5:00 PM
    private static final int GRACE_PERIOD_MINUTES = 10;
    
    public PayrollService() {
        this.payrollDAO = new PayrollDAOImpl();
        this.attendanceDAO = new AttendanceDAOImpl();
        this.approvedAttendanceDAO = new ApprovedAttendanceDAOImpl();
        this.employeeDAO = new EmployeeDAOImpl();
    }
    
    /**
     * Calculate payroll for a specific employee for a given month and year
     */
    public PayrollRecord calculatePayroll(String employeeId, int month, int year) {
        Employee employee = employeeDAO.getEmployeeById(employeeId);
        if (employee == null) {
            return null;
        }
        
        // Get attendance records for the month
        List<AttendanceRecord> attendanceRecords = 
            attendanceDAO.getAttendanceByEmployeeAndMonth(employeeId, month, year);
        
        // Calculate total hours worked and overtime
        double totalHoursWorked = 0;
        double totalOvertimeHours = 0;
        
        for (AttendanceRecord record : attendanceRecords) {
            boolean isLate = isLate(record.getClockIn());
            double hoursWorked = calculateHoursWorked(record, isLate);
            double overtimeHours = calculateOvertimeHours(record, isLate);
            
            totalHoursWorked += hoursWorked;
            totalOvertimeHours += overtimeHours;
        }
        
        // Create payroll record
        PayrollRecord payroll = new PayrollRecord();
        payroll.setPayslipId(UUID.randomUUID().toString());
        payroll.setEmployeeId(employeeId);
        payroll.setMonth(month);
        payroll.setYear(year);
        payroll.setHoursWorked(totalHoursWorked);
        payroll.setOvertimeHours(totalOvertimeHours);
        
        // Calculate gross pay
        double regularPay = totalHoursWorked * employee.getHourlyRate();
        double overtimePay = totalOvertimeHours * employee.getHourlyRate() * 1.25; // 25% overtime premium
        double grossPay = regularPay + overtimePay;
        payroll.setGrossPay(grossPay);
        
        // Calculate deductions
        double sss = calculateSSS(grossPay);
        double philHealth = calculatePhilHealth(grossPay);
        double pagIbig = calculatePagIbig(grossPay);
        double withholdingTax = calculateWithholdingTax(grossPay, sss, philHealth, pagIbig);
        
        payroll.setSss(sss);
        payroll.setPhilHealth(philHealth);
        payroll.setPagIbig(pagIbig);
        payroll.setWithholdingTax(withholdingTax);
        
        // Calculate total deductions and net pay
        double totalDeductions = sss + philHealth + pagIbig + withholdingTax;
        payroll.setTotalDeductions(totalDeductions);
        payroll.setNetPay(grossPay - totalDeductions);
        
        return payroll;
    }
    
    /**
     * Check if employee is late (more than 10 minutes after 8:00 AM)
     */
    private boolean isLate(LocalTime clockIn) {
        LocalTime graceEndTime = BUSINESS_START.plusMinutes(GRACE_PERIOD_MINUTES);
        return clockIn.isAfter(graceEndTime);
    }
    
    /**
     * Calculate hours worked for an attendance record
     */
    private double calculateHoursWorked(AttendanceRecord record, boolean isLate) {
        LocalTime effectiveStart;
        
        if (isLate) {
            // If late, count from actual clock-in time
            effectiveStart = record.getClockIn();
        } else {
            // If not late, count from business start time
            effectiveStart = BUSINESS_START;
        }
        
        // Count until business end time or clock-out time, whichever is earlier
        LocalTime effectiveEnd = record.getClockOut().isBefore(BUSINESS_END) ? 
                                 record.getClockOut() : BUSINESS_END;
        
        long minutes = ChronoUnit.MINUTES.between(effectiveStart, effectiveEnd);
        return Math.max(0, minutes / 60.0);
    }
    
    /**
     * Calculate overtime hours for an attendance record
     */
    private double calculateOvertimeHours(AttendanceRecord record, boolean isLate) {
        // No overtime if employee was late
        if (isLate) {
            return 0;
        }
        
        // Overtime is only counted after 5:00 PM
        if (record.getClockOut().isAfter(BUSINESS_END)) {
            long minutes = ChronoUnit.MINUTES.between(BUSINESS_END, record.getClockOut());
            return minutes / 60.0;
        }
        
        return 0;
    }
    
    /**
     * Calculate SSS contribution (simplified calculation)
     */
    private double calculateSSS(double grossPay) {
        if (grossPay <= 4250) return 180.00;
        if (grossPay <= 4750) return 202.50;
        if (grossPay <= 5250) return 225.00;
        if (grossPay <= 5750) return 247.50;
        if (grossPay <= 6250) return 270.00;
        if (grossPay <= 6750) return 292.50;
        if (grossPay <= 7250) return 315.00;
        if (grossPay <= 7750) return 337.50;
        if (grossPay <= 8250) return 360.00;
        if (grossPay <= 8750) return 382.50;
        if (grossPay <= 9250) return 405.00;
        if (grossPay <= 9750) return 427.50;
        if (grossPay <= 10250) return 450.00;
        if (grossPay <= 10750) return 472.50;
        if (grossPay <= 11250) return 495.00;
        if (grossPay <= 11750) return 517.50;
        if (grossPay <= 12250) return 540.00;
        if (grossPay <= 12750) return 562.50;
        if (grossPay <= 13250) return 585.00;
        if (grossPay <= 13750) return 607.50;
        if (grossPay <= 14250) return 630.00;
        if (grossPay <= 14750) return 652.50;
        if (grossPay <= 15250) return 675.00;
        if (grossPay <= 15750) return 697.50;
        if (grossPay <= 16250) return 720.00;
        if (grossPay <= 16750) return 742.50;
        if (grossPay <= 17250) return 765.00;
        if (grossPay <= 17750) return 787.50;
        if (grossPay <= 18250) return 810.00;
        if (grossPay <= 18750) return 832.50;
        if (grossPay <= 19250) return 855.00;
        if (grossPay <= 19750) return 877.50;
        return 900.00;  // Maximum
    }
    
    /**
     * Calculate PhilHealth contribution (simplified calculation)
     */
    private double calculatePhilHealth(double grossPay) {
        double rate = 0.05; // 5% of gross pay
        double contribution = grossPay * rate / 2; // Employee share is half
        double maxContribution = 5000.00;
        return Math.min(contribution, maxContribution);
    }
    
    /**
     * Calculate Pag-IBIG contribution
     */
    private double calculatePagIbig(double grossPay) {
        if (grossPay <= 1500) {
            return grossPay * 0.01; // 1%
        } else {
            return grossPay * 0.02; // 2%
        }
    }
    
    /**
     * Calculate withholding tax (simplified calculation)
     */
    private double calculateWithholdingTax(double grossPay, double sss, double philHealth, double pagIbig) {
        double taxableIncome = grossPay - sss - philHealth - pagIbig;
        
        // Simplified tax brackets
        if (taxableIncome <= 20833) return 0;
        if (taxableIncome <= 33332) return (taxableIncome - 20833) * 0.15;
        if (taxableIncome <= 66666) return 1875 + (taxableIncome - 33332) * 0.20;
        if (taxableIncome <= 166666) return 8541.80 + (taxableIncome - 66666) * 0.25;
        if (taxableIncome <= 666666) return 33541.80 + (taxableIncome - 166666) * 0.30;
        return 183541.80 + (taxableIncome - 666666) * 0.35;
    }
    
    /**
     * Process payroll for all employees for the given month/year.
     * Uses the approved-attendance-logs.csv as the source of truth.
     * Skips employees whose payroll for the period already exists.
     *
     * @return number of payroll records generated
     */
    public int processPayrollForMonth(int month, int year) {
        List<PayrollRecord> existing = payrollDAO.getAllPayrollRecords();
        // Build a set of empId+month+year already processed
        java.util.Set<String> processed = new java.util.HashSet<>();
        for (PayrollRecord r : existing) {
            if (r.getMonth() == month && r.getYear() == year) {
                processed.add(r.getEmployeeId());
            }
        }

        List<AttendanceRecord> approved = approvedAttendanceDAO.getApprovedByMonth(month, year);
        java.util.Map<String, java.util.List<AttendanceRecord>> byEmp = new java.util.HashMap<>();
        for (AttendanceRecord r : approved) {
            byEmp.computeIfAbsent(r.getEmployeeId(), k -> new java.util.ArrayList<>()).add(r);
        }

        int count = 0;
        for (java.util.Map.Entry<String, java.util.List<AttendanceRecord>> entry : byEmp.entrySet()) {
            String empId = entry.getKey();
            if (processed.contains(empId)) continue; // already calculated
            PayrollRecord rec = calculatePayrollFromRecords(empId, month, year, entry.getValue());
            if (rec != null) {
                payrollDAO.addPayrollRecord(rec);
                count++;
            }
        }
        return count;
    }

    /**
     * Save payroll record
     */
    public boolean savePayrollRecord(PayrollRecord record) {
        return payrollDAO.addPayrollRecord(record);
    }
    
    /**
     * Get payroll records by employee ID
     */
    public List<PayrollRecord> getPayrollByEmployee(String employeeId) {
        return payrollDAO.getPayrollByEmployeeId(employeeId);
    }
    
    /**
     * Get payroll for specific employee and period
     */
    public PayrollRecord getPayrollByEmployeeAndPeriod(String employeeId, int month, int year) {
        return payrollDAO.getPayrollByEmployeeAndPeriod(employeeId, month, year);
    }
    
    /**
     * Get all payroll records
     */
    public List<PayrollRecord> getAllPayrollRecords() {
        return payrollDAO.getAllPayrollRecords();
    }
    
    /**
     * Calculate payroll for all employees by batch (Finance role)
     * @param month month number (1-12)
     * @param year year
     * @return list of payroll records for all employees
     */
    
    /**
     * Finance: Calculate payroll batch for a period using ONLY the rotating approved attendance file.
     * This aligns with the requirement that payroll processing references approved-attendance-logs.csv.
     */
    public List<PayrollRecord> calculatePayrollBatchFromApproved(int month, int year) {
        List<Employee> allEmployees = employeeDAO.getAllEmployees();
        List<AttendanceRecord> approved = approvedAttendanceDAO.getApprovedByMonth(month, year);

        java.util.Map<String, java.util.List<AttendanceRecord>> byEmp = new java.util.HashMap<>();
        for (AttendanceRecord r : approved) {
            byEmp.computeIfAbsent(r.getEmployeeId(), k -> new java.util.ArrayList<>()).add(r);
        }

        List<PayrollRecord> payrollRecords = new java.util.ArrayList<>();
        for (Employee employee : allEmployees) {
            java.util.List<AttendanceRecord> records = byEmp.getOrDefault(employee.getEmployeeId(), new java.util.ArrayList<>());
            PayrollRecord payroll = calculatePayrollFromRecords(employee.getEmployeeId(), month, year, records);
            if (payroll != null) {
                payrollRecords.add(payroll);
                payrollDAO.addPayrollRecord(payroll);
            }
        }
        return payrollRecords;
    }

    private PayrollRecord calculatePayrollFromRecords(String employeeId, int month, int year, List<AttendanceRecord> attendanceRecords) {
        Employee employee = employeeDAO.getEmployeeById(employeeId);
        if (employee == null) return null;

        double totalHoursWorked = 0;
        double totalOvertimeHours = 0;

        for (AttendanceRecord record : attendanceRecords) {
            boolean isLate = isLate(record.getClockIn());
            double hoursWorked = calculateHoursWorked(record, isLate);
            double overtimeHours = calculateOvertimeHours(record, isLate);

            totalHoursWorked += hoursWorked;
            totalOvertimeHours += overtimeHours;
        }

        PayrollRecord payroll = new PayrollRecord();
        payroll.setPayslipId(UUID.randomUUID().toString());
        payroll.setEmployeeId(employeeId);
        payroll.setMonth(month);
        payroll.setYear(year);
        payroll.setHoursWorked(totalHoursWorked);
        payroll.setOvertimeHours(totalOvertimeHours);

        double regularPay = totalHoursWorked * employee.getHourlyRate();
        double overtimePay = totalOvertimeHours * employee.getHourlyRate() * 1.25;
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

        payroll.setGeneratedDate(java.time.LocalDate.now());
        return payroll;
    }

public List<PayrollRecord> calculatePayrollBatch(int month, int year) {
        List<Employee> allEmployees = employeeDAO.getAllEmployees();
        List<PayrollRecord> payrollRecords = new java.util.ArrayList<>();
        
        for (Employee employee : allEmployees) {
            PayrollRecord payroll = calculatePayroll(employee.getEmployeeId(), month, year);
            if (payroll != null) {
                payrollRecords.add(payroll);
                // Save the payroll record
                payrollDAO.addPayrollRecord(payroll);
            }
        }
        
        return payrollRecords;
    }
    
    /**
     * Calculate payroll for a single employee by name (Finance role)
     * @param employeeName first or last name to search
     * @param month month number (1-12)
     * @param year year
     * @return payroll record if employee found, null otherwise
     */
    public PayrollRecord calculatePayrollByName(String employeeName, int month, int year) {
        List<Employee> allEmployees = employeeDAO.getAllEmployees();
        
        for (Employee employee : allEmployees) {
            String fullName = employee.getFullName().toLowerCase();
            String firstName = employee.getFirstName().toLowerCase();
            String lastName = employee.getLastName().toLowerCase();
            String searchName = employeeName.toLowerCase();
            
            if (fullName.contains(searchName) || 
                firstName.contains(searchName) || 
                lastName.contains(searchName)) {
                PayrollRecord payroll = calculatePayroll(employee.getEmployeeId(), month, year);
                if (payroll != null) {
                    payrollDAO.addPayrollRecord(payroll);
                }
                return payroll;
            }
        }
        
        return null;
    }
    
    /**
     * Generate Tax Report for all employees (Finance role)
     * @param month month number (1-12)
     * @param year year
     * @return formatted tax report string
     */
    public String generateTaxReport(int month, int year) {
        List<PayrollRecord> payrollRecords = payrollDAO.getAllPayrollRecords();
        
        StringBuilder report = new StringBuilder();
        report.append("=".repeat(80)).append("\n");
        report.append("                        TAX REPORT\n");
        report.append("                    Period: ").append(month).append("/").append(year).append("\n");
        report.append("=".repeat(80)).append("\n\n");
        report.append(String.format("%-15s %-30s %15s %15s %15s\n", 
            "Employee ID", "Name", "Gross Pay", "Withholding Tax", "Net Pay"));
        report.append("-".repeat(80)).append("\n");
        
        double totalGrossPay = 0;
        double totalTax = 0;
        double totalNetPay = 0;
        int count = 0;
        
        for (PayrollRecord payroll : payrollRecords) {
            if (payroll.getMonth() == month && payroll.getYear() == year) {
                Employee employee = employeeDAO.getEmployeeById(payroll.getEmployeeId());
                if (employee != null) {
                    report.append(String.format("%-15s %-30s %,15.2f %,15.2f %,15.2f\n",
                        payroll.getEmployeeId(),
                        employee.getFullName(),
                        payroll.getGrossPay(),
                        payroll.getWithholdingTax(),
                        payroll.getNetPay()));
                    
                    totalGrossPay += payroll.getGrossPay();
                    totalTax += payroll.getWithholdingTax();
                    totalNetPay += payroll.getNetPay();
                    count++;
                }
            }
        }
        
        report.append("-".repeat(80)).append("\n");
        report.append(String.format("%-15s %-30s %,15.2f %,15.2f %,15.2f\n",
            "TOTAL", count + " employees", totalGrossPay, totalTax, totalNetPay));
        report.append("=".repeat(80)).append("\n");
        
        return report.toString();
    }
    
    /**
     * Generate Payroll Report for all employees (Finance role)
     * @param month month number (1-12)
     * @param year year
     * @return formatted payroll report string
     */
    public String generatePayrollReport(int month, int year) {
        List<PayrollRecord> payrollRecords = payrollDAO.getAllPayrollRecords();
        
        StringBuilder report = new StringBuilder();
        report.append("=".repeat(120)).append("\n");
        report.append("                                    PAYROLL REPORT\n");
        report.append("                                Period: ").append(month).append("/").append(year).append("\n");
        report.append("=".repeat(120)).append("\n\n");
        report.append(String.format("%-12s %-25s %10s %10s %10s %10s %10s %10s %12s\n",
            "Employee ID", "Name", "Hours", "Overtime", "Gross Pay", "SSS", "PhilHealth", "Pag-IBIG", "Net Pay"));
        report.append("-".repeat(120)).append("\n");
        
        double totalGrossPay = 0;
        double totalSSS = 0;
        double totalPhilHealth = 0;
        double totalPagIbig = 0;
        double totalDeductions = 0;
        double totalNetPay = 0;
        int count = 0;
        
        for (PayrollRecord payroll : payrollRecords) {
            if (payroll.getMonth() == month && payroll.getYear() == year) {
                Employee employee = employeeDAO.getEmployeeById(payroll.getEmployeeId());
                if (employee != null) {
                    report.append(String.format("%-12s %-25s %,10.2f %,10.2f %,10.2f %,10.2f %,10.2f %,10.2f %,12.2f\n",
                        payroll.getEmployeeId(),
                        employee.getFullName().substring(0, Math.min(25, employee.getFullName().length())),
                        payroll.getHoursWorked(),
                        payroll.getOvertimeHours(),
                        payroll.getGrossPay(),
                        payroll.getSss(),
                        payroll.getPhilHealth(),
                        payroll.getPagIbig(),
                        payroll.getNetPay()));
                    
                    totalGrossPay += payroll.getGrossPay();
                    totalSSS += payroll.getSss();
                    totalPhilHealth += payroll.getPhilHealth();
                    totalPagIbig += payroll.getPagIbig();
                    totalDeductions += payroll.getTotalDeductions();
                    totalNetPay += payroll.getNetPay();
                    count++;
                }
            }
        }
        
        report.append("-".repeat(120)).append("\n");
        report.append(String.format("%-12s %-25s %10s %10s %,10.2f %,10.2f %,10.2f %,10.2f %,12.2f\n",
            "TOTAL", count + " employees", "", "", 
            totalGrossPay, totalSSS, totalPhilHealth, totalPagIbig, totalNetPay));
        report.append("=".repeat(120)).append("\n");
        report.append(String.format("\nTotal Deductions: PHP %,.2f\n", totalDeductions));
        report.append(String.format("Total Net Pay: PHP %,.2f\n", totalNetPay));
        report.append("=".repeat(120)).append("\n");
        
        return report.toString();
    }
}
