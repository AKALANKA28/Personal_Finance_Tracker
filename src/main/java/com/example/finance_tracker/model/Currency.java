package com.example.finance_tracker.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Document(collection = "currencies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Currency {
    @Id
    private String id;
    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Currency code is required")
    private String currencyCode;

    @NotNull(message = "Exchange rate is required")
    @Positive(message = "Exchange rate must be positive")
    private Double exchangeRate;
    private Date lastUpdated;
}
