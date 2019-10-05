package com.example.demowebfluxr4j;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.EventProcessor;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

@SpringBootApplication
public class DemoWebfluxR4jApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoWebfluxR4jApplication.class, args);
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        final CircuitBreakerConfig config = CircuitBreakerConfig
            .custom()
            .failureRateThreshold(50) // 50%
            .ringBufferSizeInClosedState(30) // 30 * 0.5 => 15
            .ringBufferSizeInHalfOpenState(20) // 20 * 0.5 => 10
            .waitDurationInOpenState(Duration.ofSeconds(3) /* for demo */)
            .build();
        return CircuitBreakerRegistry.of(config);
    }


    @Bean
    public InitializingBean init(MeterRegistry meterRegistry, CircuitBreakerRegistry circuitBreakerRegistry) {
        final Logger log = LoggerFactory.getLogger("CIRCUIT_BREAKER");
        return () -> {
            log.info("==== Registered Circuit Breaker ====");
            circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
                log.info(cb.getName());
            });
            for (CircuitBreaker circuitBreaker : circuitBreakerRegistry.getAllCircuitBreakers()) {
                final CircuitBreaker.EventPublisher eventPublisher = circuitBreaker.getEventPublisher();
                if (!((EventProcessor) eventPublisher).hasConsumers()) {
                    eventPublisher.onStateTransition(event -> log.info("{}: {}", event.getCircuitBreakerName(), event.getStateTransition()));
                }
            }
            TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(circuitBreakerRegistry).bindTo(meterRegistry);
        };
    }
}
