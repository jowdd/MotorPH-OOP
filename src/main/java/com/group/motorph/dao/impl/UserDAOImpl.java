package com.group.motorph.dao.impl;

import com.group.motorph.dao.UserDAO;
import com.group.motorph.model.User;
import com.group.motorph.util.CSVHandler;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV-based implementation of UserDAO
 * Demonstrates POLYMORPHISM - implements UserDAO interface
 */
public class UserDAOImpl implements UserDAO {
    
    private static final String USER_FILE = CSVHandler.getDataDirectory() + "users.csv";
    private static final String[] HEADERS = {"Username", "Password", "Role", "EmployeeID"}; // Role can be HR, FINANCE, EMPLOYEE, IT, ADMIN
    
    public UserDAOImpl() {
        CSVHandler.ensureDataDirectory();
    }
    
    @Override
    public User authenticate(String username, String password) {
        List<User> users = getAllUsers();
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }
    
    @Override
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        List<String[]> data = CSVHandler.readCSV(USER_FILE);
        
        for (String[] row : data) {
            if (row.length >= 4) {
                User user = new User(
                    row[0].trim(), // username
                    row[1].trim(), // password
                    row[2].trim(), // role
                    row[3].trim()  // employeeId
                );
                users.add(user);
            }
        }
        
        return users;
    }
    
    @Override
    public User getUserByUsername(String username) {
        List<User> users = getAllUsers();
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }
    
    @Override
    public boolean addUser(User user) {
        try {
            String[] data = {
                user.getUsername(),
                user.getPassword(),
                user.getRole(),
                user.getEmployeeId()
            };
            CSVHandler.appendToCSV(USER_FILE, data);
            return true;
        } catch (Exception e) {
            System.err.println("Error adding user: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean updateUser(User user) {
        List<User> users = getAllUsers();
        List<String[]> data = new ArrayList<>();
        
        boolean updated = false;
        for (User u : users) {
            if (u.getUsername().equals(user.getUsername())) {
                data.add(new String[]{
                    user.getUsername(),
                    user.getPassword(),
                    user.getRole(),
                    user.getEmployeeId()
                });
                updated = true;
            } else {
                data.add(new String[]{
                    u.getUsername(),
                    u.getPassword(),
                    u.getRole(),
                    u.getEmployeeId()
                });
            }
        }
        
        if (updated) {
            CSVHandler.writeCSV(USER_FILE, HEADERS, data);
        }
        
        return updated;
    }
    
    @Override
    public boolean deleteUser(String username) {
        List<User> users = getAllUsers();
        List<String[]> data = new ArrayList<>();
        boolean deleted = false;
        
        for (User user : users) {
            if (!user.getUsername().equals(username)) {
                data.add(new String[]{
                    user.getUsername(),
                    user.getPassword(),
                    user.getRole(),
                    user.getEmployeeId()
                });
            } else {
                deleted = true;
            }
        }
        
        if (deleted) {
            CSVHandler.writeCSV(USER_FILE, HEADERS, data);
        }
        
        return deleted;
    }
}
