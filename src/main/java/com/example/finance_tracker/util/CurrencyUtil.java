package com.example.finance_tracker.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.finance_tracker.repository.UserRepository;
import com.example.finance_tracker.model.User;
import com.example.finance_tracker.exception.ResourceNotFoundException;

@Component
public class CurrencyUtil {

    @Value("${app.base-currency:LKR}")
    private String defaultBaseCurrency;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get the base currency for a user.
     * If the user has not set a preference, use the default base currency.
     */
    public String getBaseCurrencyForUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Use the user's base currency if set; otherwise, use the default
        return user.getBaseCurrency() != null ? user.getBaseCurrency() : defaultBaseCurrency;
    }
}