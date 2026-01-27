package com.group.motorph.model;


/** Represents an employee with personal information and compensation details.

* The class enforces basic validation rules to ensure data integrity,
* such as preventing null or empty required fields and disallowing
* negative monetary values.
**/
    
public class Employee {

    private final String employeeNumber;            // Unique identifier assigned to the employee (immutable).
    private String position;                        // Employee's job position or title.
    private String lastName;                        // Employee's last name
    private String firstName;                       // Employee's first name
    private CompensationComponents compensation;    // Encapsulated compensation components.
    private String sssNumber;                       // Employee's SSS number
    private String philhealthNumber;                // Employee's PhilHealth number
    private String pagibigNumber;                   // Employee's Pag-IBIG number
    private String tinNumber;                       // Employee's TIN number

    /**
     * @param employeeNumber unique employee identifier
     * @param position employee job position
     * @param lastName employee last name
     * @param firstName employee first name
     * @param basicSalary base monthly salary
     * @param sssNumber SSS identification number
     * @param philhealthNumber PhilHealth identification number
     * @param pagibigNumber Pag-IBIG identification number
     * @param tinNumber tax identification number
     * @param riceSubsidy monthly rice subsidy allowance
     * @param phoneAllowance monthly phone allowance
     * @param clothingAllowance monthly clothing allowance
     */
    public Employee(String employeeNumber, String position, String lastName, String firstName,
            double basicSalary, String sssNumber, String philhealthNumber,
            String pagibigNumber, String tinNumber, double riceSubsidy, double phoneAllowance,
            double clothingAllowance) {
        this.employeeNumber = validateRequired(employeeNumber, "Employee Number");
        this.lastName = validateRequired(lastName, "Last Name");
        this.firstName = validateRequired(firstName, "First Name");
        this.compensation = new CompensationComponents(basicSalary, riceSubsidy,
                phoneAllowance, clothingAllowance);
        this.sssNumber = validateRequired(sssNumber, "SSS Number");
        this.philhealthNumber = validateRequired(philhealthNumber, "PhilHealth Number");
        this.pagibigNumber = validateRequired(pagibigNumber, "Pag-IBIG Number");
        this.tinNumber = validateRequired(tinNumber, "TIN Number");
        this.position = validateRequired(position, "Position");
    }

    /**
     * Validates that a required string field is neither null nor empty.
     *
     * @param value the value to validate
     * @param fieldName human-readable field name for error messages
     * @return trimmed string value if valid
     * @throws IllegalArgumentException if the value is null or empty
     */
    private String validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        return value.trim();
    }

    /**
     * Getters for employee fields.
     */
    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public double getBasicSalary() {
        return compensation.getBasicSalary();
    }

    public String getSssNumber() {
        return sssNumber;
    }

    public String getPhilhealthNumber() {
        return philhealthNumber;
    }

    public String getPagibigNumber() {
        return pagibigNumber;
    }

    public String getTinNumber() {
        return tinNumber;
    }

    public String getPosition() {
        return position;
    }

    public double getRiceSubsidy() {
        return compensation.getRiceSubsidy();
    }

    public double getPhoneAllowance() {
        return compensation.getPhoneAllowance();
    }

    public double getClothingAllowance() {
        return compensation.getClothingAllowance();
    }

    /**
     * Returns the encapsulated compensation components.
     *
     * @return {@link CompensationComponents} instance
     */
    public CompensationComponents getCompensation() {
        return compensation;
    }

    /**
     * Setters for mutable fields.
     */
    public void setLastName(String lastName) {
        this.lastName = validateRequired(lastName, "Last Name");
    }

    public void setFirstName(String firstName) {
        this.firstName = validateRequired(firstName, "First Name");
    }

    /**
     * @param basicSalary new basic salary amount
     * @throws IllegalArgumentException if the salary is negative
     */
    public void setBasicSalary(double basicSalary) {
        if (basicSalary < 0) {
            throw new IllegalArgumentException("Basic salary cannot be negative");
        }
        this.compensation = new CompensationComponents(basicSalary,
                compensation.getRiceSubsidy(),
                compensation.getPhoneAllowance(),
                compensation.getClothingAllowance());
    }

    public void setSssNumber(String sssNumber) {
        this.sssNumber = validateRequired(sssNumber, "SSS Number");
    }

    public void setPhilhealthNumber(String philhealthNumber) {
        this.philhealthNumber = validateRequired(philhealthNumber, "PhilHealth Number");
    }

    public void setPagibigNumber(String pagibigNumber) {
        this.pagibigNumber = validateRequired(pagibigNumber, "Pag-IBIG Number");
    }

    public void setTinNumber(String tinNumber) {
        this.tinNumber = validateRequired(tinNumber, "TIN Number");
    }

    public void setPosition(String position) {
        this.position = validateRequired(position, "Position");
    }

    /**
     * @param riceSubsidy new rice subsidy amount
     * @throws IllegalArgumentException if the value is negative
     */
    public void setRiceSubsidy(double riceSubsidy) {
        if (riceSubsidy < 0) {
            throw new IllegalArgumentException("Rice subsidy cannot be negative");
        }
        this.compensation = new CompensationComponents(compensation.getBasicSalary(),
                riceSubsidy,
                compensation.getPhoneAllowance(),
                compensation.getClothingAllowance());
    }

    /**
     * @param phoneAllowance new phone allowance amount
     * @throws IllegalArgumentException if the value is negative
     */
    public void setPhoneAllowance(double phoneAllowance) {
        if (phoneAllowance < 0) {
            throw new IllegalArgumentException("Phone allowance cannot be negative");
        }
        this.compensation = new CompensationComponents(compensation.getBasicSalary(),
                compensation.getRiceSubsidy(),
                phoneAllowance,
                compensation.getClothingAllowance());
    }

    /**
     * @param clothingAllowance new clothing allowance amount
     * @throws IllegalArgumentException if the value is negative
     */
    public void setClothingAllowance(double clothingAllowance) {
        if (clothingAllowance < 0) {
            throw new IllegalArgumentException("Clothing allowance cannot be negative");
        }
        this.compensation = new CompensationComponents(compensation.getBasicSalary(),
                compensation.getRiceSubsidy(),
                compensation.getPhoneAllowance(),
                clothingAllowance);
    }

    /**
     * Returns a concise string representation of the employee,
     * useful for logging and debugging.
     */
    @Override
    public String toString() {
        return String.format("Employee{id=%s, name=%s %s, position=%s}",
                employeeNumber, firstName, lastName, position);
    }
}
