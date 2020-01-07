package com.lombardrisk.ignis.spark.core.phoenix;

import com.lombardrisk.ignis.common.lang.ErrorMessage;
import com.lombardrisk.ignis.spark.api.JobRequest;
import com.lombardrisk.ignis.spark.core.schema.DatasetTableSchema;
import com.lombardrisk.ignis.spark.core.spark.SparkFunctions;
import com.lombardrisk.ignis.spark.fixture.SparkCore;
import io.vavr.control.Either;
import org.apache.phoenix.spark.DataFrameFunctions;
import org.apache.phoenix.spark.SparkSqlContextFunctions;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import scala.collection.immutable.Map;

import java.sql.Connection;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.Silent.class)
public class PhoenixDatasetRepositoryTest {

    private static final int SALT_BUCKETS = 123;

    @Mock
    private SparkSession sparkSession;
    @Mock
    private JobRequest jobRequest;
    @Mock
    private SparkFunctions sparkFunctions;
    @Mock
    private Dataset<Row> dataset;
    @Mock
    private SparkSqlContextFunctions sparkSqlFunctions;
    @Mock
    private DataFrameFunctions dataFrameFunctions;
    @Mock
    private Connection dbConnection;
    @Mock
    private Statement statement;

    private PhoenixDatasetRepository phoenixRepository;

    @Before
    public void setUp() throws Exception {
        phoenixRepository =
                new PhoenixDatasetRepository(
                        sparkSession,
                        jobRequest,
                        SALT_BUCKETS,
                        sparkFunctions,
                        "zookeeper:2181");

        when(sparkFunctions.sparkSqlFunctions())
                .thenReturn(sparkSqlFunctions);
        when(sparkFunctions.dataFrameFunctions(any()))
                .thenReturn(dataFrameFunctions);
        when(dbConnection.createStatement())
                .thenReturn(statement);
    }

    @Test
    public void writeDataFrame_CallsSaveToPhoenixOnDataFrameFunctions() {

        DatasetTableSchema tableSchema = SparkCore.Populated.phoenixTableSchema()
                .tableName("DATA_TABLE")
                .build();

        phoenixRepository.writeDataFrame(dataset, tableSchema);

        verify(dataFrameFunctions).saveToPhoenix(new Map.Map2<>(
                "table", "DATA_TABLE", "zkUrl", "zookeeper:2181"));
    }

    @Test
    public void writeDataFrame_CreatesDataFrameFunctionWithDataset() {
        phoenixRepository.writeDataFrame(dataset, SparkCore.Populated.phoenixTableSchema().build());
        verify(sparkFunctions).dataFrameFunctions(dataset);
    }

    @Test
    public void writeDataFrame_SaveDatasetFailes_ReturnsErrorMessage() {
        when(sparkFunctions.dataFrameFunctions(any()))
                .thenThrow(new RuntimeException("oops"));

        Either<ErrorMessage, Dataset<Row>> result =
                phoenixRepository.writeDataFrame(dataset, SparkCore.Populated.phoenixTableSchema().build());

        assertThat(result.getLeft())
                .isEqualTo(ErrorMessage.of("java.lang.RuntimeException: oops"));
    }
}