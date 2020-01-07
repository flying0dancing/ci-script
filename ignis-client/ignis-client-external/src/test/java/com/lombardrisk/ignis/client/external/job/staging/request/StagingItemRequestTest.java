package com.lombardrisk.ignis.client.external.job.staging.request;

import com.lombardrisk.ignis.client.external.fixture.ExternalClient.Populated;
import org.junit.Test;

import static com.lombardrisk.ignis.common.fixtures.BeanValidationAssertions.assertThat;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class StagingItemRequestTest {

    @Test
    public void validate_ReturnsNoViolations() {
        assertThat(Populated.stagingItemRequest().build())
                .hasNoViolations();
    }

    @Test
    public void validate_BlankSchemaDisplayName_ReturnsViolation() {
        assertThat(
                Populated.stagingItemRequest()
                        .schema(null)
                        .build())
                .containsViolation("schemaDisplayName", "must not be blank");

        assertThat(
                Populated.stagingItemRequest()
                        .schema(EMPTY)
                        .build())
                .containsViolation("schemaDisplayName", "must not be blank");
    }

    @Test
    public void validate_NullDatasetMetadata_ReturnsViolation() {
        assertThat(
                Populated.stagingItemRequest()
                        .dataset(null)
                        .build())
                .containsViolation("dataset", "must not be null");
    }

    @Test
    public void validate_InvalidDatasetMetadata_ReturnsViolation() {
        assertThat(
                Populated.stagingItemRequest()
                        .dataset(DatasetMetadata.builder().build())
                        .build())
                .hasViolations("dataset");
    }

    @Test
    public void validate_NullDataSource_ReturnsViolation() {
        assertThat(
                Populated.stagingItemRequest()
                        .source(null)
                        .build())
                .containsViolation("source", "must not be null");
    }

    @Test
    public void validate_InvalidDataSource_ReturnsViolation() {
        assertThat(
                Populated.stagingItemRequest()
                        .source(DataSource.builder().build())
                        .build())
                .hasViolations("source");
    }
}