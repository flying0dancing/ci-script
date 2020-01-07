package com.lombardrisk.ignis.web.common.config;

import com.lombardrisk.ignis.feature.FeatureAspect;
import com.lombardrisk.ignis.web.common.config.TogglzConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;

import java.io.File;
import java.io.IOException;

@Configuration
public class FeatureFlagConfiguration {

    @Value("${togglz.features-file}")
    private File featuresFile;

    @Bean
    @Primary
    public UserProvider userProvider() {
        return () -> new SimpleFeatureUser("admin", true);
    }

    @Bean
    public FeatureAspect featureAspect() {
        return new FeatureAspect(featureManager());
    }

    @Bean
    public FeatureManager featureManager() {
        return FeatureManagerBuilder.begin()
                .togglzConfig(togglzConfig())
                .build();
    }

    @Bean
    public TogglzConfiguration togglzConfig() {
        try {
            return new TogglzConfiguration(createFeaturesPropertyFile(), userProvider());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private File createFeaturesPropertyFile() throws IOException {
        if (!featuresFile.exists()
                && !featuresFile.createNewFile()) {
            throw new IllegalStateException("Could not create feature file");
        }
        return featuresFile;
    }
}
