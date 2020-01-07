package com.lombardrisk.ignis.server.config.pipeline;

import com.lombardrisk.ignis.server.config.ApplicationConf;
import com.lombardrisk.ignis.server.config.dataset.DatasetServiceConfiguration;
import com.lombardrisk.ignis.server.config.table.TableServiceConfiguration;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.PipelineInvocationJpaRepository;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.PipelineInvocationService;
import com.lombardrisk.ignis.server.job.pipeline.model.PipelineInputValidator;
import com.lombardrisk.ignis.server.product.pipeline.PipelineImportService;
import com.lombardrisk.ignis.server.product.pipeline.PipelineJpaRepository;
import com.lombardrisk.ignis.server.product.pipeline.PipelineService;
import com.lombardrisk.ignis.server.product.pipeline.PipelineStepService;
import com.lombardrisk.ignis.server.product.pipeline.SelectJpaRepository;
import com.lombardrisk.ignis.server.product.pipeline.validation.PipelineImportBeanValidator;
import com.lombardrisk.ignis.web.common.config.FeatureFlagConfiguration;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.manager.FeatureManager;

@Configuration
@Getter
public class PipelineServiceConfiguration {

    private final ApplicationConf applicationConf;
    private final DatasetServiceConfiguration datasetServiceConfiguration;
    private final TableServiceConfiguration tableServiceConfiguration;
    private final PipelineJpaRepository pipelineJpaRepository;
    private final SelectJpaRepository selectJpaRepository;
    private final PipelineInvocationJpaRepository pipelineInvocationJpaRepository;
    private final FeatureFlagConfiguration featureFlagConfiguration;

    public PipelineServiceConfiguration(
            final ApplicationConf applicationConf,
            final DatasetServiceConfiguration datasetServiceConfiguration,
            final TableServiceConfiguration tableServiceConfiguration,
            final PipelineJpaRepository pipelineJpaRepository,
            final SelectJpaRepository selectJpaRepository,
            final PipelineInvocationJpaRepository pipelineInvocationJpaRepository,
            final FeatureFlagConfiguration featureFlagConfiguration) {

        this.applicationConf = applicationConf;
        this.datasetServiceConfiguration = datasetServiceConfiguration;
        this.tableServiceConfiguration = tableServiceConfiguration;
        this.pipelineJpaRepository = pipelineJpaRepository;
        this.selectJpaRepository = selectJpaRepository;
        this.pipelineInvocationJpaRepository = pipelineInvocationJpaRepository;
        this.featureFlagConfiguration = featureFlagConfiguration;
    }

    @Bean
    public PipelineService pipelineService() {
        return new PipelineService(pipelineJpaRepository);
    }

    @Bean
    public PipelineStepService pipelineStepService() {
        return new PipelineStepService(selectJpaRepository);
    }

    @Bean
    public PipelineImportService pipelineImportService() {
        return new PipelineImportService(
                pipelineImportValidator(), pipelineService());
    }

    @Bean
    public PipelineInvocationService pipelineInvocationService() {
        return new PipelineInvocationService(applicationConf.timeSource(), pipelineInvocationJpaRepository);
    }

    @Bean
    public PipelineImportBeanValidator pipelineImportValidator() {
        return new PipelineImportBeanValidator(applicationConf.localValidatorFactoryBean().getValidator());
    }

    @Bean
    public FeatureManager featureManager() {
        return featureFlagConfiguration.featureManager();
    }

    @Bean
    public PipelineInputValidator pipelineInputValidator() {
        return new PipelineInputValidator(
                pipelineService(),
                pipelineStepService(),
                datasetServiceConfiguration.datasetService(),
                tableServiceConfiguration.tableService(),
                featureManager());
    }
}
