package com.lombardrisk.ignis.client.external.job;

import com.lombardrisk.ignis.client.core.view.IdView;
import com.lombardrisk.ignis.client.core.view.PagedView;
import com.lombardrisk.ignis.client.external.job.pipeline.PipelineRequest;
import com.lombardrisk.ignis.client.external.job.staging.request.v1.StagingRequest;
import com.lombardrisk.ignis.client.external.job.staging.request.v2.StagingRequestV2;
import com.lombardrisk.ignis.client.external.job.validation.ValidationJobRequest;
import com.lombardrisk.ignis.client.external.path.api;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface JobsClient {

    @POST(api.external.v1.Jobs)
    Call<IdView> startJob(
            @Body ValidationJobRequest validationJobRequest,
            @Query("type") String type);

    default Call<IdView> startJob(@Body final ValidationJobRequest validationJobRequest) {
        return startJob(validationJobRequest, "validation");
    }

    @GET(api.external.v1.Jobs)
    Call<PagedView<JobExecutionView>> getAllJobs();

    @POST(api.external.v1.Jobs)
    Call<IdView> startJob(
            @Body StagingRequest jobRequest,
            @Query("type") String type);
    default Call<IdView> startJob(@Body final StagingRequest stagingRequest) {
        return startJob(stagingRequest, "staging");
    }

    @POST(api.external.v2.Jobs)
    Call<IdView> startJob(
            @Body StagingRequestV2 jobRequest,
            @Query("type") String type);
    default Call<IdView> startJob(@Body final StagingRequestV2 stagingRequest) {
        return startJob(stagingRequest, "staging");
    }

    @POST(api.external.v1.Jobs)
    Call<IdView> startJob(
            @Body PipelineRequest jobRequest,
            @Query("type") String type);

    default Call<IdView> startJob(@Body final PipelineRequest jobRequest) {
        return startJob(jobRequest, "pipeline");
    }

    @GET(api.external.v1.jobs.ById)
    Call<JobExecutionView> getJob(
            @Path(api.Params.ID) Long jobExecutionId);
}
