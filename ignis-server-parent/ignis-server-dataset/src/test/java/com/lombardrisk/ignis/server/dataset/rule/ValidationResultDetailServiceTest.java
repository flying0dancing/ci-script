package com.lombardrisk.ignis.server.dataset.rule;

import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.api.rule.ValidationOutput;
import com.lombardrisk.ignis.client.core.page.response.Page;
import com.lombardrisk.ignis.client.external.rule.ValidationResultsDetailView;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.dataset.DatasetService;
import com.lombardrisk.ignis.server.dataset.fixture.DatasetPopulated;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.dataset.rule.detail.ValidationDetailQuery;
import com.lombardrisk.ignis.server.dataset.rule.detail.ValidationDetailRepository;
import com.lombardrisk.ignis.server.dataset.rule.detail.ValidationDetailRow;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.rule.ValidationRuleService;
import com.lombardrisk.ignis.server.product.table.model.DateField;
import com.lombardrisk.ignis.server.product.table.model.Field;
import com.lombardrisk.ignis.server.product.table.model.StringField;
import com.lombardrisk.ignis.server.product.table.view.FieldConverter;
import com.lombardrisk.ignis.server.product.util.PageConverter;
import io.vavr.Tuple;
import io.vavr.control.Validation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.lombardrisk.ignis.server.product.fixture.ProductPopulated.decimalField;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationResultDetailServiceTest {

    @Mock
    private PageConverter pageConverter;
    @Mock
    private FieldConverter fieldConverter;
    @Mock
    private ValidationDetailRepository validationDetailRepository;
    @Mock
    private DatasetService datasetService;
    @Mock
    private ValidationRuleService validationRuleService;

    @Captor
    private ArgumentCaptor<ValidationDetailQuery> queryCaptor;
    @Captor
    private ArgumentCaptor<Field> fieldCaptor;

    @InjectMocks
    private ValidationResultDetailService service;

    @Before
    public void setUp() {
        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset().build()));
        when(validationRuleService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.validationRule().build()));
        when(validationDetailRepository.findValidationDetails(any()))
                .thenReturn(
                        new PageImpl<>(Collections.emptyList(), Pageable.unpaged(), 0));
        when(pageConverter.apply(any()))
                .thenReturn(new Page(0, 5, 0, 2));
    }

    @Test
    public void findValidationDetails_WithDatasetSchema_callsSchemaConverterWithDatasetsSchema() {
        StringField stringField = new StringField();
        stringField.setName("name");
        DateField dateField = new DateField();
        dateField.setName("dob");

        Dataset dataset = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .fields(new LinkedHashSet<>(asList(stringField, dateField)))
                        .build())
                .build();

        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(dataset));

        service.findValidationDetails(100, PageRequest.of(10, 1));

        verify(fieldConverter, times(3)).apply(fieldCaptor.capture());
        verifyNoMoreInteractions(fieldConverter);
        assertThat(fieldCaptor.getAllValues())
                .extracting(Field::getName)
                .containsExactly("name", "dob", ValidationOutput.VALIDATION_RESULT_TYPE);
    }

    @Test
    public void findValidationDetails_UnPagedQuery_ReturnsValidationFailure() {
        Validation<List<CRUDFailure>, ValidationResultsDetailView> result =
                service.findValidationDetails(100, Pageable.unpaged());

        assertThat(result.getError())
                .contains(CRUDFailure.invalidRequestParameters(asList(
                        Tuple.of("page", "null"),
                        Tuple.of("size", "null")
                )));
    }

    @Test
    public void findValidationDetails_callsValidationRepository() {
        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset()
                        .id(100L)
                        .schema(ProductPopulated.table()
                                .fields(newLinkedHashSet(Arrays.asList(
                                        decimalField("CQ1").build(),
                                        decimalField("RS2").build())))
                                .build())
                        .build()));

        PageRequest pageable = PageRequest.of(0, 5);

        service.findValidationDetails(100L, pageable);

        verify(validationDetailRepository).findValidationDetails(queryCaptor.capture());

        assertThat(queryCaptor.getValue().getSqlQuery())
                .isEqualTo("SELECT \"CQ1\",\"RS2\",\"RESULT_TYPE\" FROM CQRS dataset "
                        + "JOIN VALIDATION_RULE_RESULTS v ON dataset.ROW_KEY = v.DATASET_ROW_KEY "
                        + "WHERE v.DATASET_ID = 100 LIMIT 5 OFFSET 0");
    }

    @Test
    public void findValidationDetails_QueryHasInvalidRequestParam_ReturnsInvalidRequestParam() {
        StringField stringField = new StringField();
        stringField.setName("name");
        DateField dateField = new DateField();
        dateField.setName("dob");

        Dataset dataset = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .fields(new LinkedHashSet<>(asList(stringField, dateField)))
                        .build())
                .build();

        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(dataset));

        Validation<List<CRUDFailure>, ValidationResultsDetailView> resultsDetailViews =
                service.findValidationDetails(
                        1003,
                        PageRequest.of(0, 2, Sort.by(Sort.Order.asc("other"))));

        assertThat(resultsDetailViews.isInvalid())
                .isTrue();
        assertThat(resultsDetailViews.getError())
                .contains(CRUDFailure.invalidRequestParameters(singletonList(Tuple.of("sortParameter", "other"))));
    }

    @Test
    public void findValidationDetails_RepositoryReturnsPage_convertsPageInformation() {
        Page page = new Page(0, 5, 0, 2);
        when(pageConverter.apply(any()))
                .thenReturn(page);

        ValidationResultsDetailView detailView = service.findValidationDetails(100, PageRequest.of(0, 2))
                .get();

        assertThat(detailView.getPage())
                .isEqualTo(page);
    }

    @Test
    public void findValidationDetails_RepositoryReturnsPage_callsConverter() {
        List<ValidationDetailRow> validationDetailRows = asList(
                DatasetPopulated.validationDetailRow().build(),
                DatasetPopulated.validationDetailRow().build(),
                DatasetPopulated.validationDetailRow().build(),
                DatasetPopulated.validationDetailRow().build(),
                DatasetPopulated.validationDetailRow().build());

        PageImpl<ValidationDetailRow> page = new PageImpl<>(
                validationDetailRows,
                PageRequest.of(0, 5),
                10);

        when(validationDetailRepository.findValidationDetails(any()))
                .thenReturn(page);

        service.findValidationDetails(100L, PageRequest.of(1, 2));

        verify(pageConverter).apply(page);
    }

    @Test
    public void findValidationDetails_RepositoryReturnsRows_covertsRows() {
        List<ValidationDetailRow> validationDetailRows = asList(
                ValidationDetailRow.builder()
                        .value("field1", "hello")
                        .build(),
                ValidationDetailRow.builder()
                        .value("field2", 1000L)
                        .build());

        when(validationDetailRepository.findValidationDetails(any()))
                .thenReturn(
                        new PageImpl<>(
                                validationDetailRows,
                                PageRequest.of(0, 5),
                                10));

        ValidationResultsDetailView detailView = service.findValidationDetails(9010, PageRequest.of(1, 2))
                .get();

        assertThat(detailView.getData())
                .contains(
                        ImmutableMap.of("field1", "hello"),
                        ImmutableMap.of("field2", 1000L));
    }

    @Test
    public void findValidationDetails_WithRuleIdWithDatasetSchema_callsSchemaConverterWithDatasetsSchema() {
        StringField stringField = new StringField();
        stringField.setName("name");
        DateField dateField = new DateField();
        dateField.setName("dob");

        Dataset dataset = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .fields(new LinkedHashSet<>(asList(stringField, dateField)))
                        .build())
                .build();

        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(dataset));

        service.findValidationDetails(100, 90, PageRequest.of(10, 1));

        verify(fieldConverter, times(3)).apply(fieldCaptor.capture());
        verifyNoMoreInteractions(fieldConverter);
        assertThat(fieldCaptor.getAllValues())
                .extracting(Field::getName)
                .containsExactly("name", "dob", ValidationOutput.VALIDATION_RESULT_TYPE);
    }

    @Test
    public void findValidationDetails_WithRuleIdUnPagedQuery_ReturnsValidationFailure() {
        Validation<List<CRUDFailure>, ValidationResultsDetailView> result =
                service.findValidationDetails(100, 109, Pageable.unpaged());

        assertThat(result.getError())
                .contains(CRUDFailure.invalidRequestParameters(asList(
                        Tuple.of("page", "null"),
                        Tuple.of("size", "null")
                )));
    }

    @Test
    public void findValidationDetails_WithRuleIdcallsValidationRepository() {
        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset()
                        .id(100L)
                        .schema(ProductPopulated.table()
                                .fields(newHashSet(
                                        decimalField("Q1").build(),
                                        decimalField("Q2").build()))
                                .build())
                        .build()));
        when(validationRuleService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.validationRule().id(800L).build()));

        PageRequest pageable = PageRequest.of(0, 5);

        service.findValidationDetails(100L, 800L, pageable);

        verify(validationDetailRepository).findValidationDetails(queryCaptor.capture());

        assertThat(queryCaptor.getValue().getSqlQuery())
                .isEqualTo("SELECT \"Q1\",\"Q2\",\"RESULT_TYPE\" FROM CQRS dataset "
                        + "JOIN VALIDATION_RULE_RESULTS v ON dataset.ROW_KEY = v.DATASET_ROW_KEY "
                        + "WHERE v.DATASET_ID = 100 "
                        + "AND v.VALIDATION_RULE_ID = 800 "
                        + "LIMIT 5 OFFSET 0");
    }

    @Test
    public void findValidationDetails_WithRuleIdQueryHasInvalidRequestParam_ReturnsInvalidRequestParam() {
        StringField stringField = new StringField();
        stringField.setName("name");
        DateField dateField = new DateField();
        dateField.setName("dob");

        Dataset dataset = DatasetPopulated.dataset()
                .schema(ProductPopulated.table()
                        .fields(new LinkedHashSet<>(asList(stringField, dateField)))
                        .build())
                .build();

        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(dataset));

        PageRequest pageRequest = PageRequest.of(0, 2, Sort.by(Sort.Order.asc("other")));

        Validation<List<CRUDFailure>, ValidationResultsDetailView> resultsDetailViews =
                service.findValidationDetails(1003, 801L, pageRequest);

        assertThat(resultsDetailViews.isInvalid())
                .isTrue();
        assertThat(resultsDetailViews.getError())
                .contains(CRUDFailure.invalidRequestParameters(singletonList(Tuple.of("sortParameter", "other"))));
    }

    @Test
    public void findValidationDetails_WithRuleIdRepositoryReturnsPage_convertsPageInformation() {
        Page page = new Page(0, 5, 0, 2);
        when(pageConverter.apply(any()))
                .thenReturn(page);

        ValidationResultsDetailView detailView = service.findValidationDetails(100, 80019, PageRequest.of(0, 2))
                .get();

        assertThat(detailView.getPage())
                .isEqualTo(page);
    }

    @Test
    public void findValidationDetails_WithRuleIdRepositoryReturnsPage_callsConverter() {
        List<ValidationDetailRow> validationDetailRows = asList(
                DatasetPopulated.validationDetailRow().build(),
                DatasetPopulated.validationDetailRow().build(),
                DatasetPopulated.validationDetailRow().build(),
                DatasetPopulated.validationDetailRow().build(),
                DatasetPopulated.validationDetailRow().build());

        PageImpl<ValidationDetailRow> page = new PageImpl<>(
                validationDetailRows,
                PageRequest.of(0, 5),
                10);

        when(validationDetailRepository.findValidationDetails(any()))
                .thenReturn(page);

        service.findValidationDetails(100L, 1029L, PageRequest.of(1, 2));

        verify(pageConverter).apply(page);
    }

    @Test
    public void findValidationDetails_WithRuleIdRepositoryReturnsRows_covertsRows() {
        List<ValidationDetailRow> validationDetailRows = asList(
                ValidationDetailRow.builder()
                        .value("field1", "hello")
                        .build(),
                ValidationDetailRow.builder()
                        .value("field2", 1000L)
                        .build());

        when(validationDetailRepository.findValidationDetails(any()))
                .thenReturn(
                        new PageImpl<>(
                                validationDetailRows,
                                PageRequest.of(0, 5),
                                10));

        ValidationResultsDetailView detailView = service.findValidationDetails(9010, 91882, PageRequest.of(1, 2))
                .get();

        assertThat(detailView.getData())
                .contains(
                        ImmutableMap.of("field1", "hello"),
                        ImmutableMap.of("field2", 1000L));
    }
}
