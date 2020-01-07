package com.lombardrisk.ignis.client.external.drillback;

import com.lombardrisk.ignis.client.core.page.request.PageRequest;
import com.lombardrisk.ignis.client.external.path.api;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

import java.util.Map;

public interface DrillbackClient {

    @GET(api.external.v1.drillBack.dataset.byId)
    Call<DatasetRowDataView> getDrillback(
            @Path(api.Params.DATASET_ID) Long datasetId,
            @QueryMap Map<String, Object> options);

    default Call<DatasetRowDataView> getOutputDrillback(
            final Long datasetId,
            final Long pipelineId,
            final Long pipelineStepId,
            final PageRequest page) {
        Map<String, Object> options = page.toParameterMap();
        options.put("type", "output");
        options.put("pipelineId", pipelineId);
        options.put("pipelineStepId", pipelineStepId);
        return getDrillback(datasetId, options);
    }
}
