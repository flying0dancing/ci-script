package com.lombardrisk.ignis.server.config.job;

import com.lombardrisk.ignis.server.batch.product.AutoTriggeredRollbackTasklet;
import com.lombardrisk.ignis.server.batch.product.ManuallyTriggeredRollbackTasklet;
import com.lombardrisk.ignis.server.batch.product.MigrationHelper;
import com.lombardrisk.ignis.server.batch.product.MigrationTasklet;
import com.lombardrisk.ignis.server.batch.product.RollbackMigration;
import com.lombardrisk.ignis.server.batch.product.datasource.MigrationTableService;
import com.lombardrisk.ignis.server.config.batch.BatchConfiguration;
import com.lombardrisk.ignis.server.config.data.PhoenixDatasourceBean;
import com.lombardrisk.ignis.server.config.dataset.DatasetRepositoryConfiguration;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestRepository;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigService;
import com.lombardrisk.ignis.server.product.table.TableService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType.IMPORT_PRODUCT;
import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType.ROLLBACK_PRODUCT;
import static org.springframework.batch.core.ExitStatus.COMPLETED;
import static org.springframework.batch.core.ExitStatus.FAILED;
import static org.springframework.batch.core.ExitStatus.NOOP;
import static org.springframework.batch.core.ExitStatus.STOPPED;

@Configuration
@Import(BatchConfiguration.class)
public class ImportProductJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory steps;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ProductConfigService productConfigService;
    private final TableService tableService;
    private final PhoenixDatasourceBean phoenixDatasourceBean;
    private final MigrationTableService migrationTableService;
    private final DatasetRepositoryConfiguration datasetRepositoryConfiguration;
    private final JobServiceConfiguration jobServiceConfiguration;

    @Autowired
    public ImportProductJobConfiguration(
            final JobBuilderFactory jobBuilderFactory,
            final StepBuilderFactory steps,
            final ServiceRequestRepository serviceRequestRepository,
            final ProductConfigService productConfigService,
            final TableService tableService,
            final PhoenixDatasourceBean phoenixDatasourceBean,
            final DatasetRepositoryConfiguration datasetRepositoryConfiguration,
            final MigrationTableService migrationTableService,
            final JobServiceConfiguration jobServiceConfiguration) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.steps = steps;
        this.serviceRequestRepository = serviceRequestRepository;
        this.productConfigService = productConfigService;
        this.tableService = tableService;
        this.phoenixDatasourceBean = phoenixDatasourceBean;
        this.migrationTableService = migrationTableService;
        this.datasetRepositoryConfiguration = datasetRepositoryConfiguration;
        this.jobServiceConfiguration = jobServiceConfiguration;
    }

    @Bean
    public Job importProductJob() {
        return jobBuilderFactory.get(IMPORT_PRODUCT.name())
                .incrementer(new RunIdIncrementer())
                .listener(jobServiceConfiguration.jobExecutionStatusListener())

                .start(importStep())

                .on(NOOP.getExitCode()).end()
                .on(COMPLETED.getExitCode()).end()

                .on(FAILED.getExitCode())
                .to(autoRollbackStep())

                .on(STOPPED.getExitCode())
                .to(autoRollbackStep())

                .end()
                .build();
    }

    @Bean
    public Job manuallyTriggeredRollbackProductJob() {
        return jobBuilderFactory.get(ROLLBACK_PRODUCT.name())
                .incrementer(new RunIdIncrementer())
                .listener(jobServiceConfiguration.jobExecutionStatusListener())

                .start(manualRollbackStep())

                .on(NOOP.getExitCode()).end()
                .on(COMPLETED.getExitCode()).end()
                .on(FAILED.getExitCode()).fail()

                .end()
                .build();
    }

    @Bean
    public Step importStep() {
        return steps.get("import product")
                .tasklet(importTasklet())
                .transactionAttribute(BatchConfiguration.NOT_SUPPORTED_TRANSACTION)
                .build();
    }

    @Bean
    public Step autoRollbackStep() {
        return steps.get("automatically rollback product")
                .tasklet(autoTriggeredRollbackTasklet())
                .transactionAttribute(BatchConfiguration.NOT_SUPPORTED_TRANSACTION)
                .build();
    }

    @Bean
    public Step manualRollbackStep() {
        return steps.get("manually rollback product")
                .tasklet(manuallyTriggeredRollbackTasklet())
                .transactionAttribute(BatchConfiguration.NOT_SUPPORTED_TRANSACTION)
                .build();
    }

    @Bean
    public MigrationHelper migrationHelper() {
        return new MigrationHelper(phoenixDatasourceBean.phoenixJdbcTemplate());
    }

    @Bean
    @StepScope
    public Tasklet importTasklet() {
        return new MigrationTasklet(
                datasetRepositoryConfiguration.sparkJobExecutor(),
                tableService,
                productConfigService,
                migrationTableService);
    }

    @Bean
    public RollbackMigration rollbackMigration() {
        return new RollbackMigration(migrationTableService, phoenixDatasourceBean.phoenixJdbcTemplate());
    }

    @Bean
    @StepScope
    public Tasklet autoTriggeredRollbackTasklet() {
        return new AutoTriggeredRollbackTasklet(
                datasetRepositoryConfiguration.sparkJobExecutor(),
                rollbackMigration(),
                productConfigService);
    }

    @Bean
    @StepScope
    public Tasklet manuallyTriggeredRollbackTasklet() {
        return new ManuallyTriggeredRollbackTasklet(
                datasetRepositoryConfiguration.sparkJobExecutor(),
                serviceRequestRepository,
                rollbackMigration());
    }
}
