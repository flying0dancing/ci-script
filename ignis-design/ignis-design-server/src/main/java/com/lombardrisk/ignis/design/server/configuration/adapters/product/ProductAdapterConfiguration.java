package com.lombardrisk.ignis.design.server.configuration.adapters.product;

import com.lombardrisk.ignis.design.server.configuration.DatasourceConfig;
import com.lombardrisk.ignis.design.server.jpa.JpaRepositoryConfiguration;
import com.lombardrisk.ignis.design.server.pipeline.PipelineConfiguration;
import com.lombardrisk.ignis.design.server.productconfig.api.ProductConfigRepository;
import com.lombardrisk.ignis.design.server.productconfig.api.ProductPipelineRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Slf4j
@Configuration
@Import({ DatasourceConfig.class, JpaRepositoryConfiguration.class, PipelineConfiguration.class })
public class ProductAdapterConfiguration {

    private final JpaRepositoryConfiguration productRepositoryDependencies;
    private final PipelineConfiguration pipelineDependencies;

    public ProductAdapterConfiguration(
            final JpaRepositoryConfiguration productRepositoryDependencies,
            final PipelineConfiguration pipelineDependencies) {
        this.productRepositoryDependencies = productRepositoryDependencies;
        this.pipelineDependencies = pipelineDependencies;
    }

    @Bean
    public ProductConfigRepository productRepository() {
        return new ProductAdapter(productRepositoryDependencies.getProductConfigRepository());
    }

    @Bean
    public ProductPipelineRepository productPipelineRepository() {
        return new ProductPipelineAdapter(pipelineDependencies.pipelineService());
    }
}
