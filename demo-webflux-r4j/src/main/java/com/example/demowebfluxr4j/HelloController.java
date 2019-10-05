package com.example.demowebfluxr4j;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@RestController
public class HelloController {

    private final Logger log = LoggerFactory.getLogger(HelloController.class);

    private final WebClient webClient;

    private final CircuitBreaker helloCircuitBreaker;

    public HelloController(WebClient.Builder builder, FragileApi fragileApi, CircuitBreakerRegistry circuitBreakerRegistry) {
        log.info("Creating Hello Controller");
        this.webClient = builder
            .baseUrl("http://localhost:" + fragileApi.getPort())
            .build();
        this.helloCircuitBreaker = circuitBreakerRegistry.circuitBreaker("hello");
    }

    @GetMapping(path = "/")
    public Flux<String> hello() {
        return this.webClient.get()
            .retrieve()
            .bodyToFlux(String.class)
            .transform(CircuitBreakerOperator.of(this.helloCircuitBreaker))
            .onErrorReturn("Fallback!");
    }
}