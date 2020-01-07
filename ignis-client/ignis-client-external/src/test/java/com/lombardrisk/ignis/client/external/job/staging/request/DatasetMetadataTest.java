package com.lombardrisk.ignis.client.external.job.staging.request;

import com.lombardrisk.ignis.client.external.fixture.ExternalClient.Populated;
import org.junit.Test;

import static com.lombardrisk.ignis.common.fixtures.BeanValidationAssertions.assertThat;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class DatasetMetadataTest {

    @Test
    public void validate_ReturnsNoViolations() {
        assertThat(Populated.datasetMetadata().build())
                .hasNoViolations();
    }

    @Test
    public void validate_BlankEntityCode_ReturnsViolation() {
        assertThat(
                Populated.datasetMetadata()
                        .entityCode(null)
                        .build())
                .containsViolation("entityCode", "must not be blank");

        assertThat(
                Populated.datasetMetadata()
                        .entityCode(EMPTY)
                        .build())
                .containsViolation("entityCode", "must not be blank");
    }

    @Test
    public void validate_BlankReferenceDate_ReturnsViolation() {
        assertThat(
                Populated.datasetMetadata()
                        .referenceDate(null)
                        .build())
                .containsViolation("referenceDate", "must not be blank");

        assertThat(
                Populated.datasetMetadata()
                        .referenceDate(EMPTY)
                        .build())
                .containsViolation("referenceDate", "must not be blank");
    }
}