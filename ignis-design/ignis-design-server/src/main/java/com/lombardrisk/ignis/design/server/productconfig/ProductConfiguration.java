package com.lombardrisk.ignis.design.server.productconfig;

import com.lombardrisk.ignis.common.jexl.JexlEngineFactory;
import com.lombardrisk.ignis.design.field.api.DatasetSchemaFieldRepository;
import com.lombardrisk.ignis.design.field.api.FieldRepository;
import com.lombardrisk.ignis.design.server.pipeline.PipelineConfiguration;
import com.lombardrisk.ignis.design.server.productconfig.api.ProductConfigRepository;
import com.lombardrisk.ignis.design.server.productconfig.api.ProductPipelineRepository;
import com.lombardrisk.ignis.design.server.productconfig.api.ValidationRuleRepository;
import com.lombardrisk.ignis.design.server.productconfig.converter.ProductConfigConverter;
import com.lombardrisk.ignis.design.server.productconfig.converter.SchemaConverter;
import com.lombardrisk.ignis.design.server.productconfig.converter.ValidationRuleRequestConverter;
import com.lombardrisk.ignis.design.server.productconfig.converter.request.SchemaRequestConverter;
import com.lombardrisk.ignis.design.server.productconfig.export.ProductConfigExportFileService;
import com.lombardrisk.ignis.design.server.productconfig.export.ProductConfigImportFileService;
import com.lombardrisk.ignis.design.server.productconfig.export.ProductConfigZipObjectMapper;
import com.lombardrisk.ignis.design.server.productconfig.export.converter.FieldExportConverter;
import com.lombardrisk.ignis.design.server.productconfig.export.converter.ProductConfigExportConverter;
import com.lombardrisk.ignis.design.server.productconfig.export.converter.ValidationRuleExportConverter;
import com.lombardrisk.ignis.design.server.productconfig.export.validator.ProductImportValidator;
import com.lombardrisk.ignis.design.server.productconfig.rule.test.RuleExpressionTester;
import com.lombardrisk.ignis.design.server.productconfig.schema.SchemaConfiguration;
import com.lombardrisk.ignis.design.server.productconfig.validation.ProductConfigValidator;
import lombok.AllArgsConstructor;
import org.apache.commons.jexl3.JexlEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ SchemaConfiguration.class, PipelineConfiguration.class })
@AllArgsConstructor
public class ProductConfiguration {

    private final SchemaConfiguration schemaConfiguration;
    private final PipelineConfiguration pipelineConfiguration;
    private final ProductConfigRepository productRepository;
    private final DatasetSchemaFieldRepository datasetSchemaFieldRepository;
    private final FieldRepository fieldRepository;
    private final ValidationRuleRepository validationRuleRepository;
    private final ProductPipelineRepository productPipelineRepository;

    @Bean
    public ProductConfigExportConverter productConfigExportConverter() {
        return new ProductConfigExportConverter(schemaConfiguration.schemaExportConverter());
    }

    @Bean
    public ProductConfigConverter productConfigConverter() {
        return new ProductConfigConverter();
    }

    @Bean
    public SchemaConverter schemaConverter() {
        return new SchemaConverter();
    }

    @Bean
    public SchemaRequestConverter schemaRequestConverter() {
        return new SchemaRequestConverter();
    }

    @Bean
    public FieldExportConverter fieldExportConverter() {
        return new FieldExportConverter();
    }

    @Bean
    public ValidationRuleExportConverter validationRuleExportConverter() {
        return new ValidationRuleExportConverter(fieldExportConverter());
    }

    @Bean
    public ValidationRuleRequestConverter validationRuleRequestConverter() {
        return new ValidationRuleRequestConverter();
    }

    @Bean
    public JexlEngine jexlEngine() {
        return JexlEngineFactory.jexlEngine().create();
    }

    @Bean
    public RuleExpressionTester ruleExpressionTester() {
        return new RuleExpressionTester(jexlEngine());
    }

    @Bean
    public ProductImportValidator productImportValidator() {
        return new ProductImportValidator(
                productRepository,
                schemaConfiguration.createValidator(),
                schemaConfiguration.schemaExportConverter(),
                pipelineConfiguration.pipelineConstraintsValidator());
    }

    @Bean
    public ProductConfigService productConfigService() {
        return new ProductConfigService(
                schemaConfiguration.schemaService(),
                productRepository,
                productConfigConverter(),
                schemaRequestConverter(),
                productPipelineRepository);
    }

    @Bean
    public ProductConfigValidator productConfigValidator() {
        return new ProductConfigValidator(
                productPipelineRepository,
                pipelineConfiguration.pipelineValidator(),
                productConfigService());
    }

    @Bean
    public ProductConfigZipObjectMapper productConfigZipObjectMapper() {
        return new ProductConfigZipObjectMapper();
    }

    @Bean
    public ProductConfigExportFileService productConfigExportService() {
        return new ProductConfigExportFileService(
                productPipelineRepository,
                productConfigExportConverter(),
                productConfigZipObjectMapper(),
                productConfigService());
    }

    @Bean
    public ProductConfigImportFileService productConfigImportFileService() {
        return new ProductConfigImportFileService(productConfigZipObjectMapper());
    }

    @Bean
    public ProductConfigImportService productConfigImportService() {
        return new ProductConfigImportService(
                productConfigImportFileService(),
                productImportValidator(),
                schemaConfiguration.schemaExportConverter(),
                productConfigService(),
                schemaConfiguration.schemaService(),
                schemaConfiguration.fieldService(),
                schemaConfiguration.ruleService(),
                pipelineConfiguration.pipelineService(),
                pipelineConfiguration.pipelineStepService(),
                productRepository);
    }
}
