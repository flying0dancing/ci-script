package com.lombardrisk.ignis.functional.test.constants;

import com.lombardrisk.ignis.client.external.job.staging.request.DataSource;
import com.lombardrisk.ignis.client.external.job.staging.request.DatasetMetadata;
import com.lombardrisk.ignis.client.external.job.staging.request.v1.StagingItemRequest;
import com.lombardrisk.ignis.functional.test.utils.ReferenceDateParser;
import lombok.experimental.UtilityClass;

import static com.lombardrisk.ignis.functional.test.constants.TestConstants.REG_ADJUSTMENTS_CSV;
import static com.lombardrisk.ignis.functional.test.constants.TestConstants.WHOLESALE_CORPORATE_CSV;
import static com.lombardrisk.ignis.functional.test.utils.ReferenceDateParser.convertToString;

@UtilityClass
public class StagingItem {

    public static final StagingItemRequest WHOLESALE_CORPORATE =
            StagingItemRequest.builder()
                    .schema(TestConstants.WHOLESALE_CORPORATE_SCHEMA)
                    .source(DataSource.builder()
                            .header(true)
                            .filePath(WHOLESALE_CORPORATE_CSV)
                            .build())
                    .dataset(DatasetMetadata.builder()
                            .entityCode("CIL-ENTITY-CODE")
                            .referenceDate(convertToString(ReferenceDateParser.convertToDate("28/02/2018")))
                            .build())
                    .build();

    public static final StagingItemRequest REG_ADJUSTMENTS =
            StagingItemRequest.builder()
                    .schema(TestConstants.REG_ADJUSTMENTS_SCHEMA)
                    .source(DataSource.builder()
                            .header(true)
                            .filePath(REG_ADJUSTMENTS_CSV)
                            .build())
                    .dataset(DatasetMetadata.builder()
                            .entityCode("CIL-ENTITY-CODE")
                            .referenceDate(convertToString(ReferenceDateParser.convertToDate("28/02/2018")))
                            .build())
                    .build();
}
