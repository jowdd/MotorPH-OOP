package com.group.motorph.service;

import com.group.motorph.dao.UserDAO;
import com.group.motorph.dao.impl.UserDAOImpl;
import com.group.motorph.model.User;

/**
 * Handles user login by delegating to the UserDAO.
 */
public class AuthenticationService {

    private final UserDAO userDAO;

    public AuthenticationService() {
        this.userDAO = new UserDAOImpl();
    }

    /**
     * Authenticate a user with their username and password.
     * @return the matching User, or null if credentials are invalid
     */
    public User login(String username, String password) {
        return userDAO.authenticate(username, password);
    }
}
