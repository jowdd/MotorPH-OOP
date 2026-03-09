package com.group.motorph.model;

import java.time.LocalDate;

/**
 * ITEmployee - Information Technology department employee
 */
public class ITEmployee extends Employee {
    
    /**
     * Default constructor
     */
    public ITEmployee() {
        super();
    }
    
    /**
     * Constructor with basic information
     */
    public ITEmployee(String employeeId, String lastName, String firstName, LocalDate birthday, String address, 
                    String phoneNumber, String sssNum, String philhealthNum, String tinNum, String pagibigNum,
                    String status, String position, String immediateSupervisor, double basicSalary, 
                    double riceSubsidy, double phoneAllowance, double clothingAllowance,
                    double grossSemiMonthly, double hourlyRate) {
        super(employeeId, lastName, firstName, birthday, address, phoneNumber, sssNum, philhealthNum, tinNum, pagibigNum,
                status, position, immediateSupervisor, basicSalary, riceSubsidy, phoneAllowance, clothingAllowance,
                grossSemiMonthly, hourlyRate);
    }
    
    /**
     * @return type of employee
     */
    @Override
    public String getEmployeeType() {
        return "IT Employee";
    }
}
