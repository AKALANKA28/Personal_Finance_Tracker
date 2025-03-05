package com.example.finance_tracker.model;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Notification {
    @Id
    private String id;
    private String userId;
    private String title;
    private String message;
    private String type;
    private boolean read;
    private Date createdAt;
    private String email;

}

