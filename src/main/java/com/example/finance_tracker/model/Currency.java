package com.example.finance_tracker.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "currencies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Currency {
    @Id
    private String id;
    private String userId;
    private String currencyCode;
    private double exchangeRate;
    private Date lastUpdated;
}
