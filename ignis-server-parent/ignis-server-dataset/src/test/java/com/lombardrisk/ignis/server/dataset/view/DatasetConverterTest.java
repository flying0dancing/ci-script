package com.lombardrisk.ignis.server.dataset.view;

import com.lombardrisk.ignis.api.dataset.ValidationStatus;
import com.lombardrisk.ignis.common.fixtures.PopulatedDates;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.server.dataset.fixture.DatasetPopulated;
import com.lombardrisk.ignis.server.dataset.model.DatasetOnlyBean;
import com.lombardrisk.ignis.server.dataset.model.DatasetServiceRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Date;
import java.time.Clock;
import java.time.LocalDate;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DatasetConverterTest {

    private DatasetConverter datasetConverter = new DatasetConverter(new TimeSource(Clock.systemDefaultZone()));

    @Test
    public void apply_SetsId() {
        DatasetOnlyBean dataset = aDataset().id(2039L).build();

        assertThat(datasetConverter.apply(dataset).getId())
                .isEqualTo(2039L);
    }

    @Test
    public void apply_SetsName() {
        DatasetOnlyBean dataset = aDataset().name("test").build();

        assertThat(datasetConverter.apply(dataset).getName())
                .isEqualTo("test");
    }

    @Test
    public void apply_SetsRowKeySeed() {
        DatasetOnlyBean dataset = aDataset().rowKeySeed(1234L).build();

        assertThat(datasetConverter.apply(dataset).getRowKeySeed())
                .isEqualTo(1234L);
    }

    @Test
    public void apply_SetsPredicate() {
        DatasetOnlyBean dataset = aDataset().predicate("1=3").build();

        assertThat(datasetConverter.apply(dataset).getPredicate())
                .isEqualTo("1=3");
    }

    @Test
    public void apply_SetsRecordsCount() {
        DatasetOnlyBean dataset = aDataset().recordsCount(3L).build();

        assertThat(datasetConverter.apply(dataset).getRecordsCount())
                .isEqualTo(3L);
    }

    @Test
    public void apply_SetsCreatedTime() {
        Date createdTime = new Date(10203023L);
        DatasetOnlyBean dataset = aDataset().createdTime(createdTime).build();

        assertThat(datasetConverter.apply(dataset).getCreatedTime())
                .isEqualTo(createdTime);
    }

    @Test
    public void apply_SetsLastUpdated() {
        Date updatedTime = new Date(10203023L);
        DatasetOnlyBean dataset = aDataset().lastUpdated(updatedTime).build();

        assertThat(datasetConverter.apply(dataset).getLastUpdated())
                .isEqualTo(updatedTime);
    }

    @Test
    public void apply_SetsValidationJobId() {
        DatasetOnlyBean dataset = aDataset().validationJobId(12323L).build();

        assertThat(datasetConverter.apply(dataset).getValidationJobId())
                .isEqualTo(12323);
    }

    @Test
    public void apply_SetsPipelineJobId() {
        DatasetOnlyBean dataset = aDataset().pipelineJobId(12432L).build();

        assertThat(datasetConverter.apply(dataset).getPipelineJobId())
                .isEqualTo(12432);
    }

    @Test
    public void apply_SetsPipelineInvocationId() {
        DatasetOnlyBean dataset = aDataset().pipelineInvocationId(27236L).build();

        assertThat(datasetConverter.apply(dataset).getPipelineInvocationId())
                .isEqualTo(27236);
    }

    @Test
    public void apply_SetsPipelineStepInvocationId() {
        DatasetOnlyBean dataset = aDataset().pipelineStepInvocationId(4534578L).build();

        assertThat(datasetConverter.apply(dataset).getPipelineStepInvocationId())
                .isEqualTo(4534578L);
    }

    @Test
    public void apply_SetsDisplayName() {
        DatasetOnlyBean dataset = aDataset().schemaDisplayName("D S Table").build();

        assertThat(datasetConverter.apply(dataset).getTable())
                .isEqualTo("D S Table");
    }

    @Test
    public void apply_SetsTableId() {
        DatasetOnlyBean dataset = aDataset().schemaId(5262L).build();

        assertThat(datasetConverter.apply(dataset).getTableId())
                .isEqualTo(5262);
    }

    @Test
    public void apply_SetsReferenceDate() {
        LocalDate referenceDate = LocalDate.of(2018, 5, 24);
        DatasetOnlyBean dataset = aDataset()
                .referenceDate(referenceDate)
                .build();

        assertThat(datasetConverter.apply(dataset).getReferenceDate())
                .isEqualTo(PopulatedDates.toDate("2018-5-24"));
    }

    @Test
    public void apply_SetsLocalReferenceDate() {
        LocalDate referenceDate = LocalDate.of(2018, 5, 24);
        DatasetOnlyBean dataset = aDataset()
                .referenceDate(referenceDate)
                .build();

        assertThat(datasetConverter.apply(dataset).getLocalReferenceDate())
                .isEqualTo(LocalDate.of(2018, 5, 24));
    }

    @Test
    public void apply_SetsEntityCode() {
        DatasetOnlyBean dataset = aDataset().entityCode("BHAM_UK_101").build();

        assertThat(datasetConverter.apply(dataset).getEntityCode())
                .isEqualTo("BHAM_UK_101");
    }

    @Test
    public void apply_ValidationStatus_SetsValidationStatus() {
        DatasetOnlyBean dataset = aDataset().validationStatus("VALIDATED").build();

        assertThat(datasetConverter.apply(dataset).getValidationStatus())
                .isEqualTo("VALIDATED");
    }

    @Test
    public void apply_ValidationStatusNull_SetsValidationStatus() {
        DatasetOnlyBean dataset = aDataset().validationStatus(null).build();

        assertThat(datasetConverter.apply(dataset).getValidationStatus())
                .isEqualTo(null);
    }

    @Test
    public void apply_RulesDefined_SetsHasRules() {
        DatasetOnlyBean dataset = aDataset().rulesDefined(true).build();

        assertThat(datasetConverter.apply(dataset).isHasRules())
                .isTrue();
    }

    @Test
    public void apply_RulesNotDefined_SetsHasRules() {
        DatasetOnlyBean dataset = aDataset().rulesDefined(false).build();

        assertThat(datasetConverter.apply(dataset).isHasRules())
                .isFalse();
    }

    @Test
    public void apply_SetsRunKey() {
        DatasetOnlyBean dataset = aDataset().runKey(789L).build();

        assertThat(datasetConverter.apply(dataset).getRunKey())
                .isEqualTo(789L);
    }

    private DatasetOnlyBean.DatasetOnlyBeanBuilder aDataset() {
        return DatasetPopulated.datasetOnly().id(1L);
    }
}
