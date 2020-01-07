package com.lombardrisk.ignis.functional.test.config;

import com.lombardrisk.ignis.functional.test.config.properties.ClientProperties;
import com.lombardrisk.ignis.functional.test.steps.DesignStudioSteps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DesignStudioClientConfig.class)
public class DesignStudioStepConfig {
    private final DesignStudioClientConfig designStudioClientConfig;
    private final ClientProperties clientProperties;

    @Autowired
    public DesignStudioStepConfig(
            final DesignStudioClientConfig designStudioClientConfig,
            final ClientProperties clientProperties) {
        this.designStudioClientConfig = designStudioClientConfig;
        this.clientProperties = clientProperties;
    }

    @Bean
    public DesignStudioSteps designStudioSteps() {
        return new DesignStudioSteps(
                designStudioClientConfig.designProductConfigClient(),
                designStudioClientConfig.schemaClient(),
                designStudioClientConfig.designPipelineClient(),
                designStudioClientConfig.designTableService(),
                designStudioClientConfig.ruleService(),
                designStudioClientConfig.pipelineService(),
                clientProperties);
    }
}
