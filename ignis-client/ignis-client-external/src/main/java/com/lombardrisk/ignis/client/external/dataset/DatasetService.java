package com.lombardrisk.ignis.client.external.dataset;

import com.lombardrisk.ignis.client.core.page.request.PageRequest;
import com.lombardrisk.ignis.client.core.response.IgnisResponse;
import com.lombardrisk.ignis.client.core.response.IgnisResponseHandler;
import com.lombardrisk.ignis.client.external.dataset.model.PagedDataset;
import com.lombardrisk.ignis.client.external.path.api;
import com.lombardrisk.ignis.client.external.rule.ValidationRuleSummaryView;
import com.lombardrisk.ignis.client.external.rule.ValidationResultsDetailView;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class DatasetService {

    private final DatasetClient datasetClient;
    private final IgnisResponseHandler ignisResponseHandler;

    public DatasetService(
            final DatasetClient datasetClient,
            final IgnisResponseHandler ignisResponseHandler) {
        this.datasetClient = datasetClient;
        this.ignisResponseHandler = ignisResponseHandler;
    }

    public IgnisResponse<PagedDataset> findDatasets(final DatasetQueryParams datasetQuery) throws IOException {
        Call<PagedDataset> datasets = datasetClient.getDatasets(datasetQuery.toParameterMap());

        return ignisResponseHandler.handleResponse(datasets.execute());
    }

    public IgnisResponse<List<ValidationRuleSummaryView>> findValidationResultSummaries(final long datasetId)
            throws IOException {
        Call<List<ValidationRuleSummaryView>> resultsSummaries = datasetClient.getValidationResultsSummaries(datasetId);

        return ignisResponseHandler.handleResponse(resultsSummaries.execute());
    }

    public IgnisResponse<ValidationResultsDetailView> findValidationResultDetails(
            final long datasetId, final PageRequest page) throws IOException {

        Call<ValidationResultsDetailView> resultsSummaries = datasetClient.getValidationResultsDetails(
                datasetId, page.toParameterMap());

        return ignisResponseHandler.handleResponse(resultsSummaries.execute());
    }

    public IgnisResponse<ValidationResultsDetailView> findValidationResultDetails(
            final long datasetId, final long ruleId, final PageRequest page) throws IOException {

        Map<String, Object> options = new HashMap<>();
        options.put(api.Params.RULE_ID, ruleId);
        options.putAll(page.toParameterMap());

        Call<ValidationResultsDetailView> resultsSummaries = datasetClient.getValidationResultsDetails(
                datasetId, options);

        return ignisResponseHandler.handleResponse(resultsSummaries.execute());
    }
}
