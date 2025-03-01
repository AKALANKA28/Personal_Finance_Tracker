package com.example.finance_tracker.service;

import com.example.finance_tracker.model.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User registerUser(User user);
    String authenticatedUser(String username, String password);
    User updateUser(User user);
    boolean deleteUser(String userId);
    List<User> getAllUsers();
    Optional<User> getUserById(String userId);

    UserDetails loadUserByUsername(String username);
}
