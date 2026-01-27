package com.group.motorph.model;

/**
 * The results of payroll calculations for a single employee.
 * This class provides a cohesive representation of:
 * - Gross pay
 * - Government contributions (SSS, PhilHealth, Pag-IBIG)
 * - Withholding tax
 * - Total allowances
 * - Net pay after deductions
 */ 
public class SalaryCalculationResult {
    private final double grossPay;
    private final double sssContribution;
    private final double philhealthContribution;
    private final double pagibigContribution;
    private final double withholdingTax;
    private final double totalAllowances;

    /**
     * Constructs a new SalaryCalculationResult instance.
     * Validates that all amounts are non-negative.
     * @param grossPay Total gross pay before allowances and deductions
     * @param sssContribution SSS contribution
     * @param philhealthContribution PhilHealth contribution
     * @param pagibigContribution Pag-IBIG contribution
     * @param withholdingTax Withholding tax
     * @param totalAllowances Sum of all allowances
     */
    public SalaryCalculationResult(double grossPay, double sssContribution, double philhealthContribution, double pagibigContribution, 
                                double withholdingTax, double totalAllowances) {
        this.grossPay = validateAmount(grossPay, "Gross Pay");
        this.sssContribution = validateAmount(sssContribution, "SSS Contribution");
        this.philhealthContribution = validateAmount(philhealthContribution, "PhilHealth Contribution");
        this.pagibigContribution = validateAmount(pagibigContribution, "Pag-IBIG Contribution");
        this.withholdingTax = validateAmount(withholdingTax, "Withholding Tax");
        this.totalAllowances = validateAmount(totalAllowances, "Total Allowances");
    }

    /**
     * Validates that a monetary amount is non-negative.
     * @param amount The amount to validate
     * @param fieldName Name of the field (used for exception message)
     * @return The validated amount
     */
    private double validateAmount(double amount, String fieldName) {
        if (amount < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative: " + amount);
        }
        return amount;
    }

    
    // Getters

    /**
     * Calculates total deductions (all contributions + tax).
     */
    public double getTotalDeductions() {
        return sssContribution + philhealthContribution + pagibigContribution + withholdingTax;
    }

    /**
     * Calculates gross salary (gross pay + allowances).
     */
    public double getGrossSalary() {
        return grossPay + totalAllowances;
    }

    /**
     * Calculates net pay (gross salary - deductions).
     */
    public double getNetPay() {
        return getGrossSalary() - getTotalDeductions();
    }

    public double getGrossPay() {
        return grossPay;
    }

    public double getSSSContribution() {
        return sssContribution;
    }

    public double getPhilhealthContribution() {
        return philhealthContribution;
    }

    public double getPagibigContribution() {
        return pagibigContribution;
    }

    public double getWithholdingTax() {
        return withholdingTax;
    }

    public double getTotalAllowances() {
        return totalAllowances;
    }

    /**
     * Returns a formatted string representation of the salary calculation results.
     */
    @Override
    public String toString() {
        return String.format(
            "SalaryCalculationResult{" +
            "grossPay=%.2f, " +
            "sss=%.2f, " +
            "philhealth=%.2f, " +
            "pagibig=%.2f, " +
            "tax=%.2f, " +
            "allowances=%.2f, " +
            "netPay=%.2f" +
            "}",
            grossPay, sssContribution, philhealthContribution, 
            pagibigContribution, withholdingTax, totalAllowances, getNetPay()
        );
    }
}
