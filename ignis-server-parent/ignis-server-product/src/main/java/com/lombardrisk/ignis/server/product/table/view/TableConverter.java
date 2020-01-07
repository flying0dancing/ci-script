package com.lombardrisk.ignis.server.product.table.view;

import com.lombardrisk.ignis.client.external.productconfig.export.SchemaExport;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
import com.lombardrisk.ignis.server.product.rule.view.ValidationRuleConverter;
import com.lombardrisk.ignis.server.product.table.model.Field;
import com.lombardrisk.ignis.server.product.table.model.Table;
import io.vavr.Function1;
import io.vavr.control.Option;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class TableConverter implements Function<Table, SchemaExport> {

    private final FieldConverter fieldConverter;
    private final ValidationRuleConverter ruleConverter;
    private final TimeSource timeSource;

    public TableConverter(
            final FieldConverter fieldConverter,
            final ValidationRuleConverter ruleConverter, final TimeSource timeSource) {
        this.fieldConverter = fieldConverter;
        this.ruleConverter = ruleConverter;
        this.timeSource = timeSource;
    }

    @Override
    public SchemaExport apply(final Table table) {
        boolean hasDatasets = Option.of(table.getHasDatasets())
                .getOrElse(false);

        Set<Field> fields = table.getFields();
        Set<ValidationRule> validationRules = table.getValidationRules();

        return SchemaExport.builder()
                .id(table.getId())
                .physicalTableName(table.getPhysicalTableName())
                .displayName(table.getDisplayName())
                .version(table.getVersion())
                .hasDatasets(hasDatasets)
                .createdBy(table.getCreatedBy())
                .createdTime(table.getCreatedTime())
                .startDate(timeSource.fromLocalDate(table.getStartDate()))
                .endDate(Optional.ofNullable(table.getEndDate())
                        .map(timeSource::fromLocalDate)
                        .orElse(null))
                .fields(mapOrEmpty(fields, fieldConverter))
                .validationRules(mapOrEmpty(validationRules, ruleConverter))
                .build();
    }

    public <T, R> List<R> mapOrEmpty(final Set<T> in, final Function1<T, R> converter) {
        return in != null ? MapperUtils.map(in, converter) : Collections.emptyList();
    }
}
