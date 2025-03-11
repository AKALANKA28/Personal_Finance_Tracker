package com.example.finance_tracker.service;

import com.example.finance_tracker.model.User;
import com.example.finance_tracker.repository.UserRepository;
import com.example.finance_tracker.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Validated
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(User user) {
        // Check if the username already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if the email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Encode the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Set default role if not provided
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(Collections.singletonList("ROLE_USER"));
        }

        // Save the user
        return userRepository.save(user);
    }

    @Override
    public String authenticatedUser(String username, String password) {
        // Validate username and password
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        return "Successfully Login";
    }

    @Override
    public User updateUser( User user) {
        // Check if the user exists
        if (!userRepository.existsById(user.getId())) {
            throw new UserNotFoundException("User not found with ID: " + user.getId());
        }

        // Encode the password if it has changed
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Update the user
        return userRepository.save(user);
    }

    @Override
    public boolean deleteUser(String userId) {
        // Check if the user exists
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }

        // Delete the user
        userRepository.deleteById(userId);
        return true;
    }

    @Override
    public List<User> getAllUsers() {
        // Fetch all users
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(String userId) {
        // Fetch user by ID
        return Optional.ofNullable(userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId)));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch user by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Ensure roles is not null
        if (user.getRoles() == null) {
            user.setRoles(Collections.singletonList("ROLE_USER")); // Default role
        }

        return user;
    }
}