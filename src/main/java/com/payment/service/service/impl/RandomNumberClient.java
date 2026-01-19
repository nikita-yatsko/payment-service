package com.payment.service.service.impl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class RandomNumberClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public RandomNumberClient(@Value("${spring.external.random.base_url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    public List<Integer> getRandomNumber() {
        ResponseEntity<List> response = restTemplate.getForEntity(baseUrl, List.class);
        return response.getBody();
    }
}
