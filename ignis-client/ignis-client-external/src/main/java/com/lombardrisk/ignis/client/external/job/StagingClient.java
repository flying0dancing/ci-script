package com.lombardrisk.ignis.client.external.job;

import com.lombardrisk.ignis.client.external.job.staging.StagingItemView;
import com.lombardrisk.ignis.client.external.path.api;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

public interface StagingClient {

    @PUT(api.external.v1.staging.ById)
    Call<Void> updateDataSetState(@Path(api.Params.ID) long datasetId, @Query("state") String state);

    @GET(api.external.v1.Staging)
    Call<List<StagingItemView>> getStagingDatasets(
            @Query(api.Params.JOB_ID) Long jobExecutionId);

    @GET(api.external.v1.staging.byID.ValidationErrorFile)
    Call<ResponseBody> downloadErrorFile(@Path(api.Params.ID) Long stagingDatasetId);
}
