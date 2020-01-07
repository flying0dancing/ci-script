package com.lombardrisk.ignis.spark.core.phoenix.ranged;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.client.internal.CreateDatasetCall;
import com.lombardrisk.ignis.client.internal.UpdateDatasetRunCall;
import com.lombardrisk.ignis.spark.TestApplication;
import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.spark.api.fixture.Populated;
import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import com.lombardrisk.ignis.spark.core.mock.StatefulDatasetClientStore;
import com.lombardrisk.ignis.spark.core.mock.TestDatasetRepository;
import com.lombardrisk.ignis.spark.fixture.SparkCore;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class PhoenixRowKeyedRepositoryIT {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Autowired
    private PhoenixRowKeyedRepository repository;

    @Autowired
    private TestDatasetRepository datasetRepository;

    @Autowired
    private SparkSession spark;

    @Autowired
    private StatefulDatasetClientStore internalDatasetClient;

    @After
    public void tearDown() throws Exception {
        datasetRepository.dropTable("Characters");
    }

    @Test
    public void readDataFrame_WithPredicate_ReturnsDataset() {
        StructType employeeType = DataTypes.createStructType(ImmutableList.of(
                DataTypes.createStructField("ID", DataTypes.LongType, false),
                DataTypes.createStructField("FirstName", DataTypes.StringType, false),
                DataTypes.createStructField("LastName", DataTypes.StringType, false),
                DataTypes.createStructField("Role", DataTypes.StringType, false)));

        Dataset<Row> data = spark.createDataFrame(ImmutableList.of(
                RowFactory.create(1L, "Homer", "Simpson", "Safety Inspector"),
                RowFactory.create(2L, "Lenny", "Leonard", "Supervisor"),
                RowFactory.create(3L, "Carl", "Carlson", "Supervisor"),
                RowFactory.create(4L, "Charles Montgomery", "Burns", "Owner"),
                RowFactory.create(5L, "Waylon", "Smithers", "Assistant")), employeeType);

        datasetRepository.writeDataFrame(data, SparkCore.Populated.phoenixTableSchema()
                .tableName("Characters")
                .build());

        Dataset<Row> employees = repository.readDataFrame(DatasetTableLookup.builder()
                .datasetName("Characters")
                .predicate("ID >= 1 AND ID < 4")
                .build());

        assertThat(employees.collectAsList())
                .containsExactlyInAnyOrder(
                        RowFactory.create(1L, "Homer", "Simpson", "Safety Inspector"),
                        RowFactory.create(2L, "Lenny", "Leonard", "Supervisor"),
                        RowFactory.create(3L, "Carl", "Carlson", "Supervisor"));
    }

    @Test
    public void generateRowKeyAndSaveDataset_StagingConfiguration_SavesDatasetWithRowKeys() {
        StructType employeeType = DataTypes.createStructType(ImmutableList.of(
                DataTypes.createStructField("ID", DataTypes.LongType, false),
                DataTypes.createStructField("FirstName", DataTypes.StringType, false),
                DataTypes.createStructField("LastName", DataTypes.StringType, false),
                DataTypes.createStructField("Role", DataTypes.StringType, false)));

        Dataset<Row> data = spark.createDataFrame(ImmutableList.of(
                RowFactory.create(1L, "Homer", "Simpson", "Safety Inspector"),
                RowFactory.create(2L, "Lenny", "Leonard", "Supervisor"),
                RowFactory.create(3L, "Carl", "Carlson", "Supervisor"),
                RowFactory.create(4L, "Charles Montgomery", "Burns", "Owner"),
                RowFactory.create(5L, "Waylon", "Smithers", "Assistant")), employeeType);

        StagingDatasetConfig config = Populated.stagingDatasetConfig()
                .stagingSchemaValidation(Populated.stagingSchemaValidation().physicalTableName("Characters").build())
                .build();

        DatasetTableLookup datasetTableLookup = repository.generateRowKeyAndSaveDataset(1L, data, config);

        soft.assertThat(datasetTableLookup)
                .isEqualTo(DatasetTableLookup.builder()
                        .datasetName("Characters")
                        .predicate("ROW_KEY >= 4294967296 and ROW_KEY <= 8589934591")
                        .rowKeySeed(1L)
                        .datasetId(123L)
                        .recordsCount(5L)
                        .build());

        soft.assertThat(datasetRepository.readDataFrame(datasetTableLookup).collectAsList())
                .containsExactlyInAnyOrder(
                        RowFactory.create(4294967296L, 1L, "Homer", "Simpson", "Safety Inspector"),
                        RowFactory.create(4294967297L, 2L, "Lenny", "Leonard", "Supervisor"),
                        RowFactory.create(4294967298L, 3L, "Carl", "Carlson", "Supervisor"),
                        RowFactory.create(4294967299L, 4L, "Charles Montgomery", "Burns", "Owner"),
                        RowFactory.create(4294967300L, 5L, "Waylon", "Smithers", "Assistant"));
    }

    @Test
    public void generateRowKeyAndSaveDataset_StagingConfiguration_SavesDatasetMetadata() {
        StructType employeeType = DataTypes.createStructType(ImmutableList.of(
                DataTypes.createStructField("ID", DataTypes.LongType, false),
                DataTypes.createStructField("FirstName", DataTypes.StringType, false),
                DataTypes.createStructField("LastName", DataTypes.StringType, false),
                DataTypes.createStructField("Role", DataTypes.StringType, false)));

        Dataset<Row> data = spark.createDataFrame(ImmutableList.of(
                RowFactory.create(1L, "Homer", "Simpson", "Safety Inspector"),
                RowFactory.create(2L, "Lenny", "Leonard", "Supervisor"),
                RowFactory.create(3L, "Carl", "Carlson", "Supervisor"),
                RowFactory.create(4L, "Charles Montgomery", "Burns", "Owner"),
                RowFactory.create(5L, "Waylon", "Smithers", "Assistant")), employeeType);

        StagingDatasetConfig config = Populated.stagingDatasetConfig()
                .id(2345L)
                .datasetProperties(Populated.datasetProperties()
                        .entityCode("the-entity-code")
                        .referenceDate(LocalDate.of(2018, 7, 21))
                        .build())
                .stagingSchemaValidation(Populated.stagingSchemaValidation()
                        .schemaId(909L)
                        .physicalTableName("Characters")
                        .build())
                .build();

        repository.generateRowKeyAndSaveDataset(1L, data, config);

        CreateDatasetCall createDatasetCall = internalDatasetClient.getCachedDatasetCall();

        assertThat(createDatasetCall)
                .isEqualTo(CreateDatasetCall.builder()
                        .stagingDatasetId(2345L)
                        .stagingJobId(2876543L)
                        .entityCode("the-entity-code")
                        .referenceDate(LocalDate.of(2018, 7, 21))
                        .schemaId(909L)
                        .rowKeySeed(1L)
                        .predicate("ROW_KEY >= 4294967296 and ROW_KEY <= 8589934591")
                        .recordsCount(5)
                        .build());
    }

    @Test
    public void generateRowKeyAndSaveDataset_AppendToExistingDataset_SavesDatasetWithRowKeys() {
        StructType employeeType = DataTypes.createStructType(ImmutableList.of(
                DataTypes.createStructField("ID", DataTypes.LongType, false),
                DataTypes.createStructField("FirstName", DataTypes.StringType, false),
                DataTypes.createStructField("LastName", DataTypes.StringType, false),
                DataTypes.createStructField("Role", DataTypes.StringType, false)));

        Dataset<Row> data = spark.createDataFrame(ImmutableList.of(
                RowFactory.create(1L, "Homer", "Simpson", "Safety Inspector"),
                RowFactory.create(2L, "Lenny", "Leonard", "Supervisor"),
                RowFactory.create(3L, "Carl", "Carlson", "Supervisor"),
                RowFactory.create(4L, "Charles Montgomery", "Burns", "Owner"),
                RowFactory.create(5L, "Waylon", "Smithers", "Assistant")), employeeType);

        Dataset<Row> dataToAppend = spark.createDataFrame(ImmutableList.of(
                RowFactory.create(6L, "Seymour", "Skinner", "Head Teacher"),
                RowFactory.create(7L, "Moe", "Szyslak", "Bartender"),
                RowFactory.create(8L, "Troy", "McClure", "Actor")), employeeType);

        StagingDatasetConfig config = Populated.stagingDatasetConfig()
                .stagingSchemaValidation(Populated.stagingSchemaValidation().physicalTableName("Characters").build())
                .build();

        DatasetTableLookup datasetTableLookup = repository.generateRowKeyAndSaveDataset(1L, data, config);

        StagingDatasetConfig appendConfig = Populated.stagingDatasetConfig()
                .stagingSchemaValidation(Populated.stagingSchemaValidation().physicalTableName("Characters").build())
                .appendToDataset(datasetTableLookup)
                .build();

        DatasetTableLookup appendDatasetLookup = repository.generateRowKeyAndSaveDataset(1L, dataToAppend, appendConfig);

        soft.assertThat(appendDatasetLookup)
                .isEqualTo(DatasetTableLookup.builder()
                        .datasetName("Characters")
                        .predicate("ROW_KEY >= 4294967296 and ROW_KEY <= 8589934591")
                        .rowKeySeed(1L)
                        .datasetId(123L)
                        .recordsCount(8L)
                        .build());

        soft.assertThat(datasetRepository.readDataFrame(appendDatasetLookup).collectAsList())
                .containsExactlyInAnyOrder(
                        RowFactory.create(4294967296L, 1L, "Homer", "Simpson", "Safety Inspector"),
                        RowFactory.create(4294967297L, 2L, "Lenny", "Leonard", "Supervisor"),
                        RowFactory.create(4294967298L, 3L, "Carl", "Carlson", "Supervisor"),
                        RowFactory.create(4294967299L, 4L, "Charles Montgomery", "Burns", "Owner"),
                        RowFactory.create(4294967300L, 5L, "Waylon", "Smithers", "Assistant"),
                        RowFactory.create(4294967301L, 6L, "Seymour", "Skinner", "Head Teacher"),
                        RowFactory.create(4294967302L, 7L, "Moe", "Szyslak", "Bartender"),
                        RowFactory.create(4294967303L, 8L, "Troy", "McClure", "Actor"));
    }

    @Test
    public void generateRowKeyAndSaveDataset_AppendToExistingDataset_UpdatesExistingDatasetMetadata() {
        StructType employeeType = DataTypes.createStructType(ImmutableList.of(
                DataTypes.createStructField("ID", DataTypes.LongType, false),
                DataTypes.createStructField("FirstName", DataTypes.StringType, false),
                DataTypes.createStructField("LastName", DataTypes.StringType, false),
                DataTypes.createStructField("Role", DataTypes.StringType, false)));

        Dataset<Row> data = spark.createDataFrame(ImmutableList.of(
                RowFactory.create(1L, "Homer", "Simpson", "Safety Inspector"),
                RowFactory.create(2L, "Lenny", "Leonard", "Supervisor"),
                RowFactory.create(3L, "Carl", "Carlson", "Supervisor"),
                RowFactory.create(4L, "Charles Montgomery", "Burns", "Owner"),
                RowFactory.create(5L, "Waylon", "Smithers", "Assistant")), employeeType);

        Dataset<Row> dataToAppend = spark.createDataFrame(ImmutableList.of(
                RowFactory.create(6L, "Seymour", "Skinner", "Head Teacher"),
                RowFactory.create(7L, "Moe", "Szyslak", "Bartender"),
                RowFactory.create(8L, "Troy", "McClure", "Actor")), employeeType);

        StagingDatasetConfig config = Populated.stagingDatasetConfig()
                .stagingSchemaValidation(Populated.stagingSchemaValidation().physicalTableName("Characters").build())
                .build();

        DatasetTableLookup datasetTableLookup = repository.generateRowKeyAndSaveDataset(1L, data, config);

        StagingDatasetConfig appendConfig = Populated.stagingDatasetConfig()
                .id(2345L)
                .stagingSchemaValidation(Populated.stagingSchemaValidation().physicalTableName("Characters").build())
                .appendToDataset(datasetTableLookup)
                .build();

        repository.generateRowKeyAndSaveDataset(1L, dataToAppend, appendConfig);

        assertThat(internalDatasetClient.getCachedUpdateDatasetRunCall())
                .isEqualTo(UpdateDatasetRunCall.builder()
                        .stagingDatasetId(2345L)
                        .stagingJobId(2876543L)
                        .recordsCount(8L)
                        .build());
    }
}