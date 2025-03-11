package com.example.finance_tracker.service.integration;

import com.example.finance_tracker.model.User;
import com.example.finance_tracker.repository.UserRepository;
import com.example.finance_tracker.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserServiceImplIntegrationTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll(); // Clear the database before each test
    }

    @Test
    public void testRegisterUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");

        User registeredUser = userService.registerUser(user);

        assertNotNull(registeredUser);
        assertEquals("testuser", registeredUser.getUsername());
        assertTrue(passwordEncoder.matches("password", registeredUser.getPassword()));

        // Verify that the user was saved in the database
        Optional<User> savedUser = userRepository.findByUsername("testuser");
        assertTrue(savedUser.isPresent());
        assertEquals("test@example.com", savedUser.get().getEmail());
    }

    @Test
    public void testRegisterUserWithDuplicateUsername() {
        User user1 = new User();
        user1.setUsername("testuser");
        user1.setPassword("password");
        user1.setEmail("test1@example.com");
        userRepository.save(user1);

        User user2 = new User();
        user2.setUsername("testuser"); // Same username
        user2.setPassword("password");
        user2.setEmail("test2@example.com");

        assertThrows(Exception.class, () -> userService.registerUser(user2), "Duplicate username should throw an exception");
    }

    @Test
    public void testRegisterUserWithInvalidEmail() {
        User user = new User();
        user.setUsername("invalidemailuser");
        user.setPassword("password");
        user.setEmail("invalid-email"); // Invalid email format

        assertThrows(Exception.class, () -> userService.registerUser(user), "Invalid email format should throw an exception");
    }

    @Test
    public void testGetUserById() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        userRepository.save(user);

        Optional<User> foundUser = userService.getUserById(user.getId());

        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
    }

//    @Test
//    public void testGetUserByInvalidId() {
//        Optional<User> foundUser = userService.getUserById("999L"); // Non-existent ID
//        assertFalse(foundUser.isPresent(), "Fetching a non-existent user should return an empty optional");
//    }

    @Test
    public void testUpdateUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        userRepository.save(user);

        user.setEmail("updated@example.com");
        User updatedUser = userService.updateUser(user);

        assertEquals("updated@example.com", updatedUser.getEmail());
    }

    @Test
    public void testUpdateNonExistentUser() {
        User user = new User();
        user.setId(String.valueOf(999L)); // Non-existent ID
        user.setUsername("nonexistent");
        user.setPassword("password");
        user.setEmail("nonexistent@example.com");

        assertThrows(Exception.class, () -> userService.updateUser(user), "Updating a non-existent user should throw an exception");
    }

    @Test
    public void testDeleteUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        userRepository.save(user);

        boolean isDeleted = userService.deleteUser(user.getId());
        assertTrue(isDeleted);

        Optional<User> deletedUser = userRepository.findById(user.getId());
        assertFalse(deletedUser.isPresent());
    }

//    @Test
//    public void testDeleteNonExistentUser() {
//        boolean isDeleted = userService.deleteUser(String.valueOf(999L)); // Non-existent ID
//        assertFalse(isDeleted, "Deleting a non-existent user should return false");
//    }
}