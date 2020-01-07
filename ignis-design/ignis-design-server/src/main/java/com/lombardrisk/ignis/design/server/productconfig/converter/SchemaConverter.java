package com.lombardrisk.ignis.design.server.productconfig.converter;

import com.lombardrisk.ignis.client.design.schema.SchemaDto;
import com.lombardrisk.ignis.design.field.FieldConverter;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import io.vavr.Function1;

import java.util.Set;

import static com.lombardrisk.ignis.common.MapperUtils.mapOrEmpty;

public class SchemaConverter implements Function1<Schema, SchemaDto> {

    private static final long serialVersionUID = -8854520395049678395L;
    private final FieldConverter fieldConverter = new FieldConverter();
    private final ValidationRuleConverter ruleConverter = new ValidationRuleConverter(fieldConverter);

    @Override
    public SchemaDto apply(final Schema table) {
        Set<Field> fields = table.getFields();
        Set<ValidationRule> validationRules = table.getValidationRules();

        return SchemaDto.builder()
                .id(table.getId())
                .physicalTableName(table.getPhysicalTableName())
                .displayName(table.getDisplayName())
                .majorVersion(table.getMajorVersion())
                .createdBy(table.getCreatedBy())
                .createdTime(table.getCreatedTime())
                .startDate(table.getStartDate())
                .endDate(table.getEndDate())
                .latest(table.getLatest())
                .fields(mapOrEmpty(fields, fieldConverter))
                .validationRules(mapOrEmpty(validationRules, ruleConverter))
                .build();
    }
}
