package com.example.finance_tracker.model;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Builder
@Document(collection = "transactions")
@Getter @Setter  @AllArgsConstructor @ToString
public class Transaction {
    @Id
    private String id;
    private String userId;
    private String type;
    private double amount;
    private String currencyCode;
    private String category;
    private String source;
    private Date date;
    private String description;
    private List<String> tags;
    private String goalId;

    private boolean IsRecurring;
    private String recurrencePattern; // e.g., "daily", "weekly", "monthly"
    private Date recurrenceEndDate;

    public Transaction() {
        this.date = new Date();
    }

    public Transaction(String userId, double allocation, String savings, Date now, String s) {
    }
}
