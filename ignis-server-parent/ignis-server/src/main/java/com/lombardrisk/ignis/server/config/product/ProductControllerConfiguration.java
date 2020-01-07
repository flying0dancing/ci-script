package com.lombardrisk.ignis.server.config.product;

import com.lombardrisk.ignis.server.config.job.JobServiceConfiguration;
import com.lombardrisk.ignis.server.config.pipeline.PipelineServiceConfiguration;
import com.lombardrisk.ignis.server.controller.FeaturesController;
import com.lombardrisk.ignis.server.controller.pipeline.PipelineController;
import com.lombardrisk.ignis.server.controller.pipeline.PipelineStatusController;
import com.lombardrisk.ignis.server.controller.productconfig.ProductConfigController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.manager.FeatureManager;

@Configuration
public class ProductControllerConfiguration {

    @Value("${togglz.console.enabled:false}")
    private boolean togglzEnabled;

    private final FeatureManager featureManager;
    private final ProductServiceConfiguration productServices;
    private final JobServiceConfiguration jobServiceConfiguration;
    private final PipelineServiceConfiguration pipelineServiceConfiguration;

    @Autowired
    public ProductControllerConfiguration(
            final FeatureManager featureManager,
            final ProductServiceConfiguration productServices,
            final JobServiceConfiguration jobServiceConfiguration,
            final PipelineServiceConfiguration pipelineServiceConfiguration) {
        this.featureManager = featureManager;
        this.productServices = productServices;
        this.jobServiceConfiguration = jobServiceConfiguration;
        this.pipelineServiceConfiguration = pipelineServiceConfiguration;
    }

    @Bean
    public ProductConfigController productController() {
        return new ProductConfigController(
                productServices.productConfigService(),
                productServices.productConfigFileService(),
                jobServiceConfiguration.productConfigImporter());
    }

    @Bean
    public PipelineController pipelineController() {
        return new PipelineController(
                pipelineServiceConfiguration.pipelineService(),
                pipelineServiceConfiguration.pipelineInvocationService());
    }

    @Bean
    public PipelineStatusController pipelineStatusController() {
        return new PipelineStatusController(pipelineServiceConfiguration.pipelineInvocationService());
    }

    @Bean
    public FeaturesController featuresController() {
        return new FeaturesController(togglzEnabled, featureManager);
    }
}
