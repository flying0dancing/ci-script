package com.lombardrisk.ignis.design.server.productconfig.schema;

import com.lombardrisk.ignis.common.jexl.JexlEngineFactory;
import com.lombardrisk.ignis.design.field.FieldService;
import com.lombardrisk.ignis.design.field.api.DatasetSchemaFieldRepository;
import com.lombardrisk.ignis.design.field.api.FieldDependencyRepository;
import com.lombardrisk.ignis.design.field.api.FieldRepository;
import com.lombardrisk.ignis.design.server.configuration.ServerConfiguration;
import com.lombardrisk.ignis.design.server.productconfig.api.SchemaRepository;
import com.lombardrisk.ignis.design.server.productconfig.api.ValidationRuleRepository;
import com.lombardrisk.ignis.design.server.productconfig.converter.SchemaConverter;
import com.lombardrisk.ignis.design.server.productconfig.converter.ValidationRuleRequestConverter;
import com.lombardrisk.ignis.design.server.productconfig.converter.request.SchemaRequestConverter;
import com.lombardrisk.ignis.design.server.productconfig.export.converter.FieldExportConverter;
import com.lombardrisk.ignis.design.server.productconfig.export.converter.SchemaExportConverter;
import com.lombardrisk.ignis.design.server.productconfig.export.converter.ValidationRuleExportConverter;
import com.lombardrisk.ignis.design.server.productconfig.rule.test.RuleExpressionTester;
import com.lombardrisk.ignis.design.server.productconfig.schema.validation.SchemaConstraintsValidator;
import com.lombardrisk.ignis.design.server.productconfig.schema.validation.UpdateValidator;
import lombok.AllArgsConstructor;
import org.apache.commons.jexl3.JexlEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ ServerConfiguration.class })
@AllArgsConstructor
public class SchemaConfiguration {

    private final SchemaRepository schemaRepository;
    private final DatasetSchemaFieldRepository datasetSchemaFieldRepository;
    private final FieldRepository fieldRepository;
    private final FieldDependencyRepository fieldDependencyRepository;
    private final ValidationRuleRepository validationRuleRepository;
    private final ServerConfiguration serverConfiguration;

    @Bean
    public SchemaExportConverter schemaExportConverter() {
        return new SchemaExportConverter(serverConfiguration.timeSource());
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
    public RuleService ruleService() {
        return new RuleService(schemaRepository, validationRuleRepository, ruleExpressionTester());
    }

    @Bean
    public UpdateValidator updateValidator() {
        return new UpdateValidator(schemaRepository);
    }

    @Bean
    public SchemaConstraintsValidator createValidator() {
        return new SchemaConstraintsValidator(schemaRepository);
    }

    @Bean
    public SchemaService schemaService() {
        return new SchemaService(
                schemaRepository,
                fieldService(),
                updateValidator(),
                createValidator()
        );
    }

    @Bean
    public FieldService fieldService() {
        return new FieldService(fieldRepository, datasetSchemaFieldRepository, fieldDependencyRepository);
    }
}
