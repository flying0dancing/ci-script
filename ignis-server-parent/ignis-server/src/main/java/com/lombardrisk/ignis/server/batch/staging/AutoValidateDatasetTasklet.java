package com.lombardrisk.ignis.server.batch.staging;

import com.lombardrisk.ignis.server.batch.JobService;
import com.lombardrisk.ignis.server.batch.SparkJobExecutor;
import com.lombardrisk.ignis.server.dataset.DatasetJpaRepository;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.List;

import static com.lombardrisk.ignis.api.dataset.ValidationStatus.QUEUED;

@Slf4j
public class AutoValidateDatasetTasklet implements StoppableTasklet {

    private final SparkJobExecutor sparkJobExecutor;
    private final DatasetJpaRepository datasetRepository;
    private final JobService jobService;

    public AutoValidateDatasetTasklet(
            final SparkJobExecutor sparkJobExecutor,
            final DatasetJpaRepository datasetRepository,
            final JobService jobService) {
        this.sparkJobExecutor = sparkJobExecutor;
        this.datasetRepository = datasetRepository;
        this.jobService = jobService;
    }

    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        ServiceRequest serviceRequest = sparkJobExecutor.setupRequest(chunkContext);

        Long serviceRequestId = serviceRequest.getId();
        List<Dataset> datasets = datasetRepository
                .findByStagingJobsServiceRequestIdAndValidationStatus(serviceRequestId, QUEUED);

        if (datasets.isEmpty()) {
            log.info("No datasets were marked for auto validation for job [{}]", serviceRequestId);
            return RepeatStatus.FINISHED;
        }

        for (final Dataset dataset : datasets) {
            log.info("Starting validation job for dataset [{}]", dataset.getId());

            jobService.startValidationJob(
                    dataset.getId(),
                    dataset.getName() + " validation job",
                    serviceRequest.getCreatedBy());
        }
        return RepeatStatus.FINISHED;
    }

    @Override
    public void stop() {
        //no-op
    }
}
