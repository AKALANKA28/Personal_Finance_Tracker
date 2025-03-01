package com.example.finance_tracker.model;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collections;
import java.util.List;

@Document(collection = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class User {
    @Id
    private String id;
    private String username;
    private String password;
    private String email;
    private String role;

    public List<String> getRoles() {
        return Collections.singletonList(role); // Convert the role to a list
    }
}
