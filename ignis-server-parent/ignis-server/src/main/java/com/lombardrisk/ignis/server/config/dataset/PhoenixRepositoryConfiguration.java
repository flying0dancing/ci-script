package com.lombardrisk.ignis.server.config.dataset;

import com.lombardrisk.ignis.server.config.data.PhoenixDatasourceBean;
import com.lombardrisk.ignis.server.config.pipeline.PipelineServiceConfiguration;
import com.lombardrisk.ignis.server.config.product.ProductConfigConverterConfiguration;
import com.lombardrisk.ignis.server.config.table.TableServiceConfiguration;
import com.lombardrisk.ignis.server.dataset.result.DatasetResultRepository;
import com.lombardrisk.ignis.server.dataset.rule.ValidationResultDetailService;
import com.lombardrisk.ignis.server.dataset.rule.detail.ValidationDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PhoenixRepositoryConfiguration {
    private final PhoenixDatasourceBean phoenixDatasourceBean;
    private final ProductConfigConverterConfiguration productConfigConverterConfiguration;
    private final DatasetServiceConfiguration datasetServiceConfiguration;
    private final TableServiceConfiguration tableServiceConfiguration;
    private final PipelineServiceConfiguration pipelineServiceConfiguration;

    @Autowired
    public PhoenixRepositoryConfiguration(
            final PhoenixDatasourceBean phoenixDatasourceBean,
            final ProductConfigConverterConfiguration productConfigConverterConfiguration,
            final DatasetServiceConfiguration datasetServiceConfiguration,
            final TableServiceConfiguration tableServiceConfiguration,
            final PipelineServiceConfiguration pipelineServiceConfiguration) {
        this.phoenixDatasourceBean = phoenixDatasourceBean;
        this.productConfigConverterConfiguration = productConfigConverterConfiguration;
        this.datasetServiceConfiguration = datasetServiceConfiguration;
        this.tableServiceConfiguration = tableServiceConfiguration;
        this.pipelineServiceConfiguration = pipelineServiceConfiguration;
    }

    @Bean
    public ValidationDetailRepository validationDetailRepository() {
        return new ValidationDetailRepository(phoenixDatasourceBean.phoenixJdbcTemplate());
    }

    @Bean
    public ValidationResultDetailService validationResultDetailService() {
        return new ValidationResultDetailService(
                productConfigConverterConfiguration.pageConverter(),
                productConfigConverterConfiguration.fieldConverter(),
                datasetServiceConfiguration.datasetService(),
                tableServiceConfiguration.validationRuleService(),
                validationDetailRepository());
    }

    @Bean
    public DatasetResultRepository datasetResultRepository() {
        return new DatasetResultRepository(
                phoenixDatasourceBean.phoenixJdbcTemplate(),
                phoenixDatasourceBean.filterToSQLConverter(),
                pipelineServiceConfiguration.pipelineStepService());
    }
}
