
package com.group.motorph.model;
import java.time.LocalDate;

/**
 * Represents a payroll record for an employee for a specific period
 */
public class PayrollRecord {
    private String payslipId;
    private String employeeId;
    private int month;
    private int year;
    private double hoursWorked;
    private double overtimeHours;
    private double grossPay;
    private double sss;
    private double philHealth;
    private double pagIbig;
    private double withholdingTax;
    private double totalDeductions;
    private double netPay;
    private LocalDate generatedDate;
    
    // Constructors
    public PayrollRecord() {
        this.generatedDate = LocalDate.now();
    }
    
    public PayrollRecord(String payslipId, String employeeId, int month, int year) {
        this.payslipId = payslipId;
        this.employeeId = employeeId;
        this.month = month;
        this.year = year;
        this.generatedDate = LocalDate.now();
    }
    
    // Getters and Setters
    public String getPayslipId() {
        return payslipId;
    }
    
    public void setPayslipId(String payslipId) {
        this.payslipId = payslipId;
    }
    
    public String getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
    
    public int getMonth() {
        return month;
    }
    
    public void setMonth(int month) {
        this.month = month;
    }
    
    public int getYear() {
        return year;
    }
    
    public void setYear(int year) {
        this.year = year;
    }
    
    public double getHoursWorked() {
        return hoursWorked;
    }
    
    public void setHoursWorked(double hoursWorked) {
        this.hoursWorked = hoursWorked;
    }
    
    public double getOvertimeHours() {
        return overtimeHours;
    }
    
    public void setOvertimeHours(double overtimeHours) {
        this.overtimeHours = overtimeHours;
    }
    
    public double getGrossPay() {
        return grossPay;
    }
    
    public void setGrossPay(double grossPay) {
        this.grossPay = grossPay;
    }
    
    public double getSss() {
        return sss;
    }
    
    public void setSss(double sss) {
        this.sss = sss;
    }
    
    public double getPhilHealth() {
        return philHealth;
    }
    
    public void setPhilHealth(double philHealth) {
        this.philHealth = philHealth;
    }
    
    public double getPagIbig() {
        return pagIbig;
    }
    
    public void setPagIbig(double pagIbig) {
        this.pagIbig = pagIbig;
    }
    
    public double getWithholdingTax() {
        return withholdingTax;
    }
    
    public void setWithholdingTax(double withholdingTax) {
        this.withholdingTax = withholdingTax;
    }
    
    public double getTotalDeductions() {
        return totalDeductions;
    }
    
    public void setTotalDeductions(double totalDeductions) {
        this.totalDeductions = totalDeductions;
    }
    
    public double getNetPay() {
        return netPay;
    }
    
    public void setNetPay(double netPay) {
        this.netPay = netPay;
    }
    
    public LocalDate getGeneratedDate() {
        return generatedDate;
    }
    
    public void setGeneratedDate(LocalDate generatedDate) {
        this.generatedDate = generatedDate;
    }
    
    @Override
    public String toString() {
        return "PayrollRecord{" +
                "payslipId='" + payslipId + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", month=" + month +
                ", year=" + year +
                ", netPay=" + netPay +
                '}';
    }
}
