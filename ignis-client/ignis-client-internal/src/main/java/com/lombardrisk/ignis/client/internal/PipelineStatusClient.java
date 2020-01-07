package com.lombardrisk.ignis.client.internal;

import com.lombardrisk.ignis.client.core.view.IdView;
import com.lombardrisk.ignis.client.internal.path.api;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface PipelineStatusClient {

    @Headers("Content-Type:application/json")
    @POST(api.internal.pipelineInvocations.ById)
    Call<IdView> updatePipelineStepInvocationStatus(
            @Path(api.Params.ID) long pipelineInvocationId,
            @Body UpdatePipelineStepStatusRequest request);
}
