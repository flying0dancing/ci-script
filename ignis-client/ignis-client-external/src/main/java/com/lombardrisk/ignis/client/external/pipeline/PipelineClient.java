package com.lombardrisk.ignis.client.external.pipeline;

import com.lombardrisk.ignis.client.external.path.api;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineInvocationView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineView;
import retrofit2.Call;
import retrofit2.http.GET;

import java.util.List;

public interface PipelineClient {

    @GET(api.external.v1.Pipelines)
    Call<List<PipelineView>> getPipelines();

    @GET(api.external.v1.PipelineInvocations)
    Call<List<PipelineInvocationView>> getPipelineInvocations();
}
