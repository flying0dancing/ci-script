package com.lombardrisk.ignis.spark.staging.execution;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.client.external.job.StagingClient;
import com.lombardrisk.ignis.spark.TestStagingApplication;
import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.spark.api.fixture.Populated;
import com.lombardrisk.ignis.spark.api.staging.DatasetProperties;
import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import com.lombardrisk.ignis.spark.api.staging.StagingErrorOutput;
import com.lombardrisk.ignis.spark.api.staging.StagingSchemaValidation;
import com.lombardrisk.ignis.spark.core.repository.DatasetRepository;
import com.lombardrisk.ignis.spark.util.DatasetRow;
import io.vavr.control.Either;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import retrofit2.Call;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestStagingApplication.class)
public class DatasetRegisterIT {

    @Autowired
    private DatasetRegister register;

    @Autowired
    private SparkSession spark;

    @Autowired
    private DatasetRepository datasetRepository;

    @MockBean
    private StagingClient stagingClient;

    @Mock
    private Call<Void> voidCall;

    @Before
    public void setup() {
        when(stagingClient.updateDataSetState(anyLong(), anyString()))
                .thenReturn(voidCall);

        deleteQuietly(new File("target/datasets/error/"));
    }

    @Test
    public void saveValidDataset() {
        StructField field1 = new StructField("id", DataTypes.IntegerType, true, Metadata.empty());
        StructField field2 = new StructField("name", DataTypes.StringType, true, Metadata.empty());
        StructType structType = new StructType(new StructField[]{ field1, field2 });

        List<Row> data = ImmutableList.of(RowFactory.create(1, "a1"));
        Dataset<Row> df = spark.createDataFrame(data, structType);

        StagingDatasetConfig employeeStagingRequest =
                Populated.stagingDatasetConfig()
                        .stagingSchemaValidation(StagingSchemaValidation.builder()
                                .schemaId(90L)
                                .physicalTableName("employee")
                                .build())
                        .datasetProperties(DatasetProperties.builder()
                                .build())
                        .build();

        register.register(1L, employeeStagingRequest, Either.right(df));

        List<Row> rows = datasetRepository
                .readDataFrame(DatasetTableLookup.builder().datasetName("Employee").build())
                .collectAsList();

        Map<String, Object> map = new DatasetRow(rows.get(0)).asMap();

        assertThat(map.size()).isEqualTo(3);
    }

    @Test
    public void saveInValidDataset_HdfsErrorOutputType_SavesFileToFileSystem() throws Exception {
        List<Row> data = ImmutableList.of(
                RowFactory.create("1, HOMER, SIMSPON <expected 5 fields and got 3>"),
                RowFactory.create("3, NED, FLANDERS <Flanders is not an allowable surname>"));

        StructType justAnError = DataTypes.createStructType(
                Collections.singletonList(
                        DataTypes.createStructField("JUST_AN_ERROR", DataTypes.StringType, false)));

        JavaRDD<String> df = spark.createDataFrame(data, justAnError)
                .javaRDD()
                .map(row -> (String) row.get(0));

        StagingDatasetConfig employeeStagingRequest = Populated.stagingDatasetConfig()
                .stagingErrorOutput(StagingErrorOutput.builder()
                        .errorFileSystemUri("file:///")
                        .temporaryFilePath("target/datasets/temp/10/")
                        .errorFilePath("target/datasets/error/10/E_SIMPSONS.csv")
                        .build())
                .build();

        register.register(1L, employeeStagingRequest, Either.left(df));

        List<String> errors = Files.lines(Paths.get("target/datasets/error/10/E_SIMPSONS.csv"))
                .collect(Collectors.toList());

        assertThat(errors)
                .containsExactly(
                        "1, HOMER, SIMSPON <expected 5 fields and got 3>",
                        "3, NED, FLANDERS <Flanders is not an allowable surname>");
    }
}