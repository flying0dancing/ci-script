package com.lombardrisk.ignis.design.server.pipeline;

import com.lombardrisk.ignis.design.server.configuration.ImportCsvConfiguration;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineRepository;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineStepRepository;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineStepTestRepository;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineStepTestRowRepository;
import com.lombardrisk.ignis.design.server.pipeline.converter.PipelineConverter;
import com.lombardrisk.ignis.design.server.pipeline.converter.PipelineStepConverter;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.design.server.pipeline.test.PipelineStepTestExecuteService;
import com.lombardrisk.ignis.design.server.pipeline.test.PipelineStepTestRowService;
import com.lombardrisk.ignis.design.server.pipeline.test.PipelineStepTestService;
import com.lombardrisk.ignis.design.server.pipeline.validator.PipelineConstraintsValidator;
import com.lombardrisk.ignis.design.server.productconfig.schema.SchemaConfiguration;
import com.lombardrisk.ignis.design.server.productconfig.validation.PipelineValidator;
import com.lombardrisk.ignis.pipeline.PipelinePlanGenerator;
import com.lombardrisk.ignis.pipeline.step.common.MapStepExecutor;
import com.lombardrisk.ignis.pipeline.step.common.UnionStepExecutor;
import com.lombardrisk.ignis.pipeline.step.common.WindowStepExecutor;
import lombok.AllArgsConstructor;
import org.apache.spark.sql.SparkSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@AllArgsConstructor
@Import({ SchemaConfiguration.class })
@Configuration
public class PipelineConfiguration {

    private final PipelineRepository pipelineRepository;
    private final PipelineStepRepository pipelineStepRepository;
    private final PipelineStepTestRepository pipelineStepTestRepository;
    private final PipelineStepTestRowRepository pipelineStepTestRowRepository;
    private final SchemaConfiguration schemaConfiguration;
    private final ImportCsvConfiguration importCsvConfiguration;

    @Bean
    public PipelineConverter pipelineConverter() {
        return new PipelineConverter(pipelineStepConverter(), pipelinePlanGenerator());
    }

    @Bean
    public PipelineStepConverter pipelineStepConverter() {
        return new PipelineStepConverter();
    }

    @Bean
    public PipelinePlanGenerator<Long, PipelineStep> pipelinePlanGenerator() {
        return new PipelinePlanGenerator<>();
    }

    @Bean
    public PipelineService pipelineService() {
        return new PipelineService(pipelineRepository, pipelineConverter());
    }

    @Bean
    public SparkSession sparkSession() {
        return SparkSession.builder()
                .config("spark.master", "local")
                .getOrCreate();
    }

    @Bean
    public PipelineStepService pipelineStepService() {
        return new PipelineStepService(
                pipelineStepRepository,
                pipelineStepConverter(),
                pipelineRepository,
                pipelineStepTestRepository,
                pipelineStepSelectsValidator());
    }

    @Bean
    public PipelineStepSelectsValidator pipelineStepSelectsValidator() {
        return new PipelineStepSelectsValidator(
                schemaConfiguration.schemaService(), pipelineStepExecutor(), sparkUDFService());
    }

    @Bean
    public MapStepExecutor mapStepExecutor() {
        return new MapStepExecutor();
    }

    @Bean
    public WindowStepExecutor windowStepExecutor() {
        return new WindowStepExecutor();
    }

    @Bean
    public UnionStepExecutor unionStepExecutor() {
        return new UnionStepExecutor();
    }

    @Bean
    public PipelineStepExecutor pipelineStepExecutor() {
        return new PipelineStepExecutor(
                sparkSession(),
                mapStepExecutor(),
                windowStepExecutor(),
                unionStepExecutor(),
                pipelineSparkSqlExecutor());
    }

    @Bean
    public SparkUDFService sparkUDFService() {
        return new SparkUDFService(sparkSession());
    }

    @Bean
    public PipelineStepTestExecuteService pipelineStepTestExecuteService() {
        return new PipelineStepTestExecuteService(
                pipelineStepExecutor(),
                sparkUDFService(),
                schemaConfiguration.schemaService());
    }

    @Bean
    public PipelineStepSparkSqlExecutor pipelineSparkSqlExecutor() {
        return new PipelineStepSparkSqlExecutor(sparkSession());
    }

    @Bean
    public PipelineStepTestService pipelineStepTestService() {
        return new PipelineStepTestService(
                pipelineStepRepository,
                pipelineStepTestRepository,
                pipelineStepTestRowRepository,
                pipelineStepTestExecuteService(),
                pipelineStepSelectsValidator(),
                schemaConfiguration.schemaService());
    }

    @Bean
    public PipelineStepTestRowService inputDataRowService() {
        return new PipelineStepTestRowService(
                pipelineStepRepository,
                pipelineStepTestRepository,
                pipelineStepTestRowRepository,
                schemaConfiguration.schemaService(),
                schemaConfiguration.fieldService(),
                importCsvConfiguration.getImportMaxLines());
    }

    @Bean
    public PipelineValidator pipelineValidator() {
        return new PipelineValidator(pipelineStepSelectsValidator(), pipelinePlanGenerator());
    }

    @Bean
    public PipelineConstraintsValidator pipelineConstraintsValidator() {
        return new PipelineConstraintsValidator(pipelineRepository);
    }
}
