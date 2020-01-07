package com.lombardrisk.ignis.server.config.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class MetricsConfiguration {

    @Value("${ignis.environment}")
    private String environment;

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> customizer() {
        return customizedRegistry -> customizedRegistry
                .config()
                .meterFilter(MeterFilter.deny(meter -> !meter.getName().startsWith("ignis.")))
                .commonTags("application", "ignis", "environment", environment);
    }
}
