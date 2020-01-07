package com.lombardrisk.ignis.spark.staging.execution;

import com.lombardrisk.ignis.client.external.job.StagingClient;
import com.lombardrisk.ignis.spark.TestStagingApplication;
import com.lombardrisk.ignis.spark.api.staging.DatasetProperties;
import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import com.lombardrisk.ignis.spark.api.staging.StagingSchemaValidation;
import com.lombardrisk.ignis.spark.api.staging.field.DateFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.FieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.IntegerFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.LongFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.StringFieldValidation;
import com.lombardrisk.ignis.spark.staging.TestDataSource;
import com.lombardrisk.ignis.spark.staging.datafields.DataRow;
import com.lombardrisk.ignis.spark.staging.execution.load.CsvDatasetLoader;
import io.vavr.control.Either;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import retrofit2.Call;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.io.FileUtils.copyDirectory;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestStagingApplication.class)
public class DatasetValidatorIT {

    @Autowired
    private DatasetValidator validator;

    @Autowired
    private CsvDatasetLoader loader;

    @MockBean
    private StagingClient stagingClient;
    @Mock
    private Call<Void> voidCall;

    @BeforeClass
    public static void setupDatasets() throws IOException {
        File datasetsDir = new File("target/datasets").getAbsoluteFile();

        deleteQuietly(datasetsDir);
        copyDirectory(new File("src/test/resources/datasets"), datasetsDir);
    }

    @Before
    public void setup() {
        when(stagingClient.updateDataSetState(anyLong(), anyString()))
                .thenReturn(voidCall);
    }

    @Test
    public void validDataset() {
        StagingDatasetConfig datasetValid = StagingDatasetConfig.builder()
                .stagingSchemaValidation(StagingSchemaValidation.builder()
                        .physicalTableName("VALID_EMPLOYEE")
                        .fields(createEmployeeSchema())
                        .build())
                .datasetProperties(DatasetProperties.builder().build())
                .source(new TestDataSource("target/datasets/employee/staging/1/VALID_EMPLOYEE", false))
                .build();

        JavaRDD<DataRow> rdd = loader.load(datasetValid);
        Either<JavaRDD<String>, Dataset<Row>> result = validator.validate(datasetValid, rdd);

        assertThat(result.isRight()).isTrue();
    }

    @Test
    public void datasetWithHeader() {
        StagingDatasetConfig datasetHeader = StagingDatasetConfig.builder()
                .stagingSchemaValidation(StagingSchemaValidation.builder()
                        .physicalTableName("HEAD_EMPLOYEE")
                        .fields(createEmployeeSchema())
                        .build())
                .datasetProperties(DatasetProperties.builder().build())
                .source(new TestDataSource("target/datasets/employee/staging/1/HEAD_EMPLOYEE", true))
                .build();

        JavaRDD<DataRow> rdd = loader.load(datasetHeader);
        Either<JavaRDD<String>, Dataset<Row>> result = validator.validate(datasetHeader, rdd);

        assertThat(result.isRight()).isTrue();
        assertThat(result.get().count()).isEqualTo(3);
    }

    @Test
    public void execute_invalidDataset() {
        StagingDatasetConfig datasetInvalid = StagingDatasetConfig.builder()
                .stagingSchemaValidation(StagingSchemaValidation.builder()
                        .physicalTableName("INVALID_EMPLOYEE")
                        .fields(createEmployeeSchema())
                        .build())
                .datasetProperties(DatasetProperties.builder().build())
                .source(new TestDataSource("target/datasets/employee/staging/1/INVALID_EMPLOYEE", false))
                .build();

        JavaRDD<DataRow> rdd = loader.load(datasetInvalid);
        Either<JavaRDD<String>, Dataset<Row>> result = validator.validate(datasetInvalid, rdd);

        assertThat(result.isLeft()).isTrue();

        List<String> invalidDataset = result.getLeft().collect();
        assertThat(invalidDataset)
                .containsSequence(
                        "2,name2,30,female123,2010-02-03,Field [gender] expected to have a maximum length of 6 - actual: 9",
                        "3,name3,40a,m,2010-02-23,Field [age] expected numeric value - actual: 40a;Field [gender] expected to match the regex ^(male|female)$ - actual: m",
                        "5,name5,<NULL>,female,<NULL>,Field [age] is not nullable",
                        "6,name6,50000000000,male,02/02/2010,Field [age] expected to be a integer - actual: 50000000000;Field [birthday] expected to be a date with format yyyy-MM-dd - actual: 02/02/2010",
                        "7,name7,Expected 5 fields - actual: 2");
    }

    private static LinkedHashSet<FieldValidation> createEmployeeSchema() {
        LongFieldValidation idFieldValidation = new LongFieldValidation();
        idFieldValidation.setName("id");
        idFieldValidation.setNullable(true);

        StringFieldValidation nameFieldValidation = new StringFieldValidation();
        nameFieldValidation.setName("name");
        nameFieldValidation.setNullable(false);
        nameFieldValidation.setMaxLength(15);
        nameFieldValidation.setMinLength(2);

        IntegerFieldValidation ageFieldValidation = new IntegerFieldValidation();
        ageFieldValidation.setName("age");
        ageFieldValidation.setNullable(false);

        StringFieldValidation genderFieldValidation = new StringFieldValidation();
        genderFieldValidation.setName("gender");
        genderFieldValidation.setNullable(false);
        genderFieldValidation.setMaxLength(6);
        genderFieldValidation.setRegularExpression("^(male|female)$");

        DateFieldValidation birthdayFieldValidation = new DateFieldValidation();
        birthdayFieldValidation.setName("birthday");
        birthdayFieldValidation.setFormat("yyyy-MM-dd");
        birthdayFieldValidation.setNullable(true);

        return new LinkedHashSet<>(
                asList(
                        idFieldValidation,
                        nameFieldValidation,
                        ageFieldValidation,
                        genderFieldValidation,
                        birthdayFieldValidation));
    }
}