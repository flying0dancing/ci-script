package com.lombardrisk.ignis.client.external.dataset;

import com.lombardrisk.ignis.client.external.dataset.model.PagedDataset;
import com.lombardrisk.ignis.client.external.path.api;
import com.lombardrisk.ignis.client.external.path.api.Params;
import com.lombardrisk.ignis.client.external.rule.ValidationRuleSummaryView;
import com.lombardrisk.ignis.client.external.rule.ValidationResultsDetailView;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

import java.util.List;
import java.util.Map;

public interface DatasetClient {

    @GET(api.external.v1.Datasets)
    Call<PagedDataset> getDatasets(@QueryMap Map<String, Object> options);

    @GET(api.external.v1.datasets.byID.ValidationResultsSummaries)
    Call<List<ValidationRuleSummaryView>> getValidationResultsSummaries(@Path(Params.ID) long datasetId);

    @GET(api.external.v1.datasets.byID.ValidationResultsDetails)
    Call<ValidationResultsDetailView> getValidationResultsDetails(
            @Path(Params.ID) long datasetId,
            @QueryMap Map<String, Object> options);
}
