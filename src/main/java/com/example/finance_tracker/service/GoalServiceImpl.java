package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Goal;
import com.example.finance_tracker.repository.GoalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;

    @Autowired
    public GoalServiceImpl(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    @Override
    public Goal setGoal(Goal goal) {
        return goalRepository.save(goal);
    }

    @Override
    public Goal updateGoal(Goal goal) {
        return goalRepository.save(goal);
    }

    @Override
    public boolean deleteGoal(String goalId) {
        goalRepository.deleteById(goalId);
        return true;
    }

    @Override
    public void trackGoalProgress(String goalId) {
        // Logic to track goal progress
    }

    @Override
    public void allocateSavings(String userId, double amount) {
        // Logic to allocate savings
    }
}
