package com.group.motorph.model;

import java.time.LocalDate;

/**
 * Represents an employee and all their associated data.
 */
public class Employee {

    private String employeeId;
    private String lastName;
    private String firstName;
    private LocalDate birthday;
    private String address;
    private String phoneNumber;
    private String sssNum;
    private String philhealthNum;
    private String tinNum;
    private String pagibigNum;
    private String status;
    private String position;
    private String immediateSupervisor;
    private double basicSalary;
    private double riceSubsidy;
    private double phoneAllowance;
    private double clothingAllowance;
    private double grossSemiMonthly;
    private double hourlyRate;

    // Default Constructor
    public Employee() {
    }

    // Constructor with employee's information
    public Employee(String employeeId, String lastName, String firstName, LocalDate birthday, String address,
            String phoneNumber, String sssNum, String philhealthNum, String tinNum, String pagibigNum,
            String status, String position, String immediateSupervisor, double basicSalary,
            double riceSubsidy, double phoneAllowance, double clothingAllowance,
            double grossSemiMonthly, double hourlyRate) {
        this.employeeId = employeeId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthday = birthday;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.sssNum = sssNum;
        this.philhealthNum = philhealthNum;
        this.tinNum = tinNum;
        this.pagibigNum = pagibigNum;
        this.status = status;
        this.position = position;
        this.immediateSupervisor = immediateSupervisor;
        this.basicSalary = basicSalary;
        this.riceSubsidy = riceSubsidy;
        this.phoneAllowance = phoneAllowance;
        this.clothingAllowance = clothingAllowance;
        this.grossSemiMonthly = grossSemiMonthly;
        this.hourlyRate = hourlyRate;
    }

    // Getters and Setters
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSssNum() {
        return sssNum;
    }

    public void setSssNum(String sssNum) {
        this.sssNum = sssNum;
    }

    public String getPhilhealthNum() {
        return philhealthNum;
    }

    public void setPhilhealthNum(String philhealthNum) {
        this.philhealthNum = philhealthNum;
    }

    public String getTinNum() {
        return tinNum;
    }

    public void setTinNum(String tinNum) {
        this.tinNum = tinNum;
    }

    public String getPagibigNum() {
        return pagibigNum;
    }

    public void setPagibigNum(String pagibigNum) {
        this.pagibigNum = pagibigNum;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getImmediateSupervisor() {
        return immediateSupervisor;
    }

    public void setImmediateSupervisor(String immediateSupervisor) {
        this.immediateSupervisor = immediateSupervisor;
    }

    public double getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(double basicSalary) {
        this.basicSalary = basicSalary;
    }

    public double getRiceSubsidy() {
        return riceSubsidy;
    }

    public void setRiceSubsidy(double riceSubsidy) {
        this.riceSubsidy = riceSubsidy;
    }

    public double getPhoneAllowance() {
        return phoneAllowance;
    }

    public void setPhoneAllowance(double phoneAllowance) {
        this.phoneAllowance = phoneAllowance;
    }

    public double getClothingAllowance() {
        return clothingAllowance;
    }

    public void setClothingAllowance(double clothingAllowance) {
        this.clothingAllowance = clothingAllowance;
    }

    public double getGrossSemiMonthly() {
        return grossSemiMonthly;
    }

    public void setGrossSemiMonthly(double grossSemiMonthly) {
        this.grossSemiMonthly = grossSemiMonthly;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    @Override
    public String toString() {
        return employeeId + " - " + getFullName() + " (" + position + ")";
    }

    /**
     * Returns the employee type label for this instance. Subclasses override
     * this to return their specific type.
     */
    public String getEmployeeType() {
        return "Employee";
    }
}
