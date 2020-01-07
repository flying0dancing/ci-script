package com.lombardrisk.ignis.client.internal;

import com.lombardrisk.ignis.client.core.view.IdView;
import com.lombardrisk.ignis.client.internal.path.api;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.List;

public interface InternalDatasetClient {

    @Headers("Content-Type:application/json")
    @POST(api.internal.Datasets)
    Call<IdView> createDataset(@Body CreateDatasetCall datasetCall);

    @Headers("Content-Type:application/json")
    @PATCH(api.internal.datasets.byId)
    Call<IdView> updateDatasetRun(@Path(api.Params.ID) long datasetId, @Body UpdateDatasetRunCall datasetCall);

    @Headers("Content-Type:application/json")
    @POST(api.internal.datasets.byID.ValidationResultsSummaries)
    Call<Void> createRuleSummaries(
            @Path(api.Params.ID) long datasetId,
            @Body List<RuleSummaryRequest> summaryRequests);
}
