package com.example.demowebfluxsccbr4j;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.EventProcessor;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CircuitBreakerFactoryCustomizer implements Customizer<ReactiveResilience4JCircuitBreakerFactory> {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public CircuitBreakerFactoryCustomizer(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @Override
    public void customize(ReactiveResilience4JCircuitBreakerFactory factory) {
        final Logger log = LoggerFactory.getLogger("CIRCUIT_BREAKER");
        factory
            .configureCircuitBreakerRegistry(this.circuitBreakerRegistry);
        factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
            .circuitBreakerConfig(CircuitBreakerConfig
                .custom()
                .failureRateThreshold(50) // 50%
                .ringBufferSizeInClosedState(30) // 30 * 0.5 => 15
                .ringBufferSizeInHalfOpenState(20) // 20 * 0.5 => 10
                .waitDurationInOpenState(Duration.ofSeconds(3) /* for demo */)
                .build())
            .timeLimiterConfig(TimeLimiterConfig
                .custom()
                .build())
            .build());
        factory.addCircuitBreakerCustomizer(circuitBreaker -> {
            final CircuitBreaker.EventPublisher eventPublisher = circuitBreaker.getEventPublisher();
            if (!((EventProcessor) eventPublisher).hasConsumers()) {
                eventPublisher.onStateTransition(event -> log.info("{}: {}", event.getCircuitBreakerName(), event.getStateTransition()));
            }
        }, "hello");
    }
}
