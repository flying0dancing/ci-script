package com.lombardrisk.ignis.server.config;

import com.lombardrisk.ignis.fs.FileSystemTemplate;
import com.lombardrisk.ignis.server.health.HadoopHealthChecks;
import com.lombardrisk.ignis.server.health.MemoizedHealthCheck;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConditionalOnProperty(name = "management.health.hadoop.enabled", havingValue = "true")
public class HadoopHealthConfiguration {

    private static final Duration YARN_HEALTH_REFRESH_RATE = Duration.ofMinutes(2);
    private static final Duration HDFS_HEALTH_REFRESH_RATE = Duration.ofMinutes(2);

    private final YarnClient sparkYarnClient;
    private final FileSystemTemplate fileSystemTemplate;

    public HadoopHealthConfiguration(final YarnClient sparkYarnClient, final FileSystemTemplate fileSystemTemplate) {
        this.sparkYarnClient = sparkYarnClient;
        this.fileSystemTemplate = fileSystemTemplate;
    }

    @Bean
    public MemoizedHealthCheck yarnHealthCheck() {
        return HadoopHealthChecks.yarnHealthCheck(sparkYarnClient, YARN_HEALTH_REFRESH_RATE);
    }

    @Bean
    public MemoizedHealthCheck hdfsHealthCheck() {
        return HadoopHealthChecks.hdfsHealthCheck(fileSystemTemplate, HDFS_HEALTH_REFRESH_RATE);
    }
}
