package com.lombardrisk.ignis.server.eraser.config;

import com.lombardrisk.ignis.server.dataset.DatasetJpaRepository;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.PipelineInvocationJpaRepository;
import com.lombardrisk.ignis.server.dataset.rule.ValidationResultsSummaryRepository;
import com.lombardrisk.ignis.server.eraser.controller.PageController;
import com.lombardrisk.ignis.server.eraser.service.PipelineService;
import com.lombardrisk.ignis.server.eraser.service.ProductService;
import com.lombardrisk.ignis.server.eraser.service.SchemaService;
import com.lombardrisk.ignis.server.product.pipeline.PipelineJpaRepository;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigRepository;
import com.lombardrisk.ignis.server.product.table.TableRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@AllArgsConstructor
public class ControllerConfig {

    private final ProductConfigRepository productConfigRepository;
    private final TableRepository tableRepository;
    private final DatasetJpaRepository datasetJpaRepository;
    private final PipelineJpaRepository pipelineJpaRepository;
    private final PipelineInvocationJpaRepository pipelineInvocationJpaRepository;
    private final ValidationResultsSummaryRepository validationResultsSummaryRepository;
    private final JdbcTemplate phoenixJdbcTemplate;
    private final PhoenixDatasourceBean phoenixDatasourceBean;

    @Bean
    public SchemaService schemaService() {
        return new SchemaService(
                tableRepository,
                datasetJpaRepository,
                validationResultsSummaryRepository,
                phoenixDatasourceBean.phoenixJdbcTemplate());
    }

    @Bean
    public ProductService productService() {
        return new ProductService(
                productConfigRepository,
                pipelineService(),
                schemaService(),
                phoenixDatasourceBean.phoenixJdbcTemplate());
    }

    @Bean
    public PipelineService pipelineService() {
        return new PipelineService(
                pipelineJpaRepository,
                pipelineInvocationJpaRepository);
    }

    @Bean
    public PageController utilityPageController() {
        return new PageController(productConfigRepository, schemaService(), productService(), pipelineService());
    }
}
