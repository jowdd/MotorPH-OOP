package com.group.motorph.service;

import java.util.List;

import com.group.motorph.dao.UserDAO;
import com.group.motorph.dao.impl.UserDAOImpl;
import com.group.motorph.model.User;

/**
 * Service class for user management operations (IT role)
 * Handles user CRUD operations and role management
 */
public class UserService {
    
    private final UserDAO userDAO;
    
    public UserService() {
        this.userDAO = new UserDAOImpl();
    }
    
    /**
     * Get all users (IT role)
     */
    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }
    
    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        return userDAO.getUserByUsername(username);
    }
    
    /**
     * Create new user (IT role)
     * @param username username for login
     * @param password password
     * @param employeeId associated employee ID
     * @param role user role (HR, FINANCE, EMPLOYEE, IT)
     * @return true if created successfully
     */
    public boolean createUser(String username, String password, String employeeId, String role) {
        // Validate inputs
        if (username == null || username.trim().isEmpty()) {
            System.err.println("Username is required");
            return false;
        }
        if (password == null || password.trim().isEmpty()) {
            System.err.println("Password is required");
            return false;
        }
        if (employeeId == null || employeeId.trim().isEmpty()) {
            System.err.println("Employee ID is required");
            return false;
        }
        // Validate role
        if (!isValidRole(role)) {
            System.err.println("Invalid role. Must be: HR, FINANCE, EMPLOYEE, IT, or ADMIN");
            return false;
        }
        // Check if username already exists
        if (userDAO.getUserByUsername(username) != null) {
            System.err.println("Username already exists");
            return false;
        }
        // Create new user
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmployeeId(employeeId);
        user.setRole(role);
        return userDAO.addUser(user);
    }
    
    /**
     * Update existing user (IT role)
     * Can update password, employee ID, or role
     */
    public boolean updateUser(String username, String newPassword, String newEmployeeId, String newRole) {
        User existingUser = userDAO.getUserByUsername(username);
        
        if (existingUser == null) {
            System.err.println("User not found: " + username);
            return false;
        }
        
        // Update fields if new values provided
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            existingUser.setPassword(newPassword);
        }
        
        if (newEmployeeId != null && !newEmployeeId.trim().isEmpty()) {
            existingUser.setEmployeeId(newEmployeeId);
        }
        
        if (newRole != null && !newRole.trim().isEmpty()) {
            if (!isValidRole(newRole)) {
                System.err.println("Invalid role. Must be: HR, FINANCE, EMPLOYEE, IT, or ADMIN");
                return false;
            }
            existingUser.setRole(newRole);
        }
        
        return userDAO.updateUser(existingUser);
    }
    
    /**
     * Update only user role (IT role)
     * Convenient method for role changes
     */
    public boolean updateUserRole(String username, String newRole) {
        return updateUser(username, null, null, newRole);
    }
    
    /**
     * Delete user (IT role)
     */
    public boolean deleteUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            System.err.println("Username is required");
            return false;
        }
        
        User user = userDAO.getUserByUsername(username);
        if (user == null) {
            System.err.println("User not found: " + username);
            return false;
        }
        
        return userDAO.deleteUser(username);
    }
    
    /**
     * Validate if role is one of the allowed roles
     */
    private boolean isValidRole(String role) {
        if (role == null) {
            return false;
        }
        String roleUpper = role.toUpperCase();
        return roleUpper.equals("HR") || 
               roleUpper.equals("FINANCE") || 
               roleUpper.equals("EMPLOYEE") || 
               roleUpper.equals("IT") ||
               roleUpper.equals("ADMIN");
    }
    
    /**
     * Change user password
     */
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        User user = userDAO.authenticate(username, oldPassword);
        
        if (user == null) {
            System.err.println("Invalid username or password");
            return false;
        }
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            System.err.println("New password is required");
            return false;
        }
        
        user.setPassword(newPassword);
        return userDAO.updateUser(user);
    }
}
