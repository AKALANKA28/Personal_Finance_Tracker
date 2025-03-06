//package com.example.finance_tracker.scheduler;
//
//import com.example.finance_tracker.service.ExchangeRateService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//@Component
//public class ExchangeRateScheduler {
//
//    @Autowired
//    private ExchangeRateService exchangeRateService;
//
//    @Scheduled(fixedRate = 3600000) // Update every hour
//    public void updateExchangeRates() {
//        exchangeRateService.updateAllExchangeRates();
//    }
//}