package com.lombardrisk.ignis.server.config.drillback;

import com.lombardrisk.ignis.server.config.dataset.DatasetServiceConfiguration;
import com.lombardrisk.ignis.server.config.dataset.PhoenixRepositoryConfiguration;
import com.lombardrisk.ignis.server.config.pipeline.PipelineServiceConfiguration;
import com.lombardrisk.ignis.server.config.table.TableServiceConfiguration;
import com.lombardrisk.ignis.server.config.web.HttpConnectorConfiguration;
import com.lombardrisk.ignis.server.controller.drillback.DrillBackController;
import com.lombardrisk.ignis.server.dataset.drillback.DatasetExportService;
import com.lombardrisk.ignis.server.dataset.drillback.DrillBackService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class DrillBackConfiguration {

    private final HttpConnectorConfiguration connectorConfiguration;
    private final TableServiceConfiguration tableServiceConfiguration;
    private final PipelineServiceConfiguration pipelineServiceConfiguration;
    private final PhoenixRepositoryConfiguration phoenixRepositoryConfiguration;
    private final DatasetServiceConfiguration datasetServiceConfiguration;
    private final ExportConfiguration exportConfiguration;

    @Bean
    public DrillBackService drillBackService() {
        return new DrillBackService(
                phoenixRepositoryConfiguration.datasetResultRepository(),
                datasetServiceConfiguration.datasetService(),
                pipelineServiceConfiguration.pipelineService(),
                pipelineServiceConfiguration.pipelineStepService(),
                pipelineServiceConfiguration.pipelineInvocationService(),
                tableServiceConfiguration.tableService());
    }

    @Bean
    public DatasetExportService datasetExportService() {
        return new DatasetExportService(
                exportConfiguration.getExportDatasetPageSize(),
                exportConfiguration.getExportDatasetLimit(),
                datasetServiceConfiguration.datasetService(),
                pipelineServiceConfiguration.pipelineService(),
                phoenixRepositoryConfiguration.datasetResultRepository());
    }

    @Bean
    public DrillBackController drillBackController() {
        return new DrillBackController(
                connectorConfiguration.redirectService(),
                drillBackService(),
                datasetExportService(),
                datasetServiceConfiguration.datasetService());
    }
}
