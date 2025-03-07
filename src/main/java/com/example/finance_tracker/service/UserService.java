package com.example.finance_tracker.service;

import com.example.finance_tracker.model.User;
import jakarta.validation.Valid;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;


public interface UserService {
    User registerUser(@Valid User user);
    String authenticatedUser(String username, String password);
    User updateUser(@Valid User user);
    boolean deleteUser(String userId);
    List<User> getAllUsers();
    Optional<User> getUserById(String userId);
    UserDetails loadUserByUsername(String username);

}