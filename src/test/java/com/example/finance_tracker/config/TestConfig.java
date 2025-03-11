package com.example.finance_tracker.config;

import com.example.finance_tracker.service.IncomeService;
import com.example.finance_tracker.util.CurrencyUtil;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
class TestConfig {
    @Bean
    public CurrencyUtil currencyUtil() {
        return Mockito.mock(CurrencyUtil.class);
    }

    @Bean
    public IncomeService incomeService() {
        return Mockito.mock(IncomeService.class);
    }
}