package com.duyphuc.olympics.service;

import java.sql.SQLException;

import com.duyphuc.olympics.exception.AuthenticationException;
import com.duyphuc.olympics.model.User;

public interface IAuthService {
    User login(String username, String password) throws AuthenticationException;
    void logout();
    User getCurrentUser();
    boolean registerUser(String username, String plainPassword, String email, String role) throws AuthenticationException, SQLException; // Thêm throws
    boolean changePassword(String username, String oldPassword, String newPassword) throws AuthenticationException;
}