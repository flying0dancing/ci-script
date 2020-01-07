package com.lombardrisk.ignis.spark.staging.execution;

import com.lombardrisk.ignis.api.dataset.DatasetState;
import com.lombardrisk.ignis.client.external.job.StagingClient;
import com.lombardrisk.ignis.spark.staging.exception.StagingJobOperatorException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class StagingListener {

    private StagingClient stagingClient;

    public StagingListener(final StagingClient stagingClient) {
        this.stagingClient = stagingClient;
    }

    private void updateDatasetState(final long stagingDatasetId, final DatasetState state) {
        try {
            stagingClient.updateDataSetState(stagingDatasetId, state.name()).execute();
        } catch (final IOException e) {
            throw new StagingJobOperatorException(e);
        }
    }

    void onValidationStart(final long stagingDatasetId) {
        updateDatasetState(stagingDatasetId, DatasetState.VALIDATING);
    }

    void onValidationFinished(final long stagingDatasetId) {
        updateDatasetState(stagingDatasetId, DatasetState.VALIDATED);
    }

    void onValidationFailed(final long stagingDatasetId) {
        updateDatasetState(stagingDatasetId, DatasetState.VALIDATION_FAILED);
    }

    void onRegisterStart(final long stagingDatasetId) {
        updateDatasetState(stagingDatasetId, DatasetState.REGISTERING);
    }

    void onRegisterFinished(final long stagingDatasetId) {
        updateDatasetState(stagingDatasetId, DatasetState.REGISTERED);
    }

    void onRegisterFailed(final long stagingDatasetId) {
        updateDatasetState(stagingDatasetId, DatasetState.REGISTRATION_FAILED);
    }
}