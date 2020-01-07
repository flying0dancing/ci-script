package com.lombardrisk.ignis.server.config.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.datadog.DatadogConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.CompositeHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@ConditionalOnProperty(name = "management.metrics.export.datadog.enabled", havingValue = "true")
@Slf4j
public class DatadogMetricsConfiguration {

    private static final String IGNIS_HEALTH = "ignis.health";
    private static final String DB_HEALTH = "ignis.db_health";
    private static final String PHOENIX_HEALTH = "ignis.phoenix_health";
    private static final String YARN_HEALTH = "ignis.yarn_health";
    private static final String HDFS_HEALTH = "ignis.hdfs_health";
    private static final String DISK_SPACE_HEALTH = "ignis.disk_space_health";

    private static final String UP_STATUS_CODE = Status.UP.getCode();

    private final MeterRegistry registry;
    private final DatadogConfig datadogConfig;
    private final HealthAggregator healthAggregator;
    private final HealthIndicator dbHealthCheck;
    private final HealthIndicator phoenixHealthCheck;
    private final HealthIndicator yarnHealthCheck;
    private final HealthIndicator hdfsHealthCheck;
    private final HealthIndicator diskSpaceHealthIndicator;

    private CompositeHealthIndicator healthIndicator;

    @Autowired
    public DatadogMetricsConfiguration(
            final MeterRegistry registry,
            final DatadogConfig datadogConfig,
            final HealthAggregator healthAggregator,
            final HealthIndicator dbHealthCheck,
            final HealthIndicator phoenixHealthCheck,
            final HealthIndicator yarnHealthCheck,
            final HealthIndicator hdfsHealthCheck,
            final HealthIndicator diskSpaceHealthIndicator) {

        this.registry = registry;
        this.datadogConfig = datadogConfig;
        this.healthAggregator = healthAggregator;
        this.dbHealthCheck = dbHealthCheck;
        this.phoenixHealthCheck = phoenixHealthCheck;
        this.yarnHealthCheck = yarnHealthCheck;
        this.hdfsHealthCheck = hdfsHealthCheck;
        this.diskSpaceHealthIndicator = diskSpaceHealthIndicator;
    }

    @PostConstruct
    public void registerDatadogMetrics() {
        registry.config().commonTags("host", datadogConfig.hostTag());

        healthIndicator = new CompositeHealthIndicator(healthAggregator);
        healthIndicator.addHealthIndicator(DB_HEALTH, dbHealthCheck);
        healthIndicator.addHealthIndicator(PHOENIX_HEALTH, phoenixHealthCheck);
        healthIndicator.addHealthIndicator(YARN_HEALTH, yarnHealthCheck);
        healthIndicator.addHealthIndicator(HDFS_HEALTH, hdfsHealthCheck);
        healthIndicator.addHealthIndicator(DISK_SPACE_HEALTH, diskSpaceHealthIndicator);

        registerStatus(IGNIS_HEALTH, UP_STATUS_CODE);
        registerDetailStatus(DB_HEALTH, UP_STATUS_CODE);
        registerDetailStatus(PHOENIX_HEALTH, UP_STATUS_CODE);
        registerDetailStatus(YARN_HEALTH, UP_STATUS_CODE);
        registerDetailStatus(HDFS_HEALTH, UP_STATUS_CODE);
        registerDetailStatus(DISK_SPACE_HEALTH, UP_STATUS_CODE);
    }

    private void registerStatus(final String metricName, final String statusCode) {
        registry.gauge(metricName, Tags.of("status", statusCode), healthIndicator,
                health -> getStatusValue(health.health(), statusCode));
    }

    private void registerDetailStatus(final String metricName, final String statusCode) {
        registry.gauge(metricName, Tags.of("status", statusCode), healthIndicator,
                health -> getStatusValue((Health) health.health().getDetails().get(metricName), statusCode));
    }

    private int getStatusValue(final Health health, final String statusCode) {
        return statusCode.equals(health.getStatus().getCode()) ? 1 : 0;
    }
}
