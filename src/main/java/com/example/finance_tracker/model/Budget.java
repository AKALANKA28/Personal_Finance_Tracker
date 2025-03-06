package com.example.finance_tracker.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "budgets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Budget {
    @Id
    private String id;
    private String userId;
    private String category;
    private double limit;
    private Date startDate;
    private Date endDate;
    private boolean notificationEnabled;
    private String currencyCode;
    private String goalId;

}