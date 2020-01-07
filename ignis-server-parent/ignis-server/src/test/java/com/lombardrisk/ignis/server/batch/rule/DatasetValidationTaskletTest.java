package com.lombardrisk.ignis.server.batch.rule;

import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.server.batch.AppId;
import com.lombardrisk.ignis.server.batch.DatasetId;
import com.lombardrisk.ignis.server.batch.SparkJobExecutor;
import com.lombardrisk.ignis.server.dataset.DatasetJpaRepository;
import com.lombardrisk.ignis.server.dataset.fixture.DatasetPopulated;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.job.fixture.JobPopulated;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
import com.lombardrisk.ignis.server.product.table.model.Table;
import com.lombardrisk.ignis.server.util.spark.SparkConfFactory;
import com.lombardrisk.ignis.server.util.spark.SparkSubmitOption;
import com.lombardrisk.ignis.spark.api.validation.DatasetValidationJobRequest;
import com.lombardrisk.ignis.spark.api.validation.DatasetValidationRule;
import org.apache.spark.SparkConf;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.core.env.Environment;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatasetValidationTaskletTest {

    @Mock
    private StepContribution stepContribution;
    @Mock
    private ChunkContext chunkContext;
    @Mock
    private SparkConfFactory sparkConfFactory;
    @Mock
    private SparkConf sparkConf;
    @Mock
    private SparkJobExecutor sparkJobExecutor;
    @Mock
    private DatasetJpaRepository datasetRepository;
    @Mock
    private Environment environment;
    private AppId appId = new AppId();
    private DatasetId datasetId = new DatasetId();

    private DatasetValidationTasklet datasetValidationTasklet;

    @Captor
    private ArgumentCaptor<Dataset> datasetCaptor;
    @Captor
    private ArgumentCaptor<SparkSubmitOption> submitOptionCaptor;

    @Before
    public void setup() throws JobExecutionException {
        when(environment.getRequiredProperty(anyString())).thenReturn("test");

        when(sparkConfFactory.create()).thenReturn(sparkConf);

        Table schema = ProductPopulated.table()
                .validationRules(singleton(ProductPopulated.validationRule().id(1L).build()))
                .build();

        when(datasetRepository.findByIdPrefetchSchemaRules(anyLong()))
                .thenReturn(Optional.of(
                        DatasetPopulated.dataset()
                                .id(0L)
                                .schema(schema)
                                .build()));

        when(sparkJobExecutor.setupRequest(any()))
                .thenReturn(JobPopulated.validationJobServiceRequest().id(0L).build());

        datasetValidationTasklet = datasetValidationTasklet(environment);
    }

    private DatasetValidationTasklet datasetValidationTasklet(final Environment environment) {
        return new DatasetValidationTasklet(
                sparkConfFactory, sparkJobExecutor, datasetRepository, appId, datasetId, environment);
    }

    @Test
    public void execute_CallsExecutorWithDriverJarOnOption() throws Exception {
        when(environment.getRequiredProperty("spark.drivers.validation.resource"))
                .thenReturn("driver_jar");

        datasetValidationTasklet = datasetValidationTasklet(environment);

        datasetValidationTasklet.execute(stepContribution, chunkContext);

        verify(sparkJobExecutor)
                .executeSparkJob(any(), any(), any(), submitOptionCaptor.capture());

        assertThat(submitOptionCaptor.getValue().getDriverJar())
                .isEqualTo("driver_jar");
    }

    @Test
    public void execute_CallsExecutorWithMainClassOption() throws Exception {
        when(environment.getRequiredProperty("spark.drivers.validation.mainClass"))
                .thenReturn("main_class");

        datasetValidationTasklet = datasetValidationTasklet(environment);

        datasetValidationTasklet.execute(stepContribution, chunkContext);

        verify(sparkJobExecutor)
                .executeSparkJob(any(), any(), any(), submitOptionCaptor.capture());
        assertThat(submitOptionCaptor.getValue().getClazz())
                .isEqualTo("main_class");
    }

    @Test
    public void execute_CallsExecutorWithHdfsUserOption() throws Exception {
        when(environment.getRequiredProperty("hadoop.user"))
                .thenReturn("hdfs_user");

        datasetValidationTasklet = datasetValidationTasklet(environment);

        datasetValidationTasklet.execute(stepContribution, chunkContext);

        verify(sparkJobExecutor)
                .executeSparkJob(any(), any(), any(), submitOptionCaptor.capture());
        assertThat(submitOptionCaptor.getValue().getHdfsUser())
                .isEqualTo("hdfs_user");
    }

    @Test
    public void execute_CallsExecutorWithSparkConfiguration() throws Exception {
        datasetValidationTasklet.execute(stepContribution, chunkContext);

        verify(sparkJobExecutor)
                .executeSparkJob(any(), any(), any(), submitOptionCaptor.capture());
        assertThat(submitOptionCaptor.getValue().getSparkConf())
                .isEqualTo(sparkConf);
    }

    @Test
    public void execute_CallsExecutorWithJobExecutionId() throws Exception {
        when(sparkJobExecutor.setupRequest(any()))
                .thenReturn(JobPopulated.validationJobServiceRequest().id(123L).build());

        datasetValidationTasklet.execute(stepContribution, chunkContext);

        verify(sparkJobExecutor)
                .executeSparkJob(any(), any(), any(), submitOptionCaptor.capture());

        assertThat(submitOptionCaptor.getValue().getJobExecutionId())
                .isEqualTo(123L);
        assertThat(submitOptionCaptor.getValue().getJob().getServiceRequestId())
                .isEqualTo(123L);
    }

    @Test
    public void execute_CallsExecutorWithDatasetDetailsOnJobRequest() throws Exception {
        when(datasetRepository.findByIdPrefetchSchemaRules(anyLong()))
                .thenReturn(Optional.of(
                        DatasetPopulated.dataset()
                                .id(542L)
                                .name("data_set")
                                .schema(ProductPopulated.table()
                                        .validationRules(singleton(ProductPopulated.validationRule().id(1L).build()))
                                        .build())
                                .build()));
        when(sparkJobExecutor.setupRequest(any()))
                .thenReturn(JobPopulated.validationJobServiceRequest()
                        .name("request")
                        .id(0L)
                        .build());

        datasetValidationTasklet.execute(stepContribution, chunkContext);

        verify(sparkJobExecutor)
                .executeSparkJob(any(), any(), any(), submitOptionCaptor.capture());

        DatasetValidationJobRequest jobRequest = (DatasetValidationJobRequest) submitOptionCaptor.getValue().getJob();
        assertThat(jobRequest)
                .extracting(
                        DatasetValidationJobRequest::getDatasetName,
                        DatasetValidationJobRequest::getDatasetId,
                        DatasetValidationJobRequest::getName)
                .containsExactly("data_set", 542L, "request");
    }

    @Test
    public void execute_CallsExecutorWithValidationRulesOnJobRequest() throws Exception {
        Set<ValidationRule> validationRules = ImmutableSet.of(
                ProductPopulated.validationRule()
                        .id(1L).name("D1 Rule").expression("2 != 3")
                        .startDate(LocalDate.of(2018, 1, 1))
                        .endDate(LocalDate.of(2018, 3, 1))
                        .build(),
                ProductPopulated.validationRule()
                        .id(2L).name("D2 Rule").expression("12 != 13")
                        .startDate(LocalDate.of(2018, 1, 1))
                        .endDate(LocalDate.of(2018, 3, 1))
                        .build());

        when(datasetRepository.findByIdPrefetchSchemaRules(anyLong()))
                .thenReturn(Optional.of(
                        DatasetPopulated.dataset()
                                .id(0L)
                                .schema(ProductPopulated.table().validationRules(validationRules).build())
                                .build()));

        datasetValidationTasklet.execute(stepContribution, chunkContext);

        verify(sparkJobExecutor)
                .executeSparkJob(any(), any(), any(), submitOptionCaptor.capture());
        DatasetValidationJobRequest jobRequest = (DatasetValidationJobRequest) submitOptionCaptor.getValue().getJob();
        assertThat(jobRequest.getDatasetValidationRules())
                .extracting(
                        DatasetValidationRule::getId,
                        DatasetValidationRule::getName,
                        DatasetValidationRule::getExpression)
                .containsExactly(
                        tuple(1L, "D1 Rule", "2 != 3"),
                        tuple(2L, "D2 Rule", "12 != 13"));
    }

    @Test
    public void execute_ValidationRuleNotValidForDatasetDate_FiltersOutValidationRulesOnJobRequest() throws Exception {
        LocalDate referenceDate = LocalDate.of(2018, 2, 1);

        Set<ValidationRule> validationRules = ImmutableSet.of(
                ProductPopulated.validationRule()
                        .id(1L)
                        .startDate(LocalDate.of(2018, 1, 1))
                        .endDate(LocalDate.of(2018, 3, 1))
                        .build(),
                ProductPopulated.validationRule()
                        .id(2L)
                        .startDate(LocalDate.of(2017, 11, 1))
                        .endDate(LocalDate.of(2018, 1, 1))
                        .build());

        Dataset dataset = DatasetPopulated.dataset()
                .id(0L)
                .schema(ProductPopulated.table().validationRules(validationRules).build())
                .referenceDate(referenceDate)
                .build();

        when(datasetRepository.findByIdPrefetchSchemaRules(anyLong()))
                .thenReturn(Optional.of(dataset));

        datasetValidationTasklet.execute(stepContribution, chunkContext);

        verify(sparkJobExecutor)
                .executeSparkJob(any(), any(), any(), submitOptionCaptor.capture());
        DatasetValidationJobRequest jobRequest = (DatasetValidationJobRequest) submitOptionCaptor.getValue().getJob();
        assertThat(jobRequest.getDatasetValidationRules())
                .extracting(DatasetValidationRule::getId)
                .containsOnly(1L);
    }

    @Test
    public void execute_CallsDatasetRepository() throws Exception {
        when(sparkJobExecutor.setupRequest(any()))
                .thenReturn(JobPopulated.validationJobServiceRequest()
                        .id(0L)
                        .requestMessage("4456")
                        .build());

        datasetValidationTasklet.execute(stepContribution, chunkContext);

        verify(datasetRepository).findByIdPrefetchSchemaRules(4456L);
    }

    @Test
    public void execute_UpdatesDatasetWithJobExecutionId() throws Exception {
        when(sparkJobExecutor.setupRequest(any()))
                .thenReturn(JobPopulated.validationJobServiceRequest().id(901202L).build());

        datasetValidationTasklet.execute(stepContribution, chunkContext);

        verify(datasetRepository).save(datasetCaptor.capture());

        assertThat(datasetCaptor.getValue().getLatestValidationJobId())
                .isEqualTo(901202L);
    }
}
