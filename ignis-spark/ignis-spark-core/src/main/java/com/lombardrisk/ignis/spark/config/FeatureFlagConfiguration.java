package com.lombardrisk.ignis.spark.config;

import com.lombardrisk.ignis.feature.FeatureAspect;
import com.lombardrisk.ignis.feature.IgnisFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.file.FileBasedStateRepository;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;

import java.io.File;
import java.io.IOException;

@Configuration
@Slf4j
public class FeatureFlagConfiguration {

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${job.features.file}")
    private String jobFeaturesFileLocation;

    @Bean
    @Primary
    public UserProvider userProvider() {
        return () -> new SimpleFeatureUser("admin", true);
    }

    @Bean
    public FeatureManager featureManager() {
        log.debug("Creating feature manager with properties file {}", jobFeaturesFileLocation);
        try {
            File file = resourceLoader.getResource(jobFeaturesFileLocation).getFile();

            return FeatureManagerBuilder.begin()
                    .name("SparkJobFeatureManager")
                    .featureEnum(IgnisFeature.class)
                    .userProvider(userProvider())
                    .stateRepository(new FileBasedStateRepository(file))
                    .build();

        } catch (final IOException e) {
            throw new IllegalStateException("Error in reading feature file", e);
        }
    }

    @Bean
    public FeatureAspect featureAspect(final FeatureManager featureManager) {
        return new FeatureAspect(featureManager);
    }
}
