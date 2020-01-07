package com.lombardrisk.ignis.functional.test.config;

import com.lombardrisk.ignis.client.internal.FeaturesClient;
import com.lombardrisk.ignis.functional.test.feature.FeatureService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(FunctionalTestClientConfig.class)
public class FeaturesConfig {

    private final FeaturesClient featuresClient;

    public FeaturesConfig(final FeaturesClient featuresClient) {
        this.featuresClient = featuresClient;
    }

    @Bean
    public FeatureService featureService() {
        return new FeatureService(featuresClient);
    }

}
