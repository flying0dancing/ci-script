package com.lombardrisk.ignis.spark.core.mock;

import com.lombardrisk.ignis.client.core.view.IdView;
import com.lombardrisk.ignis.client.internal.CreateDatasetCall;
import com.lombardrisk.ignis.client.internal.InternalDatasetClient;
import com.lombardrisk.ignis.client.internal.RuleSummaryRequest;
import com.lombardrisk.ignis.client.internal.UpdateDatasetRunCall;
import lombok.Getter;
import retrofit2.Call;

import java.util.List;

/**
 * The pipeline request job calls out to the DatasetClient to store dataset metadata information
 * this class acts as a mock database for this client. Storing one dataset in memory to be retrieved later.
 */
@Getter
public class StatefulDatasetClientStore implements InternalDatasetClient {

    private static final long JOB_EXECUTION_ID = 1L;

    private CreateDatasetCall cachedDatasetCall;
    private UpdateDatasetRunCall cachedUpdateDatasetRunCall;

    @Override
    public Call<IdView> createDataset(final CreateDatasetCall datasetCall) {
        this.cachedDatasetCall = datasetCall;
        return new RetrofitCall<>(new IdView(123L));
    }

    @Override
    public Call<IdView> updateDatasetRun(final long datasetId, final UpdateDatasetRunCall datasetCall) {
        this.cachedUpdateDatasetRunCall = datasetCall;
        return new RetrofitCall<>(new IdView(456L));
    }

    @Override
    public Call<Void> createRuleSummaries(
            final long datasetId, final List<RuleSummaryRequest> summaryRequests) {
        return RetrofitCall.success();
    }
}
