package com.example.demowebmvcsccbr4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class HelloController {

    private final Logger log = LoggerFactory.getLogger(HelloController.class);

    private final RestTemplate restTemplate;

    private final CircuitBreakerFactory circuitBreakerFactory;

    public HelloController(RestTemplateBuilder builder, FragileApi fragileApi, CircuitBreakerFactory circuitBreakerFactory) {
        log.info("Creating Hello Controller");
        this.restTemplate = builder
            .rootUri("http://localhost:" + fragileApi.getPort())
            .build();
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @GetMapping(path = "/")
    public String hello() {
        return this.circuitBreakerFactory.create("hello")
            .run(() -> this.restTemplate.getForObject("/", String.class), t -> "Fallback!");
    }
}