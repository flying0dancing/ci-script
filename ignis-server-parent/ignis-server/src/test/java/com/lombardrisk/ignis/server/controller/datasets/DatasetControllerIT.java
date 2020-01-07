package com.lombardrisk.ignis.server.controller.datasets;

import com.lombardrisk.ignis.client.internal.CreateDatasetCall;
import com.lombardrisk.ignis.client.internal.RuleSummaryRequest;
import com.lombardrisk.ignis.client.internal.UpdateDatasetRunCall;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.feature.IgnisFeature;
import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.dataset.DatasetService;
import com.lombardrisk.ignis.server.dataset.fixture.DatasetPopulated;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.dataset.rule.ValidationResultsSummaryService;
import com.lombardrisk.ignis.spark.api.staging.DatasetProperties;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.togglz.core.manager.FeatureManager;
import org.togglz.junit.TogglzRule;

import java.time.LocalDate;
import java.util.Arrays;

import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static com.lombardrisk.ignis.test.config.AdminUser.BASIC_AUTH;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class DatasetControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DatasetService datasetService;

    @MockBean
    private ValidationResultsSummaryService validationRuleSummaryService;

    @MockBean
    private FeatureManager featureManager;

    @Captor
    private ArgumentCaptor<DatasetProperties> datasetPropertiesArgumentCaptor;

    @Rule
    public TogglzRule togglz = TogglzRule.allDisabled(IgnisFeature.class);

    @Before
    public void setUp() {
        when(featureManager.isActive(any()))
                .thenReturn(true);
        when(datasetService.findById(anyLong()))
                .thenReturn(Option.of(DatasetPopulated.dataset().build()));

        when(validationRuleSummaryService.create(any(), any()))
                .thenReturn(Either.right(singletonList(DatasetPopulated.validationRuleSummary().build())));
    }

    @Test
    public void getLastVersionOfDataset4AR_CallsService() throws Exception {
        when(datasetService.findLatestDataset(any(), any()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset().build()));

        mockMvc.perform(
                get("/api/datasets/EMPLOYEE/lastVersionForAR")
                        .with(BASIC_AUTH)
                        .param("entityCodeField", "ENTITY_ID")
                        .param("entityCode", "0001")
                        .param("referenceDateField", "REFERENCE_DATE")
                        .param("referenceDate", "13/12/2017"))
                .andExpect(status().isOk());

        verify(datasetService).findLatestDataset(eq("EMPLOYEE"), datasetPropertiesArgumentCaptor.capture());
        assertThat(datasetPropertiesArgumentCaptor.getValue())
                .isEqualTo(DatasetProperties.builder()
                        .entityCode("0001")
                        .referenceDate(LocalDate.of(2017, 12, 13))
                        .build());
    }

    @Test
    public void getLastVersionOfDataset4AR_DatasetNotFound_ReturnsError() throws Exception {
        DatasetProperties datasetProperties = com.lombardrisk.ignis.spark.api.fixture.Populated.datasetProperties()
                .referenceDate(LocalDate.of(2017, 12, 13))
                .entityCode("0001")
                .build();

        when(datasetService.findLatestDataset(any(), any()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIndices(
                        "metadata",
                        "Dataset",
                        singletonList(MAPPER.writeValueAsString(datasetProperties)))));

        mockMvc.perform(
                get("/api/datasets/EMPLOYEE/lastVersionForAR")
                        .with(BASIC_AUTH)
                        .param("entityCodeField", "ENTITY_ID")
                        .param("entityCode", "0001")
                        .param("referenceDateField", "REFERENCE_DATE")
                        .param("referenceDate", "13/12/2017"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$[0].errorMessage")
                        .value("Could not find Dataset for metadatas "
                                + "[{\"entityCode\":\"0001\",\"referenceDate\":\"2017-12-13\"}]"));
    }

    @Test
    public void getLastVersionOfDataset4AR_InvalidReferenceDate_ReturnsError() throws Exception {
        mockMvc.perform(
                get("/api/datasets/EMPLOYEE/lastVersionForAR")
                        .with(BASIC_AUTH)
                        .param("entityCodeField", "ENTITY_ID")
                        .param("entityCode", "0001")
                        .param("referenceDateField", "REFERENCE_DATE")
                        .param("referenceDate", "2017-01-01"))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("INVALID_REQUEST_PARAMETERS"))
                .andExpect(jsonPath("$[0].errorMessage").value(
                        "Supplied parameters are invalid [(referenceDate, 2017-01-01)]"));
    }

    @Test
    public void createSummaries_DatasetNotFound_ReturnsBadRequestResponse() throws Exception {
        when(datasetService.findById(anyLong()))
                .thenReturn(Option.none());

        mockMvc.perform(
                post("/api/internal/datasets/123/validationResultsSummaries")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(emptyList())))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("DATASET"))
                .andExpect(jsonPath("$[0].errorMessage").value("Dataset not found for id 123"));

        verify(datasetService).findById(123L);
    }

    @Test
    public void createDataset_ReturnsIdentifiable() throws Exception {
        when(datasetService.createDataset(any(CreateDatasetCall.class)))
                .thenReturn(Validation.valid(
                        DatasetPopulated.dataset().id(121L).build()));

        mockMvc.perform(
                post("/api/internal/datasets")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(
                                DatasetPopulated.createDatasetCall().build())))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(121)))
                .andExpect(jsonPath("$.*", containsInAnyOrder(121)));

        verify(datasetService).createDataset(any(CreateDatasetCall.class));
    }

    @Test
    public void createDataset_ReturnsErrors() throws Exception {
        when(datasetService.createDataset(any(CreateDatasetCall.class)))
                .thenReturn(Validation.invalid(
                        CRUDFailure.constraintFailure("oops")));

        mockMvc.perform(
                post("/api/internal/datasets")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(
                                DatasetPopulated.createDatasetCall().build())))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorMessage", equalTo("oops")))
                .andExpect(jsonPath("[0].errorCode", notNullValue()));
    }

    @Test
    public void createSummaries_RuleNotFound_ReturnsBadRequestResponse() throws Exception {
        when(validationRuleSummaryService.create(any(), any()))
                .thenReturn(Either.left(
                        CRUDFailure.notFoundIds("validation rule", Arrays.asList(1L, 2L, 3L, 4L))));

        mockMvc.perform(
                post("/api/internal/datasets/123/validationResultsSummaries")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(emptyList())))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$[0].errorMessage")
                        .value("Could not find validation rule for ids [1, 2, 3, 4]"));
    }

    @Test
    public void createSummaries_CallRuleSetService() throws Exception {

        Dataset dataset = DatasetPopulated.dataset().build();
        when(datasetService.findById(anyLong()))
                .thenReturn(Option.of(dataset));

        RuleSummaryRequest ruleSummaryRequest = DatasetPopulated.ruleSummaryRequest().build();
        mockMvc.perform(
                post("/api/internal/datasets/123/validationResultsSummaries")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(
                                singletonList(ruleSummaryRequest))));

        verify(validationRuleSummaryService).create(dataset, singletonList(ruleSummaryRequest));
    }

    @Test
    public void createSummaries_ReturnsNewSummariesIds() throws Exception {

        Dataset dataset = DatasetPopulated.dataset().build();
        when(datasetService.findById(anyLong()))
                .thenReturn(Option.of(dataset));

        when(validationRuleSummaryService.create(any(), any()))
                .thenReturn(Either.right(Arrays.asList(
                        DatasetPopulated.validationRuleSummary().id(1L).build(),
                        DatasetPopulated.validationRuleSummary().id(2L).build(),
                        DatasetPopulated.validationRuleSummary().id(3L).build()
                )));

        mockMvc.perform(
                post("/api/internal/datasets/123/validationResultsSummaries")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(
                                Arrays.asList(
                                        DatasetPopulated.ruleSummaryRequest().build(),
                                        DatasetPopulated.ruleSummaryRequest().build(),
                                        DatasetPopulated.ruleSummaryRequest().build()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[2].id").value(3));
    }

    @Test
    public void updateDatasetRun_InvalidRequestBody_ReturnsBadRequest() throws Exception {
        mockMvc.perform(
                patch("/api/internal/datasets/1234")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"not\":\"valid\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode", equalTo("JSON parsing error")));
    }

    @Test
    public void updateDatasetRun_DatasetIdNotFound_ReturnsBadRequest() throws Exception {
        when(datasetService.updateDatasetRun(any(), any()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("Dataset", 1234L)));

        mockMvc.perform(
                patch("/api/internal/datasets/1234")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stagingJobId\":1111,\"recordsCount\":10000}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode", equalTo("NOT_FOUND")))
                .andExpect(jsonPath("$[0].errorMessage", containsString("[1234]")));
    }

    @Test
    public void updateDatasetRun_DatasetIdFound_ReturnsSuccess() throws Exception {
        when(datasetService.updateDatasetRun(any(), any()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset().id(1234L).build()));

        mockMvc.perform(
                patch("/api/internal/datasets/1234")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stagingJobId\":1111,\"recordsCount\":10000}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1234)));
    }

    @Test
    public void updateDatasetRun_CallsDatasetService() throws Exception {
        when(datasetService.updateDatasetRun(any(), any()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset().id(1234L).build()));

        mockMvc.perform(
                patch("/api/internal/datasets/1234")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stagingJobId\":1111,\"recordsCount\":10000}"))
                .andDo(print())
                .andExpect(status().isOk());

        ArgumentCaptor<Long> datasetIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<UpdateDatasetRunCall> updateDatasetRequest = ArgumentCaptor.forClass(UpdateDatasetRunCall.class);

        verify(datasetService).updateDatasetRun(datasetIdCaptor.capture(), updateDatasetRequest.capture());

        assertThat(datasetIdCaptor.getValue()).isEqualTo(1234L);
        assertThat(updateDatasetRequest.getValue().getStagingJobId()).isEqualTo(1111L);
        assertThat(updateDatasetRequest.getValue().getRecordsCount()).isEqualTo(10000L);

        verifyNoMoreInteractions(datasetService);
    }
}
