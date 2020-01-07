package com.lombardrisk.ignis.server.controller.external.converter.request;

import com.lombardrisk.ignis.client.external.fixture.ExternalClient;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaExport;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaPeriod;
import com.lombardrisk.ignis.client.external.productconfig.export.ValidationRuleExport;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
import com.lombardrisk.ignis.server.product.rule.view.ValidationRuleExportConverter;
import com.lombardrisk.ignis.server.product.table.model.Field;
import com.lombardrisk.ignis.server.product.table.model.Table;
import com.lombardrisk.ignis.server.product.table.view.FieldExportConverter;
import com.lombardrisk.ignis.server.product.table.view.SchemaExportConverter;
import org.junit.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;

import static com.lombardrisk.ignis.common.fixtures.PopulatedDates.toDate;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class SchemaExportConverterTest {

    private static final TimeSource TIME_SOURCE = new TimeSource(Clock.systemUTC());

    private SchemaExportConverter schemaViewConverter = new SchemaExportConverter(
            new FieldExportConverter(),
            new ValidationRuleExportConverter(),
            TIME_SOURCE
    );

    @Test
    public void apply_SetsName() {
        SchemaExport schemaView = ExternalClient.Populated.schemaExport()
                .physicalTableName("table name")
                .build();

        Table table = schemaViewConverter.apply(schemaView);

        assertThat(table.getPhysicalTableName())
                .isEqualTo("table name");
    }

    @Test
    public void apply_SetsDisplayName() {
        SchemaExport schemaView = ExternalClient.Populated.schemaExport()
                .displayName("table display name")
                .build();

        Table table = schemaViewConverter.apply(schemaView);

        assertThat(table.getDisplayName())
                .isEqualTo("table display name");
    }

    @Test
    public void apply_SetsVersion() {
        SchemaExport schemaView = ExternalClient.Populated.schemaExport()
                .version(1)
                .build();

        Table table = schemaViewConverter.apply(schemaView);

        assertThat(table.getVersion())
                .isEqualTo(1);
    }

    @Test
    public void apply_SetsFields() {
        SchemaExport schemaView = ExternalClient.Populated.schemaExport()
                .version(1)
                .fields(asList(
                        FieldExport.DateFieldExport.builder().name("field1").build(),
                        FieldExport.DateFieldExport.builder().name("field2").build(),
                        FieldExport.DateFieldExport.builder().name("field3").build(),
                        FieldExport.DateFieldExport.builder().name("field4").build(),
                        FieldExport.DateFieldExport.builder().name("field5").build()
                ))
                .build();

        Table table = schemaViewConverter.apply(schemaView);

        assertThat(table.getFields())
                .extracting(Field::getName)
                .containsExactly("field1", "field2", "field3", "field4", "field5");
    }

    @Test
    public void apply_SetsValidationRules() {
        ValidationRuleExport ruleView1 = ExternalClient.Populated.validationRuleExport()
                .name("test1")
                .build();

        ValidationRuleExport ruleView2 = ExternalClient.Populated.validationRuleExport()
                .name("test2")
                .build();

        SchemaExport schemaExport = ExternalClient.Populated.schemaExport()
                .validationRules(Arrays.asList(ruleView1, ruleView2))
                .build();

        Table table = schemaViewConverter.apply(schemaExport);

        assertThat(table.getValidationRules())
                .extracting(ValidationRule::getName)
                .containsExactlyInAnyOrder("test1", "test2");
    }

    @Test
    public void apply_PeriodNotPresent_SetsStartDateFromStartDate() {
        SchemaExport schemaView = ExternalClient.Populated.schemaExport()
                .startDate(toDate("1996-03-03"))
                .period(null)
                .version(1)
                .build();

        Table table = schemaViewConverter.apply(schemaView);

        assertThat(table.getStartDate())
                .isEqualTo(LocalDate.of(1996, 3, 3));
    }

    @Test
    public void apply_PeriodPresent_SetsStartDateFromPeriod() {
        SchemaExport schemaView = ExternalClient.Populated.schemaExport()
                .startDate(toDate("1996-03-03"))
                .period(SchemaPeriod.between(LocalDate.of(2001, 3, 3), null))
                .version(1)
                .build();

        Table table = schemaViewConverter.apply(schemaView);

        assertThat(table.getStartDate())
                .isEqualTo(LocalDate.of(2001, 3, 3));
    }

    @Test
    public void apply_PeriodNotPresent_SetsEndDateFromEndDate() {
        SchemaExport schemaExport = ExternalClient.Populated.schemaExport()
                .endDate(toDate("2016-12-31"))
                .period(null)
                .version(1)
                .build();

        Table table = schemaViewConverter.apply(schemaExport);

        assertThat(table.getEndDate())
                .isEqualTo(LocalDate.of(2016, 12, 31));
    }

    @Test
    public void apply_PeriodPresent_SetsEndDateFromPeriod() {
        SchemaExport schemaExport = ExternalClient.Populated.schemaExport()
                .endDate(toDate("2016-12-31"))
                .period(SchemaPeriod.between(
                        LocalDate.of(2001, 1, 1), LocalDate.of(2017, 1, 1)))
                .version(1)
                .build();

        Table table = schemaViewConverter.apply(schemaExport);

        assertThat(table.getEndDate())
                .isEqualTo(LocalDate.of(2017, 1, 1));
    }
}
