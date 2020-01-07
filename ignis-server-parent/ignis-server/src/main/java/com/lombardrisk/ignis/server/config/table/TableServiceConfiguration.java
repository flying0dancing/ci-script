package com.lombardrisk.ignis.server.config.table;

import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.server.config.product.ProductConfigConverterConfiguration;
import com.lombardrisk.ignis.server.config.product.ProductRepositoryConfiguration;
import com.lombardrisk.ignis.server.product.rule.ValidationRuleService;
import com.lombardrisk.ignis.server.product.table.FieldRepository;
import com.lombardrisk.ignis.server.product.table.TableService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class TableServiceConfiguration {

    private final ProductRepositoryConfiguration productRepositoryConfiguration;
    private final ProductConfigConverterConfiguration productConfigConverterConfiguration;
    private final TimeSource timeSource;
    private final FieldRepository fieldRepository;

    @Autowired
    public TableServiceConfiguration(
            final ProductRepositoryConfiguration productRepositoryConfiguration,
            final ProductConfigConverterConfiguration productConfigConverterConfiguration,
            final TimeSource timeSource,
            final FieldRepository fieldRepository) {
        this.productRepositoryConfiguration = productRepositoryConfiguration;
        this.productConfigConverterConfiguration = productConfigConverterConfiguration;
        this.timeSource = timeSource;
        this.fieldRepository = fieldRepository;
    }

    @Bean
    public TableService tableService() {
        return new TableService(
                productRepositoryConfiguration.tableRepository(),
                timeSource,
                fieldRepository,
                productConfigConverterConfiguration.pageToViewConverter(),
                productConfigConverterConfiguration.tableToSchemaWithRulesView());
    }

    @Bean
    public ValidationRuleService validationRuleService() {
        return new ValidationRuleService(productRepositoryConfiguration.ruleRepository());
    }
}
