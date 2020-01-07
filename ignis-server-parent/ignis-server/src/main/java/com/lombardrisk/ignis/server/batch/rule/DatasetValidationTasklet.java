package com.lombardrisk.ignis.server.batch.rule;

import com.lombardrisk.ignis.api.dataset.ValidationStatus;
import com.lombardrisk.ignis.server.batch.AppId;
import com.lombardrisk.ignis.server.batch.DatasetId;
import com.lombardrisk.ignis.server.batch.SparkJobExecutor;
import com.lombardrisk.ignis.server.dataset.DatasetJpaRepository;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
import com.lombardrisk.ignis.server.util.spark.SparkConfFactory;
import com.lombardrisk.ignis.server.util.spark.SparkSubmitOption;
import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.spark.api.JobType;
import com.lombardrisk.ignis.spark.api.validation.DatasetValidationJobRequest;
import com.lombardrisk.ignis.spark.api.validation.DatasetValidationRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
public class DatasetValidationTasklet implements StoppableTasklet {

    private final SparkConfFactory sparkConfFactory;
    private final SparkJobExecutor sparkJobExecutor;
    private final DatasetJpaRepository datasetRepository;
    private final String driverJar;
    private final String mainClass;
    private final String hdfsUser;
    private final AppId taskletScopedAppId;
    private final DatasetId taskletScopedDatasetId;

    public DatasetValidationTasklet(
            final SparkConfFactory sparkConfFactory,
            final SparkJobExecutor sparkJobExecutor,
            final DatasetJpaRepository datasetRepository,
            final AppId taskletScopedAppId,
            final DatasetId taskletScopedDatasetId,
            final Environment env) {
        this.sparkConfFactory = sparkConfFactory;
        this.sparkJobExecutor = sparkJobExecutor;
        this.datasetRepository = datasetRepository;
        this.taskletScopedAppId = taskletScopedAppId;
        this.taskletScopedDatasetId = taskletScopedDatasetId;

        driverJar = env.getRequiredProperty("spark.drivers.validation.resource");
        mainClass = env.getRequiredProperty("spark.drivers.validation.mainClass");
        hdfsUser = env.getRequiredProperty("hadoop.user");
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        ServiceRequest serviceRequest = sparkJobExecutor.setupRequest(chunkContext);

        String datasetIdMessage = serviceRequest.getRequestMessage();
        taskletScopedDatasetId.set(Long.valueOf(datasetIdMessage));

        Dataset dataset = findDataset(taskletScopedDatasetId.get());

        Long serviceRequestId = serviceRequest.getId();
        dataset.setLatestValidationJobId(serviceRequestId);
        datasetRepository.save(dataset);

        DatasetValidationJobRequest datasetValidationJobRequest =
                DatasetValidationJobRequest.builder()
                        .serviceRequestId(serviceRequestId)
                        .name(serviceRequest.getName())
                        .datasetId(dataset.getId())
                        .datasetName(dataset.getName())
                        .datasetTableLookup(DatasetTableLookup.builder()
                                .datasetId(dataset.getId())
                                .datasetName(dataset.getName())
                                .predicate(dataset.getPredicate())
                                .rowKeySeed(dataset.getRowKeySeed())
                                .recordsCount(dataset.getRecordsCount())
                                .build())
                        .datasetValidationRules(findDatasetRules(dataset))
                        .build();

        SparkSubmitOption sparkSubmitOption =
                createSparkSubmitOption(serviceRequestId, datasetValidationJobRequest);

        sparkJobExecutor.executeSparkJob(
                contribution,
                chunkContext,
                taskletScopedAppId,
                sparkSubmitOption);

        return RepeatStatus.FINISHED;
    }

    private Dataset findDataset(final Long datasetId) {
        return datasetRepository.findByIdPrefetchSchemaRules(datasetId)
                .orElseThrow(() -> new IllegalStateException("Could not find dataset with id " + datasetId));
    }

    private List<DatasetValidationRule> findDatasetRules(final Dataset dataset) {
        return dataset.getSchema()
                .getValidationRules()
                .stream()
                .filter(rule -> rule.isValidFor(dataset.getReferenceDate()))
                .map(DatasetValidationTasklet::toDatasetValidationRule)
                .collect(toList());
    }

    private static DatasetValidationRule toDatasetValidationRule(final ValidationRule validationRule) {
        return DatasetValidationRule.builder()
                .id(validationRule.getId())
                .name(validationRule.getName())
                .expression(validationRule.getExpression())
                .build();
    }

    private SparkSubmitOption createSparkSubmitOption(
            final Long serviceRequestId,
            final DatasetValidationJobRequest datasetValidationJobRequest) {
        return SparkSubmitOption.builder()
                .clazz(mainClass)
                .jobExecutionId(serviceRequestId)
                .job(datasetValidationJobRequest)
                .jobType(JobType.DATASET_VALIDATION)
                .jobName(JobType.DATASET_VALIDATION + " Job " + serviceRequestId)
                .hdfsUser(hdfsUser)
                .driverJar(driverJar)
                .sparkConf(sparkConfFactory.create())
                .build();
    }

    @Override
    public void stop() {
        if (taskletScopedAppId.isNotEmpty()) {
            sparkJobExecutor.stop(taskletScopedAppId);
        } else {
            log.warn("Tasklet app id was not updated: {}", taskletScopedAppId);
        }
        if (taskletScopedDatasetId.isPresent()) {
            Dataset dataset = findDataset(taskletScopedDatasetId.get());
            dataset.setValidationStatus(ValidationStatus.NOT_VALIDATED);
            datasetRepository.save(dataset);
        } else {
            log.warn("Dataset id was not updated: {}", taskletScopedDatasetId);
        }
    }
}
