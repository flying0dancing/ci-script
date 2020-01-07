package com.lombardrisk.ignis.spark.staging.execution;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.client.external.job.StagingClient;
import com.lombardrisk.ignis.spark.TestStagingApplication;
import com.lombardrisk.ignis.spark.api.fixture.Populated;
import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import com.lombardrisk.ignis.spark.api.staging.StagingSchemaValidation;
import com.lombardrisk.ignis.spark.staging.datafields.DataRow;
import com.lombardrisk.ignis.spark.staging.datafields.IndexedDataField;
import io.vavr.control.Either;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import retrofit2.Call;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestStagingApplication.class)
public class DatasetValidator_FieldParsingIT {

    @Autowired
    private DatasetValidator validator;

    @Autowired
    private JavaSparkContext javaSparkContext;

    @MockBean
    private StagingClient stagingClient;
    @Mock
    private Call<Void> voidCall;

    @Before
    public void setup() {
        when(stagingClient.updateDataSetState(anyLong(), anyString()))
                .thenReturn(voidCall);
    }

    @Test
    public void validate_DecimalFieldWithValueCanApplyPrecisionAfterScaleRounding_ConvertsToBigDecimal() {
        StagingDatasetConfig stagingDatasetConfig = Populated.stagingDatasetConfig()
                .stagingSchemaValidation(StagingSchemaValidation.builder()
                        .fields(
                                ImmutableSet.of(
                                        Populated.decimalFieldValidation()
                                                .precision(3)
                                                .scale(1)
                                                .build()))
                        .build())
                .build();

        DataRow decimalDataRow = new DataRow(ImmutableList.of(new IndexedDataField(0, "23.15")));
        JavaRDD<DataRow> decimalRDD = javaSparkContext.parallelize(ImmutableList.of(decimalDataRow));

        Either<JavaRDD<String>, Dataset<Row>> decimalDatasets = validator.validate(stagingDatasetConfig, decimalRDD);

        assertThat(decimalDatasets.isRight()).isTrue();

        BigDecimal roundedDecimal = (BigDecimal) decimalDatasets.get().first().get(0);
        assertThat(roundedDecimal.toPlainString()).isEqualTo("23.2");
    }

    @Test
    public void validate_DecimalFieldWithValueCanNotApplyPrecisionAndScale_ReturnsInvalidDataset() {
        StagingDatasetConfig stagingDatasetConfig = Populated.stagingDatasetConfig()
                .stagingSchemaValidation(StagingSchemaValidation.builder()
                        .fields(
                                ImmutableSet.of(
                                        Populated.decimalFieldValidation()
                                                .name("SCALE_TOO_BIG")
                                                .precision(4)
                                                .scale(2)
                                                .build()))
                        .build())
                .build();

        DataRow decimalDataRow = new DataRow(ImmutableList.of(new IndexedDataField(0, "334.5")));
        JavaRDD<DataRow> decimalRDD = javaSparkContext.parallelize(ImmutableList.of(decimalDataRow));

        Either<JavaRDD<String>, Dataset<Row>> decimalDatasets = validator.validate(stagingDatasetConfig, decimalRDD);

        assertThat(decimalDatasets.isRight()).isFalse();
        assertThat(decimalDatasets.getLeft().collect())
                .contains(
                        "334.5,"
                                + "\"Field [SCALE_TOO_BIG] cannot be converted to decimal type: "
                                + "precision [4] and scale [2] cannot be applied for value [334.5], "
                                + "because the resulting number (334.xx) will not fit the precision (4 < 5)\"");
    }
}