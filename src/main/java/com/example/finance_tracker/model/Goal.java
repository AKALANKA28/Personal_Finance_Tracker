package com.example.finance_tracker.model;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private double currentAmount;
    private double manualContribution;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date deadline;
    private String budgetId;
    private double progressPercentage;



}
