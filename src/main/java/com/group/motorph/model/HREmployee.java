package com.group.motorph.model;

import java.time.LocalDate;

/**
 * HREmployee - Human Resources department employee
 */
public class HREmployee extends Employee {

    //Default constructor
    public HREmployee() {
        super();
    }

    // Constructor with employee's information
    public HREmployee(String employeeId, String lastName, String firstName, LocalDate birthday, String address,
            String phoneNumber, String sssNum, String philhealthNum, String tinNum, String pagibigNum,
            String status, String position, String immediateSupervisor, double basicSalary,
            double riceSubsidy, double phoneAllowance, double clothingAllowance,
            double grossSemiMonthly, double hourlyRate) {
        super(employeeId, lastName, firstName, birthday, address, phoneNumber, sssNum, philhealthNum, tinNum, pagibigNum,
                status, position, immediateSupervisor, basicSalary, riceSubsidy, phoneAllowance, clothingAllowance,
                grossSemiMonthly, hourlyRate);
    }

    @Override
    public String getEmployeeType() {
        return "HR Employee";
    }
}
