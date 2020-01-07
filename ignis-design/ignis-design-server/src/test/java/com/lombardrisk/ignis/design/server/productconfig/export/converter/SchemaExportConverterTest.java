package com.lombardrisk.ignis.design.server.productconfig.export.converter;

import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaExport;
import com.lombardrisk.ignis.common.fixtures.PopulatedDates;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.design.field.model.IntField;
import com.lombardrisk.ignis.design.field.model.StringField;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;

import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Sets.newLinkedHashSet;

public class SchemaExportConverterTest {

    private final SchemaExportConverter tableConverter = new SchemaExportConverter(
            new TimeSource(Clock.systemUTC())
    );

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void apply_ConvertsSchemaToSchemaExport() {
        Date createdTime = new Calendar.Builder().setDate(2018, 11, 31).build().getTime();

        IntField ageField = new IntField();
        ageField.setName("age");
        StringField descriptionField = new StringField();
        descriptionField.setName("description");

        Schema schema = Populated.schema()
                .id(123456L)
                .physicalTableName("schema name")
                .displayName("schema display name")
                .majorVersion(5)
                .startDate(LocalDate.of(2017, 1, 1))
                .endDate(LocalDate.of(2018, 1, 1))
                .createdTime(createdTime)
                .fields(new LinkedHashSet<>(asList(ageField, descriptionField)))
                .build();

        SchemaExport schemaExport = tableConverter.apply(schema);

        soft.assertThat(schemaExport.getId()).isNull();
        soft.assertThat(schemaExport.getPhysicalTableName()).isEqualTo("schema name");
        soft.assertThat(schemaExport.getDisplayName()).isEqualTo("schema display name");
        soft.assertThat(schemaExport.getVersion()).isEqualTo(5);
        soft.assertThat(schemaExport.getStartDate()).isEqualTo(PopulatedDates.toDate("2017-1-1"));
        soft.assertThat(schemaExport.getEndDate()).isEqualTo(PopulatedDates.toDate("2018-1-1"));
        soft.assertThat(schemaExport.getPeriod().getStartDate())
                .isEqualTo(LocalDate.of(2017, 1, 1));
        soft.assertThat(schemaExport.getPeriod().getEndDate())
                .isEqualTo(LocalDate.of(2018, 1, 1));
        soft.assertThat(schemaExport.getCreatedBy()).isEqualTo("Ignis");
        soft.assertThat(schemaExport.getCreatedTime()).isEqualTo(createdTime);
        soft.assertThat(schemaExport.getFields())
                .extracting(FieldExport::getName)
                .containsExactly("age", "description");
    }

    @Test
    public void apply_WithNoRules_ConvertsToRuleSetView() {
        Schema schema = Populated.schema()
                .validationRules(singleton(Populated.validationRule()
                        .name("rule")
                        .build()))
                .build();

        SchemaExport view = tableConverter.apply(schema);

        assertThat(view.getValidationRules().get(0).getName())
                .isEqualTo("rule");
    }

    @Test
    public void inverse_ConvertsBackToOriginal() {
        Date createdTime = new Calendar.Builder().setDate(2018, 11, 31).build().getTime();

        IntField ageField = new IntField();
        ageField.setName("age");
        StringField descriptionField = new StringField();
        descriptionField.setName("description");

        Schema original = Populated.schema()
                .physicalTableName("schema name")
                .displayName("schema display name")
                .majorVersion(5)
                .startDate(LocalDate.of(2017, 1, 1))
                .endDate(LocalDate.of(2018, 1, 1))
                .createdTime(createdTime)
                .fields(new LinkedHashSet<>(asList(ageField, descriptionField)))
                .validationRules(newLinkedHashSet(Populated.validationRule()
                        .validationRuleExamples(null)
                        .build()))
                .build();

        SchemaExport export = tableConverter.apply(original);
        Schema reConverted = tableConverter.inverse().apply(export);

        assertThat(reConverted)
                .isEqualToIgnoringGivenFields(original, "latest", "id", "productId");
    }

}
