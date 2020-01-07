
package com.lombardrisk.ignis.server.config.job;

import com.lombardrisk.ignis.server.util.spark.SparkConfFactory;
import com.lombardrisk.ignis.web.common.config.TogglzConfiguration;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "spark-defaults")
public class SparkDefaultsConfiguration {

    private Map<String, String> conf;

    private final Environment environment;
    private final TogglzConfiguration togglzConfiguration;

    @Autowired
    public SparkDefaultsConfiguration(
            final Environment environment,
            final TogglzConfiguration togglzConfiguration) {
        this.environment = environment;
        this.togglzConfiguration = togglzConfiguration;
    }

    @Bean
    public SparkConfFactory sparkConfFactory() {
        return new SparkConfFactory(conf, environment, togglzConfiguration);
    }
}