package com.example.finance_tracker.repository;

import com.example.finance_tracker.model.Goal;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface GoalRepository extends MongoRepository<Goal, String> {
    List<Goal> findByUserId(String userId);


    List<Goal> findByUserIdAndProgressPercentageGreaterThanEqual(String userId, int i);


    List<Goal> findByUserIdAndDeadlineAfter(String userId, Date date);

    List<Goal> findByUserIdAndDeadlineBeforeAndProgressPercentageLessThan(String userId, Date date, int i);

    List<Goal> findByDeadlineAfter(Date date);
}

