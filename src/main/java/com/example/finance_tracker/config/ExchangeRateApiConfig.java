package com.example.finance_tracker.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class ExchangeRateApiConfig {

    @Value("${exchange.rate.api.key}")
    private String apiKey;

    @Value("${exchange.rate.api.url}")
    private String apiUrl;

}