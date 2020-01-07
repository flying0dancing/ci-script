package com.lombardrisk.ignis.design.server.configuration;

import com.lombardrisk.ignis.design.server.configuration.adapters.pipeline.PipelineAdapterConfiguration;
import com.lombardrisk.ignis.design.server.configuration.adapters.product.ProductAdapterConfiguration;
import com.lombardrisk.ignis.design.server.configuration.properties.FeedbackProperties;
import com.lombardrisk.ignis.design.server.configuration.web.WebConfiguration;
import com.lombardrisk.ignis.design.server.controller.FieldController;
import com.lombardrisk.ignis.design.server.controller.PipelineController;
import com.lombardrisk.ignis.design.server.controller.PipelineStepController;
import com.lombardrisk.ignis.design.server.controller.PipelineStepTestController;
import com.lombardrisk.ignis.design.server.controller.ProductConfigController;
import com.lombardrisk.ignis.design.server.controller.RuleController;
import com.lombardrisk.ignis.design.server.controller.SchemaController;
import com.lombardrisk.ignis.design.server.controller.ScriptletController;
import com.lombardrisk.ignis.design.server.feedback.FeedbackController;
import com.lombardrisk.ignis.design.server.pipeline.PipelineConfiguration;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfiguration;
import com.lombardrisk.ignis.design.server.productconfig.schema.SchemaConfiguration;
import com.lombardrisk.ignis.design.server.scriptlet.ScriptletServiceConfiguration;
import com.lombardrisk.ignis.web.common.config.FeatureFlagConfiguration;
import com.lombardrisk.ignis.web.common.exception.GlobalExceptionHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties({ FeedbackProperties.class })
@Import({
        FeatureFlagConfiguration.class,
        WebConfiguration.class,
        PipelineAdapterConfiguration.class,
        PipelineConfiguration.class,
        ProductAdapterConfiguration.class,
        ProductConfiguration.class,
        SchemaConfiguration.class,
        ScriptletServiceConfiguration.class })
public class DesignConfiguration {

    private final FeedbackProperties feedbackProperties;
    private final ProductConfiguration productModuleConfiguration;
    private final SchemaConfiguration schemaModuleConfiguration;
    private final PipelineConfiguration pipelineModuleConfiguration;
    private final ScriptletServiceConfiguration scriptletServiceConfiguration;

    public DesignConfiguration(
            final FeedbackProperties feedbackProperties,
            final ProductConfiguration productModuleConfiguration,
            final SchemaConfiguration schemaModuleConfiguration,
            final PipelineConfiguration pipelineModuleConfiguration,
            final ScriptletServiceConfiguration scriptletServiceConfiguration) {
        this.feedbackProperties = feedbackProperties;
        this.productModuleConfiguration = productModuleConfiguration;
        this.schemaModuleConfiguration = schemaModuleConfiguration;
        this.pipelineModuleConfiguration = pipelineModuleConfiguration;
        this.scriptletServiceConfiguration = scriptletServiceConfiguration;
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .build();
    }

    @Bean
    public SchemaController schemaController() {
        return new SchemaController(
                schemaModuleConfiguration.schemaService(),
                productModuleConfiguration.productConfigService(),
                productModuleConfiguration.schemaConverter());
    }

    @Bean
    public ProductConfigController productConfigController() {
        return new ProductConfigController(
                productModuleConfiguration.productConfigService(),
                productModuleConfiguration.productConfigExportService(),
                productModuleConfiguration.productConfigImportService(),
                productModuleConfiguration.productConfigValidator());
    }

    @Bean
    public RuleController validationRuleController() {
        return new RuleController(
                schemaModuleConfiguration.ruleService(),
                productModuleConfiguration.validationRuleRequestConverter(),
                productModuleConfiguration.validationRuleExportConverter());
    }

    @Bean
    public FieldController fieldController() {
        return new FieldController(schemaModuleConfiguration.fieldService());
    }

    @Bean
    public PipelineController pipelineController() {
        return new PipelineController(pipelineModuleConfiguration.pipelineService());
    }

    @Bean
    public PipelineStepController pipelineStepController() {
        return new PipelineStepController(pipelineModuleConfiguration.pipelineStepService());
    }

    @Bean
    public FeedbackController feedbackController() {
        return new FeedbackController(feedbackProperties, restTemplate());
    }

    @Bean
    public PipelineStepTestController pipelineStepTestController() {
        return new PipelineStepTestController(
                pipelineModuleConfiguration.pipelineStepTestService(),
                pipelineModuleConfiguration.pipelineStepTestExecuteService(),
                pipelineModuleConfiguration.inputDataRowService());
    }

    @Bean
    public ScriptletController scriptletController() {
        return new ScriptletController(scriptletServiceConfiguration.scriptletService());
    }
}
