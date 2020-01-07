package com.lombardrisk.ignis.design.server.configuration.adapters.pipeline;

import com.lombardrisk.ignis.design.server.configuration.DatasourceConfig;
import com.lombardrisk.ignis.design.server.jpa.JpaRepositoryConfiguration;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineRepository;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineStepRepository;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineStepTestRepository;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineStepTestRowRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Slf4j
@Configuration
@Import({ DatasourceConfig.class, JpaRepositoryConfiguration.class })
public class PipelineAdapterConfiguration {

    private final JpaRepositoryConfiguration repositoryDependencies;

    public PipelineAdapterConfiguration(
            final JpaRepositoryConfiguration repositoryDependencies) {
        this.repositoryDependencies = repositoryDependencies;
    }

    @Bean
    public PipelineRepository pipelineRepository() {
        return new PipelineAdapter(repositoryDependencies.getPipelineRepository());
    }

    @Bean
    public PipelineStepRepository pipelineStepRepository() {
        return new PipelineStepAdapter(repositoryDependencies.getPipelineStepRepository());
    }

    @Bean
    public PipelineStepTestRepository pipelineStepTestRepository() {
        return new PipelineStepTestAdapter(repositoryDependencies.getPipelineStepTestJpaRepository());
    }

    @Bean
    public PipelineStepTestRowRepository pipelineStepTestRowRepository() {
        return new PipelineStepTestRowAdapter(
                repositoryDependencies.getInputDataRowJpaRepository(),
                repositoryDependencies.getExpectedDataRowJpaRepository(),
                repositoryDependencies.getActualDataRowJpaRepository(),
                repositoryDependencies.getPipelineStepTestRowJpaRepository());
    }
}
