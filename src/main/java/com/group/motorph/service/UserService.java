package com.group.motorph.service;

import java.util.List;

import com.group.motorph.dao.UserDAO;
import com.group.motorph.dao.impl.UserDAOImpl;
import com.group.motorph.model.User;
import com.group.motorph.util.PasswordUtil;

/**
 * Service for user account management (IT role). Handles create, update,
 * delete, and role-change operations. Passwords are always stored as PBKDF2
 * hashes — never plain text.
 */
public class UserService {

    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAOImpl();
    }

    // Returns all user accounts in the system.
    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    // Looks up a user by username. Returns null if not found.
    public User getUserByUsername(String username) {
        return userDAO.getUserByUsername(username);
    }

    /**
     * Creates a new user account with a hashed password. Returns false if any
     * required field is missing, the role is invalid, or the username already
     * exists.
     */
    public boolean createUser(String username, String password, String employeeId, String role) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return false;
        }
        if (!isValidRole(role)) {
            return false;
        }
        if (userDAO.getUserByUsername(username) != null) {
            return false;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.hash(password));
        user.setEmployeeId(employeeId);
        user.setRole(role);
        return userDAO.addUser(user);
    }

    /**
     * Updates an existing user's password, employee ID, and/or role. Pass null
     * for any field you don't want to change.
     */
    public boolean updateUser(String username, String newPassword, String newEmployeeId, String newRole) {
        User user = userDAO.getUserByUsername(username);
        if (user == null) {
            return false;
        }

        if (newPassword != null && !newPassword.trim().isEmpty()) {
            user.setPassword(PasswordUtil.hash(newPassword));
        }
        if (newEmployeeId != null && !newEmployeeId.trim().isEmpty()) {
            user.setEmployeeId(newEmployeeId);
        }
        if (newRole != null && !newRole.trim().isEmpty()) {
            if (!isValidRole(newRole)) {
                return false;
            }
            user.setRole(newRole);
        }

        return userDAO.updateUser(user);
    }

    // Convenience method to change only a user's role.
    public boolean updateUserRole(String username, String newRole) {
        return updateUser(username, null, null, newRole);
    }

    // Deletes a user account by username. Returns false if not found.
    public boolean deleteUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        if (userDAO.getUserByUsername(username) == null) {
            return false;
        }
        return userDAO.deleteUser(username);
    }

    // Allowed roles in the system
    private boolean isValidRole(String role) {
        if (role == null) {
            return false;
        }
        return switch (role.toUpperCase()) {
            case "HR", "FINANCE", "EMPLOYEE", "IT", "ADMIN" -> true;
            default -> false;
        };
    }
}
