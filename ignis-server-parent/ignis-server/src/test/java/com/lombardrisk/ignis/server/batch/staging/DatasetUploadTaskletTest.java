package com.lombardrisk.ignis.server.batch.staging;

import com.lombardrisk.ignis.api.calendar.HolidayCalendarModule;
import com.lombardrisk.ignis.fs.FileSystemTemplate;
import com.lombardrisk.ignis.server.batch.SparkJobExecutor;
import com.lombardrisk.ignis.server.fixtures.Populated;
import com.lombardrisk.ignis.server.job.fixture.JobPopulated;
import com.lombardrisk.ignis.server.job.staging.StagingDatasetRepository;
import com.lombardrisk.ignis.server.job.staging.model.StagingDataset;
import com.lombardrisk.ignis.spark.api.staging.StagingAppConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;

import java.io.File;
import java.io.IOException;

import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.api.dataset.DatasetState.UPLOADED;
import static com.lombardrisk.ignis.api.dataset.DatasetState.UPLOAD_FAILED;
import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatasetUploadTaskletTest {

    @Mock
    private HdfsDatasetConf datasetConf;
    @Mock
    private FileSystemTemplate fileSystemTemplate;
    @Mock
    private StagingDatasetRepository stagingDatasetRepository;
    @Mock
    private StepContribution stepContribution;
    @Mock
    private ChunkContext chunkContext;
    @Mock
    private StepContext stepContext;
    @Mock
    private SparkJobExecutor sparkJobExecutor;

    private DatasetUploadTasklet datasetUploadTasklet;

    @Captor
    private ArgumentCaptor<File> fileArgumentCaptor;
    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;
    @Captor
    private ArgumentCaptor<StagingDataset> stagingDatasetArgumentCaptor;

    @Before
    public void setup() throws Exception {
        MAPPER.registerModule(new HolidayCalendarModule());

        datasetUploadTasklet = new DatasetUploadTasklet(datasetConf,
                fileSystemTemplate,
                stagingDatasetRepository,
                sparkJobExecutor,
                MAPPER);
        when(stagingDatasetRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArguments()[0]);

        when(datasetConf.getLocalPath()).thenReturn("");

        JobExecution jobExecution = new JobExecution(123L);
        jobExecution.setStatus(BatchStatus.STARTING);

        when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(stepContext.getStepExecution()).thenReturn(new StepExecution("1", jobExecution));

        StagingDataset stagingDataset = new StagingDataset();
        stagingDataset.setId(1L);
        stagingDataset.setStagingFile("/test/test.csv");

        when(
                stagingDatasetRepository.findByServiceRequestIdAndDatasetNameAndEntityCodeAndReferenceDate(
                        anyLong(), any(), anyString(), any()))
                .thenReturn(stagingDataset);

        when(sparkJobExecutor.setupRequest(chunkContext))
                .thenReturn(
                        JobPopulated.stagingJobServiceRequest()
                                .id(123L)
                                .requestMessage(MAPPER.writeValueAsString(JobPopulated.stagingAppConfig().build()))
                                .build());
    }

    @Test
    public void execute_JobNotRunning_DoesNotUpload() throws Exception {
        JobExecution jobExecution = new JobExecution(123L);
        jobExecution.setStatus(BatchStatus.STOPPED);
        when(stepContext.getStepExecution()).thenReturn(new StepExecution("1", jobExecution));

        datasetUploadTasklet.execute(stepContribution, chunkContext);

        verifyZeroInteractions(fileSystemTemplate);
    }

    @Test
    public void execute_CopiesFileToHdfsDataSourceFileLocation() throws Exception {
        StagingAppConfig stagingAppConfig = JobPopulated.stagingAppConfig()
                .serviceRequestId(9292)
                .items(newHashSet(JobPopulated.stagingDatasetConfig()
                        .source(Populated.hdfsCsvDataSource()
                                .hdfsPath("hdfs://root/Path/9292/TRADES")
                                .build())
                        .build()))
                .build();
        when(sparkJobExecutor.setupRequest(chunkContext))
                .thenReturn(JobPopulated.stagingJobServiceRequest()
                        .id(9292L)
                        .requestMessage(MAPPER.writeValueAsString(stagingAppConfig))
                        .build());

        datasetUploadTasklet.execute(stepContribution, chunkContext);

        verify(fileSystemTemplate).copy(any(), stringArgumentCaptor.capture());

        assertThat(stringArgumentCaptor.getValue())
                .isEqualTo("hdfs://root/Path/9292/TRADES");
    }

    @Test
    public void execute_CopiesFileFromStagingCsvSource() throws Exception {
        when(stagingDatasetRepository.findByServiceRequestIdAndDatasetNameAndEntityCodeAndReferenceDate(
                anyLong(), anyString(), anyString(), any()))
                .thenReturn(JobPopulated.stagingDataset()
                        .stagingFile("file/path")
                        .build());

        when(datasetConf.getLocalPath())
                .thenReturn("localPathPart/");

        datasetUploadTasklet.execute(stepContribution, chunkContext);

        verify(fileSystemTemplate).copy(fileArgumentCaptor.capture(), any());

        assertThat(fileArgumentCaptor.getValue())
                .isEqualTo(new File("localPathPart/file/path"));
    }

    @Test
    public void execute_UploadSuccessful_UpdatesStagingDatasetStatus() throws Exception {
        datasetUploadTasklet.execute(stepContribution, chunkContext);

        verify(stagingDatasetRepository, times(2))
                .save(stagingDatasetArgumentCaptor.capture());

        assertThat(stagingDatasetArgumentCaptor.getAllValues())
                .extracting(StagingDataset::getStatus)
                .contains(UPLOADED);
    }

    @Test
    public void execute_UploadErrors_UpdatesStagingDatasetErrorStatus() throws Exception {
        when(fileSystemTemplate.copy(any(), any()))
                .thenThrow(new IOException());

        datasetUploadTasklet.execute(stepContribution, chunkContext);

        verify(stagingDatasetRepository, times(2))
                .save(stagingDatasetArgumentCaptor.capture());

        assertThat(stagingDatasetArgumentCaptor.getAllValues())
                .extracting(StagingDataset::getStatus)
                .contains(UPLOAD_FAILED);
    }

    @Test
    public void execute_UploadErrors_UpdatesStagingDatasetErrorMessage() throws Exception {
        when(fileSystemTemplate.copy(any(), any()))
                .thenThrow(new IOException("HADOOP WENT ON HOLIDAY"));

        datasetUploadTasklet.execute(stepContribution, chunkContext);

        verify(stagingDatasetRepository, times(2))
                .save(stagingDatasetArgumentCaptor.capture());

        assertThat(stagingDatasetArgumentCaptor.getAllValues())
                .extracting(StagingDataset::getMessage)
                .contains("HADOOP WENT ON HOLIDAY");
    }

    @Test
    public void execute_UploadErrors_UpdatesStepExecutionExitStatus() throws Exception {
        when(fileSystemTemplate.copy(any(), any()))
                .thenThrow(new IOException("HADOOP WENT ON HOLIDAY"));

        datasetUploadTasklet.execute(stepContribution, chunkContext);

        StepExecution stepExecution = stepContext.getStepExecution();
        assertThat(stepExecution.getExitStatus())
                .isEqualTo(ExitStatus.NOOP);
    }
}
