package com.example.finance_tracker.controller;

import com.example.finance_tracker.model.User;
import com.example.finance_tracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository; // Use the real repository

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll(); // Clear the database before each test
    }

    @Test
    public void testRegisterUser() throws Exception {
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"password\",\"email\":\"test@example.com\"}"))
                .andExpect(status().isCreated());

        // Verify that the user was saved in the database
        User savedUser = userRepository.findByUsername("testuser").orElse(null);
        assertNotNull(savedUser);
        assertEquals("test@example.com", savedUser.getEmail());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAllUsers() throws Exception {
        // Add a user to the database
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        userRepository.save(user);

        mockMvc.perform(get("/api/user/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    public void testLogin() throws Exception {
        // Register a user first
        User user = new User();
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("password"));
        user.setEmail("test@example.com");
        userRepository.save(user);

        // Perform login
        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isString()); // Expect a JWT token
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAdminDashboard() throws Exception {
        mockMvc.perform(get("/api/user/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome to the Admin Dashboard!"));
    }
}