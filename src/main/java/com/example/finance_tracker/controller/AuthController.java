package com.example.finance_tracker.controller;

import com.example.finance_tracker.model.User;
import com.example.finance_tracker.service.UserService;
import com.example.finance_tracker.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@Tag(name = "Use Auth API", description = "Operations related to user authentication and management")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(UserService userService, AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "Register a new user", description = "Creates a new user and returns the registered user object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody User user) {
        User registeredUser = userService.registerUser(user);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(registeredUser.getId())
                .toUri();
        return ResponseEntity.created(location).body(registeredUser);
    }

    @Operation(summary = "Authenticate a user", description = "Logs in a user and returns a JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<String> authenticatedUser(@RequestBody User user) throws Exception {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

        final UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
        String userId = ((User) userDetails).getId();
        final String jwt = jwtUtil.generateToken(userDetails, userId);

        return ResponseEntity.ok(jwt);
    }

    @Operation(summary = "Update an existing user", description = "Updates a user's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<User> updateUser(
            @Parameter(description = "User ID to update") @PathVariable String id,
            @RequestBody User user) {
        user.setId(id);
        User updatedUser = userService.updateUser(user);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Delete a user", description = "Deletes a user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(
            @Parameter(description = "User ID to delete") @PathVariable String id) {
        if (userService.deleteUser(id)) {
            return ResponseEntity.ok("User deleted successfully!");
        }
        return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Get all users", description = "Retrieves a list of all users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users retrieved",
                    content = @Content(schema = @Schema(implementation = User.class)))
    })
    @SecurityRequirement(name = "bearerAuth") // JWT authentication required
    @GetMapping("/")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get a user by ID", description = "Retrieves a user based on their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(
            @Parameter(description = "User ID to retrieve") @PathVariable String id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Admin Dashboard", description = "Accessible only by admins")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin dashboard accessed"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @SecurityRequirement(name = "bearerAuth") // JWT authentication required
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String adminDashboard() {
        return "Welcome to the Admin Dashboard!";
    }
}
