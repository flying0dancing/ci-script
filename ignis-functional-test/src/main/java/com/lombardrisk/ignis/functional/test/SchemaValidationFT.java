package com.lombardrisk.ignis.functional.test;

import com.lombardrisk.ignis.client.external.job.staging.DatasetState;
import com.lombardrisk.ignis.client.external.job.staging.request.DataSource;
import com.lombardrisk.ignis.client.external.job.staging.request.DatasetMetadata;
import com.lombardrisk.ignis.client.external.job.staging.request.v1.StagingItemRequest;
import com.lombardrisk.ignis.client.external.job.staging.request.v1.StagingRequest;
import com.lombardrisk.ignis.client.external.productconfig.view.FieldView;
import com.lombardrisk.ignis.client.external.productconfig.view.ProductConfigView;
import com.lombardrisk.ignis.functional.test.assertions.expected.ExpectedPhysicalTable;
import com.lombardrisk.ignis.functional.test.config.FunctionalTest;
import com.lombardrisk.ignis.functional.test.dsl.FcrEngine;
import com.lombardrisk.ignis.functional.test.dsl.FcrEngineTest;
import com.lombardrisk.ignis.functional.test.dsl.StagingJobContext;
import com.lombardrisk.ignis.functional.test.junit.PassingTestsCleanupRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.functional.test.constants.TestConstants.ACCOUNTS;
import static com.lombardrisk.ignis.functional.test.constants.TestConstants.ACCOUNTS_PRODUCT_CONFIG;
import static com.lombardrisk.ignis.functional.test.constants.TestConstants.ACCOUNTS_SCHEMA;
import static com.lombardrisk.ignis.functional.test.constants.TestConstants.INVALID_ACCOUNTS_CSV;
import static java.util.Arrays.asList;

@SuppressWarnings({ "squid:S109", "squid:S1192", "squid:S00100", "findbugs:URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD" })
@RunWith(SpringRunner.class)
@FunctionalTest
public class SchemaValidationFT {

    @Autowired
    private FcrEngine fcrEngine;

    private FcrEngineTest test;

    @Before
    public void setUp() {
        test = fcrEngine.newTest();
    }

    @Rule
    public PassingTestsCleanupRule passingTestsCleanupRule = PassingTestsCleanupRule.withSteps(() -> test.cleanup());

    @Test
    public void stagingDataset_SchemaInvalid_CreatesErrorFile() {
        ProductConfigView importedProduct = test.importProductConfig(ACCOUNTS_PRODUCT_CONFIG).waitForJobToSucceed();

        StagingJobContext stagingJob = test.stageDatasets(importedProduct, StagingRequest.builder()
                .name("Invalid accounts csv")
                .items(newHashSet(
                        StagingItemRequest.builder()
                                .schema(ACCOUNTS_SCHEMA)
                                .dataset(DatasetMetadata.builder()
                                        .entityCode("entity-21")
                                        .referenceDate("28/02/2018")
                                        .build())
                                .source(DataSource.builder()
                                        .header(true)
                                        .filePath(INVALID_ACCOUNTS_CSV)
                                        .build())
                                .build()))
                .build());

        test.assertDatasetDoesNotExist(importedProduct, stagingJob, ACCOUNTS);

        test.assertPhysicalTableExists(
                importedProduct,
                ExpectedPhysicalTable.expected()
                        .name(ACCOUNTS)
                        .columns(asList(
                                FieldView.of("CUSTOMER_NAME", FieldView.Type.STRING),
                                FieldView.of("OPENING_DATE", FieldView.Type.DATE),
                                FieldView.of("ACCOUNT_TYPE", FieldView.Type.INTEGER),
                                FieldView.of("CURRENT_BALANCE", FieldView.Type.INTEGER),
                                FieldView.of("OTHER_FACILITIES", FieldView.Type.STRING),
                                FieldView.of("INTERESTE_RATE", FieldView.Type.DOUBLE),
                                FieldView.of("CLOSING_DATE", FieldView.Type.DATE)))
                        .numberOfRows(0)
                        .build());

        test.assertStagingDatasetState(importedProduct, stagingJob, ACCOUNTS, DatasetState.VALIDATION_FAILED);

        test.assertStagingValidationErrors(
                stagingJob,
                ACCOUNTS,
                "NEO,11/06/1999 00:00:00,<NULL>,1000,Being the one,100,11/06/2000 00:00:00,Field [ACCOUNT_TYPE] is not nullable");
    }
}
