package com.example.finance_tracker.controller;

import com.example.finance_tracker.model.Goal;
import com.example.finance_tracker.repository.GoalRepository;
import com.example.finance_tracker.service.GoalsAndSavingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class GoalsAndSavingsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GoalRepository goalRepository;

    private Goal goal;

    @BeforeEach
    void setUp() {
        // Clear the database before each test
        goalRepository.deleteAll();

        // Create a test goal
        goal = new Goal();
        goal.setId("123");
        goal.setUserId("user123");
        goal.setTargetAmount(1000.0);
        goal.setCurrentAmount(500.0);
        goal.setDeadline(new Date(System.currentTimeMillis() + 100000000)); // Future date
    }

    @Test
    @WithMockUser(username = "user123", roles = {"USER"})
    void setGoal_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"123\",\"userId\":\"user123\",\"targetAmount\":1000.0,\"currentAmount\":500.0,\"deadline\":\"2023-12-31\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.targetAmount").value(1000.0))
                .andExpect(jsonPath("$.currentAmount").value(500.0));

        // Verify the goal is saved in the database
        Goal savedGoal = goalRepository.findById("123").orElse(null);
        assertNotNull(savedGoal);
        assertEquals("123", savedGoal.getId());
    }

    @Test
    @WithMockUser(username = "user123", roles = {"USER"})
    void getGoalsByUser_Success() throws Exception {
        // Arrange
        goalRepository.save(goal);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/goals/user/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("123"))
                .andExpect(jsonPath("$[0].userId").value("user123"))
                .andExpect(jsonPath("$[0].targetAmount").value(1000.0))
                .andExpect(jsonPath("$[0].currentAmount").value(500.0));
    }

    @Test
    @WithMockUser(username = "user123", roles = {"USER"})
    void getGoalById_Success() throws Exception {
        // Arrange
        goalRepository.save(goal);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/goals/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.targetAmount").value(1000.0))
                .andExpect(jsonPath("$.currentAmount").value(500.0));
    }
}