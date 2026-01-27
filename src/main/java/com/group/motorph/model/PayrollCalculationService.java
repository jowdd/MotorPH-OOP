package com.group.motorph.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Centralized payroll calculation service.
 * All business rules and computations related to:
 * - Gross pay calculation
 * - Regular and overtime hours
 * - Statutory deductions (SSS, PhilHealth, Pag-IBIG)
 * - Withholding tax
 */
public class PayrollCalculationService {
    
    // Constants for calculations
    private static final double OVERTIME_MULTIPLIER = 1.25;
    private static final java.time.LocalTime STORE_START = java.time.LocalTime.of(8, 0); // 8:00 AM
    private static final java.time.LocalTime STORE_END = java.time.LocalTime.of(17, 0); // 5:00 PM
    private static final java.time.LocalTime GRACE_END = java.time.LocalTime.of(8, 10); // 10-minute grace period
    private static final String USER_DIR = System.getProperty("user.dir");
    private static final String SSS_TABLE_PATH = USER_DIR + "/src/main/java/com/group/motorph/Resources/sss-contribution-table.tsv";
    

    // Private constructor to prevent instantiation
    private PayrollCalculationService() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Calculates the complete payroll breakdown for an employee.
     * This includes:
     * - Gross pay based on worked hours
     * - Government contributions
     * - Withholding tax
     * - Allowances
     *
     * @param employee Employee whose payroll is being calculated
     * @param timeLogs List of time log entries for the payroll period
     * @return {@link SalaryCalculationResult} containing payroll details
     * @throws IllegalArgumentException if employee or timeLogs are null or empty
     */
    public static SalaryCalculationResult calculateCompletePayroll(Employee employee, List<TimeLog> timeLogs) {
        if (employee == null || timeLogs == null || timeLogs.isEmpty()) {
            throw new IllegalArgumentException("Employee and time logs cannot be null or empty");
        }

        // Calculate gross pay from hours worked using new rules
        double grossPay = calculateGrossPay(employee, timeLogs);

        // Calculate all deductions
        double sssContribution = calculateSSSContribution(grossPay, SSS_TABLE_PATH);
        double philhealthContribution = calculatePhilhealthContribution(grossPay);
        double pagibigContribution = calculatePagibigContribution(grossPay);
        
        // Taxable income is gross pay minus contributions
        double taxableIncome = grossPay - sssContribution - philhealthContribution - pagibigContribution;
        double withholdingTax = calculateWithholdingTax(taxableIncome);

        // Get total allowances
        double totalAllowances = employee.getCompensation().getTotalAllowances();

        return new SalaryCalculationResult(
            grossPay, 
            sssContribution, 
            philhealthContribution, 
            pagibigContribution, 
            withholdingTax, 
            totalAllowances
        );
    }

    /**
     * Calculates the gross pay (excluding allowances) based on time logs.
     * Hourly rate is derived from the employee's monthly basic salary
     *
     * @param employee Employee whose salary rate applies
     * @param timeLogs Complete time logs for the period
     * @return Gross pay rounded to two decimal places
     */
    public static double calculateGrossPay(Employee employee, List<TimeLog> timeLogs) {
        double totalPay = 0.0;

        // Derive hourly rate from monthly basic salary. Assumes 160 payable hours per month.
        // Example: P15,000 / 160 hour
        double hourlyRate = employee.getBasicSalary() / 160.0;

        for (TimeLog log : timeLogs) {
            if (log.isComplete()) {
                DayHours dh = calculateDayHours(log);

                double regularPay = dh.regularHours * hourlyRate;
                double overtimePay = dh.overtimeHours * hourlyRate * OVERTIME_MULTIPLIER;

                totalPay += regularPay + overtimePay;
            }
        }

        return roundToTwoDecimals(totalPay);
    }

    /**
     * Computes total paid hours (regular + overtime) across all complete logs.
     *
     * @param timeLogs List of time logs
     * @return Total paid hours rounded to two decimals
     */
    public static double computePaidHours(List<TimeLog> timeLogs) {
        double total = 0.0;
        for (TimeLog log : timeLogs) {
            if (log.isComplete()) {
                DayHours dh = calculateDayHours(log);
                total += (dh.regularHours + dh.overtimeHours);
            }
        }
        return roundToTwoDecimals(total);
    }

    /**
     * Computes total overtime hours across all complete time logs.
     *
     * @param timeLogs List of time logs
     * @return Total overtime hours
     */
    public static double computeOvertimeHours(List<TimeLog> timeLogs) {
        double total = 0.0;
        for (TimeLog log : timeLogs) {
            if (log.isComplete()) {
                DayHours dh = calculateDayHours(log);
                total += dh.overtimeHours;
            }
        }
        return roundToTwoDecimals(total);
    }

    /**
     * Calculates SSS contribution based on gross weekly pay.
     * This uses the SSS contribution table to determine the appropriate contribution amount.
     * 
     * @param grossWeekPay The gross weekly pay amount
     * @param sssFilePath The file path of SSS table
     * @return The calculated SSS contribution amount
     */
    public static double calculateSSSContribution(double grossPay, String sssFilePath) {
        double sss = 0.0;

        try (BufferedReader br = new BufferedReader(new FileReader(sssFilePath))) {
            // Skip header
            br.readLine();

            String line;

            // Process each line in the SSS contribution table
            while ((line = br.readLine()) != null) {

                // Split line by tab character to get individual fields
                String[] fields = line.split("\t");

                // Parse salary range and contribution from the table
                int compensationRangeFrom = parseInt(fields[0]);
                int compensationRangeTo = parseInt(fields[2]);
                double contribution = parseDouble(fields[3]);

                // Check if gross week pay falls within this compensation range
                // Convert monthly ranges to weekly by dividing by 4
                if (grossPay > compensationRangeFrom && grossPay < compensationRangeTo) {

                    // If within range, use this contribution amount (divided by 4 for weekly)
                    sss = contribution;
                    break; // Found the right bracket, stop searching
                }
            }

            // Special case: If pay exceeds maximum SSS compensation bracket
            // Use maximum contribution amount
            if (grossPay > 24750) {
                sss = 1125.00;
            }

        } catch (IOException e) {
            System.err.println("Error reading SSS table file: " + e.getMessage());
        }

        return sss;
    }

    /**
     * Calculates PhilHealth contribution.
     * @param grossPay Gross pay
     * @return PhilHealth contribution
     */
    public static double calculatePhilhealthContribution(double grossPay) {
        double rate = 0.05;
        double contribution;
        double maxContribution = 5000 / 2;

        if (grossPay <= 5000) {
            contribution = 0;
        } else if (grossPay <= 10000) {
            contribution = 500 / 2;
        } else if (grossPay <= 100000) {
            contribution = (grossPay * rate) / 2; // Employee share is half of 5%
        } else {
            contribution = maxContribution;
        }

        return roundToTwoDecimals(Math.min(contribution, maxContribution));
    }

    /**
     * Calculates Pag-IBIG contribution.
     */
    public static double calculatePagibigContribution(double grossPay) {
        double rate = (grossPay > 1500) ? 0.02 : 0.01;
        double contribution = grossPay * rate;
        double maxContribution = 100.00;
        return roundToTwoDecimals(Math.min(contribution, maxContribution));
    }

    /**
     * Calculates withholding tax based on taxable income.
     */
    public static double calculateWithholdingTax(double taxableIncome) {
        if (taxableIncome <= 20833) {
            return 0;
        } else if (taxableIncome <= 33333) {
            return roundToTwoDecimals((taxableIncome - 20833) * 0.20);
        } else if (taxableIncome <= 66667) {
            return roundToTwoDecimals(2500 + ((taxableIncome - 33333) * 0.25));
        } else if (taxableIncome <= 166667) {
            return roundToTwoDecimals(10833.33 + ((taxableIncome - 66667) * 0.30));
        } else if (taxableIncome <= 666667) {
            return roundToTwoDecimals(40833.33 + ((taxableIncome - 166667) * 0.32));
        } else {
            return roundToTwoDecimals(200833.33 + ((taxableIncome - 666667) * 0.35));
        }
    }

    /**
     * Calculates per-day hours with business rules:
     * - Store hours: 08:00–17:00
     * - Grace period: up to 08:10 is considered on-time
     * - Overtime: only paid if on-time; hours after 17:00
     * - Regular hours: up to 8 hours, counted from effective start until 17:00
     */
    private static DayHours calculateDayHours(TimeLog log) {
        java.time.LocalDate date = log.getDate();
        java.time.LocalTime in = log.getTimeIn();
        java.time.LocalTime out = log.getTimeOut();

        if (in == null || out == null) {
            return new DayHours(0, 0);
        }

        boolean onTime = !in.isAfter(GRACE_END);

        // Effective regular start respects grace: treat <= 08:10 as 08:00
        java.time.LocalTime effectiveStartTime = onTime ? STORE_START : in.isBefore(STORE_START) ? STORE_START : in;

        java.time.LocalDateTime startDT = java.time.LocalDateTime.of(date, effectiveStartTime);
        java.time.LocalDateTime endDT;
        if (out.isBefore(in)) {
            endDT = java.time.LocalDateTime.of(date.plusDays(1), out);
        } else {
            endDT = java.time.LocalDateTime.of(date, out);
        }

        java.time.LocalDateTime storeEndDT = java.time.LocalDateTime.of(date, STORE_END);

        // Regular hours are the duration from effective start to min(end, 17:00), capped at 8 hours
        java.time.LocalDateTime regularEnd = endDT.isBefore(storeEndDT) ? endDT : storeEndDT;
        double regularHours = java.time.Duration.between(startDT, regularEnd).toMinutes() / 60.0;
        if (regularHours < 0) regularHours = 0.0;
        regularHours = Math.min(regularHours, 8.0);

        // Overtime hours only if on time and worked past 17:00
        double overtimeHours = 0.0;
        if (onTime && endDT.isAfter(storeEndDT)) {
            overtimeHours = java.time.Duration.between(storeEndDT, endDT).toMinutes() / 60.0;
            if (overtimeHours < 0) overtimeHours = 0.0;
        }

        return new DayHours(roundToTwoDecimals(regularHours), roundToTwoDecimals(overtimeHours));
    }

    /** Lightweight container for per-day hours. */
    private static class DayHours {
        final double regularHours;
        final double overtimeHours;
        DayHours(double regularHours, double overtimeHours) {
            this.regularHours = regularHours;
            this.overtimeHours = overtimeHours;
        }
    }

    /**
     * Rounds a value to two decimal places.
     */
    private static double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Parses a string containing a number with possible commas into an integer value.
     *
     * @param number The string representation of the number to parse
     * @return The parsed integer value
     */
    public static int parseInt(String number) {
        return Integer.parseInt(number.replace(",", "").trim());
    }

    /**
     * Parses a string containing a number with possible commas into a double value.
     *
     * @param number The string representation of the number to parse
     * @return The parsed double value
     */
    public static double parseDouble(String number) {
        return Double.parseDouble(number.replace(",", "").trim());
    }
}
