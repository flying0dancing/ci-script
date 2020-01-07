package com.lombardrisk.ignis.functional.test.assertions;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.client.external.dataset.model.Dataset;
import com.lombardrisk.ignis.client.external.job.staging.DatasetState;
import com.lombardrisk.ignis.client.external.job.staging.StagingItemView;
import com.lombardrisk.ignis.client.external.job.staging.StagingSchemaView;
import com.lombardrisk.ignis.client.external.productconfig.view.SchemaView;
import com.lombardrisk.ignis.functional.test.assertions.expected.ExpectedDataset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PhysicalSchemaAssertionsTest {

    @Mock
    private JdbcTemplate phoenixTemplate;
    @InjectMocks
    private PhysicalSchemaAssertions assertions;

    @Test
    public void hasExpectedDatasetsInPhoenix_SingleDataset_DatasetFound() {
        List<ExpectedDataset> expected = ImmutableList.of(ExpectedDataset.expected()
                .schema("dataset")
                .entityCode("entity")
                .referenceDate(LocalDate.of(2018, 1, 1))
                .numberOfRows(5)
                .build());

        List<Dataset> actual = ImmutableList.of(Dataset.builder()
                .id(1234L)
                .name("dataset")
                .entityCode("entity")
                .localReferenceDate(LocalDate.of(2018, 1, 1))
                .recordsCount(5)
                .build());

        when(phoenixTemplate.queryForList(any(), eq(Long.class)))
                .thenReturn(ImmutableList.of(1L, 2L, 3L, 4L, 5L));

        assertions.hasExpectedDatasetsInPhoenix(expected, actual).assertAll();
    }

    @Test
    public void hasExpectedDatasetsInPhoenix_SingleDataset_DatasetNotFound() {
        List<ExpectedDataset> expected = ImmutableList.of(ExpectedDataset.expected()
                .schema("dataset")
                .entityCode("entity")
                .referenceDate(LocalDate.of(2018, 1, 1))
                .numberOfRows(5)
                .build());

        List<Dataset> actual = ImmutableList.of(Dataset.builder()
                .id(1234L)
                .name("another dataset")
                .entityCode("entity")
                .localReferenceDate(LocalDate.of(2018, 1, 1))
                .recordsCount(5)
                .build());

        assertThatThrownBy(() -> assertions.hasExpectedDatasetsInPhoenix(expected, actual).assertAll())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Expected datasets");
    }

    @Test
    public void hasExpectedDatasetsInPhoenix_SingleDataset_UnexpectedDatasetFound() {
        List<ExpectedDataset> expected = ImmutableList.of(ExpectedDataset.expected()
                .schema("dataset")
                .entityCode("entity")
                .referenceDate(LocalDate.of(2018, 1, 1))
                .numberOfRows(5)
                .build());

        List<Dataset> actual = ImmutableList.of(
                Dataset.builder()
                        .id(1234L)
                        .name("dataset")
                        .entityCode("entity")
                        .localReferenceDate(LocalDate.of(2018, 1, 1))
                        .recordsCount(5)
                        .build(),
                Dataset.builder()
                        .id(2468L)
                        .name("dataset")
                        .entityCode("entity")
                        .localReferenceDate(LocalDate.of(2018, 1, 1))
                        .recordsCount(5)
                        .build()
        );

        assertThatThrownBy(() -> assertions.hasExpectedDatasetsInPhoenix(expected, actual).assertAll())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Expected datasets");
    }

    @Test
    public void hasExpectedDatasetsInPhoenix_IncorrectRowCount() {
        List<ExpectedDataset> expected = ImmutableList.of(ExpectedDataset.expected()
                .schema("dataset")
                .entityCode("entity")
                .referenceDate(LocalDate.of(2018, 1, 1))
                .numberOfRows(5)
                .build());

        List<Dataset> actual = ImmutableList.of(Dataset.builder()
                .id(1234L)
                .name("dataset")
                .entityCode("entity")
                .localReferenceDate(LocalDate.of(2018, 1, 1))
                .recordsCount(4)
                .build());

        assertThatThrownBy(() -> assertions.hasExpectedDatasetsInPhoenix(expected, actual).assertAll())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Expected datasets");
    }

    @Test
    public void hasExpectedDatasetsInPhoenix_MultipleIdenticalDatasets() {
        List<ExpectedDataset> expected = ImmutableList.of(
                ExpectedDataset.expected()
                        .schema("dataset")
                        .entityCode("entity")
                        .referenceDate(LocalDate.of(2018, 1, 1))
                        .numberOfRows(5)
                        .build(),
                ExpectedDataset.expected()
                        .schema("dataset")
                        .entityCode("entity")
                        .referenceDate(LocalDate.of(2018, 1, 1))
                        .numberOfRows(5)
                        .build()
        );

        List<Dataset> actual = ImmutableList.of(
                Dataset.builder()
                        .id(1234L)
                        .name("dataset")
                        .entityCode("entity")
                        .localReferenceDate(LocalDate.of(2018, 1, 1))
                        .recordsCount(5)
                        .build(),
                Dataset.builder()
                        .id(9876L)
                        .name("dataset")
                        .entityCode("entity")
                        .localReferenceDate(LocalDate.of(2018, 1, 1))
                        .recordsCount(5)
                        .build()
        );

        when(phoenixTemplate.queryForList(any(), eq(Long.class)))
                .thenReturn(ImmutableList.of(1L, 2L, 3L, 4L, 5L))
                .thenReturn(ImmutableList.of(6L, 7L, 8L, 9L, 10L));

        assertions.hasExpectedDatasetsInPhoenix(expected, actual).assertAll();
    }

    @Test
    public void hasExpectedDatasetsInPhoenix_MultipleIdenticalDatasets_IncorrectRowCount() {
        List<ExpectedDataset> expected = ImmutableList.of(
                ExpectedDataset.expected()
                        .schema("dataset")
                        .entityCode("entity")
                        .referenceDate(LocalDate.of(2018, 1, 1))
                        .numberOfRows(5)
                        .build(),
                ExpectedDataset.expected()
                        .schema("dataset")
                        .entityCode("entity")
                        .referenceDate(LocalDate.of(2018, 1, 1))
                        .numberOfRows(5)
                        .build()
        );

        List<Dataset> actual = ImmutableList.of(
                Dataset.builder()
                        .id(1234L)
                        .name("dataset")
                        .entityCode("entity")
                        .localReferenceDate(LocalDate.of(2018, 1, 1))
                        .recordsCount(5)
                        .build(),
                Dataset.builder()
                        .id(9876L)
                        .name("dataset")
                        .entityCode("entity")
                        .localReferenceDate(LocalDate.of(2018, 1, 1))
                        .recordsCount(6)
                        .build()
        );

        assertThatThrownBy(() -> assertions.hasExpectedDatasetsInPhoenix(expected, actual).assertAll())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Expected datasets");
    }

    @Test
    public void hasExpectedDatasetsInPhoenix_MatchingDataset_SameNumberOfRowsInPhoenix() {
        List<ExpectedDataset> expected = ImmutableList.of(ExpectedDataset.expected()
                .schema("dataset")
                .entityCode("entity")
                .referenceDate(LocalDate.of(2018, 1, 1))
                .numberOfRows(5)
                .build());

        List<Dataset> actual = ImmutableList.of(Dataset.builder()
                .id(1234L)
                .name("dataset")
                .entityCode("entity")
                .localReferenceDate(LocalDate.of(2018, 1, 1))
                .recordsCount(5)
                .predicate("ROW_KEY >= 1 and ROW_KEY <= 5")
                .build());

        when(phoenixTemplate.queryForList(any(), eq(Long.class)))
                .thenReturn(ImmutableList.of(1L, 2L, 3L, 4L, 5L));

        assertions.hasExpectedDatasetsInPhoenix(expected, actual).assertAll();
    }

    @Test
    public void hasExpectedDatasetsInPhoenix_MatchingDataset_IncorrectNumberOfRowsInPhoenix() {
        List<ExpectedDataset> expected = ImmutableList.of(ExpectedDataset.expected()
                .schema("dataset")
                .entityCode("entity")
                .referenceDate(LocalDate.of(2018, 1, 1))
                .numberOfRows(5)
                .build());

        List<Dataset> actual = ImmutableList.of(Dataset.builder()
                .id(1234L)
                .name("dataset")
                .entityCode("entity")
                .localReferenceDate(LocalDate.of(2018, 1, 1))
                .recordsCount(5)
                .predicate("ROW_KEY >= 1 and ROW_KEY <= 6")
                .build());

        when(phoenixTemplate.queryForList(any(), eq(Long.class)))
                .thenReturn(ImmutableList.of(1L, 2L, 3L, 4L, 5L, 6L));

        assertThatThrownBy(() -> assertions.hasExpectedDatasetsInPhoenix(expected, actual).assertAll())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Phoenix row count");
    }

    @Test
    public void hasExpectedDatasetsInPhoenix_MultipleIdenticalDatasets_SameNumberOfRowsInPhoenix() {
        List<ExpectedDataset> expected = ImmutableList.of(
                ExpectedDataset.expected()
                        .schema("dataset")
                        .entityCode("entity")
                        .referenceDate(LocalDate.of(2018, 1, 1))
                        .numberOfRows(5)
                        .build(),
                ExpectedDataset.expected()
                        .schema("dataset")
                        .entityCode("entity")
                        .referenceDate(LocalDate.of(2018, 1, 1))
                        .numberOfRows(5)
                        .build()
        );

        List<Dataset> actual = ImmutableList.of(
                Dataset.builder()
                        .id(1234L)
                        .name("dataset")
                        .entityCode("entity")
                        .localReferenceDate(LocalDate.of(2018, 1, 1))
                        .recordsCount(5)
                        .predicate("ROW_KEY >= 1 and ROW_KEY <= 5")
                        .build(),
                Dataset.builder()
                        .id(9876L)
                        .name("dataset")
                        .entityCode("entity")
                        .localReferenceDate(LocalDate.of(2018, 1, 1))
                        .recordsCount(5)
                        .predicate("ROW_KEY >= 6 and ROW_KEY <= 10")
                        .build()
        );

        when(phoenixTemplate.queryForList(any(), eq(Long.class)))
                .thenReturn(ImmutableList.of(1L, 2L, 3L, 4L, 5L))
                .thenReturn(ImmutableList.of(6L, 7L, 8L, 9L, 10L));

        assertions.hasExpectedDatasetsInPhoenix(expected, actual).assertAll();
    }

    @Test
    public void hasExpectedDatasetsInPhoenix_MultipleIdenticalDatasets_IncorrectNumberOfRowsInPhoenix() {
        List<ExpectedDataset> expected = ImmutableList.of(
                ExpectedDataset.expected()
                        .schema("dataset")
                        .entityCode("entity")
                        .referenceDate(LocalDate.of(2018, 1, 1))
                        .numberOfRows(5)
                        .build(),
                ExpectedDataset.expected()
                        .schema("dataset")
                        .entityCode("entity")
                        .referenceDate(LocalDate.of(2018, 1, 1))
                        .numberOfRows(5)
                        .build()
        );

        List<Dataset> actual = ImmutableList.of(
                Dataset.builder()
                        .id(1234L)
                        .name("dataset")
                        .entityCode("entity")
                        .localReferenceDate(LocalDate.of(2018, 1, 1))
                        .recordsCount(5)
                        .predicate("ROW_KEY >= 1 and ROW_KEY <= 5")
                        .build(),
                Dataset.builder()
                        .id(9876L)
                        .name("dataset")
                        .entityCode("entity")
                        .localReferenceDate(LocalDate.of(2018, 1, 1))
                        .recordsCount(5)
                        .predicate("ROW_KEY >= 6 and ROW_KEY <= 10")
                        .build()
        );

        when(phoenixTemplate.queryForList(any(), eq(Long.class)))
                .thenReturn(ImmutableList.of(1L, 2L, 3L, 4L, 5L))
                .thenReturn(ImmutableList.of(6L, 7L, 8L, 9L, 10L, 11L));

        assertThatThrownBy(() -> assertions.hasExpectedDatasetsInPhoenix(expected, actual).assertAll())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Phoenix row count");
    }

    @Test
    public void hasExpectedDatasetsInPhoenix_MultipleMatchingDatasets_IncorrectNumberOfRowsInPhoenix() {
        List<ExpectedDataset> expected = ImmutableList.of(
                ExpectedDataset.expected()
                        .schema("dataset1")
                        .entityCode("entity")
                        .referenceDate(LocalDate.of(2018, 1, 1))
                        .numberOfRows(5)
                        .build(),
                ExpectedDataset.expected()
                        .schema("dataset2")
                        .entityCode("entity")
                        .referenceDate(LocalDate.of(2018, 1, 1))
                        .numberOfRows(5)
                        .build()
        );

        List<Dataset> actual = ImmutableList.of(
                Dataset.builder()
                        .id(1234L)
                        .name("dataset1")
                        .entityCode("entity")
                        .localReferenceDate(LocalDate.of(2018, 1, 1))
                        .recordsCount(5)
                        .predicate("ROW_KEY >= 1 and ROW_KEY <= 5")
                        .build(),
                Dataset.builder()
                        .id(9876L)
                        .name("dataset2")
                        .entityCode("entity")
                        .localReferenceDate(LocalDate.of(2018, 1, 1))
                        .recordsCount(5)
                        .predicate("ROW_KEY >= 6 and ROW_KEY <= 15")
                        .build()
        );

        when(phoenixTemplate.queryForList(any(), eq(Long.class)))
                .thenReturn(ImmutableList.of(1L, 2L, 3L, 4L, 5L))
                .thenReturn(ImmutableList.of(6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L));

        assertThatThrownBy(() -> assertions.hasExpectedDatasetsInPhoenix(expected, actual).assertAll())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Phoenix row count");
    }

    @Test
    public void doesNotHaveDataset_DatasetNotFound() {
        List<Dataset> stagedDatasets = ImmutableList.of(
                Dataset.builder()
                        .id(1234L)
                        .name("another dataset")
                        .entityCode("entity code")
                        .localReferenceDate(LocalDate.of(2018, 1, 1))
                        .recordsCount(1)
                        .build(),
                Dataset.builder()
                        .id(2468L)
                        .name("another dataset")
                        .entityCode("entity code")
                        .localReferenceDate(LocalDate.of(2018, 1, 1))
                        .recordsCount(999)
                        .build()
        );

        assertions.doesNotHaveDatasetWithName("dataset", stagedDatasets).assertAll();
    }

    @Test
    public void doesNotHaveDataset_DatasetFound() {
        List<Dataset> stagedDatasets = ImmutableList.of(
                Dataset.builder()
                        .id(1234L)
                        .name("dataset")
                        .entityCode("entity code")
                        .localReferenceDate(LocalDate.of(2018, 1, 1))
                        .recordsCount(1)
                        .build(),
                Dataset.builder()
                        .id(2468L)
                        .name("another dataset")
                        .entityCode("entity code")
                        .localReferenceDate(LocalDate.of(2018, 1, 1))
                        .recordsCount(999)
                        .build()
        );

        assertThatThrownBy(() -> assertions.doesNotHaveDatasetWithName("dataset", stagedDatasets).assertAll())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Did not expect dataset");
    }

    @Test
    public void hasStagingDatasetWithStatus_StagingDatasetFound() {
        List<StagingItemView> stagingDatasets = ImmutableList.of(
                StagingItemView.builder()
                        .schema(StagingSchemaView.builder()
                                .name("schema name")
                                .physicalName("SCHEMA_NAME")
                                .build())
                        .status(DatasetState.REGISTERED)
                        .build(),
                StagingItemView.builder()
                        .schema(StagingSchemaView.builder()
                                .name("schema name 2")
                                .physicalName("SCHEMA_NAME_TWO")
                                .build())
                        .status(DatasetState.VALIDATION_FAILED)
                        .build()
        );

        assertions
                .hasStagingDatasetWithStatus(
                        SchemaView.builder().physicalTableName("SCHEMA_NAME").build(),
                        DatasetState.REGISTERED,
                        stagingDatasets)
                .assertAll();
    }

    @Test
    public void hasStagingDatasetWithStatus_StagingDatasetNotFound() {
        List<StagingItemView> stagingDatasets = ImmutableList.of(
                StagingItemView.builder()
                        .schema(StagingSchemaView.builder()
                                .name("schema name")
                                .physicalName("SCHEMA_NAME")
                                .build())
                        .status(DatasetState.REGISTERED)
                        .build(),
                StagingItemView.builder()
                        .schema(StagingSchemaView.builder()
                                .name("schema name 2")
                                .physicalName("SCHEMA_NAME_TWO")
                                .build())
                        .status(DatasetState.VALIDATION_FAILED)
                        .build()
        );

        assertThatThrownBy(() ->
                assertions
                        .hasStagingDatasetWithStatus(
                                SchemaView.builder().physicalTableName("SCHEMA_NAME").build(),
                                DatasetState.VALIDATION_FAILED,
                                stagingDatasets)
                        .assertAll())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Staging dataset");
    }
}