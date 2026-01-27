package com.group.motorph.model;

import java.util.List;

/**
 * Facade for payroll calculation operations.
 *
 * This class provides a simplified interface for performing payroll-related calculations.
 * It delegates actual computation to {@link PayrollCalculationService}, ensuring that
 * business logic remains centralized and maintainable.
 * 
 * Responsibilities:
 * - Calculate gross pay from time logs
 * - Compute statutory contributions (SSS, PhilHealth, Pag-IBIG)
 * - Compute withholding tax
 * - Provide complete payroll breakdowns
 * 
 */
public class PayrollCalculator {

    /** Path to the SSS contribution table file (TSV) for reference calculations. */
    private static final String USER_DIR = System.getProperty("user.dir");
    private static final String SSS_TABLE_PATH = USER_DIR + "/src/main/java/com/group/motorph/Resources/sss-contribution-table.tsv";
    
    /**
     * Calculates gross pay (before allowances) from time logs.
     */
    public double calculateGrossPay(Employee employee, List<TimeLog> timeLogs) {
        return PayrollCalculationService.calculateGrossPay(employee, timeLogs);
    }

    /**
     * Calculates the gross pay for an employee based on their time logs.
     */
    public double calculateSSSContribution(double grossPay) {
        return PayrollCalculationService.calculateSSSContribution(grossPay, SSS_TABLE_PATH);
    }

    /**
     * Calculates PhilHealth contribution.
     */
    public double calculatePhilhealthContribution(double grossPay) {
        return PayrollCalculationService.calculatePhilhealthContribution(grossPay);
    }

    /**
     * Calculates Pag-IBIG contribution.
     */
    public double calculatePagibigContribution(double grossPay) {
        return PayrollCalculationService.calculatePagibigContribution(grossPay);
    }

    /**
     * Calculates withholding tax.
     */
    public double calculateWithholdingTax(double taxableIncome) {
        return PayrollCalculationService.calculateWithholdingTax(taxableIncome);
    }

    /**
     * Calculates complete payroll breakdown.
     */
    public SalaryCalculationResult calculateCompletePayroll(Employee employee, List<TimeLog> timeLogs) {
        return PayrollCalculationService.calculateCompletePayroll(employee, timeLogs);
    }
}
