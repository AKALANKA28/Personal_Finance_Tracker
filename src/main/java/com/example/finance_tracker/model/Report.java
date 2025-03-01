package com.example.finance_tracker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Report {
    @Id
    private String id;
    private String userId;
    private String reportType;
    private String reportData;
}
