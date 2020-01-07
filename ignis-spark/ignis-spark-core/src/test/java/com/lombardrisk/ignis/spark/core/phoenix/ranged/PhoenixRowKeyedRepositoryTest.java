package com.lombardrisk.ignis.spark.core.phoenix.ranged;

import com.lombardrisk.ignis.common.lang.ErrorMessage;
import com.lombardrisk.ignis.spark.api.JobRequest;
import com.lombardrisk.ignis.spark.core.JobOperatorException;
import com.lombardrisk.ignis.spark.core.repository.DatasetRepository;
import com.lombardrisk.ignis.spark.core.server.DatasetEntityRepository;
import io.vavr.control.Either;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PhoenixRowKeyedRepositoryTest {

    @Mock
    private DatasetRepository datasetRepository;
    @Mock
    private DatasetEntityRepository datasetEntityRepository;
    @Mock
    private JobRequest jobRequest;

    private SparkSession sparkSession;

    private PhoenixRowKeyedRepository phoenixRowKeyedRepository;

    @Before
    public void setUp() {
        when(datasetRepository.writeDataFrame(any(), any()))
                .then(invocation -> {
                    org.apache.spark.sql.Dataset<Row> dataset = invocation.getArgument(0);
                    return Either.right(dataset);
                });

        sparkSession = SparkSession.builder().master("local[1]").getOrCreate();

        phoenixRowKeyedRepository =
                new PhoenixRowKeyedRepository(sparkSession, jobRequest, datasetRepository, datasetEntityRepository);
    }

    @Test
    public void generateRowKeyAndSaveDataset_DatasetWriteFails_ThrowsJobException() {
        List<Row> ts = Collections.singletonList(RowFactory.create(101));

        org.apache.spark.sql.Dataset<Row> dataFrame =
                sparkSession.createDataFrame(ts, DataTypes.createStructType(
                        new StructField[]{
                                DataTypes.createStructField("number", DataTypes.IntegerType, false)
                        }));

        when(datasetRepository.writeDataFrame(any(), any()))
                .thenReturn(Either.left(ErrorMessage.of("I don't know whether the weather")));

        assertThatThrownBy(() -> phoenixRowKeyedRepository.generateRowKeyAndSaveDataset(
                1L, dataFrame, com.lombardrisk.ignis.spark.api.fixture.Populated.stagingDatasetConfig().build())
        )
                .isInstanceOf(JobOperatorException.class)
                .hasMessage("I don't know whether the weather");
    }
}