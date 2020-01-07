package com.lombardrisk.ignis.server.config.product;

import com.lombardrisk.ignis.server.config.ApplicationConf;
import com.lombardrisk.ignis.server.product.productconfig.view.ProductConfigConverter;
import com.lombardrisk.ignis.server.product.rule.view.ValidationRuleConverter;
import com.lombardrisk.ignis.server.product.table.view.FieldConverter;
import com.lombardrisk.ignis.server.product.table.view.TableConverter;
import com.lombardrisk.ignis.server.product.table.view.TableToSchemaWithRulesView;
import com.lombardrisk.ignis.server.product.util.PageConverter;
import com.lombardrisk.ignis.server.product.util.PageToViewConverter;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor(onConstructor = @__({ @Autowired }))
public class ProductConfigConverterConfiguration {

    private final ApplicationConf applicationConf;

    @Bean
    public ProductConfigConverter productConfigConverter() {
        return new ProductConfigConverter(tableConverter());
    }

    @Bean
    public TableConverter tableConverter() {
        return new TableConverter(fieldConverter(), validationRuleConverter(), applicationConf.timeSource());
    }

    @Bean
    public FieldConverter fieldConverter() {
        return new FieldConverter();
    }

    @Bean
    public ValidationRuleConverter validationRuleConverter() {
        return new ValidationRuleConverter(fieldConverter());
    }

    @Bean
    public PageConverter pageConverter() {
        return new PageConverter();
    }

    @Bean
    public PageToViewConverter pageToViewConverter() {
        return new PageToViewConverter(pageConverter());
    }

    @Bean
    public TableToSchemaWithRulesView tableToSchemaWithRulesView() {
        return new TableToSchemaWithRulesView();
    }
}
