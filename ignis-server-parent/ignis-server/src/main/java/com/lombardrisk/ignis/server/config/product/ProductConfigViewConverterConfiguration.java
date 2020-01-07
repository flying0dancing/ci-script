package com.lombardrisk.ignis.server.config.product;

import com.lombardrisk.ignis.server.config.ApplicationConf;
import com.lombardrisk.ignis.server.product.productconfig.view.ProductConfigExportConverter;
import com.lombardrisk.ignis.server.product.rule.view.ValidationRuleExportConverter;
import com.lombardrisk.ignis.server.product.table.view.FieldExportConverter;
import com.lombardrisk.ignis.server.product.table.view.SchemaExportConverter;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor(onConstructor = @__({ @Autowired }))
public class ProductConfigViewConverterConfiguration {

    private final ApplicationConf applicationConf;

    @Bean
    public ProductConfigExportConverter productConfigViewConverter() {
        return new ProductConfigExportConverter(tableViewConverter());
    }

    @Bean
    public SchemaExportConverter tableViewConverter() {
        return new SchemaExportConverter(
                fieldViewConverter(), validationRuleViewConverter(), applicationConf.timeSource());
    }

    @Bean
    public FieldExportConverter fieldViewConverter() {
        return new FieldExportConverter();
    }

    @Bean
    public ValidationRuleExportConverter validationRuleViewConverter() {
        return new ValidationRuleExportConverter();
    }
}
