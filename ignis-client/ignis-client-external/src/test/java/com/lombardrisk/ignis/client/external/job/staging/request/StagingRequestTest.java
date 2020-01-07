package com.lombardrisk.ignis.client.external.job.staging.request;

import com.lombardrisk.ignis.client.external.fixture.ExternalClient.Populated;
import org.junit.Test;

import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.common.fixtures.BeanValidationAssertions.assertThat;

public class StagingRequestTest {

    @Test
    public void validate_ReturnsNoViolations() {
        assertThat(Populated.stagingRequest().build())
                .hasNoViolations();
    }

    @Test
    public void validate_EmptyItems_ReturnsViolation() {
        assertThat(
                Populated.stagingRequest()
                        .items(newHashSet())
                        .build())
                .containsViolation("items", "size must be between 1 and 2147483647");

        assertThat(
                Populated.stagingRequest()
                        .items(null)
                        .build())
                .containsViolation("items", "must not be null");
    }

    @Test
    public void validate_InvalidItems_ReturnsViolation() {
        assertThat(
                Populated.stagingRequest()
                        .items(newHashSet(
                                Populated.stagingItemRequest().dataset(null).build()))
                        .build())
                .hasViolations("items");
    }
}