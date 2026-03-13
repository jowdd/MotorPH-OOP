package com.group.motorph.model;

/**
 * Represents an Admin employee who has access to all system modules. Extends
 * Employee with the ADMIN role type.
 */
public class AdminEmployee extends Employee {

    public AdminEmployee() {
        super();
    }

    @Override
    public String getEmployeeType() {
        return "ADMIN";
    }
}
