package com.lombardrisk.ignis.spark.staging;

import com.lombardrisk.ignis.spark.api.staging.StagingAppConfig;
import com.lombardrisk.ignis.spark.core.JobOperator;
import com.lombardrisk.ignis.spark.staging.execution.DatasetStagingWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StagingJobOperator implements JobOperator {

    private final DatasetStagingWorker datasetStagingWorker;
    private final StagingAppConfig stagingJobRequest;

    public StagingJobOperator(final DatasetStagingWorker datasetStagingWorker, final StagingAppConfig jobRequest) {
        this.datasetStagingWorker = datasetStagingWorker;
        this.stagingJobRequest = jobRequest;
    }

    @Override
    public void runJob() {
        final long rowKeySeed = stagingJobRequest.getServiceRequestId();

        log.info("Start staging job [{}]", rowKeySeed);
        log.trace("{}", stagingJobRequest);

        stagingJobRequest.getItems()
                .forEach(item -> datasetStagingWorker.execute(rowKeySeed, item));
    }
}