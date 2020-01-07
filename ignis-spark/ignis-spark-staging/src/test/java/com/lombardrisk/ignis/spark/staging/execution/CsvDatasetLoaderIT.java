package com.lombardrisk.ignis.spark.staging.execution;

import com.lombardrisk.ignis.spark.api.fixture.Populated;
import com.lombardrisk.ignis.spark.api.staging.DatasetProperties;
import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import com.lombardrisk.ignis.spark.TestStagingApplication;
import com.lombardrisk.ignis.spark.staging.TestDataSource;
import com.lombardrisk.ignis.spark.staging.datafields.DataRow;
import com.lombardrisk.ignis.spark.staging.datafields.IndexedDataField;
import com.lombardrisk.ignis.spark.staging.execution.load.CsvDatasetLoader;
import org.apache.spark.api.java.JavaRDD;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.apache.commons.io.FileUtils.copyDirectory;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestStagingApplication.class)
public class CsvDatasetLoaderIT {

    @Autowired
    private CsvDatasetLoader loader;

    @BeforeClass
    public static void setupDatasets() throws IOException {
        File datasetsDir = new File("target/datasets").getAbsoluteFile();

        deleteQuietly(datasetsDir);
        copyDirectory(new File("src/test/resources/datasets"), datasetsDir);
    }

    @Test
    public void load() {
        StagingDatasetConfig stagingItemRequest = StagingDatasetConfig.builder()
                .stagingSchemaValidation(Populated.stagingSchemaValidation()
                        .physicalTableName("VALID_EMPLOYEE")
                        .build())
                .id(1L)
                .source(new TestDataSource("target/datasets/employee/staging/1/VALID_EMPLOYEE", false))
                .datasetProperties(DatasetProperties.builder().build())
                .build();

        JavaRDD<DataRow> result = loader.load(stagingItemRequest);

        assertThat(result.map(DataRow::getFields).collect())
                .contains(
                        Arrays.asList(
                                new IndexedDataField(0, "1"),
                                new IndexedDataField(1, "name1"),
                                new IndexedDataField(2, "20"),
                                new IndexedDataField(3, "male"),
                                new IndexedDataField(4, "2010-01-01")
                        ),
                        Arrays.asList(
                                new IndexedDataField(0, "2"),
                                new IndexedDataField(1, "name2"),
                                new IndexedDataField(2, "30"),
                                new IndexedDataField(3, "female"),
                                new IndexedDataField(4, "2010-02-03")
                        ),
                        Arrays.asList(
                                new IndexedDataField(0, "3"),
                                new IndexedDataField(1, "name3"),
                                new IndexedDataField(2, "40"),
                                new IndexedDataField(3, "male"),
                                new IndexedDataField(4, null)
                        ));
    }
}