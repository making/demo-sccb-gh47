package com.example.demowebmvcsccbr4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;

import java.util.concurrent.atomic.AtomicBoolean;

public class OnceCustomizer<T> implements Customizer<T> {

    private final Logger log = LoggerFactory.getLogger(OnceCustomizer.class);

    private final AtomicBoolean customized = new AtomicBoolean(false);

    private final Customizer<T> delegate;

    private OnceCustomizer(Customizer<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void customize(T t) {
        if (this.customized.compareAndSet(false, true)) {
            log.info("Customize {}", t);
            this.delegate.customize(t);
        }
    }

    public static <T> OnceCustomizer<T> of(Customizer<T> customizer) {
        return new OnceCustomizer<>(customizer);
    }
}
