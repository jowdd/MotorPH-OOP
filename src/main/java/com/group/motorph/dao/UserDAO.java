package com.group.motorph.dao;

import java.util.List;

import com.group.motorph.model.User;

/**
 * Data access contract for user accounts. Defines authentication and CRUD
 * operations that each storage implementation must provide.
 */
public interface UserDAO {

    User authenticate(String username, String password);

    List<User> getAllUsers();

    User getUserByUsername(String username);

    boolean addUser(User user);

    boolean updateUser(User user);

    boolean deleteUser(String username);
}
