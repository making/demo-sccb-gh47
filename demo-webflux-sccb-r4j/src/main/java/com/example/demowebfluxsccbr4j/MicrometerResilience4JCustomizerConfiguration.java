package com.example.demowebfluxsccbr4j;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;

@Configuration
// see https://github.com/spring-cloud/spring-cloud-circuitbreaker/pull/51
public class MicrometerResilience4JCustomizerConfiguration {

    @Autowired
    private ReactiveResilience4JCircuitBreakerFactory factory;

    @Autowired
    private MeterRegistry meterRegistry;

    @PostConstruct
    public void init() throws Exception {
        final Field field = ReflectionUtils.findField(ReactiveResilience4JCircuitBreakerFactory.class, "circuitBreakerRegistry");
        ReflectionUtils.makeAccessible(field);
        final CircuitBreakerRegistry circuitBreakerRegistry = (CircuitBreakerRegistry) ReflectionUtils.getField(field, factory);

        if (factory != null) {
            TaggedCircuitBreakerMetrics
                .ofCircuitBreakerRegistry(circuitBreakerRegistry)
                .bindTo(meterRegistry);
        }
    }
}
