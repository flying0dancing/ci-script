package com.lombardrisk.ignis.spark.config.initializer;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

public class CorrelationIdInitializer implements ApplicationContextInitializer<GenericApplicationContext> {

    private final String correlationId;

    public CorrelationIdInitializer(final String correlationId) {
        this.correlationId = correlationId;
    }

    @Override
    public void initialize(final GenericApplicationContext context) {
        context.registerBean("correlationId", String.class, () -> correlationId);
    }
}
