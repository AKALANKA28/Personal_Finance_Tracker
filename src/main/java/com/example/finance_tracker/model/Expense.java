package com.example.finance_tracker.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "expenses")
@Getter @Setter @AllArgsConstructor @ToString
public class Expense {
    @Id
    private String id;
    private String userId;
    private String category;
    private double amount;
    private Date date;
    private String description;
    private List<String> tags;
    private boolean isRecurring;
    private String recurrencePattern;
    private String currencyCode;

    public Expense() {
        this.date = new Date();
        this.description = "";
        this.tags = new ArrayList<>();
        this.isRecurring = false;
        this.recurrencePattern = "";
    }

}