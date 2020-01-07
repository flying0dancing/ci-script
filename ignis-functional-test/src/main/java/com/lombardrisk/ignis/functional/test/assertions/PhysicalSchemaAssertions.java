package com.lombardrisk.ignis.functional.test.assertions;

import com.lombardrisk.ignis.client.external.dataset.model.Dataset;
import com.lombardrisk.ignis.client.external.job.staging.DatasetState;
import com.lombardrisk.ignis.client.external.job.staging.StagingItemView;
import com.lombardrisk.ignis.client.external.productconfig.view.FieldView;
import com.lombardrisk.ignis.client.external.productconfig.view.SchemaView;
import com.lombardrisk.ignis.functional.test.assertions.expected.ExpectedDataset;
import com.lombardrisk.ignis.functional.test.assertions.expected.ExpectedPhysicalTable;
import com.lombardrisk.ignis.functional.test.config.data.TableFinder;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.groups.Tuple;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public final class PhysicalSchemaAssertions {

    private static final int OFFSET = 2;

    private final JdbcTemplate phoenixTemplate;
    private final Map<FieldView.Type, Integer> databaseTypeMap;
    private final TableFinder tableFinder;

    private final SoftAssertions soft = new SoftAssertions();

    public PhysicalSchemaAssertions(
            final JdbcTemplate phoenixTemplate,
            final Map<FieldView.Type, Integer> databaseTypeMap,
            final TableFinder tableFinder) {
        this.phoenixTemplate = phoenixTemplate;
        this.databaseTypeMap = databaseTypeMap;
        this.tableFinder = tableFinder;
    }

    public PhysicalSchemaAssertions hasExpectedDatasetsInPhoenix(
            final List<ExpectedDataset> expectedDatasets,
            final List<Dataset> stagedDatasets) {

        Tuple[] expected = expectedDatasets.stream()
                .map(ExpectedDataset::toTuple)
                .toArray(Tuple[]::new);

        assertThat(stagedDatasets)
                .describedAs("Expected datasets with name, entity code, reference date and row count")
                .extracting(this::toDatasetTuple)
                .containsExactlyInAnyOrder(expected);

        Map<Long, List<Long>> datasetIdToRowKeys = new HashMap<>();
        for (Dataset stagedDataset : stagedDatasets) {
            String getAllRowsForDataset =
                    "SELECT ROW_KEY FROM " + stagedDataset.getName() + " WHERE " + stagedDataset.getPredicate();

            List<Long> datasetRowKeys = phoenixTemplate.queryForList(getAllRowsForDataset, Long.class);

            datasetIdToRowKeys.put(stagedDataset.getId(), datasetRowKeys);
        }

        for (ExpectedDataset expectedDataset : expectedDatasets) {
            List<Dataset> matchingDatasets = stagedDatasets.stream()
                    .filter(dataset -> datasetMatches(dataset, expectedDataset))
                    .collect(toList());

            for (Dataset matchingDataset : matchingDatasets) {
                List<Long> datasetRowKeys = datasetIdToRowKeys.get(matchingDataset.getId());

                soft.assertThat(datasetRowKeys.size())
                        .describedAs("Phoenix row count for dataset id [%d] name [%s]",
                                matchingDataset.getId(), matchingDataset.getName())
                        .isEqualTo(expectedDataset.getNumberOfRows());
            }
        }

        return this;
    }

    private Tuple toDatasetTuple(final Dataset dataset) {
        return tuple(
                dataset.getName(),
                dataset.getEntityCode(),
                dataset.getLocalReferenceDate(),
                dataset.getRecordsCount()
        );
    }

    private boolean datasetMatches(final Dataset d1, final ExpectedDataset d2) {
        return d1.getName().equals(d2.getSchema())
                && d1.getEntityCode().equals(d2.getEntityCode())
                && d1.getLocalReferenceDate().equals(d2.getReferenceDate())
                && d1.getRecordsCount() == d2.getNumberOfRows();
    }

    public PhysicalSchemaAssertions doesNotHaveDatasetWithName(
            final String datasetName,
            final List<Dataset> stagedDatasets) {
        soft.assertThat(stagedDatasets)
                .describedAs("Did not expect dataset with name, entity code, reference date and row count")
                .extracting(Dataset::getName)
                .doesNotContain(datasetName);

        return this;
    }

    public PhysicalSchemaAssertions hasStagingDatasetWithStatus(
            final SchemaView importedSchema,
            final DatasetState datasetState,
            final List<StagingItemView> stagingDatasets) {

        soft.assertThat(stagingDatasets)
                .describedAs("Staging dataset with name and state")
                .extracting(stagingDataset -> tuple(
                        stagingDataset.getSchema().getPhysicalName(),
                        stagingDataset.getStatus()))
                .contains(tuple(
                        importedSchema.getPhysicalTableName(),
                        datasetState));

        return this;
    }

    public PhysicalSchemaAssertions hasExpectedTableSchema(
            final ExpectedPhysicalTable expectedTable,
            final String physicalTableName) {

        assertThat(tableFinder.queryTablesByName(physicalTableName))
                .describedAs("Table [%s] not in Phoenix", physicalTableName)
                .isNotEmpty();

        SqlRowSetMetaData phoenixTableMetadata =
                phoenixTemplate
                        .queryForRowSet("SELECT * FROM " + physicalTableName + " LIMIT 1")
                        .getMetaData();

        Long rowKeyCount =
                phoenixTemplate.queryForObject("SELECT COUNT(ROW_KEY) FROM " + physicalTableName, Long.class);

        soft.assertThat(phoenixTableMetadata.getColumnNames()[0])
                .describedAs("First column of Phoenix table")
                .isEqualTo("ROW_KEY");

        for (int i = 0; i < expectedTable.getColumns().size(); i++) {
            FieldView expectedColumn = expectedTable.getColumns().get(i);
            String phoenixColumnName = phoenixTableMetadata.getColumnName(i + OFFSET);
            int phoenixColumnType = phoenixTableMetadata.getColumnType(i + OFFSET);

            soft.assertThat(phoenixColumnName)
                    .describedAs("Field name [%s]", expectedColumn.getName())
                    .isEqualTo(expectedColumn.getName());

            soft.assertThat(phoenixColumnType)
                    .describedAs("Field type [%s, %s]", expectedColumn.getName(), expectedColumn.getFieldType())
                    .isEqualTo(databaseTypeMap.get(expectedColumn.getFieldType()));
        }

        soft.assertThat(rowKeyCount)
                .isEqualTo(expectedTable.getNumberOfRows());

        return this;
    }

    public PhysicalSchemaAssertions doesNotHaveTableWithName(final String physicalSchema) {
        soft.assertThat(tableFinder.queryTablesByName(physicalSchema))
                .describedAs("Table [%s] in Phoenix", physicalSchema)
                .isEmpty();

        return this;
    }

    public void assertAll() {
        soft.assertAll();
    }
}
