package com.group.motorph.service;

import com.group.motorph.dao.UserDAO;
import com.group.motorph.dao.impl.UserDAOImpl;
import com.group.motorph.model.User;

/**
 * Service class for authentication and user management
 */
public class AuthenticationService {
    
    private final UserDAO userDAO;
    private User currentUser;
    
    public AuthenticationService() {
        this.userDAO = new UserDAOImpl();
    }
    
    /**
     * Authenticate user with username and password
     * @return User object if authentication successful, null otherwise
     */
    public User login(String username, String password) {
        User user = userDAO.authenticate(username, password);
        if (user != null) {
            this.currentUser = user;
        }
        return user;
    }
    
    /**
     * Logout current user
     */
    public void logout() {
        this.currentUser = null;
    }
    
    /**
     * Get current logged-in user
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Check if current user has specific role
     */
    public boolean hasRole(String role) {
        return currentUser != null && currentUser.getRole().equalsIgnoreCase(role);
    }

    /**
     * Check if current user is Admin
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }
    
    /**
     * Check if current user is HR
     */
    public boolean isHR() {
        return hasRole("HR");
    }
    
    /**
     * Check if current user is Finance
     */
    public boolean isFinance() {
        return hasRole("Finance");
    }
    
    /**
     * Check if current user is Employee
     */
    public boolean isEmployee() {
        return hasRole("Employee");
    }
    
    /**
     * Check if current user is IT
     */
    public boolean isIT() {
        return hasRole("IT");
    }
}
