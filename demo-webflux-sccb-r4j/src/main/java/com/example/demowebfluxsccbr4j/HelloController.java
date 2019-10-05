package com.example.demowebfluxsccbr4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class HelloController {

    private final Logger log = LoggerFactory.getLogger(HelloController.class);

    private final WebClient webClient;

    private final ReactiveCircuitBreaker helloCircuitBreaker;

    public HelloController(WebClient.Builder builder, FragileApi fragileApi, ReactiveCircuitBreakerFactory circuitBreakerFactory) {
        log.info("Creating Hello Controller");
        this.webClient = builder
            .baseUrl("http://localhost:" + fragileApi.getPort())
            .build();
        this.helloCircuitBreaker = circuitBreakerFactory.create("hello");
        // Register Circuit Breaker before circuitBreakerRegistry.getAllCircuitBreakers is called
        this.helloCircuitBreaker.run(Mono.just("dummy to register a circuit breaker to the registry")).subscribe();
    }

    @GetMapping(path = "/")
    public Flux<String> hello() {
        return this.webClient.get()
            .retrieve()
            .bodyToFlux(String.class)
            .transform(x -> this.helloCircuitBreaker
                .run(x, t -> Flux.just("Fallback!")));
    }
}