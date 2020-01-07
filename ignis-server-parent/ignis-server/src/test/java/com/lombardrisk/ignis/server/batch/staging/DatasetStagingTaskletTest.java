package com.lombardrisk.ignis.server.batch.staging;

import com.lombardrisk.ignis.server.batch.AppId;
import com.lombardrisk.ignis.server.batch.SparkJobExecutor;
import com.lombardrisk.ignis.server.job.fixture.JobPopulated;
import com.lombardrisk.ignis.server.util.spark.SparkConfFactory;
import com.lombardrisk.ignis.server.util.spark.SparkSubmitOption;
import com.lombardrisk.ignis.spark.api.JobType;
import com.lombardrisk.ignis.spark.api.staging.StagingAppConfig;
import org.apache.hadoop.yarn.client.api.YarnClient;
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

import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatasetStagingTaskletTest {

    @Mock
    private SparkJobExecutor sparkJobExecutor;
    @Mock
    private YarnClient yarnClient;
    @Mock
    private StepContribution stepContribution;
    @Mock
    private ChunkContext chunkContext;
    @Mock
    private Environment env;
    @Mock
    private SparkConfFactory sparkConfFactory;
    @Mock
    private SparkConf sparkConf;
    private AppId appId = new AppId();

    @Captor
    private ArgumentCaptor<SparkSubmitOption> submitOptionCaptor;

    private DatasetStagingTasklet datasetStagingTasklet;

    @Before
    public void setup() throws JobExecutionException {
        when(env.getRequiredProperty(anyString()))
                .thenReturn("a");

        when(sparkConfFactory.create())
                .thenReturn(sparkConf);

        when(sparkJobExecutor.setupRequest(any()))
                .thenReturn(JobPopulated.stagingJobServiceRequest().id(0L).build());

        datasetStagingTasklet = datasetStagingTasklet(env);
    }

    @Test
    public void execute_CallsExecutorWithDriverJar() throws Exception {
        when(env.getRequiredProperty("spark.drivers.staging.resource"))
                .thenReturn("jar");

        datasetStagingTasklet = datasetStagingTasklet(env);

        datasetStagingTasklet.execute(stepContribution, chunkContext);

        verify(sparkJobExecutor)
                .executeSparkJob(any(), any(), any(), submitOptionCaptor.capture());

        assertThat(submitOptionCaptor.getValue().getDriverJar()).isEqualTo("jar");
    }

    @Test
    public void execute_CallsExecutorWithMainClass() throws Exception {
        when(env.getRequiredProperty("spark.drivers.staging.mainClass"))
                .thenReturn("main.class");

        datasetStagingTasklet = datasetStagingTasklet(env);

        datasetStagingTasklet.execute(stepContribution, chunkContext);

        verify(sparkJobExecutor)
                .executeSparkJob(any(), any(), any(), submitOptionCaptor.capture());

        assertThat(submitOptionCaptor.getValue().getClazz()).isEqualTo("main.class");
    }

    @Test
    public void execute_CallsExecutorWithHdfsUser() throws Exception {
        when(env.getRequiredProperty("hadoop.user"))
                .thenReturn("ignis");

        datasetStagingTasklet = datasetStagingTasklet(env);

        datasetStagingTasklet.execute(stepContribution, chunkContext);

        verify(sparkJobExecutor)
                .executeSparkJob(any(), any(), any(), submitOptionCaptor.capture());

        assertThat(submitOptionCaptor.getValue().getHdfsUser()).isEqualTo("ignis");
    }

    @Test
    public void execute_CallsExecutorWithJobName() throws Exception {
        when(sparkJobExecutor.setupRequest(any()))
                .thenReturn(JobPopulated.stagingJobServiceRequest().id(444L).build());

        datasetStagingTasklet.execute(stepContribution, chunkContext);

        verify(sparkJobExecutor)
                .executeSparkJob(any(), any(), any(), submitOptionCaptor.capture());

        assertThat(submitOptionCaptor.getValue().getJobName()).isEqualTo("STAGING_DATASET Job 444");
    }

    @Test
    public void execute_CallsExecutorWithJobType() throws Exception {
        datasetStagingTasklet.execute(stepContribution, chunkContext);

        verify(sparkJobExecutor)
                .executeSparkJob(any(), any(), any(), submitOptionCaptor.capture());

        assertThat(submitOptionCaptor.getValue().getJobType()).isEqualTo(JobType.STAGING_DATASET);
    }

    @Test
    public void execute_CallsExecutorWithJobId() throws Exception {
        when(sparkJobExecutor.setupRequest(any()))
                .thenReturn(JobPopulated.stagingJobServiceRequest().id(552L).build());

        datasetStagingTasklet.execute(stepContribution, chunkContext);

        verify(sparkJobExecutor)
                .executeSparkJob(any(), any(), any(), submitOptionCaptor.capture());

        assertThat(submitOptionCaptor.getValue().getJobExecutionId()).isEqualTo(552);
    }

    @Test
    public void execute_CallsExecutorWithSparkConf() throws Exception {
        datasetStagingTasklet.execute(stepContribution, chunkContext);

        verify(sparkJobExecutor)
                .executeSparkJob(any(), any(), any(), submitOptionCaptor.capture());

        assertThat(submitOptionCaptor.getValue().getSparkConf()).isEqualTo(sparkConf);
    }

    @Test
    public void execute_CallsExecutorWithJob() throws Exception {
        StagingAppConfig stagingJobRequest = JobPopulated.stagingAppConfig().build();
        when(sparkJobExecutor.setupRequest(any()))
                .thenReturn(JobPopulated.stagingJobServiceRequest()
                        .requestMessage(MAPPER.writeValueAsString(stagingJobRequest))
                        .name("SJ")
                        .id(20L).build());

        datasetStagingTasklet.execute(stepContribution, chunkContext);

        verify(sparkJobExecutor)
                .executeSparkJob(any(), any(), any(), submitOptionCaptor.capture());

        StagingAppConfig jobRequest = (StagingAppConfig) submitOptionCaptor.getValue().getJob();
        assertThat(jobRequest)
                .isEqualTo(StagingAppConfig.builder()
                        .name(stagingJobRequest.getName())
                        .serviceRequestId(20L)
                        .items(stagingJobRequest.getItems())
                        .build());
    }

    private DatasetStagingTasklet datasetStagingTasklet(final Environment env) {
        return new DatasetStagingTasklet(env, sparkConfFactory, sparkJobExecutor, appId);
    }
}
