package com.group.motorph.model;

/**
 * Represents a system user with login credentials and an assigned role.
 */
public class User {

    private String username;
    private String password;
    private String role; // HR, Finance, Employee, IT, Admin
    private String employeeId;

    public User() {
    }

    public User(String username, String password, String role, String employeeId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.employeeId = employeeId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    @Override
    public String toString() {
        return "User{username='" + username + "', role='" + role + "', employeeId='" + employeeId + "'}";
    }
}
