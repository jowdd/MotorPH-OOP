package com.group.motorph.model;

/**
* All compensation-related components for an employee.
* Holds the employee's basic salary and allowances, ensuring that all monetary values are valid (non-negative).
*/
public class CompensationComponents {
    private final double basicSalary;
    private final double riceSubsidy;
    private final double phoneAllowance;
    private final double clothingAllowance;

    /**
     * Constructs a {@code CompensationComponents} instance with specified amounts.
     * @param basicSalary    Base salary of the employee
     * @param riceSubsidy    Monthly rice subsidy allowance
     * @param phoneAllowance Monthly phone allowance
     * @param clothingAllowance Monthly clothing allowance
     */
    public CompensationComponents(double basicSalary, double riceSubsidy, 
                                 double phoneAllowance, double clothingAllowance) {
        this.basicSalary = validateAmount(basicSalary, "Basic Salary");
        this.riceSubsidy = validateAmount(riceSubsidy, "Rice Subsidy");
        this.phoneAllowance = validateAmount(phoneAllowance, "Phone Allowance");
        this.clothingAllowance = validateAmount(clothingAllowance, "Clothing Allowance");
    }

    /**
     * Validates that a monetary amount is non-negative.
     *
     * @param amount Amount to validate
     * @param fieldName Name of the field for error reporting
     * @return The validated amount
     *
     * @throws IllegalArgumentException if the amount is negative
     */
    private double validateAmount(double amount, String fieldName) {
        if (amount < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative: " + amount);
        }
        return amount;
    }

    /**
     * Getters
    */
    public double getTotalCompensation() {
        return basicSalary + riceSubsidy + phoneAllowance + clothingAllowance;
    }

    public double getTotalAllowances() {
        return riceSubsidy + phoneAllowance + clothingAllowance;
    }

    public double getBasicSalary() {
        return basicSalary;
    }

    public double getRiceSubsidy() {
        return riceSubsidy;
    }

    public double getPhoneAllowance() {
        return phoneAllowance;
    }

    public double getClothingAllowance() {
        return clothingAllowance;
    }
}
