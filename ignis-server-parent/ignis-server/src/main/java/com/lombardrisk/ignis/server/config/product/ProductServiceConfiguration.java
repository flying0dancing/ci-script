package com.lombardrisk.ignis.server.config.product;

import com.lombardrisk.ignis.server.config.ApplicationConf;
import com.lombardrisk.ignis.server.config.pipeline.PipelineServiceConfiguration;
import com.lombardrisk.ignis.server.config.table.TableServiceConfiguration;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestRepository;
import com.lombardrisk.ignis.server.product.ProductConfigZipObjectMapper;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigFileService;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigImportDiffer;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigImportEntityService;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigImportValidator;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class ProductServiceConfiguration {

    private final ProductRepositoryConfiguration productRepositoryConfiguration;
    private final ProductConfigConverterConfiguration converterConfiguration;
    private final ProductConfigViewConverterConfiguration productConfigViewConverterConfiguration;
    private final PipelineServiceConfiguration pipelineServiceConfiguration;
    private final TableServiceConfiguration tableServiceConfiguration;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ApplicationConf applicatConf;

    @Autowired
    public ProductServiceConfiguration(
            final ProductRepositoryConfiguration productRepositoryConfiguration,
            final ProductConfigConverterConfiguration converterConfiguration,
            final ProductConfigViewConverterConfiguration productConfigViewConverterConfiguration,
            final PipelineServiceConfiguration pipelineServiceConfiguration,
            final TableServiceConfiguration tableServiceConfiguration,
            final ServiceRequestRepository serviceRequestRepository,
            final ApplicationConf applicatConf) {
        this.productRepositoryConfiguration = productRepositoryConfiguration;
        this.converterConfiguration = converterConfiguration;
        this.productConfigViewConverterConfiguration = productConfigViewConverterConfiguration;
        this.pipelineServiceConfiguration = pipelineServiceConfiguration;
        this.tableServiceConfiguration = tableServiceConfiguration;
        this.serviceRequestRepository = serviceRequestRepository;
        this.applicatConf = applicatConf;
    }

    @Bean
    public ProductConfigService productConfigService() {
        return new ProductConfigService(
                productRepositoryConfiguration.productConfigRepository(),
                applicatConf.localValidatorFactoryBean().getValidator(),
                tableServiceConfiguration.tableService(),
                applicatConf.timeSource());
    }

    @Bean
    public ProductConfigFileService productConfigFileService() {
        return new ProductConfigFileService(productConfigZipObjectMapper());
    }

    @Bean
    public ProductConfigZipObjectMapper productConfigZipObjectMapper() {
        return new ProductConfigZipObjectMapper();
    }

    @Bean
    public ProductConfigImportValidator productConfigImportValidator() {
        return new ProductConfigImportValidator(
                applicatConf.localValidatorFactoryBean().getValidator(),
                productRepositoryConfiguration.productConfigRepository());
    }

    @Bean
    public ProductConfigImportDiffer productConfigImportDiff() {
        return new ProductConfigImportDiffer(productRepositoryConfiguration.productConfigRepository());
    }

    @Bean
    public ProductConfigImportEntityService productConfigImportEntityService() {
        return new ProductConfigImportEntityService(
                productRepositoryConfiguration.productConfigRepository(),
                applicatConf.timeSource(),
                tableServiceConfiguration.tableService(),
                pipelineServiceConfiguration.pipelineImportService());
    }
}
