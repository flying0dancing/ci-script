package com.lombardrisk.ignis.server.job.staging;

import com.lombardrisk.ignis.server.dataset.fixture.DatasetPopulated;
import com.lombardrisk.ignis.server.job.fixture.JobPopulated;
import com.lombardrisk.ignis.server.job.staging.file.DataSourceService;
import com.lombardrisk.ignis.server.job.staging.file.ErrorFileService;
import com.lombardrisk.ignis.server.job.staging.model.StagingDatasetInstruction;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.table.model.Field;
import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.spark.api.staging.DatasetProperties;
import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import com.lombardrisk.ignis.spark.api.staging.StagingSchemaValidation;
import com.lombardrisk.ignis.spark.api.staging.datasource.HdfsCsvDataSource;
import com.lombardrisk.ignis.spark.api.staging.field.FieldValidation;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.HashSet;

import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.server.job.fixture.JobPopulated.stagingDatasetInstruction;
import static com.lombardrisk.ignis.spark.api.fixture.Populated.csvDataSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StagingSparkConfigServiceTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Mock
    private DataSourceService dataSourceService;
    @Mock
    private ErrorFileService errorFileService;

    @InjectMocks
    private StagingSparkConfigService stagingSparkConfigService;

    @Before
    public void setUp() {
        when(dataSourceService.createSparkDataSource(any(), anyBoolean()))
                .thenReturn(csvDataSource().build());
    }

    @Test
    public void createDatasetAppConfig_csvDatasourceFromDatasourceService_ReturnsAppConfigWithDataSource() {
        HdfsCsvDataSource hdfsCsvDataSource = csvDataSource().build();
        when(dataSourceService.createSparkDataSource(any(), anyBoolean()))
                .thenReturn(hdfsCsvDataSource);

        StagingDatasetConfig datasetConfig = stagingSparkConfigService.createDatasetAppConfig(
                JobPopulated.stagingDataset().id(1L).build(), stagingDatasetInstruction().build());

        assertThat(datasetConfig.getSource())
                .isEqualTo(hdfsCsvDataSource);
    }

    @Test
    public void createDatasetAppConfig_InstructionsWithSchema_SetsSchemaValidation() {

        HashSet<Field> fields = newHashSet(
                ProductPopulated.decimalField("test1").build(),
                ProductPopulated.decimalField("test2").build()
        );
        StagingDatasetInstruction build = stagingDatasetInstruction()
                .schema(ProductPopulated.table()
                        .id(192L)
                        .displayName("A good name")
                        .physicalTableName("GD_NM")
                        .fields(fields)
                        .build())
                .build();
        StagingDatasetConfig datasetConfig = stagingSparkConfigService.createDatasetAppConfig(
                JobPopulated.stagingDataset().id(1L).build(), build);

        StagingSchemaValidation schemaValidation = datasetConfig.getStagingSchemaValidation();

        soft.assertThat(schemaValidation.getSchemaId())
                .isEqualTo(192L);
        soft.assertThat(schemaValidation.getDisplayName())
                .isEqualTo("A good name");
        soft.assertThat(schemaValidation.getPhysicalTableName())
                .isEqualTo("GD_NM");
        soft.assertThat(schemaValidation.getFields())
                .extracting(FieldValidation::getName)
                .containsExactlyInAnyOrder("test1", "test2");
    }

    @Test
    public void createDatasetAppConfig_InstructionsWithDatasetMetadata_CreatesConfigWithMetadata() {
        StagingDatasetInstruction build = stagingDatasetInstruction()
                .entityCode("ENTITY")
                .referenceDate(LocalDate.of(1999, 1, 1))
                .autoValidate(true)
                .build();

        StagingDatasetConfig datasetConfig = stagingSparkConfigService.createDatasetAppConfig(
                JobPopulated.stagingDataset().id(1L).build(), build);

        DatasetProperties datasetProperties = datasetConfig.getDatasetProperties();
        soft.assertThat(datasetConfig.isAutoValidate())
                .isEqualTo(true);
        soft.assertThat(datasetProperties.getEntityCode())
                .isEqualTo("ENTITY");
        soft.assertThat(datasetProperties.getReferenceDate())
                .isEqualTo(LocalDate.of(1999, 1, 1));
    }

    @Test
    public void createDatasetAppConfig_InstructionWithAppendToDataset_CreatesConfigWithDatasetLookup() {
        StagingDatasetInstruction instruction = stagingDatasetInstruction()
                .appendToDataset(DatasetPopulated.dataset()
                        .id(12345L)
                        .name("THE_DATASET_NAME")
                        .predicate("where row > 0 and row < 100")
                        .rowKeySeed(777L)
                        .recordsCount(328328L)
                        .build())
                .build();

        StagingDatasetConfig datasetAppConfig = stagingSparkConfigService.createDatasetAppConfig(
                JobPopulated.stagingDataset().id(1L).build(), instruction);

        assertThat(datasetAppConfig.getAppendToDataset())
                .isEqualTo(DatasetTableLookup.builder()
                        .datasetId(12345L)
                        .datasetName("THE_DATASET_NAME")
                        .predicate("where row > 0 and row < 100")
                        .rowKeySeed(777L)
                        .recordsCount(328328L)
                        .build());
    }

    @Test
    public void createDatasetAppConfig_InstructionWithoutAppendToDataset_CreatesConfigWithoutDatasetLookup() {
        StagingDatasetInstruction instruction = stagingDatasetInstruction().appendToDataset(null).build();

        StagingDatasetConfig datasetAppConfig = stagingSparkConfigService.createDatasetAppConfig(
                JobPopulated.stagingDataset().id(1L).build(), instruction);

        assertThat(datasetAppConfig.getAppendToDataset())
                .isNull();
    }
}
