package com.lombardrisk.ignis.server.dataset.model;

import com.lombardrisk.ignis.api.dataset.ValidationStatus;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DatasetTest {

    @Test
    public void build_BuildsDatasetWithDefaultValidationStatus() {
        Dataset dataset = Dataset.builder().build();
        assertThat(dataset.getValidationStatus()).isEqualTo(ValidationStatus.NOT_VALIDATED);
    }

    @Test
    public void build_BuildsDatasetWithOtherValidationStatus() {
        Dataset dataset = Dataset.builder().validationStatus(ValidationStatus.VALIDATED).build();
        assertThat(dataset.getValidationStatus()).isEqualTo(ValidationStatus.VALIDATED);
    }

    @Test
    public void latestValidationJobId_CanBeNull() {
        Dataset build = Dataset.builder()
                .latestValidationJobId(null)
                .build();

        assertThat(build.getLatestValidationJobId())
                .isNull();
    }
}
