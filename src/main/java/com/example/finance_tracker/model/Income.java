package com.example.finance_tracker.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Date;

@Document(collection = "incomes")
@Getter @Setter @AllArgsConstructor @ToString
public class Income {
    @Id
    private String id;
    private String userId;
    private String source;
    private double amount;
    private Date date;
    private String currencyCode;

    public Income() {
        this.date = new Date();
    }

}