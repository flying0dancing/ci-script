package com.lombardrisk.ignis.functional.test.config;

import com.lombardrisk.ignis.client.external.IgnisClientConfig;
import com.lombardrisk.ignis.client.external.dataset.DatasetService;
import com.lombardrisk.ignis.functional.test.config.properties.IgnisServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(FunctionalTestClientConfig.class)
public class ExternalClientConfig {

    private final IgnisServerProperties ignisServerProperties;
    private final FunctionalTestClientConfig functionalTestClientConfig;

    @Autowired
    public ExternalClientConfig(
            final IgnisServerProperties ignisServerProperties,
            final FunctionalTestClientConfig functionalTestClientConfig) {
        this.ignisServerProperties = ignisServerProperties;
        this.functionalTestClientConfig = functionalTestClientConfig;
    }

    @Bean
    public IgnisClientConfig ignisClientConfig() {
        return new IgnisClientConfig(functionalTestClientConfig.clientConfig());
    }

    @Bean
    public DatasetService datasetService() {
        return ignisClientConfig().datasetService(ignisServerProperties.toConnectionProperties());
    }
}
