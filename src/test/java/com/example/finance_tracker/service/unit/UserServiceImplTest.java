package com.example.finance_tracker.service.unit;

import com.example.finance_tracker.model.User;
import com.example.finance_tracker.repository.UserRepository;
import com.example.finance_tracker.exception.UserNotFoundException;
import com.example.finance_tracker.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_Success() {
        // Arrange
        User user = new User();
        user.setUsername("john_doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("password123");

        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(user);

        // Act
        User registeredUser = userService.registerUser(user);

        // Assert
        assertNotNull(registeredUser);
        assertEquals("john_doe", registeredUser.getUsername());
        assertEquals("encodedPassword", registeredUser.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void registerUser_UsernameAlreadyExists() {
        // Arrange
        User user = new User();
        user.setUsername("john_doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("password123");

        when(userRepository.existsByUsername("john_doe")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(user);
        });

        assertEquals("Username already exists", exception.getMessage());
    }

    @Test
    void registerUser_EmailAlreadyExists() {
        // Arrange
        User user = new User();
        user.setUsername("john_doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("password123");

        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(user);
        });

        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    void authenticatedUser_Success() {
        // Arrange
        String username = "john_doe";
        String password = "password123";

        // Act
        String result = userService.authenticatedUser(username, password);

        // Assert
        assertEquals("Successfully Login", result);
    }

    @Test
    void authenticatedUser_UsernameIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.authenticatedUser(null, "password123");
        });

        assertEquals("Username is required", exception.getMessage());
    }

    @Test
    void authenticatedUser_PasswordIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.authenticatedUser("john_doe", null);
        });

        assertEquals("Password is required", exception.getMessage());
    }

    @Test
    void updateUser_Success() {
        // Arrange
        User user = new User();
        user.setId("123");
        user.setUsername("john_doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("newpassword123");

        when(userRepository.existsById("123")).thenReturn(true);
        when(passwordEncoder.encode("newpassword123")).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(user);

        // Act
        User updatedUser = userService.updateUser(user);

        // Assert
        assertNotNull(updatedUser);
        assertEquals("encodedPassword", updatedUser.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateUser_UserNotFound() {
        // Arrange
        User user = new User();
        user.setId("123");

        when(userRepository.existsById("123")).thenReturn(false);

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.updateUser(user);
        });

        assertEquals("User not found with ID: 123", exception.getMessage());
    }

    @Test
    void deleteUser_Success() {
        // Arrange
        String userId = "123";
        when(userRepository.existsById(userId)).thenReturn(true);

        // Act
        boolean result = userService.deleteUser(userId);

        // Assert
        assertTrue(result);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void deleteUser_UserNotFound() {
        // Arrange
        String userId = "123";
        when(userRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUser(userId);
        });

        assertEquals("User not found with ID: 123", exception.getMessage());
    }

    @Test
    void getAllUsers_Success() {
        // Arrange
        User user1 = new User();
        user1.setUsername("john_doe");
        User user2 = new User();
        user2.setUsername("jane_doe");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        // Act
        List<User> users = userService.getAllUsers();

        // Assert
        assertEquals(2, users.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_Success() {
        // Arrange
        User user = new User();
        user.setId("123");
        user.setUsername("john_doe");

        when(userRepository.findById("123")).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userService.getUserById("123");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("john_doe", result.get().getUsername());
    }

    @Test
    void getUserById_UserNotFound() {
        // Arrange
        when(userRepository.findById("123")).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById("123");
        });

        assertEquals("User not found with ID: 123", exception.getMessage());
    }

    @Test
    void loadUserByUsername_Success() {
        // Arrange
        User user = new User();
        user.setUsername("john_doe");
        user.setPassword("password123");
        user.setRoles(Collections.singletonList("ROLE_USER"));

        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = userService.loadUserByUsername("john_doe");

        // Assert
        assertNotNull(userDetails);
        assertEquals("john_doe", userDetails.getUsername());
        assertEquals("password123", userDetails.getPassword());
    }

    @Test
    void loadUserByUsername_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("john_doe");
        });

        assertEquals("User not found with username: john_doe", exception.getMessage());
    }
}