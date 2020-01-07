package com.lombardrisk.ignis.client.external.job.staging.request;

import com.lombardrisk.ignis.client.external.fixture.ExternalClient.Populated;
import org.junit.Test;

import static com.lombardrisk.ignis.common.fixtures.BeanValidationAssertions.assertThat;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class DataSourceTest {

    @Test
    public void validate_ReturnsNoViolations() {
        assertThat(Populated.dataSource().build())
                .hasNoViolations();
    }

    @Test
    public void validate_BlankFilePath_ReturnsViolation() {
        assertThat(
                Populated.dataSource()
                        .filePath(null)
                        .build())
                .containsViolation("filePath", "must not be blank");

        assertThat(
                Populated.dataSource()
                        .filePath(EMPTY)
                        .build())
                .containsViolation("filePath", "must not be blank");
    }
}