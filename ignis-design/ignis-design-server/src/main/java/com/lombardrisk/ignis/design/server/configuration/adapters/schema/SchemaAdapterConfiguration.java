package com.lombardrisk.ignis.design.server.configuration.adapters.schema;

import com.lombardrisk.ignis.design.field.api.DatasetSchemaFieldRepository;
import com.lombardrisk.ignis.design.field.api.FieldDependencyRepository;
import com.lombardrisk.ignis.design.field.api.FieldRepository;
import com.lombardrisk.ignis.design.server.configuration.DatasourceConfig;
import com.lombardrisk.ignis.design.server.jpa.JpaRepositoryConfiguration;
import com.lombardrisk.ignis.design.server.productconfig.api.SchemaRepository;
import com.lombardrisk.ignis.design.server.productconfig.api.ValidationRuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Slf4j
@Configuration
@Import({ DatasourceConfig.class, JpaRepositoryConfiguration.class })
public class SchemaAdapterConfiguration {

    private final JpaRepositoryConfiguration productRepositoryDependencies;

    public SchemaAdapterConfiguration(
            final JpaRepositoryConfiguration productRepositoryDependencies) {
        this.productRepositoryDependencies = productRepositoryDependencies;
    }

    @Bean
    public DatasetSchemaFieldRepository datasetSchemaFieldRepository() {
        return new DatasetSchemaFieldAdapter(productRepositoryDependencies.getDatasetSchemaFieldJpaRepository());
    }

    @Bean
    public FieldRepository fieldRepository() {
        return new FieldAdapter(
                productRepositoryDependencies.getFieldJpaRepository(),
                productRepositoryDependencies.getSchemaRepository());
    }

    @Bean
    public SchemaRepository schemaRepository() {
        return new SchemaAdapter(productRepositoryDependencies.getSchemaRepository());
    }

    @Bean
    public ValidationRuleRepository validationRuleRepository() {
        return new ValidationRuleAdapter(productRepositoryDependencies.getRuleRepository());
    }

    @Bean
    public FieldDependencyRepository fieldDependencyRepository() {
        return new FieldDependencyAdapter(
                productRepositoryDependencies.getPipelineSelectJpaRepository(),
                productRepositoryDependencies.getPipelineJoinJpaRepository(),
                productRepositoryDependencies.getPipelineStepTestCellJpaRepository());
    }
}
