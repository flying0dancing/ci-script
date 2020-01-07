package com.lombardrisk.ignis.server.controller.external;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.api.dataset.ValidationStatus;
import com.lombardrisk.ignis.api.rule.SummaryStatus;
import com.lombardrisk.ignis.client.core.page.response.Page;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport;
import com.lombardrisk.ignis.client.external.rule.ValidationResultsDetailView;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.dataset.DatasetService;
import com.lombardrisk.ignis.server.dataset.fixture.DatasetPopulated;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.dataset.model.DatasetOnly;
import com.lombardrisk.ignis.server.dataset.model.DatasetQuery;
import com.lombardrisk.ignis.server.dataset.model.DatasetServiceRequest;
import com.lombardrisk.ignis.server.dataset.rule.ValidationResultDetailService;
import com.lombardrisk.ignis.server.dataset.rule.ValidationResultsSummaryService;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.table.model.DateField;
import com.lombardrisk.ignis.server.product.table.model.LongField;
import com.lombardrisk.ignis.server.product.table.model.StringField;
import com.lombardrisk.ignis.server.product.table.model.Table;
import io.vavr.Tuple;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static com.lombardrisk.ignis.common.fixtures.PopulatedDates.toDateTime;
import static com.lombardrisk.ignis.test.config.AdminUser.BASIC_AUTH;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class ExternalDatasetControllerIT {

    @Rule
    public final JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DatasetService datasetService;

    @MockBean
    private ValidationResultsSummaryService validationRuleSummaryService;

    @MockBean
    private ValidationResultDetailService validationResultDetailService;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @Captor
    private ArgumentCaptor<DatasetQuery> queryCaptor;

    @Before
    public void setUp() {
        when(datasetService.findById(anyLong()))
                .thenReturn(Option.of(DatasetPopulated.dataset().build()));
        when(validationRuleSummaryService.findByDatasetId(anyLong()))
                .thenReturn(emptyList());

        when(validationResultDetailService.findValidationDetails(anyLong(), any()))
                .thenReturn(Validation.valid(
                        ValidationResultsDetailView.builder()
                                .page(new Page(2, 2, 0, 1))
                                .build()
                ));
    }

    @Test
    public void getRuleSummaries_ReturnsSummaries() throws Exception {

        Dataset dataset = DatasetPopulated.dataset().build();
        when(datasetService.findById(anyLong()))
                .thenReturn(Option.of(dataset));

        when(validationRuleSummaryService.findByDatasetId(anyLong()))
                .thenReturn(singletonList(
                        DatasetPopulated.validationRuleSummary()
                                .id(1L)
                                .createdTime(toDateTime("2000-01-01T00:00:02"))
                                .status(SummaryStatus.SUCCESS)
                                .numberOfFailures(12L)
                                .numberOfErrors(1L)
                                .errorMessage("Error")
                                .dataset(DatasetPopulated.dataset()
                                        .id(98L)
                                        .recordsCount(100L)
                                        .build())
                                .validationRule(ProductPopulated.validationRule()
                                        .expression("SCRIPT_GOES_HERE")
                                        .build())
                                .build()
                ));

        mockMvc.perform(
                get("/api/v1/datasets/123/validationResultsSummaries")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"))
                .andExpect(jsonPath("$[0].numberOfFailures").value(12))
                .andExpect(jsonPath("$[0].numberOfErrors").value(1))
                .andExpect(jsonPath("$[0].errorMessage").value("Error"))
                .andExpect(jsonPath("$[0].totalRecords").value("100"))
                .andExpect(jsonPath("$[0].datasetId").value(98));
    }

    @Test
    public void getRuleSummaries_CallsValidationResultsSummaryService() throws Exception {
        mockMvc.perform(
                get("/api/v1/datasets/123/validationResultsSummaries")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(validationRuleSummaryService).findByDatasetId(123L);
    }

    @Test
    public void getAllDatasets_ReturnsPagedDatasetsValues() throws Exception {
        List<DatasetOnly> datasets = ImmutableList.of(
                DatasetPopulated.datasetOnly()
                        .id(900L)
                        .name("Dataset1")
                        .predicate("1=1")
                        .validationJobId(91200L)
                        .schemaDisplayName("table")
                        .recordsCount(9000L)
                        .validationStatus(ValidationStatus.VALIDATED.name())
                        .entityCode("TestEntity")
                        .referenceDate(LocalDate.of(2018, 2, 28))
                        .pipelineInvocationId(346436L)
                        .pipelineStepInvocationId(2563644L)
                        .build(),
                DatasetPopulated.datasetOnly()
                        .id(800L)
                        .name("Dataset2")
                        .predicate("1=2")
                        .validationJobId(8123L)
                        .schemaDisplayName("table2")
                        .recordsCount(82002L)
                        .validationStatus(ValidationStatus.VALIDATION_FAILED.name())
                        .entityCode("OtherEntity")
                        .referenceDate(LocalDate.of(2018, 4, 28))
                        .pipelineInvocationId(64643534L)
                        .pipelineStepInvocationId(6346436L)
                        .build());

        when(datasetService.findAllDatasets(any()))
                .thenReturn(new PageImpl<>(datasets, PageRequest.of(0, 4), 4));

        mockMvc.perform(
                get("/api/v1/datasets?page=0&size=10")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.datasetList[0].id").value(900))
                .andExpect(jsonPath("$._embedded.datasetList[0].name").value("Dataset1"))
                .andExpect(jsonPath("$._embedded.datasetList[0].predicate").value("1=1"))
                .andExpect(jsonPath("$._embedded.datasetList[0].validationJobId").value(91200))
                .andExpect(jsonPath("$._embedded.datasetList[0].table").value("table"))
                .andExpect(jsonPath("$._embedded.datasetList[0].recordsCount").value(9000))
                .andExpect(jsonPath("$._embedded.datasetList[0].validationStatus").value("VALIDATED"))
                .andExpect(jsonPath("$._embedded.datasetList[0].entityCode").value("TestEntity"))
                .andExpect(jsonPath("$._embedded.datasetList[0].referenceDate")
                        .value(new Calendar.Builder().setDate(2018, 1, 28).build().getTimeInMillis()))
                .andExpect(jsonPath("$._embedded.datasetList[0].pipelineInvocationId").value(346436))
                .andExpect(jsonPath("$._embedded.datasetList[0].pipelineStepInvocationId").value(2563644))

                .andExpect(jsonPath("$._embedded.datasetList[1].id").value(800))
                .andExpect(jsonPath("$._embedded.datasetList[1].name").value("Dataset2"))
                .andExpect(jsonPath("$._embedded.datasetList[1].predicate").value("1=2"))
                .andExpect(jsonPath("$._embedded.datasetList[1].validationJobId").value(8123))
                .andExpect(jsonPath("$._embedded.datasetList[1].table").value("table2"))
                .andExpect(jsonPath("$._embedded.datasetList[1].recordsCount").value(82002))
                .andExpect(jsonPath("$._embedded.datasetList[1].validationStatus").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$._embedded.datasetList[1].entityCode").value("OtherEntity"))
                .andExpect(jsonPath("$._embedded.datasetList[1].referenceDate")
                        .value(new Calendar.Builder().setDate(2018, 3, 28).build().getTimeInMillis()))
                .andExpect(jsonPath("$._embedded.datasetList[1].pipelineInvocationId").value(64643534))
                .andExpect(jsonPath("$._embedded.datasetList[1].pipelineStepInvocationId").value(6346436));
    }

    @Test
    public void getAllDatasets_EntityCodePresentWithoutReferenceDate_ReturnsBadRequestErrorCodes() throws Exception {
        mockMvc.perform(
                get("/api/v1/datasets?page=0&size=10&datasetName=Hello&entityCode=ABC")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("referenceDate"))
                .andExpect(jsonPath("$[0].errorMessage")
                        .value("Required Date parameter 'referenceDate' is not present"));
    }

    @Test
    public void getAllDatasets_ReferenceDatePresentWithoutEntityCode_ReturnsBadRequestErrorCodes() throws Exception {
        mockMvc.perform(
                get("/api/v1/datasets?page=0&size=10&datasetName=Hello&referenceDate=2017-01-01")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("entityCode"))
                .andExpect(jsonPath("$[0].errorMessage")
                        .value("Required String parameter 'entityCode' is not present"));
    }

    @Test
    public void getAllDatasets_ReferenceDatePresentWithoutNameWithoutSchema_ReturnsBadRequestErrorCodes() throws Exception {
        mockMvc.perform(
                get("/api/v1/datasets?page=0&size=10&entityCode=Hello&referenceDate=2017-01-01")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("datasetName|datasetSchema"))
                .andExpect(jsonPath("$[0].errorMessage")
                        .value("Required String parameter 'datasetName|datasetSchema' is not present"));
    }

    @Test
    public void getAllDatasets_DatasetsNumberGreaterThanPageSize_ReturnsPageOneOfTwo() throws Exception {

        PageRequest pageable = PageRequest.of(0, 4);
        when(datasetService.findAllDatasets(any()))
                .thenReturn(new PageImpl<>(
                        ImmutableList.of(
                                DatasetPopulated.datasetOnly().id(1L).build(),
                                DatasetPopulated.datasetOnly().id(1L).build(),
                                DatasetPopulated.datasetOnly().id(1L).build(),
                                DatasetPopulated.datasetOnly().id(1L).build()),
                        pageable,
                        8L
                ));

        mockMvc.perform(
                get("/api/v1/datasets?page=0&size=4")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size").value(4))
                .andExpect(jsonPath("$.page.totalElements").value(8))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalPages").value(2));
    }

    @Test
    public void getAllDatasets_DatasetsNumberGreaterThanPageSize_CallsServiceMethodWithCorrectPageable() throws Exception {
        when(datasetService.findAllDatasets(any()))
                .thenReturn(new PageImpl<>(emptyList(), PageRequest.of(0, 4), 4));

        mockMvc.perform(
                get("/api/v1/datasets?page=1&size=4")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(datasetService).findAllDatasets(pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        soft.assertThat(pageable.getPageSize())
                .isEqualTo(4);
        soft.assertThat(pageable.getPageNumber())
                .isEqualTo(1);
        soft.assertThat(pageable.getOffset())
                .isEqualTo(4);
    }

    @Test
    public void getAllDatasets_DatasetsQueryWithDatasetName_CallsServiceMethodWithQuery() throws Exception {
        when(datasetService.findAllDatasets(any(), any()))
                .thenReturn(new PageImpl<>(emptyList(), PageRequest.of(0, 4), 0));

        mockMvc.perform(
                get("/api/v1/datasets?page=0&size=4&datasetName=Hello&entityCode=1234&referenceDate=2000-01-01")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        verify(datasetService).findAllDatasets(queryCaptor.capture(), any());

        assertThat(queryCaptor.getValue())
                .isEqualTo(DatasetQuery.builder()
                        .name("Hello")
                        .entityCode("1234")
                        .referenceDate(LocalDate.of(2000, 1, 1))
                        .build());
    }

    @Test
    public void getAllDatasets_DatasetsQueryWithDatasetSchema_CallsServiceMethodWithQuery() throws Exception {
        when(datasetService.findAllDatasets(any(), any()))
                .thenReturn(new PageImpl<>(emptyList(), PageRequest.of(0, 4), 0));

        mockMvc.perform(
                get("/api/v1/datasets?page=0&size=4&datasetSchema=Hello&entityCode=1234&referenceDate=2000-01-01")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        verify(datasetService).findAllDatasets(queryCaptor.capture(), any());

        assertThat(queryCaptor.getValue())
                .isEqualTo(DatasetQuery.builder()
                        .schema("Hello")
                        .entityCode("1234")
                        .referenceDate(LocalDate.of(2000, 1, 1))
                        .build());
    }

    @Test
    public void getAllDatasets_RequestWithSortParams_CallsServiceMethodWithCorrectSortable() throws Exception {
        List<DatasetOnly> datasets = ImmutableList.of(
                DatasetPopulated.datasetOnly().id(1L).build(),
                DatasetPopulated.datasetOnly().id(2L).build(),
                DatasetPopulated.datasetOnly().id(3L).build(),
                DatasetPopulated.datasetOnly().id(4L).build());

        when(datasetService.findAllDatasets(any()))
                .thenReturn(new PageImpl<>(datasets, PageRequest.of(0, 4), 4));

        mockMvc.perform(
                get("/api/v1/datasets?page=1&size=4&sort=id,desc")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(datasetService).findAllDatasets(pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getSort().getOrderFor("id"))
                .isEqualTo(new Sort.Order(Sort.Direction.DESC, "id"));
    }

    @Test
    public void getRuleDetails_InvalidRequest_ReturnsBadRequest() throws Exception {
        when(validationResultDetailService.findValidationDetails(anyLong(), any()))
                .thenReturn(Validation.invalid(
                        Arrays.asList(
                                CRUDFailure.invalidRequestParameters(singletonList(Tuple.of("sortParam", "ID"))),
                                CRUDFailure.notFoundIds("something", 100L))
                ));

        mockMvc.perform(
                get("/api/v1/datasets/143/validationResultsDetails")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("INVALID_REQUEST_PARAMETERS"))
                .andExpect(jsonPath("$[0].errorMessage")
                        .value("Supplied parameters are invalid [(sortParam, ID)]"))
                .andExpect(jsonPath("$[1].errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$[1].errorMessage")
                        .value("Could not find something for ids [100]"));
    }

    @Test
    public void getRuleDetails_PagedResultReturned_ReturnsPagedData() throws Exception {
        Map<String, Object> alyosha = ImmutableMap.<String, Object>builder()
                .put("name", "Alyosha")
                .put("occupation", "Monk")
                .put("nemesis", "Ivan")
                .put("status", "BLESSED")
                .build();
        Map<String, Object> ivan = ImmutableMap.<String, Object>builder()
                .put("name", "Ivan")
                .put("occupation", "Journalist")
                .put("nemesis", "Alyosha")
                .put("status", "HAUNTED")
                .build();

        when(validationResultDetailService.findValidationDetails(anyLong(), any()))
                .thenReturn(Validation.valid(ValidationResultsDetailView.builder()
                        .data(Arrays.asList(alyosha, ivan))
                        .build()));

        mockMvc.perform(
                get("/api/v1/datasets/143/validationResultsDetails")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Alyosha"))
                .andExpect(jsonPath("$.data[0].occupation").value("Monk"))
                .andExpect(jsonPath("$.data[0].nemesis").value("Ivan"))
                .andExpect(jsonPath("$.data[0].status").value("BLESSED"))
                .andExpect(jsonPath("$.data[1].name").value("Ivan"))
                .andExpect(jsonPath("$.data[1].occupation").value("Journalist"))
                .andExpect(jsonPath("$.data[1].nemesis").value("Alyosha"))
                .andExpect(jsonPath("$.data[1].status").value("HAUNTED"));
    }

    @Test
    public void getRuleDetails_PagedResultReturned_ReturnsSchema() throws Exception {
        StringField name = new StringField();
        name.setName("name");
        DateField occupation = new DateField();
        occupation.setName("occupation");
        LongField nemesis = new LongField();
        nemesis.setName("nemesis");

        when(datasetService.findById(anyLong()))
                .thenReturn(Option.of(DatasetPopulated.dataset()
                        .schema(Table.builder()
                                .fields(ImmutableSet.of(name, occupation, nemesis))
                                .build())
                        .build()));

        when(validationResultDetailService.findValidationDetails(anyLong(), any()))
                .thenReturn(Validation.valid(ValidationResultsDetailView.builder()
                        .schema(Arrays.asList(
                                FieldExport.StringFieldExport.builder().name("name").build(),
                                FieldExport.DateFieldExport.builder().name("occupation").build(),
                                FieldExport.DecimalFieldExport.builder().name("nemesis").build()))
                        .build()));

        mockMvc.perform(
                get("/api/v1/datasets/143/validationResultsDetails")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schema[0].type").value("string"))
                .andExpect(jsonPath("$.schema[0].name").value("name"))
                .andExpect(jsonPath("$.schema[1].type").value("date"))
                .andExpect(jsonPath("$.schema[1].name").value("occupation"))
                .andExpect(jsonPath("$.schema[2].type").value("decimal"))
                .andExpect(jsonPath("$.schema[2].name").value("nemesis"));
    }

    @Test
    public void getRuleDetails_PagedResultReturned_ReturnsPageInformation() throws Exception {
        when(validationResultDetailService.findValidationDetails(anyLong(), any()))
                .thenReturn(Validation.valid(
                        ValidationResultsDetailView.builder()
                                .page(new Page(0, 2, 4, 1))
                                .build()
                ));

        mockMvc.perform(
                get("/api/v1/datasets/143/validationResultsDetails")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.totalElements").value(4))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalPages").value(1));
    }

    @Test
    public void getRuleDetails_PagedRequest_CallsServiceWithPage() throws Exception {
        Dataset dataset = DatasetPopulated.dataset().build();
        when(datasetService.findById(anyLong()))
                .thenReturn(Option.of(dataset));

        mockMvc.perform(
                get("/api/v1/datasets/143/validationResultsDetails?page=0&size=4&sort=id,desc&sort=name,asc")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON));

        verify(validationResultDetailService).findValidationDetails(anyLong(), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        soft.assertThat(pageable.getPageSize())
                .isEqualTo(4);
        soft.assertThat(pageable.getPageNumber())
                .isEqualTo(0);
        soft.assertThat(pageable.getSort().iterator())
                .contains(Sort.Order.desc("id"), Sort.Order.asc("name"));
    }

    @Test
    public void getRuleDetails_PagedRequest_CallsServiceWithDatasetId() throws Exception {
        Dataset dataset = DatasetPopulated.dataset().build();
        when(datasetService.findById(anyLong()))
                .thenReturn(Option.of(dataset));

        mockMvc.perform(
                get("/api/v1/datasets/143/validationResultsDetails?page=0&size=4&sort=id,desc&sort=name,asc")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON));

        verify(validationResultDetailService).findValidationDetails(eq(143L), any());
    }

    @Test
    public void getRuleDetailsWithRuleId_PagedRequest_CallsServiceWithRuleId() throws Exception {
        Dataset dataset = DatasetPopulated.dataset().build();
        when(datasetService.findById(anyLong()))
                .thenReturn(Option.of(dataset));

        mockMvc.perform(
                get("/api/v1/datasets/143/validationResultsDetails?ruleId=200&page=0&size=4&sort=id,desc&sort=name,asc")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON));

        verify(validationResultDetailService).findValidationDetails(eq(143L), eq(200L), any());
    }
}
