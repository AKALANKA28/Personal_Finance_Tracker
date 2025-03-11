package com.example.finance_tracker.integration;

import com.example.finance_tracker.model.User;
import com.example.finance_tracker.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserServiceImplIntegrationTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    public void setUp() {
        // Clear the user collection before each test
        mongoTemplate.dropCollection(User.class);
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

        // Verify that the user was saved in the database using MongoTemplate
        Query query = new Query(Criteria.where("username").is("testuser"));
        User savedUser = mongoTemplate.findOne(query, User.class);
        assertNotNull(savedUser);
        assertEquals("test@example.com", savedUser.getEmail());
    }

    @Test
    public void testRegisterUserWithDuplicateUsername() {
        // Create and save first user with MongoTemplate
        User user1 = new User();
        user1.setUsername("testuser");
        user1.setPassword("password");
        user1.setEmail("test1@example.com");
        mongoTemplate.save(user1);

        // Attempt to create second user with the same username
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
        // Create and save user with MongoTemplate
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        mongoTemplate.save(user);

        Optional<User> foundUser = userService.getUserById(user.getId());

        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    public void testGetUserByInvalidId() {
        Optional<User> foundUser = userService.getUserById("nonexistentId");
        assertFalse(foundUser.isPresent(), "Fetching a non-existent user should return an empty optional");
    }

    @Test
    public void testUpdateUser() {
        // Create and save user with MongoTemplate
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        mongoTemplate.save(user);

        user.setEmail("updated@example.com");
        User updatedUser = userService.updateUser(user);

        assertEquals("updated@example.com", updatedUser.getEmail());

        // Verify the update with MongoTemplate
        Query query = new Query(Criteria.where("_id").is(user.getId()));
        User foundUser = mongoTemplate.findOne(query, User.class);
        assertNotNull(foundUser);
        assertEquals("updated@example.com", foundUser.getEmail());
    }

    @Test
    public void testUpdateNonExistentUser() {
        User user = new User();
        user.setId("nonexistentId"); // Non-existent ID
        user.setUsername("nonexistent");
        user.setPassword("password");
        user.setEmail("nonexistent@example.com");

        assertThrows(Exception.class, () -> userService.updateUser(user), "Updating a non-existent user should throw an exception");
    }

    @Test
    public void testDeleteUser() {
        // Create and save user with MongoTemplate
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        mongoTemplate.save(user);

        boolean isDeleted = userService.deleteUser(user.getId());
        assertTrue(isDeleted);

        // Verify deletion with MongoTemplate
        Query query = new Query(Criteria.where("_id").is(user.getId()));
        User deletedUser = mongoTemplate.findOne(query, User.class);
        assertNull(deletedUser);
    }

    @Test
    public void testDeleteNonExistentUser() {
        boolean isDeleted = userService.deleteUser("nonexistentId");
        assertFalse(isDeleted, "Deleting a non-existent user should return false");
    }
}