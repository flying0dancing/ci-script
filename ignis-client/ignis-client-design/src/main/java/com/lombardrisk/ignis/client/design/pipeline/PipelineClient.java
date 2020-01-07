package com.lombardrisk.ignis.client.design.pipeline;

import com.lombardrisk.ignis.client.core.view.IdView;
import com.lombardrisk.ignis.client.design.path.design;
import com.lombardrisk.ignis.client.design.pipeline.test.request.CreatePipelineStepDataRowRequest;
import com.lombardrisk.ignis.client.design.pipeline.test.request.CreateStepTestRequest;
import com.lombardrisk.ignis.client.design.pipeline.test.request.RowCellDataRequest;
import com.lombardrisk.ignis.client.design.pipeline.test.request.UpdateStepTestRequest;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepRowInputDataView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepRowOutputDataView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepTestRowView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepTestView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.TestRunResultView;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.List;
import java.util.Map;

public interface PipelineClient {

    @POST(design.api.v1.Pipelines)
    Call<PipelineView> createPipeline(@Body CreatePipelineRequest request);

    @GET(design.api.v1.pipelines.ById)
    Call<PipelineView> findPipeline(
            @Path(design.api.Params.PIPELINE_ID) Long pipelineId);

    @POST(design.api.v1.pipelines.pipelineId.Steps)
    Call<PipelineStepView> createPipelineStep(
            @Body PipelineStepRequest request,
            @Path(design.api.Params.PIPELINE_ID) Long pipelineId);

    @POST(design.api.v1.PipelineStepTests)
    Call<IdView> createPipelineStepTest(
            @Body CreateStepTestRequest request);

    @PATCH(design.api.v1.pipelineStepTests.ById)
    Call<StepTestView> updatePipelineStepTest(
            @Path(design.api.Params.TEST_ID) Long testId,
            @Body UpdateStepTestRequest request);

    @DELETE(design.api.v1.pipelineStepTests.ById)
    Call<IdView> deletePipelineStepTest(
            @Path(design.api.Params.TEST_ID) Long testId);

    @POST(design.api.v1.pipelineStepTests.inputDataRow)
    Call<StepTestRowView> createPipelineTestInputDataRow(
            @Path(design.api.Params.TEST_ID) Long testId,
            @Body CreatePipelineStepDataRowRequest request);

    @POST(design.api.v1.pipelineStepTests.expectedDataRow)
    Call<StepTestRowView> createPipelineTestExpectedDataRow(
            @Path(design.api.Params.TEST_ID) Long testId,
            @Body CreatePipelineStepDataRowRequest request);

    @GET(design.api.v1.pipelineStepTests.inputDataRow)
    Call<Map<Long, List<StepRowInputDataView>>> getTestInputRows(
            @Path(design.api.Params.TEST_ID) Long testId);

    @GET(design.api.v1.pipelineStepTests.outputDataRow)
    Call<List<StepRowOutputDataView>> getTestOutputRows(
            @Path(design.api.Params.TEST_ID) Long testId);

    @PATCH(design.api.v1.pipelineStepTests.rowCellDataByRowCellDataId)
    Call<StepTestRowView> patchRowCellData(
            @Path(design.api.Params.TEST_ID) Long testId,
            @Path(design.api.Params.ROW_ID) Long rowId,
            @Path(design.api.Params.ROW_CELL_DATA_ID) Long rowCellDataId,
            @Body RowCellDataRequest request);

    @POST(design.api.v1.pipelineStepTests.run)
    Call<TestRunResultView> runTest(
            @Path(design.api.Params.TEST_ID) Long testId);

    @GET(design.api.v1.pipelineStepTests.ById)
    Call<StepTestView> getPipelineStepTest(
            @Path(design.api.Params.TEST_ID) Long testId);

    @DELETE(design.api.v1.pipelineStepTests.expectedDataRowById)
    Call<IdView> deleteExpectedDataRow(
            @Path(design.api.Params.TEST_ID) Long testId,
            @Path(design.api.Params.ROW_ID) Long rowId);
}
