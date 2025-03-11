package com.example.finance_tracker.integration;

import com.example.finance_tracker.model.Goal;
import com.example.finance_tracker.repository.GoalRepository;
import com.example.finance_tracker.service.GoalsAndSavingsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GoalsAndSavingsServiceImplIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(GoalsAndSavingsServiceImplIntegrationTest.class);

    @Autowired
    private GoalsAndSavingsServiceImpl goalsAndSavingsService;

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
    void setGoal_Success() {
        // Act
        Goal result = goalsAndSavingsService.setGoal(goal);

        // Assert
        assertNotNull(result);
        assertEquals("123", result.getId());
        assertEquals("user123", result.getUserId());
        assertEquals(1000.0, result.getTargetAmount());
        assertEquals(500.0, result.getCurrentAmount());

        // Verify the goal is saved in the database
        Goal savedGoal = goalRepository.findById("123").orElse(null);
        assertNotNull(savedGoal);
        assertEquals("123", savedGoal.getId());
    }

    @Test
    void getGoalsByUser_Success() {
        // Arrange
        goalRepository.save(goal);

        // Act
        List<Goal> result = goalsAndSavingsService.getGoalsByUser("user123");

        // Assert
        assertEquals(1, result.size());
        assertEquals("123", result.get(0).getId());
        assertEquals("user123", result.get(0).getUserId());
    }

    @Test
    void getGoalById_Success() {
        // Arrange
        goalRepository.save(goal);

        // Act
        Goal result = goalsAndSavingsService.getGoalById("123");

        // Assert
        assertNotNull(result);
        assertEquals("123", result.getId());
        assertEquals("user123", result.getUserId());
    }

    @Test
    void addManualContribution_Success() {
        // Arrange
        goalRepository.save(goal);

        // Act
        Goal result = goalsAndSavingsService.addManualContribution("123", 200.0);

        // Assert
        assertNotNull(result);
        assertEquals(700.0, result.getCurrentAmount()); // 500 + 200
        assertEquals(70.0, result.getProgressPercentage()); // (700 / 1000) * 100

        // Verify the updated goal in the database
        Goal updatedGoal = goalRepository.findById("123").orElse(null);
        assertNotNull(updatedGoal);
        assertEquals(700.0, updatedGoal.getCurrentAmount());
    }
}