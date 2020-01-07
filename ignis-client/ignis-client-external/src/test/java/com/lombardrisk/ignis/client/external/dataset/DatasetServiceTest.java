package com.lombardrisk.ignis.client.external.dataset;

import com.lombardrisk.ignis.client.core.fixture.ClientCore;
import com.lombardrisk.ignis.client.core.page.request.PageRequest;
import com.lombardrisk.ignis.client.core.page.request.Sort;
import com.lombardrisk.ignis.client.core.response.IgnisResponse;
import com.lombardrisk.ignis.client.core.response.IgnisResponseHandler;
import com.lombardrisk.ignis.client.external.dataset.model.Dataset;
import com.lombardrisk.ignis.client.external.dataset.model.PagedDataset;
import com.lombardrisk.ignis.client.external.fixture.ExternalClient;
import com.lombardrisk.ignis.client.external.rule.ValidationRuleSummaryView;
import com.lombardrisk.ignis.client.external.rule.ValidationResultsDetailView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatasetServiceTest {

    @Mock
    private DatasetClient datasetClient;

    @Mock
    private IgnisResponseHandler ignisResponseMappingService;

    @InjectMocks
    private DatasetService datasetService;

    @Mock
    private Call<PagedDataset> datasetCall;
    @Mock
    private Call<List<ValidationRuleSummaryView>> resultsSummariesCall;
    @Mock
    private Call<ValidationResultsDetailView> detailViewCall;

    @Captor
    private ArgumentCaptor<Map<String, Object>> paramMapCaptor;

    @Before
    public void setUp() throws Exception {
        when(datasetClient.getDatasets(any()))
                .thenReturn(datasetCall);
        when(datasetClient.getValidationResultsSummaries(anyLong()))
                .thenReturn(resultsSummariesCall);
        when(datasetClient.getValidationResultsDetails(anyLong(), any()))
                .thenReturn(detailViewCall);

        when(datasetCall.execute())
                .thenReturn(Response.success(ExternalClient.Populated.pageFromList(
                        singletonList(ExternalClient.Populated.datasetDto().build()))));

        when(resultsSummariesCall.execute())
                .thenReturn(Response.success(
                        singletonList(ExternalClient.Populated.validationRuleSummaryView().build())));

        when(ignisResponseMappingService.handleResponse(Mockito.<Response<List<Dataset>>>any()))
                .thenReturn(IgnisResponse.success(singletonList(ExternalClient.Populated.datasetDto().build())));

        when(ignisResponseMappingService.handleResponse(Mockito.<Response<List<ValidationRuleSummaryView>>>any()))
                .thenReturn(IgnisResponse.success(singletonList(ExternalClient.Populated.validationRuleSummaryView()
                        .build())));
    }

    @Test
    public void findDatasets_clientThrowsException() throws IOException {
        RuntimeException runtimeException = new RuntimeException("Ooops");
        when(datasetCall.execute())
                .thenThrow(runtimeException);

        assertThatThrownBy(() -> datasetService.findDatasets(ExternalClient.Populated.datasetQueryParams().build()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Ooops");
    }

    @Test
    public void findDatasets_clientReturnsDataset_ReturnsDataset() throws IOException {
        when(datasetCall.execute())
                .thenReturn(Response.success(ExternalClient.Populated.pageFromList(
                        singletonList(ExternalClient.Populated.datasetDto().build()))));

        Dataset dataset = Dataset.builder()
                .id(900L)
                .name("Dataset")
                .predicate("WHERE 1=1")
                .build();

        PagedDataset pagedResponse = ExternalClient.Populated.pageFromList(singletonList(dataset));

        when(ignisResponseMappingService.handleResponse(any()))
                .thenReturn(IgnisResponse.success(pagedResponse));

        IgnisResponse<PagedDataset> datasets = datasetService.findDatasets(
                ExternalClient.Populated.datasetQueryParams().build());

        assertThat(datasets.getValue().getEmbedded().getData())
                .contains(dataset);
    }

    @Test
    public void findDatasets_clientReturnsDataset_CallsResponseServiceWithDataset() throws IOException {
        Dataset dataset = ExternalClient.Populated.datasetDto().build();
        Response<PagedDataset> response = Response.success(ExternalClient.Populated.pageFromList(
                singletonList(dataset)));

        when(datasetCall.execute()).thenReturn(response);

        datasetService.findDatasets(ExternalClient.Populated.datasetQueryParams().build());

        verify(ignisResponseMappingService).handleResponse(eq(response));
    }

    @Test
    public void findDatasets_callClientWithQueryParams() throws IOException {
        LocalDate date = LocalDate.of(2000, 1, 1);
        DatasetQueryParams datasetQuery = ExternalClient.Populated.datasetQueryParams()
                .name("Dataset")
                .entityCode("EntityCode")
                .referenceDate(date)
                .build();

        datasetService.findDatasets(datasetQuery);
        verify(datasetClient).getDatasets(paramMapCaptor.capture());
        assertThat(paramMapCaptor.getValue())
                .contains(
                        entry("datasetName", "Dataset"),
                        entry("entityCode", "EntityCode"),
                        entry("referenceDate", "2000-01-01"));
    }

    @Test
    public void findDatasets_callClientWithPagingParams() throws IOException {
        DatasetQueryParams datasetQuery = ExternalClient.Populated.datasetQueryParams()
                .pageRequest(aPage()
                        .build())
                .build();

        datasetService.findDatasets(datasetQuery);
        verify(datasetClient).getDatasets(paramMapCaptor.capture());
        assertThat(paramMapCaptor.getValue())
                .contains(
                        entry("page", 1),
                        entry("size", 100));
    }

    @Test
    public void findDatasets_callClientWithSortingParams() throws IOException {
        DatasetQueryParams datasetQuery = ExternalClient.Populated.datasetQueryParams()
                .pageRequest(ClientCore.Populated.pageRequest()
                        .sort(ClientCore.Populated.sort()
                                .field("name")
                                .direction(Sort.Direction.ASC)
                                .build())
                        .build())
                .build();

        datasetService.findDatasets(datasetQuery);
        verify(datasetClient).getDatasets(paramMapCaptor.capture());
        assertThat(paramMapCaptor.getValue())
                .contains(
                        entry("sort", "name,asc"));
    }

    @Test
    public void findValidationResultSummaries_clientThrowsException() throws IOException {
        RuntimeException runtimeException = new RuntimeException("Ooops");
        when(resultsSummariesCall.execute())
                .thenThrow(runtimeException);

        assertThatThrownBy(() -> datasetService.findValidationResultSummaries(1))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Ooops");
    }

    @Test
    public void findValidationResultSummaries_clientReturnsDataset_ReturnsDataset() throws IOException {
        ValidationRuleSummaryView ruleSummaryView = ExternalClient.Populated.validationRuleSummaryView().build();

        List<ValidationRuleSummaryView> pagedResponse = singletonList(ruleSummaryView);

        when(ignisResponseMappingService.handleResponse(any()))
                .thenReturn(IgnisResponse.success(pagedResponse));

        IgnisResponse<List<ValidationRuleSummaryView>> resultSummaries =
                datasetService.findValidationResultSummaries(1L);

        assertThat(resultSummaries.getValue())
                .contains(ruleSummaryView);
    }

    @Test
    public void findValidationResultSummaries_clientReturnsDataset_CallsResponseServiceWithDataset() throws IOException {
        ValidationRuleSummaryView ruleSummaryView = ExternalClient.Populated.validationRuleSummaryView().build();

        Response<List<ValidationRuleSummaryView>> response = Response.success(
                singletonList(ruleSummaryView));
        when(resultsSummariesCall.execute())
                .thenReturn(response);

        datasetService.findValidationResultSummaries(1L);

        verify(ignisResponseMappingService).handleResponse(eq(response));
    }

    @Test
    public void findValidationResultSummaries_callClientWithQueryParams() throws IOException {
        datasetService.findValidationResultSummaries(900L);
        verify(datasetClient).getValidationResultsSummaries(900L);
    }

    @Test
    public void findValidationResultDetails_clientThrowsException() throws IOException {
        RuntimeException runtimeException = new RuntimeException("Ooops");
        when(detailViewCall.execute())
                .thenThrow(runtimeException);

        assertThatThrownBy(() -> datasetService.findValidationResultDetails(1, aPage().build()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Ooops");
    }

    @Test
    public void findValidationResultDetails_clientReturnsDataset_ReturnsDataset() throws IOException {
        ValidationResultsDetailView resultsDetailView = ExternalClient.Populated.validationResultsDetailView()
                .build();

        when(ignisResponseMappingService.handleResponse(any()))
                .thenReturn(IgnisResponse.success(resultsDetailView));

        IgnisResponse<ValidationResultsDetailView> resultSummaries =
                datasetService.findValidationResultDetails(1, aPage().build());

        assertThat(resultSummaries.getValue())
                .isEqualTo(resultsDetailView);
    }

    @Test
    public void findValidationResultDetails_clientReturnsDataset_CallsResponseServiceWithDataset() throws IOException {
        ValidationResultsDetailView resultsDetailView = ExternalClient.Populated.validationResultsDetailView()
                .build();

        Response<ValidationResultsDetailView> response = Response.success(resultsDetailView);

        when(detailViewCall.execute())
                .thenReturn(response);

        datasetService.findValidationResultDetails(1L, aPage().build());

        verify(ignisResponseMappingService).handleResponse(eq(response));
    }

    @Test
    public void findValidationResultDetails_callClientWithQueryParams() throws IOException {
        PageRequest build = aPage()
                .page(1)
                .size(100)
                .sort(Sort.builder()
                        .field("Name")
                        .direction(Sort.Direction.DESC)
                        .build())
                .build();

        datasetService.findValidationResultDetails(900L, build);
        verify(datasetClient).getValidationResultsDetails(eq(900L), paramMapCaptor.capture());

        assertThat(paramMapCaptor.getValue())
                .containsAnyOf(
                        entry("page", "1"),
                        entry("size", "100"),
                        entry("sort", "Name,desc"));
    }

    @Test
    public void findValidationResultDetailsWithRuleId_clientThrowsException() throws IOException {
        RuntimeException runtimeException = new RuntimeException("Ooops");
        when(detailViewCall.execute())
                .thenThrow(runtimeException);

        assertThatThrownBy(() -> datasetService.findValidationResultDetails(1, 8181, aPage().build()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Ooops");
    }

    @Test
    public void findValidationResultDetailsWithRuleId_clientReturnsDataset_ReturnsDataset() throws IOException {
        ValidationResultsDetailView resultsDetailView = ExternalClient.Populated.validationResultsDetailView()
                .build();

        when(ignisResponseMappingService.handleResponse(any()))
                .thenReturn(IgnisResponse.success(resultsDetailView));

        IgnisResponse<ValidationResultsDetailView> resultSummaries =
                datasetService.findValidationResultDetails(1, 8181, aPage().build());

        assertThat(resultSummaries.getValue())
                .isEqualTo(resultsDetailView);
    }

    @Test
    public void findValidationResultDetailsWithRuleId_clientReturnsDataset_CallsResponseServiceWithDataset() throws IOException {
        ValidationResultsDetailView resultsDetailView = ExternalClient.Populated.validationResultsDetailView()
                .build();

        Response<ValidationResultsDetailView> response = Response.success(resultsDetailView);

        when(detailViewCall.execute())
                .thenReturn(response);

        datasetService.findValidationResultDetails(1L, 8181, aPage().build());

        verify(ignisResponseMappingService).handleResponse(eq(response));
    }

    @Test
    public void findValidationResultDetailsWithRuleId_callClientWithQueryParams() throws IOException {
        PageRequest build = aPage()
                .page(1)
                .size(100)
                .sort(Sort.builder()
                        .field("Name")
                        .direction(Sort.Direction.DESC)
                        .build())
                .build();

        datasetService.findValidationResultDetails(900L, 88181L, build);
        verify(datasetClient).getValidationResultsDetails(eq(900L), paramMapCaptor.capture());

        assertThat(paramMapCaptor.getValue())
                .containsAnyOf(
                        entry("ruleId", 88181L),
                        entry("page", "1"),
                        entry("size", "100"),
                        entry("sort", "Name,desc"));
    }

    private PageRequest.PageRequestBuilder aPage() {
        return PageRequest.builder()
                .size(100)
                .page(1);
    }
}