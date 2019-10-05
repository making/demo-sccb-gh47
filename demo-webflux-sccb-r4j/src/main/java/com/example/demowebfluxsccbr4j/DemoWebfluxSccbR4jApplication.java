package com.example.demowebfluxsccbr4j;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.EventProcessor;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@SpringBootApplication
public class DemoWebfluxSccbR4jApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoWebfluxSccbR4jApplication.class, args);
    }

    @Bean
    public ReactiveCircuitBreakerFactory reactiveResilience4JCircuitBreakerFactory(List<Customizer<ReactiveResilience4JCircuitBreakerFactory>> customizers) {
        final ReactiveResilience4JCircuitBreakerFactory reactiveResilience4JCircuitBreakerFactory = new ReactiveResilience4JCircuitBreakerFactory();
        // customizer.customize should be called here. Default customization is too late to be used ing HelloController's constructor
        customizers.forEach(customizer -> customizer.customize(reactiveResilience4JCircuitBreakerFactory));
        return reactiveResilience4JCircuitBreakerFactory;
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }

    @Component
    static class Initializer {

        private final MeterRegistry meterRegistry;

        private final CircuitBreakerRegistry circuitBreakerRegistry;

        public Initializer(MeterRegistry meterRegistry, CircuitBreakerRegistry circuitBreakerRegistry) {
            this.meterRegistry = meterRegistry;
            this.circuitBreakerRegistry = circuitBreakerRegistry;
        }

        @EventListener
        public void init(ApplicationReadyEvent ignored) {
            System.out.println("==== Registered Circuit Breaker ====");
            // This process must be executed after all circuit breakers are registered
            circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
                System.out.println(cb.getName());
            });
            final Logger log = LoggerFactory.getLogger("CIRCUIT_BREAKER");
            for (CircuitBreaker circuitBreaker : circuitBreakerRegistry.getAllCircuitBreakers()) {
                final CircuitBreaker.EventPublisher eventPublisher = circuitBreaker.getEventPublisher();
                if (!((EventProcessor) eventPublisher).hasConsumers()) {
                    eventPublisher.onStateTransition(event -> log.info("{}: {}", event.getCircuitBreakerName(), event.getStateTransition()));
                }
            }
            TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(circuitBreakerRegistry).bindTo(meterRegistry);
        }
    }
}
