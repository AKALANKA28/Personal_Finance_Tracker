package com.example.finance_tracker.service.api;

import com.example.finance_tracker.config.ExchangeRateApiConfig;
import com.example.finance_tracker.model.ExchangeRateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class ExchangeRateApiClientImpl implements ExchangeRateApiClient {

    private final ExchangeRateApiConfig apiConfig;
    private final RestTemplate restTemplate;

    public ExchangeRateApiClientImpl(ExchangeRateApiConfig apiConfig, RestTemplate restTemplate) {
        this.apiConfig = apiConfig;
        this.restTemplate = restTemplate;
    }

    public Map<String, Double> getLatestExchangeRates(String baseCurrency) {
        String url = apiConfig.getApiUrl() + "/v6/" + apiConfig.getApiKey() + "/latest/" + baseCurrency;
        log.info("Fetching exchange rates from URL: {}", url);

        try {
            ResponseEntity<ExchangeRateResponse> responseEntity = restTemplate.getForEntity(url, ExchangeRateResponse.class);

            log.debug("API Response: {}", responseEntity);

            if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null) {
                ExchangeRateResponse response = responseEntity.getBody();

                log.info("Exchange rate API result: {}", response.getResult());

                if ("success".equals(response.getResult())) {
                    log.info("Exchange rates retrieved successfully");

                    return response.getConversionRates();

                } else {
                    log.error("API returned an error response: {}", response);
                    return null;
                }
            } else {
                log.error("Failed to fetch exchange rates. HTTP Status: {}", responseEntity.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Error fetching exchange rates: {}", e.getMessage(), e);
            return null;
        }
    }
}
