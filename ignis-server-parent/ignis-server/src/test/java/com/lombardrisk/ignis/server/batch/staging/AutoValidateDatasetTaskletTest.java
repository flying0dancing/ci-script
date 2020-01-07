package com.lombardrisk.ignis.server.batch.staging;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.api.dataset.ValidationStatus;
import com.lombardrisk.ignis.server.batch.JobService;
import com.lombardrisk.ignis.server.batch.SparkJobExecutor;
import com.lombardrisk.ignis.server.dataset.DatasetJpaRepository;
import com.lombardrisk.ignis.server.dataset.fixture.DatasetPopulated;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.job.fixture.JobPopulated;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AutoValidateDatasetTaskletTest {

    @Mock
    private SparkJobExecutor sparkJobExecutor;
    @Mock
    private DatasetJpaRepository datasetRepository;
    @Mock
    private JobService jobService;
    @InjectMocks
    private AutoValidateDatasetTasklet datasetAutoValidateTasklet;

    @Mock
    private ChunkContext chunkContext;
    @Mock
    private StepContribution stepContribution;

    @Before
    public void setUp() throws JobExecutionException {
        when(sparkJobExecutor.setupRequest(any()))
                .thenReturn(JobPopulated.stagingJobServiceRequest().id(0L).build());

        when(datasetRepository.findByStagingJobsServiceRequestIdAndValidationStatus(anyLong(), any()))
                .thenReturn(Collections.singletonList(DatasetPopulated.dataset().build()));
    }

    @Test
    public void execute_CallsDatasetRepositoryWithServiceRequestId() throws Exception {
        when(sparkJobExecutor.setupRequest(any()))
                .thenReturn(JobPopulated.stagingJobServiceRequest().id(903L).build());

        datasetAutoValidateTasklet.execute(stepContribution, chunkContext);

        verify(datasetRepository)
                .findByStagingJobsServiceRequestIdAndValidationStatus(903L, ValidationStatus.QUEUED);
    }

    @Test
    public void execute_NoDatasetsQueuedForAutoValidation_DoesNotStartValidationJobs() throws Exception {
        when(datasetRepository.findByStagingJobsServiceRequestIdAndValidationStatus(anyLong(), any()))
                .thenReturn(emptyList());

        datasetAutoValidateTasklet.execute(stepContribution, chunkContext);

        verifyZeroInteractions(jobService);
    }

    @Test
    public void execute_DatasetsQueuedForAutoValidaton_StartsValidationJobs() throws Exception {
        List<Dataset> datasets = ImmutableList.of(
                DatasetPopulated.dataset()
                        .id(1L)
                        .name("ONE")
                        .build(),
                DatasetPopulated.dataset()
                        .id(2L)
                        .name("TWO")
                        .build()
        );

        when(datasetRepository.findByStagingJobsServiceRequestIdAndValidationStatus(anyLong(), any()))
                .thenReturn(datasets);

        datasetAutoValidateTasklet.execute(stepContribution, chunkContext);

        verify(jobService).startValidationJob(1L, "ONE validation job", "matt");
        verify(jobService).startValidationJob(2L, "TWO validation job", "matt");
        verifyNoMoreInteractions(jobService);
    }
}
