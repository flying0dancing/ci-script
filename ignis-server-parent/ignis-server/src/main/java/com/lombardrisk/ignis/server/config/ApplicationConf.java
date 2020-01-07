package com.lombardrisk.ignis.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lombardrisk.ignis.api.calendar.HolidayCalendarModule;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.server.batch.staging.HdfsDatasetConf;
import com.lombardrisk.ignis.server.config.job.HadoopSiteProperties;
import com.lombardrisk.ignis.server.config.job.SparkDefaultsConfiguration;
import com.lombardrisk.ignis.server.util.TempDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Clock;
import java.util.TimeZone;

/**
 * entry point to import properties configuration class
 */
@Configuration
@EnableConfigurationProperties({ HdfsDatasetConf.class, HadoopSiteProperties.class, SparkDefaultsConfiguration.class })
public class ApplicationConf {

    private final Environment environment;

    @Autowired
    public ApplicationConf(final Environment environment) {
        this.environment = environment;
    }

    @Bean
    public TimeSource timeSource() {
        return new TimeSource(Clock.systemDefaultZone());
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModules(
                new Jdk8Module(),
                new JavaTimeModule(),
                new GuavaModule(),
                new HolidayCalendarModule());
        objectMapper.setTimeZone(TimeZone.getTimeZone(timeSource().getClock().getZone()));
        return objectMapper;
    }

    @Bean
    public LocalValidatorFactoryBean localValidatorFactoryBean() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public TempDirectory tempDirectory() {
        return new TempDirectory(environment);
    }
}
