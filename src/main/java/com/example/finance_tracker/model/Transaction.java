package com.example.finance_tracker.model;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Transaction {
    @Id
    private String id;
    private String userId;
    private String type;
    private String category;
    private double amount;
    private String description;
    private Date date = new Date();
    private List<String> tags;
    private String currencyCode;


}
