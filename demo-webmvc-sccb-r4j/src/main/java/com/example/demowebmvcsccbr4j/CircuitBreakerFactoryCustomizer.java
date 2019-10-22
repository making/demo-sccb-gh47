package com.example.demowebmvcsccbr4j;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CircuitBreakerFactoryCustomizer implements Customizer<Resilience4JCircuitBreakerFactory> {

    @Override
    public void customize(Resilience4JCircuitBreakerFactory factory) {
        final Logger log = LoggerFactory.getLogger("CIRCUIT_BREAKER");
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
        factory.addCircuitBreakerCustomizer(OnceCustomizer.of(circuitBreaker -> {
            final CircuitBreaker.EventPublisher eventPublisher = circuitBreaker.getEventPublisher();
            eventPublisher.onStateTransition(event -> log.info("{}: {}", event.getCircuitBreakerName(), event.getStateTransition()));
        }), "hello");
    }
}
