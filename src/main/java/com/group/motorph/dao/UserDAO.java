package com.group.motorph.dao;

import java.util.List;

import com.group.motorph.model.User;

/**
 * Interface for User data access operations
 * Demonstrates ABSTRACTION - defines contract without implementation
 */
public interface UserDAO {
    User authenticate(String username, String password);
    List<User> getAllUsers();
    User getUserByUsername(String username);
    boolean addUser(User user);
    boolean updateUser(User user);
    boolean deleteUser(String username);
}
