package com.lombardrisk.ignis.design.server.productconfig.export.converter;

import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaExport;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaPeriod;
import com.lombardrisk.ignis.client.external.productconfig.export.ValidationRuleExport;
import com.lombardrisk.ignis.common.function.IsoMorphicFunction1;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import io.vavr.Function1;
import io.vavr.control.Option;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.lombardrisk.ignis.common.MapperUtils.mapCollection;
import static com.lombardrisk.ignis.common.MapperUtils.mapOrEmpty;

@SuppressWarnings("squid:S1948")
public class SchemaExportConverter implements IsoMorphicFunction1<Schema, SchemaExport> {

    private static final long serialVersionUID = -8854520395049678395L;
    private final TimeSource timeSource;
    private final FieldExportConverter fieldExportConverter = new FieldExportConverter();
    private final ValidationRuleExportConverter ruleExportConverter = new ValidationRuleExportConverter(
            fieldExportConverter);

    public SchemaExportConverter(final TimeSource timeSource) {
        this.timeSource = timeSource;
    }

    @Override
    public SchemaExport apply(final Schema schema) {
        Set<Field> fields = schema.getFields();
        Set<ValidationRule> validationRules = schema.getValidationRules();

        return SchemaExport.builder()
                .physicalTableName(schema.getPhysicalTableName())
                .displayName(schema.getDisplayName())
                .version(schema.getMajorVersion())
                .startDate(timeSource.fromLocalDate(schema.getStartDate()))
                .endDate(Option.of(schema.getEndDate())
                        .map(timeSource::fromLocalDate)
                        .getOrNull())
                .createdBy(schema.getCreatedBy())
                .createdTime(schema.getCreatedTime())
                .period(SchemaPeriod.between(schema.getStartDate(), schema.getEndDate()))
                .fields(mapOrEmpty(fields, fieldExportConverter))
                .validationRules(mapOrEmpty(validationRules, ruleExportConverter))
                .build();
    }

    @Override
    public Function1<SchemaExport, Schema> inverse() {
        Function1<FieldExport, Field> fieldImportConverter = fieldExportConverter.inverse();
        Function1<ValidationRuleExport, ValidationRule> ruleImportConverter = ruleExportConverter.inverse();

        return schemaExport -> Schema.builder()
                .physicalTableName(schemaExport.getPhysicalTableName())
                .displayName(schemaExport.getDisplayName())
                .majorVersion(schemaExport.getVersion())
                .startDate(findStartDate(schemaExport))
                .endDate(findEndDate(schemaExport))
                .createdBy(schemaExport.getCreatedBy())
                .createdTime(schemaExport.getCreatedTime())
                .fields(mapCollection(schemaExport.getFields(), fieldImportConverter, LinkedHashSet::new))
                .validationRules(mapCollection(
                        schemaExport.getValidationRules(),
                        ruleImportConverter,
                        LinkedHashSet::new))
                .build();
    }

    public LocalDate findStartDate(final SchemaExport schemaExport) {
        return Option.of(schemaExport.getPeriod())
                .map(SchemaPeriod::getStartDate)
                .getOrElse(() -> timeSource.localDateFromDate(schemaExport.getStartDate()));
    }

    public LocalDate findEndDate(final SchemaExport schemaExport) {
        if (schemaExport.getPeriod() == null) {
            return Option.of(schemaExport.getEndDate())
                    .map(timeSource::localDateFromDate)
                    .getOrNull();
        }

        return schemaExport.getPeriod().getEndDate();
    }
}
