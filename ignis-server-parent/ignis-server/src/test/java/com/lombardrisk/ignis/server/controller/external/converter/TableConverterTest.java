package com.lombardrisk.ignis.server.controller.external.converter;

import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaExport;
import com.lombardrisk.ignis.client.external.productconfig.export.ValidationRuleExport;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.rule.view.ValidationRuleConverter;
import com.lombardrisk.ignis.server.product.table.model.IntField;
import com.lombardrisk.ignis.server.product.table.model.StringField;
import com.lombardrisk.ignis.server.product.table.model.Table;
import com.lombardrisk.ignis.server.product.table.view.FieldConverter;
import com.lombardrisk.ignis.server.product.table.view.TableConverter;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;

import static com.lombardrisk.ignis.common.fixtures.PopulatedDates.toDate;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

public class TableConverterTest {

    private final FieldConverter fieldConverter = new FieldConverter();

    private TableConverter tableConverter = new TableConverter(
            fieldConverter, new ValidationRuleConverter(fieldConverter), new TimeSource(Clock.systemDefaultZone()));

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void apply_ConvertsTableToTableView() {
        Date createdTime = new Calendar.Builder().setDate(2018, 11, 31).build().getTime();

        IntField ageField = new IntField();
        ageField.setName("age");
        StringField descriptionField = new StringField();
        descriptionField.setName("description");

        Table table = ProductPopulated.table()
                .id(123456L)
                .version(5)
                .createdTime(createdTime)
                .startDate(LocalDate.of(1996, 4, 3))
                .endDate(LocalDate.of(2006, 4, 13))
                .fields(new LinkedHashSet<>(asList(ageField, descriptionField)))
                .build();

        SchemaExport view = tableConverter.apply(table);

        soft.assertThat(view.getId()).isEqualTo(123456L);
        soft.assertThat(view.getPhysicalTableName()).isEqualTo("CQRS");
        soft.assertThat(view.getDisplayName()).isEqualTo("A FRIENDLY DISPLAY NAME");
        soft.assertThat(view.getVersion()).isEqualTo(5);
        soft.assertThat(view.getCreatedBy()).isEqualTo("Ignis");
        soft.assertThat(view.getCreatedTime()).isEqualTo(createdTime);
        soft.assertThat(view.getStartDate())
                .isEqualTo(toDate("1996-04-03"));
        soft.assertThat(view.getEndDate())
                .isEqualTo(toDate("2006-04-13"));
        soft.assertThat(view.getFields())
                .extracting(FieldExport::getName)
                .containsExactly("age", "description");
    }

    @Test
    public void apply_WithNoRules_ConvertsToRuleSetView() {
        Table table = ProductPopulated.table()
                .validationRules(singleton(ProductPopulated.validationRule()
                        .name("test1")
                        .build()))
                .build();

        SchemaExport view = tableConverter.apply(table);

        assertThat(view.getValidationRules())
                .extracting(ValidationRuleExport::getName)
                .containsExactly("test1");
    }

    @Test
    public void setsHasDatasets() {
        Table table = ProductPopulated.table()
                .hasDatasets(true)
                .build();

        SchemaExport schemaView = tableConverter.apply(table);
        assertThat(schemaView.isHasDatasets())
                .isTrue();
    }

    @Test
    public void apply_HasDatasetsIsNull_SetsHasDatasetsToFalse() {
        Table table = ProductPopulated.table()
                .hasDatasets(null)
                .build();

        SchemaExport schemaView = tableConverter.apply(table);
        assertThat(schemaView.isHasDatasets())
                .isFalse();
    }
}
