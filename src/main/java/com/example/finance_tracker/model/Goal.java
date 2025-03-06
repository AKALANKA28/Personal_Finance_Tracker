package com.example.finance_tracker.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "goals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Goal {
    @Id
    private String id;
    private String userId;
    private String name;
    private double targetAmount;
    private double currentAmount = 0;
    private Date deadline;
    private String budgetId;
    private double progressPercentage;


}
