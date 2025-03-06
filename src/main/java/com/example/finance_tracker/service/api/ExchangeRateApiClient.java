package com.example.finance_tracker.service.api;

import com.example.finance_tracker.config.ExchangeRateApiConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ExchangeRateApiClient {

    private final ExchangeRateApiConfig apiConfig;
    private final RestTemplate restTemplate;

    public ExchangeRateApiClient(ExchangeRateApiConfig apiConfig, RestTemplate restTemplate) {
        this.apiConfig = apiConfig;
        this.restTemplate = restTemplate;
    }

    /**
     * Fetches the latest exchange rates from the external API.
     */
    public Map<String, Double> getLatestExchangeRates() {
        String url = apiConfig.getApiUrl() + "?access_key=" + apiConfig.getApiKey();
        ExchangeRateResponse response = restTemplate.getForObject(url, ExchangeRateResponse.class);

        if (response != null && response.isSuccess()) {
            return response.getRates();
        } else {
            throw new RuntimeException("Failed to fetch exchange rates from the API");
        }
    }

    @Setter
    @Getter
    private static class ExchangeRateResponse {
        private boolean success;
        private Map<String, Double> rates;
    }
}